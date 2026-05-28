import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import { QueryClientProvider } from "@tanstack/react-query";
import { RouterProvider } from "react-router-dom";
import { router } from "./routes";
import { queryClient } from "./lib/queryClient";
import "./styles/theme.css";
import "./styles/glass.css";

const App = () => (
  <ConfigProvider
    locale={zhCN}
    theme={{
      token: {
        colorPrimary: "#1890ff",
        colorSuccess: "#52c41a",
        colorWarning: "#faad14",
        colorError: "#f5222d",
        colorTextBase: "#1f2937",
        borderRadius: 8,
        fontFamily: "Source Han Sans SC, Noto Sans SC, system-ui, sans-serif"
      },
      components: {
        Layout: {
          bodyBg: "transparent",
          headerBg: "transparent",
          siderBg: "transparent"
        },
        Card: {
          borderRadiusLG: 8
        },
        Table: {
          headerBg: "rgba(248, 250, 252, 0.82)"
        }
      }
    }}
  >
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </ConfigProvider>
);

export default App;
