-- E-commerce Detail AI Workbench database schema

CREATE TABLE IF NOT EXISTS product_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id BIGINT,
    brand_name VARCHAR(100),
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100),
    category VARCHAR(100),
    price DECIMAL(10,2),
    description TEXT,
    images JSON,
    videos JSON,
    documents JSON,
    status TINYINT DEFAULT 1,
    original_file_path VARCHAR(500),
    file_type VARCHAR(50),
    file_size BIGINT,
    uploader VARCHAR(50),
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    parse_status TINYINT DEFAULT 0,
    parse_error TEXT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_name (product_name),
    INDEX idx_product_sku (product_sku),
    INDEX idx_category (category),
    INDEX idx_brand_id (brand_id)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS product_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT,
    product_name VARCHAR(255),
    brand_id BIGINT,
    brand_name VARCHAR(100),
    title VARCHAR(255),
    subtitle VARCHAR(500),
    selling_points TEXT,
    seo_keywords TEXT,
    image_template_id BIGINT,
    sku VARCHAR(100),
    category VARCHAR(100),
    price DECIMAL(10,2),
    description TEXT,
    ai_generated_content TEXT,
    images JSON,
    videos JSON,
    documents JSON,
    risk_level VARCHAR(20),
    risk_description TEXT,
    audit_status TINYINT DEFAULT 0,
    auditor VARCHAR(50),
    audit_time DATETIME,
    audit_comment TEXT,
    version INT DEFAULT 1,
    is_current_version TINYINT(1) DEFAULT 1,
    module_order JSON,
    deleted TINYINT DEFAULT 0,
    creator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_material_id (material_id),
    INDEX idx_product_name (product_name),
    INDEX idx_sku (sku),
    INDEX idx_brand_id (brand_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS research_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    owner VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    input_json JSON,
    result_json JSON,
    error_message TEXT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_research_task_name (task_name),
    INDEX idx_research_task_category (category),
    INDEX idx_research_task_owner (owner),
    INDEX idx_research_task_status (status)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS asset_ocr_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT,
    asset_name VARCHAR(255),
    asset_type VARCHAR(50),
    language VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INT DEFAULT 0,
    ocr_text TEXT,
    confidence DECIMAL(5,2),
    error_message TEXT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_asset_ocr_material_id (material_id),
    INDEX idx_asset_ocr_status (status),
    INDEX idx_asset_ocr_language (language)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS detail_generation_result_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    generation_result_id BIGINT NOT NULL,
    result_url VARCHAR(500) NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_detail_generation_result_link_pair (product_detail_id, generation_result_id),
    UNIQUE KEY uk_detail_generation_result_link_url (product_detail_id, result_url),
    INDEX idx_detail_generation_result_link_detail_id (product_detail_id),
    INDEX idx_detail_generation_result_link_result_id (generation_result_id)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS detail_composition_quality_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    detail_composition_id BIGINT NOT NULL,
    tool_code VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issue_count INT DEFAULT 0,
    issues_json JSON,
    screenshot_path VARCHAR(500),
    error_message TEXT,
    check_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_detail_composition_quality_check_composition_id (detail_composition_id),
    INDEX idx_detail_composition_quality_check_status (status)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS skc_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_name VARCHAR(255) NOT NULL,
    category_code VARCHAR(100),
    color_count INT,
    spec_count INT,
    colors_json JSON,
    specs_json JSON,
    render_mode VARCHAR(50),
    variant_display_mode VARCHAR(50),
    generation_rules_json JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_skc_policy_category_code (category_code),
    INDEX idx_skc_policy_status (status)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS audit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    audit_type VARCHAR(50),
    audit_status TINYINT NOT NULL,
    risk_level TINYINT,
    risk_items TEXT,
    audit_comment TEXT,
    modification_suggestions TEXT,
    submitter VARCHAR(50),
    submit_time DATETIME,
    auditor VARCHAR(50),
    audit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    audit_duration INT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_detail_id (product_detail_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_auditor (auditor),
    INDEX idx_audit_time (audit_time)
) ENGINE=InnoDB ;


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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cost_config_provider (provider_type, provider_code),
    INDEX idx_cost_config_provider_type (provider_type)
) ENGINE=InnoDB ;

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
    notes TEXT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_cost_task (task_type, task_id),
    INDEX idx_task_cost_tool (tool_code),
    INDEX idx_task_cost_model (model_code),
    INDEX idx_task_cost_source (cost_source),
    INDEX idx_task_cost_visual_plan (visual_plan_id),
    INDEX idx_task_cost_batch (batch_id)
) ENGINE=InnoDB ;


CREATE TABLE IF NOT EXISTS publish_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    check_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(255),
    target_field VARCHAR(100),
    severity VARCHAR(20) NOT NULL DEFAULT 'HARD',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    details_json JSON,
    overridden TINYINT(1) DEFAULT 0,
    override_reason TEXT,
    override_operator VARCHAR(100),
    override_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_publish_check_detail_id (product_detail_id),
    INDEX idx_publish_check_type (check_type),
    INDEX idx_publish_check_status (status),
    INDEX idx_publish_check_severity (severity)
) ENGINE=InnoDB ;

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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user_username (username),
    INDEX idx_team_user_status (status)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS team_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_role_code (role_code)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS team_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL,
    permission_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_permission_code (permission_code)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS team_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user_role (user_id, role_id),
    INDEX idx_team_user_role_user (user_id),
    INDEX idx_team_user_role_role (role_id)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS team_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_role_permission (role_id, permission_id),
    INDEX idx_team_role_permission_role (role_id),
    INDEX idx_team_role_permission_perm (permission_id)
) ENGINE=InnoDB ;

CREATE TABLE IF NOT EXISTS operation_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(200) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100),
    target_id BIGINT,
    detail_json JSON,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operation_audit_operator (operator_id),
    INDEX idx_operation_audit_action (action),
    INDEX idx_operation_audit_target (target_type, target_id),
    INDEX idx_operation_audit_time (create_time)
) ENGINE=InnoDB ;
CREATE TABLE IF NOT EXISTS post_process_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_generation_result_id BIGINT,
    source_image_path VARCHAR(500) NOT NULL,
    output_image_path VARCHAR(500),
    tool_code VARCHAR(100) NOT NULL,
    operation VARCHAR(100) NOT NULL,
    params_json JSON,
    input_width INT,
    input_height INT,
    input_file_size BIGINT,
    input_mime_type VARCHAR(100),
    output_width INT,
    output_height INT,
    output_file_size BIGINT,
    output_mime_type VARCHAR(100),
    source_chain_json JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INT DEFAULT 0,
    error_message TEXT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_process_source_result_id (source_generation_result_id),
    INDEX idx_post_process_tool_code (tool_code),
    INDEX idx_post_process_status (status),
    INDEX idx_post_process_operation (operation)
) ENGINE=InnoDB ;
