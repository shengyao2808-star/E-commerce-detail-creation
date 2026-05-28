export const formatDateTime = (value?: string): string => {
  if (!value) return "--";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString("zh-CN", { hour12: false });
};

export const safeJsonStringify = (value: unknown): string => {
  if (value === undefined) return "--";
  if (typeof value === "string") return value;
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
};

export const parseJsonValue = <T,>(raw: string, fallback: T): T => {
  const trimmed = raw.trim();
  if (!trimmed) return fallback;
  return JSON.parse(trimmed) as T;
};

export const parseListText = (raw: string): string[] =>
  raw
    .split(/[\n,，]+/)
    .map((item) => item.trim())
    .filter(Boolean);

export const textFromUnknown = (value: unknown): string => {
  if (value === undefined || value === null) return "--";
  if (typeof value === "string") return value;
  if (Array.isArray(value)) return value.map((item) => textFromUnknown(item)).join(", ");
  if (typeof value === "object") return safeJsonStringify(value);
  return String(value);
};
