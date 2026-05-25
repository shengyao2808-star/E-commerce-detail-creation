import { Table } from "antd";
import type { TableProps } from "antd";

export type ResponsiveTableProps<RecordType extends object> =
  TableProps<RecordType>;

export const ResponsiveTable = <RecordType extends object>({
  scroll,
  className,
  ...props
}: ResponsiveTableProps<RecordType>) => (
  <div className={["responsive-table", className].filter(Boolean).join(" ")}>
    <Table<RecordType>
      scroll={{ x: "max-content", ...scroll }}
      pagination={props.pagination ?? false}
      {...props}
    />
  </div>
);
