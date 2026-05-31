CREATE TABLE IF NOT EXISTS brand_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id BIGINT,
    brand_name VARCHAR(100),
    template_name VARCHAR(100) NOT NULL,
    template_type VARCHAR(50),
    template_content TEXT,
    style_tags VARCHAR(500),
    style_description TEXT,
    applicable_categories VARCHAR(500),
    enabled TINYINT(1) DEFAULT 1,
    usage_count INT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    creator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_brand_id (brand_id),
    INDEX idx_brand_name (brand_name),
    INDEX idx_template_name (template_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_content_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    tool_code VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version INT NOT NULL DEFAULT 1,
    input_json JSON,
    output_json JSON,
    output_text TEXT,
    title VARCHAR(255),
    subtitle VARCHAR(500),
    selling_points JSON,
    seo_keywords JSON,
    detail_modules JSON,
    faq JSON,
    applied_fields_json JSON,
    applied_time DATETIME,
    error_message TEXT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_content_task_detail_id (product_detail_id),
    INDEX idx_product_content_task_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS design_draft (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT,
    product_material_id BIGINT,
    draft_name VARCHAR(255) NOT NULL,
    scene_json JSON,
    selected_assets_json JSON,
    status VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_design_draft_detail_id (product_detail_id),
    INDEX idx_design_draft_material_id (product_material_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detail_composition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT,
    task_name VARCHAR(255),
    tool_code VARCHAR(100),
    input_data JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INT DEFAULT 0,
    error_message TEXT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_detail_composition_product_id (product_detail_id),
    INDEX idx_detail_composition_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detail_composition_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    detail_composition_id BIGINT NOT NULL,
    result_type VARCHAR(50),
    result_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    width INT,
    height INT,
    mime_type VARCHAR(100),
    metadata_json JSON,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_detail_composition_result_comp_id (detail_composition_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS category_visual_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(100) NOT NULL,
    category_name VARCHAR(200),
    policy_json JSON,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category_visual_policy_code (category_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS prompt_workbench_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    prompt_text TEXT,
    parameters_json JSON,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prompt_workbench_entry_type (entry_type)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS visual_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_name VARCHAR(255) NOT NULL,
    product_detail_id BIGINT,
    plan_data JSON,
    status VARCHAR(20) DEFAULT 'DRAFT',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_visual_plan_detail_id (product_detail_id),
    INDEX idx_visual_plan_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS image_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(255),
    tool_code VARCHAR(100),
    input_json JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INT DEFAULT 0,
    external_job_id VARCHAR(255),
    error_message TEXT,
    visual_plan_id BIGINT,
    slot VARCHAR(100),
    ratio VARCHAR(50),
    prompt_version VARCHAR(50),
    model_profile_id BIGINT,
    source_snapshot_json JSON,
    output_path VARCHAR(500),
    output_file_name VARCHAR(255),
    output_file_size BIGINT,
    output_width INT,
    output_height INT,
    output_mime_type VARCHAR(100),
    image_width INT,
    image_height INT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_image_job_status (status),
    INDEX idx_image_job_visual_plan_id (visual_plan_id),
    INDEX idx_image_job_tool_code (tool_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS generation_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT,
    image_job_id BIGINT,
    result_type VARCHAR(50),
    result_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    width INT,
    height INT,
    mime_type VARCHAR(100),
    metadata_json JSON,
    selected TINYINT(1) DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_generation_result_detail_id (product_detail_id),
    INDEX idx_generation_result_image_job_id (image_job_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS export_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_detail_id BIGINT NOT NULL,
    export_format VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    export_status TINYINT DEFAULT 0,
    error_message TEXT,
    exporter VARCHAR(50),
    export_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_export_record_detail_id (product_detail_id),
    INDEX idx_export_record_status (export_status)
) ENGINE=InnoDB;