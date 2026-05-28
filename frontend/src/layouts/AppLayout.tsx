import { Grid, Layout, Space, Typography } from "antd";
import { useState } from "react";
import { Outlet } from "react-router-dom";
import { SideNav } from "./SideNav";
import { TopBar } from "./TopBar";

const { Content, Footer } = Layout;
const { Text } = Typography;

const AppLayout = () => {
  const screens = Grid.useBreakpoint();
  const [collapsed, setCollapsed] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const compact = !screens.md;

  return (
    <Layout className="workbench-shell">
      <TopBar showMenuButton={compact} onOpenNav={() => setDrawerOpen(true)} />

      <Layout className="workbench-main">
        <SideNav
          collapsed={collapsed}
          compact={compact}
          drawerOpen={drawerOpen}
          onCollapseChange={setCollapsed}
          onDrawerClose={() => setDrawerOpen(false)}
        />

        <Content className="workbench-content">
          <Outlet />
        </Content>
      </Layout>

      <Footer className="workbench-footer glass-surface">
        <Space split={<span className="footer-separator" />} wrap>
          <Text>系统状态：按真实接口返回展示</Text>
          <Text>AI 服务：待接入本地 AI 服务</Text>
          <Text>工具适配器：默认待配置 / 不可用</Text>
        </Space>
      </Footer>
    </Layout>
  );
};

export default AppLayout;
