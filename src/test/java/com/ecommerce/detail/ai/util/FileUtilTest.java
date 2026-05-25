package com.ecommerce.detail.ai.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void extractTextFromImagesFailsWhenOcrIsNotConfigured() {
        assertThrows(UnsupportedOperationException.class,
                () -> FileUtil.extractTextFromImages(List.of("sample.png")));
    }

    @Test
    void extractTextFromUnsupportedDocumentFailsInsteadOfReturningPlaceholderText() {
        assertThrows(UnsupportedOperationException.class,
                () -> FileUtil.extractTextFromDocuments(List.of("sample.pdf")));
    }

    @Test
    void extractTextFromDocumentsReadsDocxContent() throws Exception {
        Path docxPath = tempDir.resolve("material.docx");
        try (XWPFDocument document = new XWPFDocument();
             OutputStream outputStream = Files.newOutputStream(docxPath)) {
            document.createParagraph().createRun().setText("Smart kettle product manual");
            document.createParagraph().createRun().setText("Capacity 1.7L with stainless steel body");
            document.write(outputStream);
        }

        String content = FileUtil.extractTextFromDocuments(List.of(docxPath.toString()));

        assertTrue(content.contains("Smart kettle product manual"));
        assertTrue(content.contains("Capacity 1.7L"));
    }
}
