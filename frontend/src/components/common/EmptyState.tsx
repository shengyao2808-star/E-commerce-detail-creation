import { Empty, Typography } from "antd";
import type { ReactNode } from "react";

type EmptyStateProps = {
  title: ReactNode;
  description?: ReactNode;
  action?: ReactNode;
  compact?: boolean;
};

export const EmptyState = ({ title, description, action, compact = false }: EmptyStateProps) => (
  <div className={compact ? "empty-state empty-state--compact" : "empty-state"}>
    <Empty
      image={Empty.PRESENTED_IMAGE_SIMPLE}
      description={
        <div>
          <Typography.Text strong>{title}</Typography.Text>
          {description && (
            <Typography.Paragraph type="secondary">
              {description}
            </Typography.Paragraph>
          )}
        </div>
      }
    >
      {action}
    </Empty>
  </div>
);
