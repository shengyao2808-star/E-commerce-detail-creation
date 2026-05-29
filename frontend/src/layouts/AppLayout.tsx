import { Outlet } from "react-router-dom";
import { SideNav } from "./SideNav";
import { TopBar } from "./TopBar";
import { AssistantPanel } from "./AssistantPanel";

const AppLayout = () => (
  <div className="df-shell">
    <TopBar />
    <SideNav />
    <main className="df-main">
      <Outlet />
    </main>
    <AssistantPanel />
  </div>
);

export default AppLayout;