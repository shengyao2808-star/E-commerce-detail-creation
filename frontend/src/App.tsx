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
        colorPrimary: "#c0392b",
        colorSuccess: "#27ae60",
        colorWarning: "#d4a017",
        colorError: "#c0392b",
        colorTextBase: "#2c2318",
        borderRadius: 8,
        fontFamily: "Noto Sans SC, Source Han Sans SC, system-ui, sans-serif"
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
          headerBg: "#f8f5f0",
          rowHoverBg: "rgba(192, 57, 43, 0.03)"
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
);

export default App;