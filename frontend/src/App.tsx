import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import { QueryClientProvider } from "@tanstack/react-query";
import { RouterProvider } from "react-router-dom";
import { router } from "./routes";
import { queryClient } from "./lib/queryClient";
import { LangProvider } from "./i18n";
import "./styles/theme.css";
import "./styles/glass.css";

const App = () => (
  <LangProvider>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: "#5B52E0",
          colorSuccess: "#16A34A",
          colorWarning: "#F97316",
          colorError: "#EF4444",
          colorTextBase: "#1A1836",
          colorBgLayout: "#FAFAFD",
          colorBgContainer: "#FEFEFF",
          colorBorder: "#E4E3F0",
          borderRadius: 10,
          borderRadiusLG: 14,
          fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif"
        },
        components: {
          Layout: {
            bodyBg: "transparent",
            headerBg: "transparent",
            siderBg: "transparent"
          },
          Card: {
            borderRadiusLG: 14,
            paddingLG: 24
          },
          Button: {
            borderRadius: 10,
            controlHeight: 40
          },
          Input: {
            borderRadius: 10,
            controlHeight: 40
          },
          Table: {
            headerBg: "#F8FAFC",
            rowHoverBg: "#F9FAFB"
          },
          Menu: {
            itemBg: "transparent",
            subMenuItemBg: "transparent"
          }
        }
      }}
    >
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>
    </ConfigProvider>
  </LangProvider>
);

export default App;