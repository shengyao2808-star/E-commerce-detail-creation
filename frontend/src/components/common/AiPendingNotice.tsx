import { RobotOutlined } from "@ant-design/icons";
import { Button, Modal, Space, Tooltip, Typography } from "antd";
import { useState } from "react";

type AiPendingNoticeProps = {
  title?: string;
  message?: string;
  label?: string;
  compact?: boolean;
  onClose?: () => void;
};

const AI_PENDING_TEXT = "待接入本地 AI 服务";

export const AiPendingNotice = ({
  title,
  message,
  label = AI_PENDING_TEXT,
  compact = false,
  onClose
}: AiPendingNoticeProps) => {
  const [open, setOpen] = useState(false);
  const modalTitle = title ?? "AI 服务未连接";
  const modalMessage =
    message ??
    "当前仅保留本地 AI 接入入口。生成、OCR、PDF 解析功能尚未实现，暂不可用。";

  return (
    <>
      <Tooltip title={AI_PENDING_TEXT}>
        <Button
          className="ai-pending-button"
          icon={<RobotOutlined />}
          onClick={() => setOpen(true)}
          size={compact ? "small" : "middle"}
        >
          {label}
        </Button>
      </Tooltip>
      <Modal
        title={modalTitle}
        open={open}
        onCancel={() => {
          setOpen(false);
          onClose?.();
        }}
        footer={[
          <Button key="close" type="primary" onClick={() => setOpen(false)}>
            关闭
          </Button>
        ]}
      >
        <Space direction="vertical" size={10}>
          <Typography.Text strong>{AI_PENDING_TEXT}</Typography.Text>
          <Typography.Text type="secondary">{modalMessage}</Typography.Text>
        </Space>
      </Modal>
    </>
  );
};
