import { useNavigate } from "react-router-dom";

export const AssistantPanel = () => {
  const navigate = useNavigate();

  return (
    <aside className="df-assistant">
      <div className="df-assistant-header">
        <div className="df-assistant-pulse" />
        PromptPilot
      </div>

      <div className="df-assistant-section">Insights</div>
      <div className="df-assistant-msg insight">
        3 products missing SEO keywords. Auto-generate from category analysis available.
      </div>
      <div className="df-assistant-msg">
        Real-ESRGAN batch upscale completed: 12 images, average quality score 94.2.
      </div>

      <div className="df-assistant-section">Suggested Prompts</div>
      <div className="df-assistant-msg">
        "Elegant summer dress on sunlit terrace, natural light, lifestyle photography, warm tones, 4K detail shot"
      </div>

      <div className="df-assistant-section">Recent Activity</div>
      <div className="df-assistant-msg" style={{ fontSize: 12 }}>
        Kitchen Tools detail pages exported (5 files, Word format)
      </div>
      <div className="df-assistant-msg" style={{ fontSize: 12 }}>
        Audit #247 approved with quality score 96
      </div>

      <div className="df-assistant-section">Quick Actions</div>
      <div className="df-assistant-action" onClick={() => navigate("/visual/prompt-workbench")}>
        Generate Prompt
      </div>
      <div className="df-assistant-action" onClick={() => navigate("/post-process")}>
        Batch Upscale
      </div>
      <div className="df-assistant-action" onClick={() => navigate("/exports")}>
        Export PDF
      </div>
      <div className="df-assistant-action" onClick={() => navigate("/research")}>
        Competitor Scan
      </div>
    </aside>
  );
};