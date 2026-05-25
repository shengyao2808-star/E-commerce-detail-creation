import type { ReactNode } from "react";
import { Typography } from "antd";

type GlassPanelProps = {
  title?: ReactNode;
  subtitle?: ReactNode;
  description?: ReactNode;
  extra?: ReactNode;
  children: ReactNode;
  className?: string;
};

export const GlassPanel = ({
  title,
  subtitle,
  description,
  extra,
  children,
  className
}: GlassPanelProps) => (
  <section className={["glass-panel", className].filter(Boolean).join(" ")}>
    {(title || description || extra) && (
      <div className="glass-panel-header">
        <div>
          {title && <h3 className="glass-panel-title">{title}</h3>}
          {(subtitle || description) && (
            <Typography.Text className="glass-panel-description">
              {subtitle ?? description}
            </Typography.Text>
          )}
        </div>
        {extra}
      </div>
    )}
    {children}
  </section>
);
