import {
  ApiOutlined,
  DisconnectOutlined,
  LoadingOutlined,
  StopOutlined,
  WarningOutlined
} from "@ant-design/icons";
import { Alert, Button, Result, Skeleton, Space, Typography } from "antd";
import type { ReactNode } from "react";

type CommonStateProps = {
  title: ReactNode;
  description?: ReactNode;
  action?: ReactNode;
  compact?: boolean;
};

type RetryStateProps = CommonStateProps & {
  onRetry?: () => void;
  retryText?: string;
};

export const LoadingState = ({
  title = "Loading",
  description,
  compact = false
}: Partial<CommonStateProps>) => (
  <div className={compact ? "state-block state-block--compact" : "state-block"}>
    <Space direction="vertical" size={12} className="state-block-content">
      <LoadingOutlined className="state-block-icon state-block-icon--info" spin />
      <Typography.Text strong>{title}</Typography.Text>
      {description ? <Typography.Text type="secondary">{description}</Typography.Text> : null}
      {!compact ? <Skeleton active paragraph={{ rows: 2 }} title={false} /> : null}
    </Space>
  </div>
);

export const ErrorState = ({
  title,
  description,
  action,
  onRetry,
  retryText = "Retry",
  compact = false
}: RetryStateProps) => (
  <div className={compact ? "state-block state-block--compact" : "state-block"}>
    <Result
      status="error"
      icon={<WarningOutlined />}
      title={title}
      subTitle={description}
      extra={
        action ??
        (onRetry ? (
          <Button type="primary" onClick={onRetry}>
            {retryText}
          </Button>
        ) : undefined)
      }
    />
  </div>
);

export const DisabledState = ({
  title,
  description,
  action,
  compact = false
}: CommonStateProps) => (
  <Alert
    className={compact ? "state-alert state-alert--compact" : "state-alert"}
    type="info"
    showIcon
    icon={<StopOutlined />}
    message={title}
    description={description}
    action={action}
  />
);

export const ApiUnavailableState = ({
  title = "API pending",
  description = "This page has no usable backend route yet, so it stays read-only and does not fabricate response data.",
  action,
  compact = false
}: Partial<CommonStateProps>) => (
  <Alert
    className={compact ? "state-alert state-alert--compact" : "state-alert"}
    type="warning"
    showIcon
    icon={<ApiOutlined />}
    message={title}
    description={description}
    action={action}
  />
);

export const ToolUnavailableState = ({
  title = "Tool unavailable",
  description = "The tool adapter is not enabled or has no configured base URL, so related actions stay disabled.",
  action,
  compact = false
}: Partial<CommonStateProps>) => (
  <Alert
    className={compact ? "state-alert state-alert--compact" : "state-alert"}
    type="warning"
    showIcon
    icon={<DisconnectOutlined />}
    message={title}
    description={description}
    action={action}
  />
);
