import { Button, Input, Space } from "antd";
import { BellOutlined, GlobalOutlined, PlusOutlined, SearchOutlined, UserOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useLang } from "../i18n";
import iconLogo from "../assets/icon-logo.png";
import textLogo from "../assets/text-logo.png";

export const TopBar = () => {
  const navigate = useNavigate();
  const { lang, toggle, t } = useLang();

  return (
    <header className="df-topbar">
      <div className="df-topbar-brand" onClick={() => navigate("/dashboard")} style={{ cursor: "pointer" }}>
        <img src={iconLogo} alt="Logo" style={{ width: 36, height: 36, borderRadius: 8 }} />
        <img src={textLogo} alt="DetailFlow" style={{ height: 28, marginLeft: 4 }} />
      </div>
      <div className="df-topbar-center">
        <Input
          className="df-topbar-search"
          placeholder={t("topbar.search")}
          prefix={<SearchOutlined style={{ color: "var(--df-text-muted)" }} />}
          allowClear
          style={{ borderRadius: "var(--df-radius-lg)", background: "var(--df-bg)" }}
        />
      </div>
      <div className="df-topbar-actions">
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate("/materials/new")}>
          {t("topbar.newProject")}
        </Button>
        <Button type="text" icon={<BellOutlined />} />
        <Button type="text" icon={<UserOutlined />} />
        <Button
          type="text"
          icon={<GlobalOutlined />}
          onClick={toggle}
          title={lang === "zh" ? "Switch to English" : "切换到中文"}
        >
          {lang === "zh" ? "EN" : "中"}
        </Button>
      </div>
    </header>
  );
};