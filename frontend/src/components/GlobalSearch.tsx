import { Input, Modal } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

type SearchResult = {
  label: string;
  path: string;
  group: string;
};

const searchableItems: SearchResult[] = [
  { label: "Dashboard", path: "/", group: "Pages" },
  { label: "Market Research", path: "/research", group: "Pages" },
  { label: "New Research Task", path: "/research/new", group: "Pages" },
  { label: "Materials", path: "/materials", group: "Pages" },
  { label: "New Material", path: "/materials/new", group: "Pages" },
  { label: "AI Generate", path: "/generate", group: "Pages" },
  { label: "Asset Library", path: "/assets", group: "Pages" },
  { label: "Results Preview", path: "/results", group: "Pages" },
  { label: "Detail Editor", path: "/details/1", group: "Pages" },
  { label: "Audit Center", path: "/audit", group: "Pages" },
  { label: "Export Records", path: "/exports", group: "Pages" },
  { label: "Post-Process", path: "/post-process", group: "Pages" },
  { label: "Prompt Lab", path: "/visual/prompt-workbench", group: "Visual" },
  { label: "Prompt Templates", path: "/visual/prompt-templates", group: "Visual" },
  { label: "Visual Plans", path: "/visual/plans", group: "Visual" },
  { label: "Category Policies", path: "/visual/category-policies", group: "Visual" },
  { label: "Model Profiles", path: "/visual/model-profiles", group: "Visual" },
  { label: "Cost Management", path: "/cost", group: "System" },
  { label: "Diagnostics", path: "/system/diagnostics", group: "System" },
  { label: "Team Management", path: "/system/team", group: "System" },
  { label: "Audit Log", path: "/system/audit-log", group: "System" },
  { label: "Tool Center", path: "/tools", group: "System" },
  { label: "Data Import", path: "/tools/imports", group: "System" },
  { label: "Design Draft", path: "/tools/design-draft", group: "System" }
];

export function GlobalSearch({ open, onClose }: { open: boolean; onClose: () => void }) {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");

  const results = query.trim()
    ? searchableItems.filter((item) =>
        item.label.toLowerCase().includes(query.toLowerCase()) ||
        item.group.toLowerCase().includes(query.toLowerCase())
      )
    : searchableItems;

  const grouped = results.reduce<Record<string, SearchResult[]>>((acc, item) => {
    (acc[item.group] ??= []).push(item);
    return acc;
  }, {});

  const handleSelect = useCallback(
    (path: string) => {
      navigate(path);
      onClose();
      setQuery("");
    },
    [navigate, onClose]
  );

  useEffect(() => {
    if (!open) setQuery("");
  }, [open]);

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      closable={false}
      width={520}
      styles={{ body: { padding: 0 } }}
    >
      <Input
        prefix={<SearchOutlined style={{ color: "var(--text-muted)" }} />}
        placeholder="Search pages, tools, actions..."
        size="large"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        autoFocus
        style={{ borderRadius: 0, border: "none", borderBottom: "1px solid var(--border-default)", boxShadow: "none" }}
      />
      <div style={{ maxHeight: 360, overflowY: "auto", padding: "8px 0" }}>
        {Object.entries(grouped).map(([group, items]) => (
          <div key={group}>
            <div style={{
              padding: "6px 16px",
              fontSize: 11,
              fontWeight: 600,
              textTransform: "uppercase",
              letterSpacing: 1,
              color: "var(--text-muted)"
            }}>
              {group}
            </div>
            {items.map((item) => (
              <div
                key={item.path}
                onClick={() => handleSelect(item.path)}
                style={{
                  padding: "8px 16px",
                  cursor: "pointer",
                  fontSize: 13,
                  color: "var(--text-primary)",
                  display: "flex",
                  justifyContent: "space-between",
                  transition: "background 0.1s"
                }}
                onMouseEnter={(e) => (e.currentTarget.style.background = "var(--bg-hover)")}
                onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
              >
                <span>{item.label}</span>
                <span style={{ fontSize: 11, color: "var(--text-muted)" }}>{item.path}</span>
              </div>
            ))}
          </div>
        ))}
        {results.length === 0 && (
          <div style={{ padding: "24px 16px", textAlign: "center", color: "var(--text-muted)", fontSize: 13 }}>
            No results for "{query}"
          </div>
        )}
      </div>
    </Modal>
  );
}