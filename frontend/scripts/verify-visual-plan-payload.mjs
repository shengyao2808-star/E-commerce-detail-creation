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

// 1. Verify VisualPlanCreateRequest type includes promptContext
const types = await read("src/services/types.ts");
assert(
  types.includes("promptContext?: Record<string, unknown>"),
  "VisualPlanCreateRequest type must include promptContext field"
);

// 2. Verify buildVisualPlanCreatePayload is exported and maps promptContext
const api = await read("src/services/api.ts");
assert(
  api.includes("export const buildVisualPlanCreatePayload"),
  "buildVisualPlanCreatePayload must be exported"
);
assert(
  api.includes("const planData = payload.planData ?? payload.promptContext ?? {}"),
  "buildVisualPlanCreatePayload must fall back from planData to promptContext"
);
assert(
  api.includes("const inputData = payload.inputData ?? payload.promptContext ?? {}"),
  "buildVisualPlanCreatePayload must fall back from inputData to promptContext"
);

// 3. Verify visualPlanApi.create uses buildVisualPlanCreatePayload
assert(
  api.includes("body: buildVisualPlanCreatePayload(payload)"),
  "visualPlanApi.create must pass payload through buildVisualPlanCreatePayload"
);

// 4. Verify the contract test covers promptContext -> inputData/planData mapping
const contractTest = await read("src/services/api.contract.test.ts");
assert(
  contractTest.includes("buildVisualPlanCreatePayload"),
  "Contract test must import buildVisualPlanCreatePayload"
);
assert(
  contractTest.includes("promptContext:"),
  "Contract test must include a promptContext test case"
);
assert(
  contractTest.includes("_mappedVisualPlanCreatePayload.inputData"),
  "Contract test must verify inputData is accessible on mapped payload"
);
assert(
  contractTest.includes("_mappedVisualPlanCreatePayload.planData"),
  "Contract test must verify planData is accessible on mapped payload"
);

console.log("Visual-plan promptContext -> inputData/planData contract checks passed.");
