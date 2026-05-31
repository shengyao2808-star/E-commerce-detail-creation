import { Button, Card, Space, Table, Typography, Upload, message } from "antd";
import { UploadOutlined, FileExcelOutlined, FilePdfOutlined, FileTextOutlined } from "@ant-design/icons";
import { useMemo, useRef, useState } from "react";
import { EmptyState } from "../../components/common";
import { parseTabularFile, type PreviewCell, type TabularPreview } from "../../lib/fileParsers";

const { Text } = Typography;

function formatCell(value: PreviewCell) {
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

export default function DataImportPage() {
  const [preview, setPreview] = useState<TabularPreview | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

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

  const handleFile = async (file: File) => {
    setLoading(true);
    setError(null);
    try {
      const result = await parseTabularFile(file);
      setPreview(result);
      message.success("文件解析成功");
    } catch (err) {
      setPreview(null);
      setError(err instanceof Error ? err.message : "解析失败");
      message.error("文件解析失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <h1 className="df-page-title">数据导入</h1>
        <p className="df-page-desc">导入 Excel、CSV、JSON 等格式的数据文件</p>
      </div>

      {/* 导入类型卡片 */}
      <div className="df-grid-3" style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-quick-action">
          <div className="df-quick-action-icon"><FileExcelOutlined /></div>
          <div className="df-quick-action-text">
            <div className="df-quick-action-title">Excel 导入</div>
            <div className="df-quick-action-desc">支持 .xlsx, .xls 格式</div>
          </div>
        </div>
        <div className="df-quick-action">
          <div className="df-quick-action-icon"><FileTextOutlined /></div>
          <div className="df-quick-action-text">
            <div className="df-quick-action-title">CSV 导入</div>
            <div className="df-quick-action-desc">支持 .csv 格式</div>
          </div>
        </div>
        <div className="df-quick-action">
          <div className="df-quick-action-icon"><FilePdfOutlined /></div>
          <div className="df-quick-action-text">
            <div className="df-quick-action-title">PDF 导入</div>
            <div className="df-quick-action-desc">支持 .pdf 格式</div>
          </div>
        </div>
      </div>

      {/* 文件上传区域 */}
      <Card style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-section-header">
          <span className="df-section-title">上传文件</span>
        </div>
        <div style={{
          border: "2px dashed var(--df-border)",
          borderRadius: "var(--df-radius-lg)",
          padding: "var(--df-space-8)",
          textAlign: "center",
          background: "var(--df-bg)"
        }}>
          <Upload.Dragger
            accept=".xlsx,.xls,.csv,.json,.pdf"
            showUploadList={false}
            beforeUpload={async (file) => {
              await handleFile(file);
              return false;
            }}
            style={{ background: "transparent" }}
          >
            <p style={{ fontSize: 48, color: "var(--df-text-muted)", marginBottom: "var(--df-space-4)" }}>
              <UploadOutlined />
            </p>
            <p style={{ fontSize: "var(--df-text-lg)", color: "var(--df-text)", marginBottom: "var(--df-space-2)" }}>
              点击或拖拽文件到此区域
            </p>
            <p style={{ color: "var(--df-text-muted)" }}>
              支持 Excel、CSV、JSON、PDF 格式
            </p>
          </Upload.Dragger>
        </div>
      </Card>

      {/* 预览区域 */}
      {error && (
        <Card style={{ marginBottom: "var(--df-space-6)" }}>
          <EmptyState title="解析失败" description={error} />
        </Card>
      )}

      {preview && (
        <Card>
          <div className="df-section-header">
            <span className="df-section-title">数据预览</span>
            <Text type="secondary">{preview.rows.length} 行数据</Text>
          </div>
          <Table
            dataSource={preview.rows.slice(0, 100)}
            columns={columns}
            rowKey={(_, index) => String(index)}
            loading={loading}
            pagination={{ pageSize: 20 }}
            scroll={{ x: "max-content" }}
            locale={{ emptyText: <EmptyState title="无数据" description="文件中没有数据" compact /> }}
          />
        </Card>
      )}

      {!preview && !error && (
        <Card>
          <EmptyState title="等待导入" description="上传文件后将在此显示数据预览" compact />
        </Card>
      )}
    </div>
  );
}