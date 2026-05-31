CREATE TABLE IF NOT EXISTS  brand_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    brand_id BIGINT ,
    brand_name VARCHAR(100) ,
    template_name VARCHAR(100) NOT NULL ,
    template_type VARCHAR(50) ,
    template_content TEXT ,
    style_tags VARCHAR(500) ,
    style_description TEXT ,
    applicable_categories VARCHAR(500) ,
    enabled TINYINT(1) DEFAULT 1 ,
    usage_count INT DEFAULT 0 ,
    deleted TINYINT DEFAULT 0 ,
    creator VARCHAR(50) create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    updater VARCHAR(50) update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_brand_id (brand_id),
    INDEX idx_brand_name (brand_name),
    INDEX idx_template_name (template_name)
) COMMENT='�?;



CREATE TABLE IF NOT EXISTS  product_content_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    product_detail_id BIGINT NOT NULL ,
    task_name VARCHAR(255) NOT NULL ,
    tool_code VARCHAR(100) NOT NULL ,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' version INT NOT NULL DEFAULT 1 input_json JSON ,
    output_json JSON ,
    output_text LONGTEXT ,
    applied_fields_json JSON ,
    applied_time DATETIME ,
    error_message TEXT ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_product_content_task_detail_id (product_detail_id),
    INDEX idx_product_content_task_status (status),
    INDEX idx_product_content_task_tool_code (tool_code)
) ;



CREATE TABLE IF NOT EXISTS  design_draft (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    product_detail_id BIGINT ,
    product_material_id BIGINT ,
    draft_name VARCHAR(255) NOT NULL ,
    scene_json LONGTEXT ,
    selected_assets_json JSON ,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_design_draft_detail_id (product_detail_id),
    INDEX idx_design_draft_material_id (product_material_id),
    INDEX idx_design_draft_status (status)
) COMMENT='�?;



CREATE TABLE IF NOT EXISTS  detail_composition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    product_detail_id BIGINT NOT NULL ,
    task_name VARCHAR(255) NOT NULL ,
    tool_code VARCHAR(100) NOT NULL ,
    input_json JSON ,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' progress INT DEFAULT 0 ,
    external_job_id VARCHAR(255) ,
    output_path VARCHAR(500) ,
    error_message TEXT ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_detail_composition_product_detail_id (product_detail_id),
    INDEX idx_detail_composition_status (status),
    INDEX idx_detail_composition_tool_code (tool_code),
    INDEX idx_detail_composition_external_job_id (external_job_id)
) COMMENT='�?;



CREATE TABLE IF NOT EXISTS  detail_composition_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    detail_composition_id BIGINT NOT NULL ,
    output_path VARCHAR(500) NOT NULL ,
    file_name VARCHAR(255) file_size BIGINT ,
    image_width INT ,
    image_height INT ,
    mime_type VARCHAR(100) ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    UNIQUE KEY uk_detail_composition_result_path (detail_composition_id, output_path),
    INDEX idx_detail_composition_result_composition_id (detail_composition_id)
) COMMENT='�?;



CREATE TABLE IF NOT EXISTS  category_visual_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    category_code VARCHAR(100) NOT NULL ,
    category_name VARCHAR(255) ,
    model_policy VARCHAR(50) ,
    model_consistency_level VARCHAR(50) ,
    allowed_shot_types_json JSON ,
    required_main_images_json JSON ,
    detail_screen_count_range_json JSON ,
    risk_rules_json JSON ,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' ,
    version INT NOT NULL DEFAULT 1 ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_category_visual_policy_code (category_code),
    INDEX idx_category_visual_policy_status (status)
) COMMENT='�?;



CREATE TABLE IF NOT EXISTS  model_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    display_name VARCHAR(255) NOT NULL ,
    front_image VARCHAR(500) ,
    side_image VARCHAR(500) ,
    back_image VARCHAR(500) ,
    height DECIMAL(10,2) ,
    weight DECIMAL(10,2) ,
    bust DECIMAL(10,2) ,
    waist DECIMAL(10,2) ,
    hip DECIMAL(10,2) ,
    style_tags_json JSON ,
    category_scopes_json JSON ,
    authorization_status VARCHAR(50) ,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' ,
    version INT NOT NULL DEFAULT 1 ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_model_profile_status (status),
    INDEX idx_model_profile_authorization_status (authorization_status)
) ;



CREATE TABLE IF NOT EXISTS  prompt_workbench_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    entry_type VARCHAR(50) NOT NULL ,
    task_name VARCHAR(255) ,
    tool_code VARCHAR(100) NOT NULL ,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' ,
    version INT NOT NULL DEFAULT 1 ,
    input_json JSON ,
    output_json JSON ,
    output_text LONGTEXT ,
    error_message TEXT ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_prompt_workbench_entry_type (entry_type),
    INDEX idx_prompt_workbench_tool_code (tool_code),
    INDEX idx_prompt_workbench_status (status)
) COMMENT='Prompt Workbench �?;



CREATE TABLE IF NOT EXISTS  visual_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    product_detail_id BIGINT ,
    plan_name VARCHAR(255) NOT NULL ,
    category_code VARCHAR(100) ,
    category_visual_policy_id BIGINT ,
    model_profile_id BIGINT ,
    skc_policy_id BIGINT ,
    prompt_workbench_entry_ids_json JSON ,
    input_json JSON ,
    plan_json JSON ,
    confirmed_snapshot_json JSON ,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' ,
    version INT NOT NULL DEFAULT 1 ,
    confirmed_time DATETIME ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_visual_plan_product_detail_id (product_detail_id),
    INDEX idx_visual_plan_category_code (category_code),
    INDEX idx_visual_plan_status (status)
) COMMENT='�?;



CREATE TABLE IF NOT EXISTS  image_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    task_name VARCHAR(255) NOT NULL ,
    tool_code VARCHAR(100) NOT NULL ,
    input_json JSON ,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' progress INT DEFAULT 0 ,
    external_job_id VARCHAR(255) ,
    error_message TEXT ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_image_job_tool_code (tool_code),
    INDEX idx_image_job_status (status),
    visual_plan_id BIGINT ,
    slot VARCHAR(100) ,
    ratio VARCHAR(20) ,
    prompt_version INT ,
    model_profile_id BIGINT ,
    source_snapshot_json JSON ,
    INDEX idx_image_job_external_job_id (external_job_id),
    INDEX idx_image_job_visual_plan_id (visual_plan_id)
) COMMENT='�?;



CREATE TABLE IF NOT EXISTS  generation_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    image_job_id BIGINT NOT NULL ,
    result_url VARCHAR(500) ,
    thumbnail_url VARCHAR(500) ,
    prompt TEXT params_json JSON ,
    compliance_status VARCHAR(50) selected TINYINT(1) DEFAULT 0 deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
    INDEX idx_generation_result_job_id (image_job_id),
    INDEX idx_generation_result_selected (selected),
    INDEX idx_generation_result_compliance_status (compliance_status)
) COMMENT='�?;



CREATE TABLE IF NOT EXISTS  export_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    product_detail_id BIGINT NOT NULL ,
    export_format VARCHAR(20) NOT NULL ,
    file_path VARCHAR(500) ,
    file_name VARCHAR(255) file_size BIGINT ,
    export_status TINYINT DEFAULT 0 error_message TEXT ,
    exporter VARCHAR(50) export_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    detail_composition_id BIGINT ,
    manifest_json JSON ,
    manifest_consistent TINYINT(1) DEFAULT 0 ,
    qa_check_id BIGINT ,
    qa_status VARCHAR(20) ,
    deleted TINYINT DEFAULT 0 ,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP ,
    INDEX idx_product_detail_id (product_detail_id),
    INDEX idx_export_status (export_status),
    INDEX idx_exporter (exporter),
    INDEX idx_export_time (export_time),
    INDEX idx_export_record_composition_id (detail_composition_id),
    INDEX idx_export_record_manifest_consistent (manifest_consistent)
) ;

