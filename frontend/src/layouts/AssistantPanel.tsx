import { useNavigate } from "react-router-dom";
import { useLang } from "../i18n";

export const AssistantPanel = () => {
  const navigate = useNavigate();
  const { t } = useLang();

  return (
    <aside className="df-assistant">
      <div className="df-assistant-header">
        <div className="df-assistant-pulse" />
        {t("assistant.title")}
      </div>

      <div className="df-assistant-section">{t("assistant.insights")}</div>
      <div className="df-assistant-msg insight">{t("assistant.insight1")}</div>
      <div className="df-assistant-msg">{t("assistant.insight2")}</div>

      <div className="df-assistant-section">{t("assistant.prompts")}</div>
      <div className="df-assistant-msg">{t("assistant.prompt1")}</div>

      <div className="df-assistant-section">{t("assistant.activity")}</div>
      <div className="df-assistant-msg" style={{ fontSize: 12 }}>{t("assistant.activity1")}</div>
      <div className="df-assistant-msg" style={{ fontSize: 12 }}>{t("assistant.activity2")}</div>

      <div className="df-assistant-section">{t("assistant.actions")}</div>
      <div className="df-assistant-action" onClick={() => navigate("/visual/prompt-workbench")}>
        {t("assistant.act.prompt")}
      </div>
      <div className="df-assistant-action" onClick={() => navigate("/post-process")}>
        {t("assistant.act.upscale")}
      </div>
      <div className="df-assistant-action" onClick={() => navigate("/exports")}>
        {t("assistant.act.pdf")}
      </div>
      <div className="df-assistant-action" onClick={() => navigate("/research")}>
        {t("assistant.act.scan")}
      </div>
    </aside>
  );
};