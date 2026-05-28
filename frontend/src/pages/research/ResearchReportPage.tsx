import { Card, Empty } from "antd";
import { useParams } from "react-router-dom";
import { P0Scaffold } from "../p0/P0Scaffold";

export default function ResearchReportPage() {
  const { id } = useParams<{ id: string }>();

  return (
    <P0Scaffold
      eyebrow="Research"
      title={`Research Report${id ? ` #${id}` : ""}`}
      description="Report output remains pending until backend report APIs and verified evidence payloads are available. No generated competitor rankings or conclusions are shown here."
      apiNotice
      toolNotice
      capabilities={[
        { title: "Report Summary", description: "Pending backend report payloads with real content.", status: "pending" },
        { title: "Evidence Chain", description: "Source links, timestamps, and authorization scope are required before render.", status: "pending" },
        { title: "AI Extraction", description: "Disabled by default until report APIs are available.", status: "disabled" }
      ]}
    >
      <Card className="p0-card" title="Report Content">
        <Empty description="No real report content is available yet. Connect backend report services before showing summary, opportunities, risks, or citations." />
      </Card>
    </P0Scaffold>
  );
}
