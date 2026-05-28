import { Card, Typography } from "antd";
import * as echarts from "echarts";
import { useEffect, useMemo, useRef } from "react";

const { Text } = Typography;

type ChartDatum = {
  label?: string;
  value?: number;
  name?: string;
  x?: number;
  y?: number;
};

type PlaceholderChartProps = {
  title: string;
  description: string;
  className?: string;
  mode?: "bar" | "scatter";
  data?: ChartDatum[];
};

const buildOption = (title: string, description: string, mode?: "bar" | "scatter", data: ChartDatum[] = []): echarts.EChartsOption => {
  if (data.length === 0) {
    return {
      backgroundColor: "transparent",
      title: {
        text: title,
        left: 16,
        top: 12,
        textStyle: {
          fontSize: 14,
          fontWeight: 600,
          color: "#172033"
        }
      },
      tooltip: { show: false },
      grid: { left: 16, right: 16, top: 52, bottom: 16, containLabel: true },
      xAxis: { type: "category", show: false },
      yAxis: { type: "value", show: false },
      series: [],
      graphic: [
        {
          type: "text",
          left: "center",
          top: "middle",
          style: {
            text: "Waiting",
            fontSize: 20,
            fontWeight: 600,
            fill: "#64748b"
          }
        },
        {
          type: "text",
          left: "center",
          top: "58%",
          style: {
            text: description,
            fontSize: 12,
            fill: "#94a3b8"
          }
        }
      ]
    };
  }

  if (mode === "scatter" || data.some((item) => typeof item.x === "number" || typeof item.y === "number")) {
    return {
      backgroundColor: "transparent",
      title: {
        text: title,
        left: 16,
        top: 12,
        textStyle: {
          fontSize: 14,
          fontWeight: 600,
          color: "#172033"
        }
      },
      tooltip: {
        trigger: "item",
        formatter: (params: any) => {
          const tuple = Array.isArray(params.value) ? params.value : [];
          return `${params.name ?? ""}<br/>X: ${tuple[0] ?? "-"}<br/>Y: ${tuple[1] ?? "-"}`;
        }
      },
      grid: { left: 24, right: 24, top: 52, bottom: 24, containLabel: true },
      xAxis: { type: "value", name: "Competition" },
      yAxis: { type: "value", name: "Popularity" },
      series: [
        {
          type: "scatter",
          symbolSize: 14,
          data: data.map((item) => ({
            value: [Number(item.x ?? 0), Number(item.y ?? 0)],
            name: item.name ?? item.label ?? ""
          }))
        }
      ]
    };
  }

  return {
    backgroundColor: "transparent",
    title: {
      text: title,
      left: 16,
      top: 12,
      textStyle: {
        fontSize: 14,
        fontWeight: 600,
        color: "#172033"
      }
    },
    tooltip: {
      trigger: "axis"
    },
    grid: { left: 24, right: 24, top: 52, bottom: 24, containLabel: true },
    xAxis: {
      type: "category",
      data: data.map((item) => item.label ?? item.name ?? ""),
      axisLabel: { color: "#64748b" }
    },
    yAxis: {
      type: "value",
      axisLabel: { color: "#64748b" }
    },
    series: [
      {
        type: "bar",
        data: data.map((item) => Number(item.value ?? 0)),
        itemStyle: {
          color: "#2563eb"
        }
      }
    ]
  };
};

export function PlaceholderChart({ title, description, className, mode, data }: PlaceholderChartProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const option = useMemo(() => buildOption(title, description, mode, data), [data, description, mode, title]);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) {
      return;
    }

    const chart = echarts.init(container);
    chart.setOption(option);

    const resize = () => chart.resize();
    window.addEventListener("resize", resize);

    return () => {
      window.removeEventListener("resize", resize);
      chart.dispose();
    };
  }, [option]);

  return (
    <Card className={className} title={title}>
      <div className="chart-placeholder">
        <div ref={containerRef} className="chart-placeholder__canvas" />
        <Text type="secondary">{description}</Text>
      </div>
    </Card>
  );
}
