import { Button, Card, Form, Input, message, Space, Typography } from "antd";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { setToken, setUser } from "../../services/auth";
import fullLogo from "../../assets/full-logo.png";

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
        message.success(isRegister ? "注册成功" : "登录成功");
        navigate("/dashboard");
      } else {
        message.error(data.message ?? "登录失败");
      }
    } catch (err) {
      message.error(err instanceof Error ? err.message : "网络错误");
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
      background: "var(--df-bg)"
    }}>
      <Card style={{
        width: 400,
        background: "var(--df-surface)",
        border: "1px solid var(--df-border)",
        borderRadius: "var(--df-radius-xl)",
        boxShadow: "var(--df-shadow-lg)"
      }}>
        <Space direction="vertical" size={24} style={{ width: "100%" }}>
          <div style={{ textAlign: "center" }}>
            <img src={fullLogo} alt="DetailFlow" style={{ width: 80, height: 80, margin: "0 auto 16px", display: "block", borderRadius: 16 }} />
            <Title level={3} style={{ color: "var(--df-text)", marginBottom: 4 }}>DetailFlow</Title>
            <Text style={{ color: "var(--df-text-muted)" }}>
              {isRegister ? "创建账号" : "登录以继续"}
            </Text>
          </div>
          <Form layout="vertical" onFinish={(v) => void onFinish(v)}>
            <Form.Item name="username" rules={[{ required: true, message: "请输入用户名" }]}>
              <Input placeholder="用户名" size="large" />
            </Form.Item>
            <Form.Item name="password" rules={[{ required: true, message: "请输入密码" }]}>
              <Input.Password placeholder="密码" size="large" />
            </Form.Item>
            <Form.Item style={{ marginBottom: 8 }}>
              <Button type="primary" htmlType="submit" loading={loading} block size="large">
                {isRegister ? "注册" : "登录"}
              </Button>
            </Form.Item>
          </Form>
          <div style={{ textAlign: "center" }}>
            <Button type="link" onClick={() => setIsRegister(!isRegister)} style={{ color: "var(--df-primary)" }}>
              {isRegister ? "已有账号？去登录" : "没有账号？去注册"}
            </Button>
          </div>
        </Space>
      </Card>
    </div>
  );
}