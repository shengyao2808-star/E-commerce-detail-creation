import { getDocument, GlobalWorkerOptions } from "pdfjs-dist";
import pdfWorkerSrc from "pdfjs-dist/build/pdf.worker.mjs?url";

GlobalWorkerOptions.workerSrc = pdfWorkerSrc;

export const renderPdfPreview = async (file: File, canvas: HTMLCanvasElement, scale = 1.35) => {
  const loadingTask = getDocument({ data: new Uint8Array(await file.arrayBuffer()) });
  const pdf = await loadingTask.promise;
  const page = await pdf.getPage(1);
  const viewport = page.getViewport({ scale });
  const context = canvas.getContext("2d");

  if (!context) {
    await pdf.destroy();
    throw new Error("PDF canvas unavailable");
  }

  canvas.width = Math.floor(viewport.width);
  canvas.height = Math.floor(viewport.height);
  canvas.style.width = `${Math.floor(viewport.width)}px`;
  canvas.style.height = `${Math.floor(viewport.height)}px`;

  await page.render({ canvas, canvasContext: context, viewport }).promise;
  await pdf.destroy();

  return {
    pageCount: pdf.numPages,
    width: viewport.width,
    height: viewport.height
  };
};
