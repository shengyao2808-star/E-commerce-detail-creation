import { MenuFoldOutlined, MenuUnfoldOutlined } from "@ant-design/icons";
import { Button, Drawer, Layout, Menu } from "antd";
import { useMemo } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { defaultOpenKeys, getSelectedKey, navigationItems } from "./navigation";

const { Sider } = Layout;

type SideNavProps = {
  collapsed: boolean;
  compact: boolean;
  drawerOpen: boolean;
  onCollapseChange: (collapsed: boolean) => void;
  onDrawerClose: () => void;
};

export const SideNav = ({
  collapsed,
  compact,
  drawerOpen,
  onCollapseChange,
  onDrawerClose,
}: SideNavProps) => {
  const navigate = useNavigate();
  const location = useLocation();
  const selectedKeys = useMemo(() => [getSelectedKey(location.pathname)], [location.pathname]);

  const menu = (
    <Menu
      mode="inline"
      selectedKeys={selectedKeys}
      defaultOpenKeys={defaultOpenKeys}
      items={navigationItems}
      onClick={({ key }) => {
        navigate(key);
        onDrawerClose();
      }}
    />
  );

  if (compact) {
    return (
      <Drawer
        title="工作台导航"
        placement="left"
        open={drawerOpen}
        onClose={onDrawerClose}
        width={292}
        className="workbench-nav-drawer"
      >
        {menu}
      </Drawer>
    );
  }

  return (
    <Sider
      className="workbench-sider glass-surface"
      width={248}
      collapsedWidth={76}
      collapsed={collapsed}
      trigger={null}
    >
      <div className="workbench-collapse">
        <Button
          aria-label={collapsed ? "展开导航" : "收起导航"}
          icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          onClick={() => onCollapseChange(!collapsed)}
          type="text"
        />
      </div>
      {menu}
    </Sider>
  );
};
