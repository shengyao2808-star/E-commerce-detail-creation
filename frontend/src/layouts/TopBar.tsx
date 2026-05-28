import { FileSearchOutlined, MenuOutlined } from "@ant-design/icons";
import { Button, Space, Tag, Typography } from "antd";
import { AiPendingNotice } from "../components/common";

const { Text, Title } = Typography;

type TopBarProps = {
  onOpenNav?: () => void;
  showMenuButton?: boolean;
};

export const TopBar = ({ onOpenNav, showMenuButton = false }: TopBarProps) => (
  <header className="workbench-header glass-surface">
    <Space className="workbench-brand" size={12}>
      {showMenuButton && (
        <Button aria-label="打开导航" icon={<MenuOutlined />} onClick={onOpenNav} type="text" />
      )}
      <FileSearchOutlined className="workbench-brand-icon" />
      <div className="workbench-brand-copy">
        <Title level={4}>电商商品视觉 AI 生产台</Title>
        <Text type="secondary">市场调研到详情页交付的企业级工作台</Text>
      </div>
    </Space>
    <Space size={8} wrap className="workbench-header-status">
      <Tag color="processing">后端接口 /api/v1</Tag>
      <Tag color="warning">工具默认待配置</Tag>
      <AiPendingNotice compact />
    </Space>
  </header>
);
