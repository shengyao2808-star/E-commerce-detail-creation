import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, "..");

async function read(relativePath) {
  return readFile(path.join(root, relativePath), "utf8");
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

const routes = await read("src/routes/index.tsx");

const lazyImports = [
  "../pages/assets/AssetLibraryPage",
  "../pages/generate/GenerateWorkbenchPage",
  "../pages/generate/GenerateTaskDetailPage",
  "../pages/results/ResultsPreviewPage",
  "../pages/details/DetailEditorPage",
  "../pages/tools/DesignDraftPage",
  "../pages/tools/ToolDetailPage"
];

for (const importPath of lazyImports) {
  assert(
    routes.includes(`lazy(() => import("${importPath}"))`),
    `Expected route module to be lazy-loaded: ${importPath}`
  );
}

const forbiddenStaticImports = [
  'import AssetLibraryPage from "../pages/assets/AssetLibraryPage";',
  'import GenerateWorkbenchPage from "../pages/generate/GenerateWorkbenchPage";',
  'import GenerateTaskDetailPage from "../pages/generate/GenerateTaskDetailPage";',
  'import ResultsPreviewPage from "../pages/results/ResultsPreviewPage";',
  'import DetailEditorPage from "../pages/details/DetailEditorPage";',
  'import DesignDraftPage from "../pages/tools/DesignDraftPage";',
  'import ToolDetailPage from "../pages/tools/ToolDetailPage";'
];

for (const staticImport of forbiddenStaticImports) {
  assert(!routes.includes(staticImport), `Unexpected eager route import: ${staticImport}`);
}

const pageChecks = [
  {
    file: "src/pages/generate/GenerateWorkbenchPage.tsx",
    tokens: ["EmptyState", "ErrorState"]
  },
  {
    file: "src/pages/generate/GenerateTaskDetailPage.tsx",
    tokens: ["EmptyState", "ErrorState"]
  },
  {
    file: "src/pages/results/ResultsPreviewPage.tsx",
    tokens: ["EmptyState", "ErrorState"]
  },
  {
    file: "src/pages/details/DetailEditorPage.tsx",
    tokens: ["EmptyState"]
  },
  {
    file: "src/pages/tools/DesignDraftPage.tsx",
    tokens: ["EmptyState"]
  }
];

for (const check of pageChecks) {
  const source = await read(check.file);
  for (const token of check.tokens) {
    assert(source.includes(token), `Expected ${check.file} to keep ${token} handling.`);
  }
}

console.log("Route splitting regression checks passed.");
