import { Button, Card, Form, Input, message, Space, Typography } from "antd";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { setToken, setUser } from "../../services/auth";

const { Text, Title } = Typography;

const API_BASE = "/api/v1";

export default function LoginPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [isRegister, setIsRegister] = useState(false);

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const endpoint = isRegister ? "/auth/register" : "/auth/login";
      const response = await fetch(API_BASE + endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(values)
      });
      const data = await response.json();
      if (data.code === 200 && data.data?.token) {
        setToken(data.data.token);
        setUser({
          username: data.data.username,
          displayName: data.data.displayName ?? data.data.username,
          role: data.data.role ?? "USER"
        });
        message.success(isRegister ? "Registered" : "Logged in");
        navigate("/");
      } else {
        message.error(data.message ?? "Login failed");
      }
    } catch (err) {
      message.error(err instanceof Error ? err.message : "Network error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: "100vh",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      background: "linear-gradient(135deg, #0a0e1a 0%, #111827 100%)"
    }}>
      <Card style={{ width: 400, background: "#1a1f35", border: "1px solid rgba(99,115,146,0.2)" }}>
        <Space direction="vertical" size={24} style={{ width: "100%" }}>
          <div style={{ textAlign: "center" }}>
            <Title level={3} style={{ color: "#e8ecf4", marginBottom: 4 }}>DetailFlow</Title>
            <Text style={{ color: "#8b95a8" }}>
              {isRegister ? "Create an account" : "Sign in to continue"}
            </Text>
          </div>
          <Form layout="vertical" onFinish={(v) => void onFinish(v)}>
            <Form.Item name="username" rules={[{ required: true, message: "Username" }]}>
              <Input placeholder="Username" size="large" />
            </Form.Item>
            <Form.Item name="password" rules={[{ required: true, message: "Password" }]}>
              <Input.Password placeholder="Password" size="large" />
            </Form.Item>
            <Form.Item style={{ marginBottom: 8 }}>
              <Button type="primary" htmlType="submit" loading={loading} block size="large">
                {isRegister ? "Register" : "Sign In"}
              </Button>
            </Form.Item>
          </Form>
          <div style={{ textAlign: "center" }}>
            <Button type="link" onClick={() => setIsRegister(!isRegister)} style={{ color: "#3b82f6" }}>
              {isRegister ? "Already have an account? Sign in" : "No account? Register"}
            </Button>
          </div>
        </Space>
      </Card>
    </div>
  );
}