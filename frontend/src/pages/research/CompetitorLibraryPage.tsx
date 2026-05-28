import { Card, Empty } from "antd";
import { P0Scaffold } from "../p0/P0Scaffold";

export default function CompetitorLibraryPage() {
  return (
    <P0Scaffold
      eyebrow="Research"
      title="Competitor Library"
      description="This page stays empty until the backend provides compliant competitor-library APIs. No external scraping, fake brands, rankings, or evidence are shown here."
      apiNotice
      toolNotice
      capabilities={[
        { title: "Competitor Records", description: "Pending backend research/competitor business APIs.", status: "pending" },
        {
          title: "Evidence & Authorization",
          description: "Source links, authorization scope, and collection timestamps are pending backend support.",
          status: "pending"
        },
        { title: "Visual Tags", description: "Tagging rules and tooling integration are pending.", status: "disabled" }
      ]}
    >
      <Card className="p0-card" title="Competitor Data">
        <Empty description="No real competitor data is available yet. Connect compliant backend sources first, then render the list." />
      </Card>
    </P0Scaffold>
  );
}
