import { Button, Space } from "antd";
import { GlobalOutlined, PlusOutlined, SearchOutlined, SettingOutlined, UserOutlined } from "@ant-design/icons";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useLang } from "../i18n";
import { GlobalSearch } from "../components/GlobalSearch";

export const TopBar = () => {
  const navigate = useNavigate();
  const { lang, toggle, t } = useLang();
  const [searchOpen, setSearchOpen] = useState(false);

  return (
    <>
      <header className="df-topbar">
        <div className="df-topbar-brand">
          <div className="df-topbar-brand-icon">D</div>
          <span>DetailFlow</span>
        </div>
        <div className="df-topbar-actions">
          <Button type="text" icon={<SearchOutlined />} size="small" onClick={() => setSearchOpen(true)}>
            {t("topbar.search")}
          </Button>
          <Button type="primary" icon={<PlusOutlined />} size="small" onClick={() => navigate("/materials/new")}>
            {t("topbar.newProject")}
          </Button>
          <Button type="text" icon={<SettingOutlined />} size="small" onClick={() => navigate("/system/diagnostics")} />
          <Button type="text" icon={<UserOutlined />} size="small" />
          <Button
            type="text"
            icon={<GlobalOutlined />}
            size="small"
            onClick={toggle}
            title={lang === "zh" ? "Switch to English" : "切换到中文"}
          >
            {lang === "zh" ? "EN" : "中"}
          </Button>
        </div>
      </header>
      <GlobalSearch open={searchOpen} onClose={() => setSearchOpen(false)} />
    </>
  );
};