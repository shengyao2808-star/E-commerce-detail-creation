import { Button, Space } from "antd";
import { PlusOutlined, SearchOutlined, SettingOutlined, UserOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";

export const TopBar = () => {
  const navigate = useNavigate();

  return (
    <header className="df-topbar">
      <div className="df-topbar-brand">
        <div className="df-topbar-brand-icon">D</div>
        <span>DetailFlow</span>
      </div>
      <div className="df-topbar-actions">
        <Button type="text" icon={<SearchOutlined />} size="small">
          Search
        </Button>
        <Button type="primary" icon={<PlusOutlined />} size="small" onClick={() => navigate("/materials/new")}>
          New Project
        </Button>
        <Button type="text" icon={<SettingOutlined />} size="small" onClick={() => navigate("/system/diagnostics")} />
        <Button type="text" icon={<UserOutlined />} size="small" />
      </div>
    </header>
  );
};