import { create } from "zustand";

export type WorkbenchProduct = {
  id?: string;
  name?: string;
  sku?: string;
  category?: string;
  brandName?: string;
};

export type WorkbenchTask = {
  id?: string;
  name?: string;
  status?: string;
  updatedAt?: string;
};

export type SelectedAsset = {
  id: string;
  name: string;
  type?: "image" | "video" | "document" | "pdf";
  url?: string;
};

export type DetailDraft = {
  id?: string;
  title: string;
  subtitle: string;
  sellingPoints: string[];
  seoKeywords: string[];
  moduleOrder: string[];
};

export type ToolState = {
  status: "idle" | "loading" | "ready" | "error";
  activeTool?: string;
  message?: string;
  lastSyncedAt?: string;
};

export const DEFAULT_DETAIL_MODULE_ORDER = [
  "Hero",
  "Subtitle",
  "Selling Points",
  "SEO",
  "Images",
  "Documents"
];

const createDefaultDetailDraft = (): DetailDraft => ({
  title: "",
  subtitle: "",
  sellingPoints: [],
  seoKeywords: [],
  moduleOrder: [...DEFAULT_DETAIL_MODULE_ORDER]
});

type WorkbenchStore = {
  currentProduct: WorkbenchProduct | null;
  currentResearchTask: WorkbenchTask | null;
  currentGenerateTask: WorkbenchTask | null;
  selectedAssets: SelectedAsset[];
  detailDraft: DetailDraft;
  toolState: ToolState;
  setCurrentProduct: (product: WorkbenchProduct | null) => void;
  setCurrentResearchTask: (task: WorkbenchTask | null) => void;
  setCurrentGenerateTask: (task: WorkbenchTask | null) => void;
  setSelectedAssets: (assets: SelectedAsset[]) => void;
  toggleSelectedAsset: (asset: SelectedAsset) => void;
  clearSelectedAssets: () => void;
  setDetailDraft: (patch: Partial<DetailDraft>) => void;
  resetDetailDraft: () => void;
  setToolState: (patch: Partial<ToolState>) => void;
  resetToolState: () => void;
};

const normalizeAsset = (asset: SelectedAsset): SelectedAsset => ({
  ...asset,
  name: asset.name.trim()
});

export const useWorkbenchStore = create<WorkbenchStore>((set) => ({
  currentProduct: null,
  currentResearchTask: null,
  currentGenerateTask: null,
  selectedAssets: [],
  detailDraft: createDefaultDetailDraft(),
  toolState: {
    status: "idle"
  },
  setCurrentProduct: (product) => set({ currentProduct: product }),
  setCurrentResearchTask: (task) => set({ currentResearchTask: task }),
  setCurrentGenerateTask: (task) => set({ currentGenerateTask: task }),
  setSelectedAssets: (assets) => set({ selectedAssets: assets.map(normalizeAsset) }),
  toggleSelectedAsset: (asset) =>
    set((state) => {
      const normalizedAsset = normalizeAsset(asset);
      const exists = state.selectedAssets.some((item) => item.id === normalizedAsset.id);

      return {
        selectedAssets: exists
          ? state.selectedAssets.filter((item) => item.id !== normalizedAsset.id)
          : [...state.selectedAssets, normalizedAsset]
      };
    }),
  clearSelectedAssets: () => set({ selectedAssets: [] }),
  setDetailDraft: (patch) =>
    set((state) => ({
      detailDraft: {
        ...state.detailDraft,
        ...patch,
        sellingPoints: patch.sellingPoints ?? state.detailDraft.sellingPoints,
        seoKeywords: patch.seoKeywords ?? state.detailDraft.seoKeywords,
        moduleOrder: patch.moduleOrder ?? state.detailDraft.moduleOrder
      }
    })),
  resetDetailDraft: () => set({ detailDraft: createDefaultDetailDraft() }),
  setToolState: (patch) =>
    set((state) => ({
      toolState: {
        ...state.toolState,
        ...patch
      }
    })),
  resetToolState: () => set({ toolState: { status: "idle" } })
}));

export const workbenchSelectors = {
  currentProduct: (state: WorkbenchStore) => state.currentProduct,
  currentResearchTask: (state: WorkbenchStore) => state.currentResearchTask,
  currentGenerateTask: (state: WorkbenchStore) => state.currentGenerateTask,
  selectedAssets: (state: WorkbenchStore) => state.selectedAssets,
  detailDraft: (state: WorkbenchStore) => state.detailDraft,
  toolState: (state: WorkbenchStore) => state.toolState
};
