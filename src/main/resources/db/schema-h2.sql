-- E-commerce Detail AI Workbench database schema

CREATE TABLE IF NOT EXISTS product_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id BIGINT,
    brand_name VARCHAR(100),
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100),
    category VARCHAR(100),
    price DECIMAL(10,2),
    description CLOB,
    images CLOB,
    videos CLOB,
    documents CLOB,
    status TINYINT DEFAULT 1
    original_file_path VARCHAR(500),
    file_type VARCHAR(50),
    file_size BIGINT,
    uploader VARCHAR(50)
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    parse_status TINYINT DEFAULT 0
    parse_error CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_name (product_name),
    INDEX idx_product_sku (product_sku),
    INDEX idx_category (category),
    INDEX idx_brand_id (brand_id)
);

CREATE TABLE IF NOT EXISTS brand_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id BIGINT,
    brand_name VARCHAR(100),
    template_name VARCHAR(100) NOT NULL,
    template_type VARCHAR(50),
    template_content CLOB,
    style_tags VARCHAR(500),
    style_description CLOB,
    applicable_categories VARCHAR(500),
    enabled TINYINT(1) DEFAULT 1,
    usage_count INT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    creator VARCHAR(50)
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(50)
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_brand_id (brand_id),
    INDEX idx_brand_name (brand_name),
    INDEX idx_template_name (template_name)
);

CREATE TABLE IF NOT EXISTS product_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT,
    product_name VARCHAR(255),
    brand_id BIGINT,
    brand_name VARCHAR(100),
    title VARCHAR(255),
    subtitle VARCHAR(500)
    selling_points CLOB,
    seo_keywords CLOB
    image_template_id BIGINT,
    sku VARCHAR(100),
    category VARCHAR(100),
    price DECIMAL(10,2),
    description CLOB,
    ai_generated_content CLOB,
    images CLOB,
    videos CLOB,
    documents CLOB,
    risk_level VARCHAR(20),
    risk_description CLOB,
    audit_status TINYINT DEFAULT 0
    auditor VARCHAR(50)
    audit_time DATETIME,
    audit_comment CLOB,
    version INT DEFAULT 1
    is_current_version TINYINT(1) DEFAULT 1,
    module_order CLOB,
    deleted TINYINT DEFAULT 0,
    creator VARCHAR(50)
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(50)
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_material_id (material_id),
    INDEX idx_product_name (product_name),
    INDEX idx_sku (sku),
    INDEX idx_brand_id (brand_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_risk_level (risk_level)
);

CREATE TABLE IF NOT EXISTS research_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    owner VARCHAR(100)
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    input_json CLOB,
    result_json CLOB,
    error_message CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_research_task_name (task_name),
    INDEX idx_research_task_category (category),
    INDEX idx_research_task_owner (owner),
    INDEX idx_research_task_status (status)
);

CREATE TABLE IF NOT EXISTS product_content_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    tool_code VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    version INT NOT NULL DEFAULT 1
    input_json CLOB,
    output_json CLOB,
    output_text CLOB,
    applied_fields_json CLOB,
    applied_time DATETIME,
    error_message CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_content_task_detail_id (product_detail_id),
    INDEX idx_product_content_task_status (status),
    INDEX idx_product_content_task_tool_code (tool_code)
);

CREATE TABLE IF NOT EXISTS asset_ocr_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT,
    asset_name VARCHAR(255),
    asset_type VARCHAR(50),
    language VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    progress INT DEFAULT 0,
    ocr_text CLOB,
    confidence DECIMAL(5,2)
    error_message CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_asset_ocr_material_id (material_id),
    INDEX idx_asset_ocr_status (status),
    INDEX idx_asset_ocr_language (language)
);

CREATE TABLE IF NOT EXISTS design_draft (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT,
    product_material_id BIGINT,
    draft_name VARCHAR(255) NOT NULL,
    scene_json CLOB,
    selected_assets_json CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_design_draft_detail_id (product_detail_id),
    INDEX idx_design_draft_material_id (product_material_id),
    INDEX idx_design_draft_status (status)
);

CREATE TABLE IF NOT EXISTS detail_composition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    tool_code VARCHAR(100) NOT NULL,
    input_json CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    progress INT DEFAULT 0,
    external_job_id VARCHAR(255),
    output_path VARCHAR(500),
    error_message CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_detail_composition_product_detail_id (product_detail_id),
    INDEX idx_detail_composition_status (status),
    INDEX idx_detail_composition_tool_code (tool_code),
    INDEX idx_detail_composition_external_job_id (external_job_id)
);

CREATE TABLE IF NOT EXISTS detail_composition_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    detail_composition_id BIGINT NOT NULL,
    output_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255)
    file_size BIGINT,
    image_width INT,
    image_height INT,
    mime_type VARCHAR(100),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_detail_composition_result_path (detail_composition_id, output_path),
    INDEX idx_detail_composition_result_composition_id (detail_composition_id)
);

CREATE TABLE IF NOT EXISTS detail_generation_result_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    generation_result_id BIGINT NOT NULL,
    result_url VARCHAR(500) NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_detail_generation_result_link_pair (product_detail_id, generation_result_id),
    UNIQUE KEY uk_detail_generation_result_link_url (product_detail_id, result_url),
    INDEX idx_detail_generation_result_link_detail_id (product_detail_id),
    INDEX idx_detail_generation_result_link_result_id (generation_result_id)
);

CREATE TABLE IF NOT EXISTS detail_composition_quality_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    detail_composition_id BIGINT NOT NULL,
    tool_code VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issue_count INT DEFAULT 0,
    issues_json CLOB,
    screenshot_path VARCHAR(500),
    error_message CLOB,
    check_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_detail_composition_quality_check_composition_id (detail_composition_id),
    INDEX idx_detail_composition_quality_check_status (status)
);

CREATE TABLE IF NOT EXISTS category_visual_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(100) NOT NULL,
    category_name VARCHAR(255),
    model_policy VARCHAR(50),
    model_consistency_level VARCHAR(50),
    allowed_shot_types_json CLOB,
    required_main_images_json CLOB,
    detail_screen_count_range_json CLOB,
    risk_rules_json CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category_visual_policy_code (category_code),
    INDEX idx_category_visual_policy_status (status)
);

CREATE TABLE IF NOT EXISTS model_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    front_image VARCHAR(500),
    side_image VARCHAR(500),
    back_image VARCHAR(500),
    height DECIMAL(10,2),
    weight DECIMAL(10,2),
    bust DECIMAL(10,2),
    waist DECIMAL(10,2),
    hip DECIMAL(10,2),
    style_tags_json CLOB,
    category_scopes_json CLOB,
    authorization_status VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_model_profile_status (status),
    INDEX idx_model_profile_authorization_status (authorization_status)
);

CREATE TABLE IF NOT EXISTS skc_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_name VARCHAR(255) NOT NULL,
    category_code VARCHAR(100),
    color_count INT,
    spec_count INT,
    colors_json CLOB,
    specs_json CLOB,
    render_mode VARCHAR(50),
    variant_display_mode VARCHAR(50),
    generation_rules_json CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_skc_policy_category_code (category_code),
    INDEX idx_skc_policy_status (status)
);

CREATE TABLE IF NOT EXISTS prompt_workbench_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_type VARCHAR(50) NOT NULL,
    task_name VARCHAR(255),
    tool_code VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version INT NOT NULL DEFAULT 1,
    input_json CLOB,
    output_json CLOB,
    output_text CLOB,
    error_message CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prompt_workbench_entry_type (entry_type),
    INDEX idx_prompt_workbench_tool_code (tool_code),
    INDEX idx_prompt_workbench_status (status)
);

CREATE TABLE IF NOT EXISTS visual_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT,
    plan_name VARCHAR(255) NOT NULL,
    category_code VARCHAR(100),
    category_visual_policy_id BIGINT,
    model_profile_id BIGINT,
    skc_policy_id BIGINT,
    prompt_workbench_entry_ids_json CLOB,
    input_json CLOB,
    plan_json CLOB,
    confirmed_snapshot_json CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    confirmed_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_visual_plan_product_detail_id (product_detail_id),
    INDEX idx_visual_plan_category_code (category_code),
    INDEX idx_visual_plan_status (status)
);

CREATE TABLE IF NOT EXISTS image_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    tool_code VARCHAR(100) NOT NULL,
    input_json CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    progress INT DEFAULT 0,
    external_job_id VARCHAR(255),
    error_message CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_image_job_tool_code (tool_code),
    INDEX idx_image_job_status (status),
    visual_plan_id BIGINT,
    slot VARCHAR(100),
    ratio VARCHAR(20),
    prompt_version INT,
    model_profile_id BIGINT,
    source_snapshot_json CLOB,
    INDEX idx_image_job_external_job_id (external_job_id),
    INDEX idx_image_job_visual_plan_id (visual_plan_id)
);

CREATE TABLE IF NOT EXISTS generation_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_job_id BIGINT NOT NULL,
    result_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    prompt CLOB
    params_json CLOB,
    compliance_status VARCHAR(50)
    selected TINYINT(1) DEFAULT 0
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_generation_result_job_id (image_job_id),
    INDEX idx_generation_result_selected (selected),
    INDEX idx_generation_result_compliance_status (compliance_status)
);

CREATE TABLE IF NOT EXISTS audit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    audit_type VARCHAR(50),
    audit_status TINYINT NOT NULL
    risk_level TINYINT,
    risk_items CLOB
    audit_comment CLOB,
    modification_suggestions CLOB,
    submitter VARCHAR(50)
    submit_time DATETIME,
    auditor VARCHAR(50)
    audit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    audit_duration INT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_detail_id (product_detail_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_auditor (auditor),
    INDEX idx_audit_time (audit_time)
);

CREATE TABLE IF NOT EXISTS export_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    export_format VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    file_name VARCHAR(255)
    file_size BIGINT,
    export_status TINYINT DEFAULT 0
    error_message CLOB,
    exporter VARCHAR(50)
    export_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    detail_composition_id BIGINT,
    manifest_json CLOB,
    manifest_consistent TINYINT(1) DEFAULT 0,
    qa_check_id BIGINT,
    qa_status VARCHAR(20),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_detail_id (product_detail_id),
    INDEX idx_export_status (export_status),
    INDEX idx_exporter (exporter),
    INDEX idx_export_time (export_time),
    INDEX idx_export_record_composition_id (detail_composition_id),
    INDEX idx_export_record_manifest_consistent (manifest_consistent)
);


CREATE TABLE IF NOT EXISTS cost_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_type VARCHAR(20) NOT NULL,
    provider_code VARCHAR(100) NOT NULL,
    unit_price DECIMAL(12,6),
    unit_type VARCHAR(20) DEFAULT 'PER_CALL',
    currency VARCHAR(10) DEFAULT 'USD',
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cost_config_provider (provider_type, provider_code),
    INDEX idx_cost_config_provider_type (provider_type)
);

CREATE TABLE IF NOT EXISTS task_cost_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_type VARCHAR(50) NOT NULL,
    task_id BIGINT NOT NULL,
    tool_code VARCHAR(100),
    model_code VARCHAR(100),
    duration_ms BIGINT,
    invoke_count INT DEFAULT 1,
    cost_amount DECIMAL(12,6),
    cost_currency VARCHAR(10) DEFAULT 'USD',
    cost_source VARCHAR(20),
    cost_config_id BIGINT,
    external_receipt_id VARCHAR(255),
    visual_plan_id BIGINT,
    batch_id VARCHAR(100),
    notes CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_cost_task (task_type, task_id),
    INDEX idx_task_cost_tool (tool_code),
    INDEX idx_task_cost_model (model_code),
    INDEX idx_task_cost_source (cost_source),
    INDEX idx_task_cost_visual_plan (visual_plan_id),
    INDEX idx_task_cost_batch (batch_id)
);


CREATE TABLE IF NOT EXISTS publish_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    check_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(255),
    target_field VARCHAR(100),
    severity VARCHAR(20) NOT NULL DEFAULT 'HARD',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message CLOB
    details_json CLOB,
    overridden TINYINT(1) DEFAULT 0,
    override_reason CLOB,
    override_operator VARCHAR(100),
    override_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_publish_check_detail_id (product_detail_id),
    INDEX idx_publish_check_type (check_type),
    INDEX idx_publish_check_status (status),
    INDEX idx_publish_check_severity (severity)
);

-- ============================================================
-- P4.3 Team Collaboration & Permissions
-- ============================================================

CREATE TABLE IF NOT EXISTS team_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    email VARCHAR(200),
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user_username (username),
    INDEX idx_team_user_status (status)
);

CREATE TABLE IF NOT EXISTS team_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_role_code (role_code)
);

CREATE TABLE IF NOT EXISTS team_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL,
    permission_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_permission_code (permission_code)
);

CREATE TABLE IF NOT EXISTS team_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user_role (user_id, role_id),
    INDEX idx_team_user_role_user (user_id),
    INDEX idx_team_user_role_role (role_id)
);

CREATE TABLE IF NOT EXISTS team_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_role_permission (role_id, permission_id),
    INDEX idx_team_role_permission_role (role_id),
    INDEX idx_team_role_permission_perm (permission_id)
);

CREATE TABLE IF NOT EXISTS operation_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(200) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100),
    target_id BIGINT,
    detail_json CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operation_audit_operator (operator_id),
    INDEX idx_operation_audit_action (action),
    INDEX idx_operation_audit_target (target_type, target_id),
    INDEX idx_operation_audit_time (create_time)
);
CREATE TABLE IF NOT EXISTS post_process_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_generation_result_id BIGINT,
    source_image_path VARCHAR(500) NOT NULL,
    output_image_path VARCHAR(500),
    tool_code VARCHAR(100) NOT NULL,
    operation VARCHAR(100) NOT NULL,
    params_json CLOB,
    input_width INT,
    input_height INT,
    input_file_size BIGINT,
    input_mime_type VARCHAR(100),
    output_width INT,
    output_height INT,
    output_file_size BIGINT,
    output_mime_type VARCHAR(100),
    source_chain_json CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    progress INT DEFAULT 0,
    error_message CLOB,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_process_source_result_id (source_generation_result_id),
    INDEX idx_post_process_tool_code (tool_code),
    INDEX idx_post_process_status (status),
    INDEX idx_post_process_operation (operation)
);

CREATE TABLE IF NOT EXISTS prompt_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    scene_type VARCHAR(100),
    platform VARCHAR(50),
    style VARCHAR(100),
    positive_prompt CLOB NOT NULL,
    negative_prompt CLOB,
    style_tags_json CLOB,
    constraints_json CLOB,
    description CLOB,
    preview_image_url VARCHAR(500),
    usage_count INT DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 0.00,
    source VARCHAR(50) DEFAULT 'CUSTOM',
    source_ref VARCHAR(500),
    language VARCHAR(20) DEFAULT 'zh-CN',
    author VARCHAR(100),
    tags_json CLOB,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prompt_template_category (category),
    INDEX idx_prompt_template_platform (platform),
    INDEX idx_prompt_template_style (style),
    INDEX idx_prompt_template_source (source),
    INDEX idx_prompt_template_status (status)
);

CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    email VARCHAR(200),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_account_username (username),
    INDEX idx_user_account_status (status)
);