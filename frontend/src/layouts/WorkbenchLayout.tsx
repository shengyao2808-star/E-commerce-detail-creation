import {
  AuditOutlined,
  ExportOutlined,
  FileSearchOutlined,
  FileTextOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ProductOutlined,
  SafetyCertificateOutlined
} from "@ant-design/icons";
import { Button, Grid, Layout, Menu, Space, Tag, Typography } from "antd";
import type { MenuProps } from "antd";
import { useMemo, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { AiPendingNotice } from "../components/common";

const { Header, Sider, Content, Footer } = Layout;
const { Text, Title } = Typography;

const navigationItems: MenuProps["items"] = [
  { key: "/materials", icon: <ProductOutlined />, label: "商品资料" },
  { key: "/details/1", icon: <FileTextOutlined />, label: "详情编辑" },
  { key: "/details/1/review", icon: <SafetyCertificateOutlined />, label: "合规审查" },
  { key: "/audit", icon: <AuditOutlined />, label: "审核中心" },
  { key: "/exports", icon: <ExportOutlined />, label: "导出记录" }
];

const getSelectedKey = (pathname: string) => {
  if (pathname.startsWith("/materials")) return "/materials";
  if (pathname.endsWith("/review")) return "/details/1/review";
  if (pathname.startsWith("/details")) return "/details/1";
  if (pathname.startsWith("/audit")) return "/audit";
  if (pathname.startsWith("/exports")) return "/exports";
  return "/materials";
};

const WorkbenchLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const screens = Grid.useBreakpoint();
  const [collapsed, setCollapsed] = useState(false);

  const selectedKeys = useMemo(
    () => [getSelectedKey(location.pathname)],
    [location.pathname]
  );

  const isCompact = !screens.md;
  const sideCollapsed = isCompact || collapsed;

  return (
    <Layout className="workbench-shell">
      <Header className="workbench-header glass-surface">
        <Space className="workbench-brand" size={12}>
          <FileSearchOutlined className="workbench-brand-icon" />
          <div>
            <Title level={4}>电商详情页 AI 工作台</Title>
            <Text type="secondary">企业后台控制台</Text>
          </div>
        </Space>
        <Space size={8} wrap>
          <Tag color="success">后端接口 /api/v1</Tag>
          <AiPendingNotice compact />
        </Space>
      </Header>

      <Layout className="workbench-main">
        <Sider
          className="workbench-sider glass-surface"
          width={232}
          collapsedWidth={isCompact ? 0 : 72}
          collapsed={sideCollapsed}
          trigger={null}
        >
          <div className="workbench-collapse">
            {!isCompact && (
              <Button
                aria-label={collapsed ? "展开导航" : "收起导航"}
                icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                onClick={() => setCollapsed((value) => !value)}
                type="text"
              />
            )}
          </div>
          <Menu
            mode="inline"
            selectedKeys={selectedKeys}
            items={navigationItems}
            onClick={({ key }) => navigate(key)}
          />
        </Sider>

        <Content className="workbench-content">
          <Outlet />
        </Content>
      </Layout>

      <Footer className="workbench-footer glass-surface">
        <Space split={<span className="footer-separator" />} wrap>
          <Text>系统状态：正常</Text>
          <Text>AI 服务：待接入本地 AI 服务</Text>
          <Text>版本：v0.1.0</Text>
        </Space>
      </Footer>
    </Layout>
  );
};

export default WorkbenchLayout;
