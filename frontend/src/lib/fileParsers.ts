import Papa, { type ParseResult } from "papaparse";
import * as XLSX from "xlsx";

export type PreviewCell = string | number | boolean | null;

export type TabularPreview = {
  fileName: string;
  sourceType: "excel" | "csv";
  sheetNames: string[];
  headers: string[];
  rows: Array<Record<string, PreviewCell>>;
  rowCount: number;
};

type ParsedRow = Record<string, unknown>;

const isCsvFile = (file: File) => file.name.toLowerCase().endsWith(".csv") || file.type.includes("csv");

const normalizeCell = (value: unknown): PreviewCell => {
  if (value === undefined) {
    return null;
  }

  if (value === null) {
    return null;
  }

  if (typeof value === "string" || typeof value === "number" || typeof value === "boolean") {
    return value;
  }

  if (value instanceof Date) {
    return value.toISOString();
  }

  return String(value);
};

const collectHeaders = (rows: Array<Record<string, PreviewCell>>) => {
  const headers = new Set<string>();
  rows.forEach((row) => {
    Object.keys(row).forEach((key) => headers.add(key));
  });
  return Array.from(headers);
};

export const parseTabularFile = async (file: File): Promise<TabularPreview> => {
  if (isCsvFile(file)) {
    const result = await new Promise<ParseResult<ParsedRow>>((resolve, reject) => {
      Papa.parse<ParsedRow>(file, {
        header: true,
        skipEmptyLines: true,
        dynamicTyping: false,
        complete: (parseResult: ParseResult<ParsedRow>) => resolve(parseResult),
        error: (error: Error) => reject(error)
      });
    });

    const rows = (result.data ?? [])
      .filter((row: ParsedRow) => Object.keys(row).length > 0)
      .slice(0, 20)
      .map((row: ParsedRow) =>
        Object.fromEntries(
          Object.entries(row).map(([key, value]) => [key, normalizeCell(value)])
        )
      );

    return {
      fileName: file.name,
      sourceType: "csv",
      sheetNames: ["CSV"],
      headers: result.meta.fields ?? collectHeaders(rows),
      rows,
      rowCount: result.data?.length ?? 0
    };
  }

  const buffer = await file.arrayBuffer();
  const workbook = XLSX.read(buffer, { type: "array" });
  const firstSheetName = workbook.SheetNames[0] ?? "";
  const sheet = firstSheetName ? workbook.Sheets[firstSheetName] : null;
  const rawRows = sheet
    ? XLSX.utils.sheet_to_json<Record<string, unknown>>(sheet, { defval: "", blankrows: false })
    : [];
  const rows = rawRows.slice(0, 20).map((row) =>
    Object.fromEntries(
      Object.entries(row).map(([key, value]) => [key, normalizeCell(value)])
    )
  );

  return {
    fileName: file.name,
    sourceType: "excel",
    sheetNames: workbook.SheetNames,
    headers: collectHeaders(rows),
    rows,
    rowCount: rawRows.length
  };
};
