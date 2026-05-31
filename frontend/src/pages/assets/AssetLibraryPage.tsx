import { Button, Card, Empty, Space, Table, Tag, Typography, Upload, message } from "antd";
import { UploadOutlined, FolderOpenOutlined, FileImageOutlined, FileTextOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";

const { Text } = Typography;

interface AssetOcrTask {
  id: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  status: string;
  createTime: string;
}

function formatBytes(bytes?: number): string {
  if (!bytes) return "-";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatDateTime(value?: string) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("zh-CN", { hour12: false });
}

export default function AssetLibraryPage() {
  const [uploading, setUploading] = useState(false);

  const assetsQuery = useQuery({
    queryKey: ["assets"],
    queryFn: () => api.ocrTasks.list({ pageNum: 1, pageSize: 50 })
  });

  const assets = useMemo(() => {
    const data = assetsQuery.data as { data?: AssetOcrTask[] } | undefined;
    return data?.data ?? [];
  }, [assetsQuery.data]);

  const metrics = useMemo(() => {
    const total = assets.length;
    const images = assets.filter((a) => a.fileType?.startsWith("image")).length;
    const documents = assets.filter((a) => a.fileType?.startsWith("application")).length;
    const totalSize = assets.reduce((sum, a) => sum + (a.fileSize || 0), 0);
    return { total, images, documents, totalSize };
  }, [assets]);

  const columns = [
    { title: "ID", dataIndex: "id", width: 70 },
    { title: "文件名", dataIndex: "fileName", ellipsis: true },
    { title: "类型", dataIndex: "fileType", width: 120, render: (v: string) => <Tag>{v || "--"}</Tag> },
    { title: "大小", dataIndex: "fileSize", width: 100, render: (v: number) => formatBytes(v) },
    { title: "状态", dataIndex: "status", width: 100, render: (v: string) => <Tag color="green">{v || "正常"}</Tag> },
    { title: "创建时间", dataIndex: "createTime", width: 160, render: formatDateTime }
  ];

  if (assetsQuery.isError) {
    return <ErrorState title="素材加载失败" description={assetsQuery.error?.message} onRetry={() => void assetsQuery.refetch()} />;
  }

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <h1 className="df-page-title">素材库</h1>
        <p className="df-page-desc">管理商品图片、文档等素材资源</p>
      </div>

      {/* 指标卡片 */}
      <div className="df-grid-4" style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-metric-card">
          <div className="df-metric-icon blue"><FolderOpenOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">素材总数</div>
            <div className="df-metric-value">{metrics.total}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon green"><FileImageOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">图片素材</div>
            <div className="df-metric-value">{metrics.images}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon purple"><FileTextOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">文档素材</div>
            <div className="df-metric-value">{metrics.documents}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon orange"><FolderOpenOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">总大小</div>
            <div className="df-metric-value">{formatBytes(metrics.totalSize)}</div>
          </div>
        </div>
      </div>

      {/* 素材列表 */}
      <Card>
        <div className="df-section-header">
          <span className="df-section-title">素材列表</span>
          <Space>
            <Upload
              showUploadList={false}
              beforeUpload={async (file) => {
                setUploading(true);
                try {
                  await api.ocrTasks.create({ fileName: file.name, sourceType: "UPLOAD" } as any);
                  message.success("上传成功");
                  void assetsQuery.refetch();
                } catch (err) {
                  message.error(err instanceof Error ? err.message : "上传失败");
                } finally {
                  setUploading(false);
                }
                return false;
              }}
            >
              <Button type="primary" icon={<UploadOutlined />} loading={uploading}>
                上传素材
              </Button>
            </Upload>
            <Button icon={<FolderOpenOutlined />} onClick={() => void assetsQuery.refetch()} loading={assetsQuery.isFetching}>
              刷新
            </Button>
          </Space>
        </div>
        <Table
          dataSource={assets}
          columns={columns}
          rowKey="id"
          loading={assetsQuery.isPending}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: <Empty description="暂无素材，上传第一个文件" /> }}
        />
      </Card>
    </div>
  );
}