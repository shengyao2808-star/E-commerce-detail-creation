import {
  Button,
  Card,
  Descriptions,
  Empty,
  Space,
  Table,
  Tag,
  Typography
} from "antd";
import { UploadOutlined } from "@ant-design/icons";
import { useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState } from "../../components/common";
import { P0Scaffold } from "../p0/P0Scaffold";
import { parseTabularFile, type PreviewCell, type TabularPreview } from "../../lib/fileParsers";
import { renderPdfPreview } from "../../lib/pdfPreview";
import { useWorkbenchStore } from "../../stores/workbenchStore";

const { Paragraph, Text } = Typography;

type TabularImportCardProps = {
  title: string;
  description: string;
  accept: string;
  onPreview?: (preview: TabularPreview) => void;
};

function formatCell(value: PreviewCell) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  return String(value);
}

function TabularImportCard({ title, description, accept, onPreview }: TabularImportCardProps) {
  const [preview, setPreview] = useState<TabularPreview | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const columns = useMemo(
    () =>
      preview?.headers.map((header) => ({
        title: header,
        dataIndex: header,
        key: header,
        render: (value: PreviewCell) => formatCell(value)
      })) ?? [],
    [preview?.headers]
  );

  const handlePickFile = async (file: File | null) => {
    if (!file) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const nextPreview = await parseTabularFile(file);
      setPreview(nextPreview);
      onPreview?.(nextPreview);
    } catch (requestError) {
      setPreview(null);
      setError(requestError instanceof Error ? requestError.message : "解析失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card
      className="p0-card"
      title={title}
      extra={
        <Space>
          <input
            ref={inputRef}
            type="file"
            hidden
            accept={accept}
            onChange={(event) => void handlePickFile(event.target.files?.[0] ?? null)}
          />
          <Button icon={<UploadOutlined />} onClick={() => inputRef.current?.click()} loading={loading}>
            选择文件
          </Button>
        </Space>
      }
    >
      <Space direction="vertical" size={16} style={{ width: "100%" }}>
        <Paragraph type="secondary">{description}</Paragraph>

        {error && <EmptyState title="解析失败" description={error} />}
        {!error && !preview && <EmptyState compact title="待接入" description="选择真实文件后显示结构化预览。" />}

        {preview && (
          <>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="文件名">{preview.fileName}</Descriptions.Item>
              <Descriptions.Item label="来源">{preview.sourceType.toUpperCase()}</Descriptions.Item>
              <Descriptions.Item label="表数量">{preview.sheetNames.length}</Descriptions.Item>
              <Descriptions.Item label="预览行">{preview.rows.length}</Descriptions.Item>
            </Descriptions>

            <Space wrap>
              {preview.sheetNames.map((sheetName) => (
                <Tag key={sheetName}>{sheetName}</Tag>
              ))}
            </Space>

            {preview.rows.length > 0 ? (
              <Table
                size="small"
                rowKey={(_, index) => String(index)}
                dataSource={preview.rows}
                columns={columns}
                pagination={false}
                scroll={{ x: true }}
              />
            ) : (
              <Empty description="文件已解析，但没有可展示的数据行。" />
            )}
          </>
        )}
      </Space>
    </Card>
  );
}

function PdfPreviewCard() {
  const [fileName, setFileName] = useState("");
  const [pageCount, setPageCount] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const handlePickFile = async (file: File | null) => {
    if (!file) {
      return;
    }

    setLoading(true);
    setError(null);
    setPageCount(null);
    setFileName(file.name);

    try {
      const canvas = canvasRef.current;
      if (!canvas) {
        throw new Error("预览画布不可用");
      }

      const next = await renderPdfPreview(file, canvas);
      setPageCount(next.pageCount);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "PDF 预览失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card
      className="p0-card"
      title="商品资料 PDF 预览"
      extra={
        <Space>
          <input
            ref={inputRef}
            type="file"
            hidden
            accept=".pdf"
            onChange={(event) => void handlePickFile(event.target.files?.[0] ?? null)}
          />
          <Button icon={<UploadOutlined />} onClick={() => inputRef.current?.click()} loading={loading}>
            选择 PDF
          </Button>
        </Space>
      }
    >
      <Space direction="vertical" size={16} style={{ width: "100%" }}>
        {!fileName && !error && <EmptyState compact title="待接入" description="上传真实 PDF 后预览第一页。" />}
        {error && <EmptyState title="预览失败" description={error} />}

        {fileName && !error && (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="文件名">{fileName}</Descriptions.Item>
            <Descriptions.Item label="页数">{pageCount ?? "-"}</Descriptions.Item>
          </Descriptions>
        )}

        <div className="pdf-preview-shell">
          <canvas ref={canvasRef} className="pdf-preview-canvas" />
        </div>
      </Space>
    </Card>
  );
}

export default function DataImportPage() {
  const setCurrentProduct = useWorkbenchStore((state) => state.setCurrentProduct);

  return (
    <P0Scaffold
      eyebrow="工具中心"
      title="资料导入工具"
      description="本页接入前端文件解析能力，仅在用户选择真实文件后显示预览，不生成虚构数据。"
      actions={[
        { label: "返回工具中心", to: "/tools" },
        { label: "设计草稿", to: "/tools/design-draft" }
      ]}
      flow={["Excel 导入", "CSV 解析", "PDF 预览", "草稿流转"]}
      apiNotice={false}
      toolNotice={false}
    >
      <div className="p0-notice-grid">
        <Card className="p0-card" title="当前商品上下文">
          <Space direction="vertical" size={8}>
            <Text type="secondary">商品资料 Excel 的首行解析结果会同步到当前商品状态。</Text>
            <CurrentProductPanel />
          </Space>
        </Card>
        <Card className="p0-card" title="接入说明">
          <Space direction="vertical" size={8}>
            <Text type="secondary">SheetJS 用于 Excel 预览，PapaParse 用于 CSV 预览，PDF.js 用于本地 PDF 首页渲染。</Text>
            <Link to="/materials/new">打开商品资料创建页</Link>
            <Link to="/research">打开市场调研页</Link>
          </Space>
        </Card>
      </div>

      <TabularImportCard
        title="商品资料 Excel 导入"
        description="支持 .xlsx / .xls，解析首个工作表并展示前 20 行结构化预览。"
        accept=".xlsx,.xls"
        onPreview={(preview) => {
          const firstRow = preview.rows[0];
          if (!firstRow) {
            setCurrentProduct(null);
            return;
          }

          const productName =
            firstRow.productName ??
            firstRow.商品名称 ??
            firstRow.name ??
            firstRow.title ??
            "";
          const sku = firstRow.sku ?? firstRow.SKU ?? firstRow.商品SKU ?? "";
          const category = firstRow.category ?? firstRow.类目 ?? firstRow.分类 ?? "";
          const brandName = firstRow.brandName ?? firstRow.brand ?? firstRow.品牌 ?? "";

          setCurrentProduct({
            id: preview.fileName,
            name: productName ? String(productName) : preview.fileName,
            sku: sku ? String(sku) : undefined,
            category: category ? String(category) : undefined,
            brandName: brandName ? String(brandName) : undefined
          });
        }}
      />

      <TabularImportCard
        title="竞品 Excel 导入"
        description="支持竞品资料的工作表预览，不自动生成竞品结论或平台数据。"
        accept=".xlsx,.xls"
      />

      <TabularImportCard
        title="评论 CSV / Excel 导入"
        description="支持 CSV 和 Excel 的评论数据结构预览，仅展示真实文件内容。"
        accept=".csv,.xlsx,.xls"
      />

      <PdfPreviewCard />
    </P0Scaffold>
  );
}

function CurrentProductPanel() {
  const currentProduct = useWorkbenchStore((state) => state.currentProduct);

  if (!currentProduct) {
    return <Empty description="尚未从导入文件识别出当前商品。" />;
  }

  return (
    <Descriptions bordered size="small" column={1}>
      <Descriptions.Item label="名称">{currentProduct.name ?? "-"}</Descriptions.Item>
      <Descriptions.Item label="SKU">{currentProduct.sku ?? "-"}</Descriptions.Item>
      <Descriptions.Item label="类目">{currentProduct.category ?? "-"}</Descriptions.Item>
      <Descriptions.Item label="品牌">{currentProduct.brandName ?? "-"}</Descriptions.Item>
    </Descriptions>
  );
}
