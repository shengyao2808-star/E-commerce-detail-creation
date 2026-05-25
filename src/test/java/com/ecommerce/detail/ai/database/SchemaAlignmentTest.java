package com.ecommerce.detail.ai.database;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaAlignmentTest {

    @Test
    void schemaContainsFieldsUsedByCoreEntities() throws Exception {
        String schema = Files.readString(Path.of("src/main/resources/db/schema.sql"), StandardCharsets.UTF_8);

        assertTrue(schema.contains("material_id BIGINT"), "product_detail must contain material_id");
        assertTrue(schema.contains("product_name VARCHAR"), "product_detail must contain product_name");
        assertTrue(schema.contains("image_template_id BIGINT"), "product_detail must contain image_template_id");
        assertTrue(schema.contains("export_status TINYINT"), "export_record must contain export_status");
        assertTrue(schema.contains("error_message TEXT"), "export_record must contain error_message");
        assertTrue(schema.contains("audit_type VARCHAR"), "audit_record must contain audit_type");
        assertTrue(schema.contains("risk_items TEXT"), "audit_record must contain risk_items");
        assertTrue(schema.contains("style_description TEXT"), "brand_template must contain style_description");
        assertTrue(schema.contains("parse_status TINYINT"), "product_material must contain parse_status");
    }
}
