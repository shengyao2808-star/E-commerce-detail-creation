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
  title = "加载中",
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
  title = "加载失败",
  description,
  action,
  onRetry,
  retryText = "重试",
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
  title = "API 待接入",
  description = "此页面尚无可用的后端路由，因此保持只读状态且不伪造响应数据。",
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
  title = "工具不可用",
  description = "工具适配器未启用或未配置基础 URL，因此相关操作保持禁用状态。",
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