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

const AI_PENDING_TEXT = "Local AI service pending";

export const AiPendingNotice = ({
  title,
  message,
  label = AI_PENDING_TEXT,
  compact = false,
  onClose
}: AiPendingNoticeProps) => {
  const [open, setOpen] = useState(false);
  const modalTitle = title ?? "AI service not connected";
  const modalMessage =
    message ??
    "This page only exposes the local AI entry point. Generation, OCR orchestration, and PDF extraction remain unavailable until a real service is connected.";

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
            Close
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
