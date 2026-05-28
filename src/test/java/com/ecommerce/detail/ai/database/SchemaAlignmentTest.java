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
        assertTrue(schema.contains("module_order JSON"), "product_detail must contain module_order");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS research_task"), "schema must contain research_task");
        assertTrue(schema.contains("input_json JSON"), "research_task must contain input_json");
        assertTrue(schema.contains("result_json JSON"), "research_task must contain result_json");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS product_content_task"), "schema must contain product_content_task");
        assertTrue(schema.contains("product_detail_id BIGINT NOT NULL"), "product_content_task must contain product_detail_id");
        assertTrue(schema.contains("output_text LONGTEXT"), "product_content_task must contain output_text");
        assertTrue(schema.contains("applied_fields_json JSON"), "product_content_task must contain applied_fields_json");
        assertTrue(schema.contains("applied_time DATETIME"), "product_content_task must contain applied_time");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS asset_ocr_task"), "schema must contain asset_ocr_task");
        assertTrue(schema.contains("ocr_text TEXT"), "asset_ocr_task must contain ocr_text");
        assertTrue(schema.contains("confidence DECIMAL"), "asset_ocr_task must contain confidence");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS design_draft"), "schema must contain design_draft");
        assertTrue(schema.contains("scene_json LONGTEXT"), "design_draft must contain scene_json");
        assertTrue(schema.contains("selected_assets_json JSON"), "design_draft must contain selected_assets_json");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS category_visual_policy"), "schema must contain category_visual_policy");
        assertTrue(schema.contains("allowed_shot_types_json JSON"), "category_visual_policy must contain allowed_shot_types_json");
        assertTrue(schema.contains("required_main_images_json JSON"), "category_visual_policy must contain required_main_images_json");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS model_profile"), "schema must contain model_profile");
        assertTrue(schema.contains("style_tags_json JSON"), "model_profile must contain style_tags_json");
        assertTrue(schema.contains("category_scopes_json JSON"), "model_profile must contain category_scopes_json");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS skc_policy"), "schema must contain skc_policy");
        assertTrue(schema.contains("colors_json JSON"), "skc_policy must contain colors_json");
        assertTrue(schema.contains("generation_rules_json JSON"), "skc_policy must contain generation_rules_json");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS prompt_workbench_entry"), "schema must contain prompt_workbench_entry");
        assertTrue(schema.contains("version INT NOT NULL DEFAULT 1"), "prompt_workbench_entry must contain version");
        assertTrue(schema.contains("output_text LONGTEXT"), "prompt_workbench_entry must contain output_text");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS visual_plan"), "schema must contain visual_plan");
        assertTrue(schema.contains("prompt_workbench_entry_ids_json JSON"), "visual_plan must contain prompt_workbench_entry_ids_json");
        assertTrue(schema.contains("confirmed_snapshot_json JSON"), "visual_plan must contain confirmed_snapshot_json");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS detail_composition"), "schema must contain detail_composition");
        assertTrue(schema.contains("product_detail_id BIGINT NOT NULL"), "detail_composition must contain product_detail_id");
        assertTrue(schema.contains("input_json JSON"), "detail_composition must contain input_json");
        assertTrue(schema.contains("tool_code VARCHAR"), "detail_composition must contain tool_code");
        assertTrue(schema.contains("status VARCHAR"), "detail_composition must contain status");
        assertTrue(schema.contains("output_path VARCHAR"), "detail_composition must contain output_path");
        assertTrue(schema.contains("error_message TEXT"), "detail_composition must contain error_message");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS detail_composition_result"), "schema must contain detail_composition_result");
        assertTrue(schema.contains("detail_composition_id BIGINT"), "detail_composition_result must contain detail_composition_id");
        assertTrue(schema.contains("mime_type VARCHAR"), "detail_composition_result must contain mime_type");
        assertTrue(schema.contains("image_width INT"), "detail_composition_result must contain image_width");
        assertTrue(schema.contains("image_height INT"), "detail_composition_result must contain image_height");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS image_job"), "schema must contain image_job");
        assertTrue(schema.contains("external_job_id"), "image_job must contain external_job_id");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS generation_result"), "schema must contain generation_result");
        assertTrue(schema.contains("image_job_id"), "generation_result must contain image_job_id");
        assertTrue(schema.contains("result_url"), "generation_result must contain result_url");
        assertTrue(schema.contains("compliance_status"), "generation_result must contain compliance_status");
        assertTrue(schema.contains("selected TINYINT"), "generation_result must contain selected state");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS detail_generation_result_link"), "schema must contain detail_generation_result_link");
        assertTrue(schema.contains("product_detail_id BIGINT NOT NULL"), "detail_generation_result_link must contain product_detail_id");
        assertTrue(schema.contains("generation_result_id BIGINT NOT NULL"), "detail_generation_result_link must contain generation_result_id");
        assertTrue(schema.contains("result_url VARCHAR(500) NOT NULL"), "detail_generation_result_link must contain result_url");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS detail_composition_quality_check"), "schema must contain detail_composition_quality_check");
        assertTrue(schema.contains("detail_composition_id BIGINT NOT NULL"), "detail_composition_quality_check must contain detail_composition_id");
        assertTrue(schema.contains("issue_count INT"), "detail_composition_quality_check must contain issue_count");
        assertTrue(schema.contains("issues_json JSON"), "detail_composition_quality_check must contain issues_json");
        assertTrue(schema.contains("screenshot_path VARCHAR"), "detail_composition_quality_check must contain screenshot_path");
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS publish_check"), "schema must contain publish_check");
        assertTrue(schema.contains("check_type VARCHAR"), "publish_check must contain check_type");
        assertTrue(schema.contains("target_type VARCHAR"), "publish_check must contain target_type");
        assertTrue(schema.contains("target_id VARCHAR"), "publish_check must contain target_id");
        assertTrue(schema.contains("target_field VARCHAR"), "publish_check must contain target_field");
        assertTrue(schema.contains("severity VARCHAR"), "publish_check must contain severity");
        assertTrue(schema.contains("overridden TINYINT"), "publish_check must contain overridden");
        assertTrue(schema.contains("override_reason TEXT"), "publish_check must contain override_reason");
        assertTrue(schema.contains("override_operator VARCHAR"), "publish_check must contain override_operator");
    }
}
