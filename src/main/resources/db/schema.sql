-- E-commerce Detail AI Workbench database schema

CREATE TABLE IF NOT EXISTS product_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    brand_id BIGINT COMMENT '品牌ID',
    brand_name VARCHAR(100) COMMENT '品牌名称',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    product_sku VARCHAR(100) COMMENT '商品SKU',
    category VARCHAR(100) COMMENT '商品类目',
    price DECIMAL(10,2) COMMENT '商品价格',
    description TEXT COMMENT '商品描述',
    images JSON COMMENT '商品图片列表',
    videos JSON COMMENT '商品视频列表',
    documents JSON COMMENT '商品文档列表',
    status TINYINT DEFAULT 1 COMMENT '资料状�?,
    original_file_path VARCHAR(500) COMMENT '原始文件路径',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小',
    uploader VARCHAR(50) COMMENT '上传�?,
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    parse_status TINYINT DEFAULT 0 COMMENT '解析状�?,
    parse_error TEXT COMMENT '解析错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_product_name (product_name),
    INDEX idx_product_sku (product_sku),
    INDEX idx_category (category),
    INDEX idx_brand_id (brand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品资料�?;

CREATE TABLE IF NOT EXISTS brand_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    brand_id BIGINT COMMENT '品牌ID',
    brand_name VARCHAR(100) COMMENT '品牌名称',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) COMMENT '模板类型',
    template_content TEXT COMMENT '模板内容',
    style_tags VARCHAR(500) COMMENT '风格标签',
    style_description TEXT COMMENT '风格描述',
    applicable_categories VARCHAR(500) COMMENT '适用类目',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    creator VARCHAR(50) COMMENT '创建�?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(50) COMMENT '更新�?,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_brand_id (brand_id),
    INDEX idx_brand_name (brand_name),
    INDEX idx_template_name (template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌模板�?;

CREATE TABLE IF NOT EXISTS product_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    material_id BIGINT COMMENT '商品资料ID',
    product_name VARCHAR(255) COMMENT '商品名称',
    brand_id BIGINT COMMENT '品牌ID',
    brand_name VARCHAR(100) COMMENT '品牌名称',
    title VARCHAR(255) COMMENT '标题',
    subtitle VARCHAR(500) COMMENT '副标�?,
    selling_points TEXT COMMENT '核心卖点列表',
    seo_keywords TEXT COMMENT 'SEO关键词列�?,
    image_template_id BIGINT COMMENT '图片模板ID',
    sku VARCHAR(100) COMMENT '商品SKU',
    category VARCHAR(100) COMMENT '分类',
    price DECIMAL(10,2) COMMENT '价格',
    description TEXT COMMENT '描述',
    ai_generated_content TEXT COMMENT 'AI生成内容',
    images JSON COMMENT '图片列表',
    videos JSON COMMENT '视频列表',
    documents JSON COMMENT '文档列表',
    risk_level VARCHAR(20) COMMENT '风险等级',
    risk_description TEXT COMMENT '风险描述',
    audit_status TINYINT DEFAULT 0 COMMENT '审核状�?,
    auditor VARCHAR(50) COMMENT '审核�?,
    audit_time DATETIME COMMENT '审核时间',
    audit_comment TEXT COMMENT '审核意见',
    version INT DEFAULT 1 COMMENT '版本�?,
    is_current_version TINYINT(1) DEFAULT 1 COMMENT '是否当前版本',
    module_order JSON COMMENT 'module order',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    creator VARCHAR(50) COMMENT '创建�?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(50) COMMENT '更新�?,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_material_id (material_id),
    INDEX idx_product_name (product_name),
    INDEX idx_sku (sku),
    INDEX idx_brand_id (brand_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情页表';

CREATE TABLE IF NOT EXISTS research_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    category VARCHAR(100) COMMENT '类目',
    owner VARCHAR(100) COMMENT '负责�?,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状�?,
    input_json JSON COMMENT '输入JSON',
    result_json JSON COMMENT '结果JSON',
    error_message TEXT COMMENT '错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_research_task_name (task_name),
    INDEX idx_research_task_category (category),
    INDEX idx_research_task_owner (owner),
    INDEX idx_research_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场调研任务�?;

CREATE TABLE IF NOT EXISTS product_content_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_detail_id BIGINT NOT NULL COMMENT '商品详情ID',
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    tool_code VARCHAR(100) NOT NULL COMMENT '工具编码',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状�?,
    version INT NOT NULL DEFAULT 1 COMMENT '版本�?,
    input_json JSON COMMENT '输入快照JSON',
    output_json JSON COMMENT 'AI relay结果JSON',
    output_text LONGTEXT COMMENT 'AI relay原始输出',
    applied_fields_json JSON COMMENT '已应用字段JSON',
    applied_time DATETIME COMMENT '应用时间',
    error_message TEXT COMMENT '错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_product_content_task_detail_id (product_detail_id),
    INDEX idx_product_content_task_status (status),
    INDEX idx_product_content_task_tool_code (tool_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品内容生成任务';

CREATE TABLE IF NOT EXISTS asset_ocr_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    material_id BIGINT COMMENT '资料ID',
    asset_name VARCHAR(255) COMMENT '素材名称',
    asset_type VARCHAR(50) COMMENT '素材类型',
    language VARCHAR(50) COMMENT 'OCR语言',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状�?,
    progress INT DEFAULT 0 COMMENT '进度',
    ocr_text TEXT COMMENT 'OCR�ı�',
    confidence DECIMAL(5,2) COMMENT '置信�?,
    error_message TEXT COMMENT '错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_asset_ocr_material_id (material_id),
    INDEX idx_asset_ocr_status (status),
    INDEX idx_asset_ocr_language (language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材OCR任务�?;

CREATE TABLE IF NOT EXISTS design_draft (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_detail_id BIGINT COMMENT '商品详情页ID',
    product_material_id BIGINT COMMENT '商品资料ID',
    draft_name VARCHAR(255) NOT NULL COMMENT '草稿名称',
    scene_json LONGTEXT COMMENT 'Excalidraw场景JSON',
    selected_assets_json JSON COMMENT '选中素材JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '草稿状�?,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_design_draft_detail_id (product_detail_id),
    INDEX idx_design_draft_material_id (product_material_id),
    INDEX idx_design_draft_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计草稿�?;

CREATE TABLE IF NOT EXISTS detail_composition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_detail_id BIGINT NOT NULL COMMENT '商品详情页ID',
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    tool_code VARCHAR(100) NOT NULL COMMENT '工具编码',
    input_json JSON COMMENT '输入JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状�?,
    progress INT DEFAULT 0 COMMENT '进度',
    external_job_id VARCHAR(255) COMMENT '外部任务ID',
    output_path VARCHAR(500) COMMENT '输出路径',
    error_message TEXT COMMENT '错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_detail_composition_product_detail_id (product_detail_id),
    INDEX idx_detail_composition_status (status),
    INDEX idx_detail_composition_tool_code (tool_code),
    INDEX idx_detail_composition_external_job_id (external_job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情组合任务�?;

CREATE TABLE IF NOT EXISTS detail_composition_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    detail_composition_id BIGINT NOT NULL COMMENT '组合任务ID',
    output_path VARCHAR(500) NOT NULL COMMENT '输出路径',
    file_name VARCHAR(255) COMMENT '文件�?,
    file_size BIGINT COMMENT '文件大小',
    image_width INT COMMENT '图片宽度',
    image_height INT COMMENT '图片高度',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_detail_composition_result_path (detail_composition_id, output_path),
    INDEX idx_detail_composition_result_composition_id (detail_composition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情组合结果�?;

CREATE TABLE IF NOT EXISTS detail_generation_result_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_detail_id BIGINT NOT NULL COMMENT '商品详情ID',
    generation_result_id BIGINT NOT NULL COMMENT '生成结果ID',
    result_url VARCHAR(500) NOT NULL COMMENT '真实结果URL',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_detail_generation_result_link_pair (product_detail_id, generation_result_id),
    UNIQUE KEY uk_detail_generation_result_link_url (product_detail_id, result_url),
    INDEX idx_detail_generation_result_link_detail_id (product_detail_id),
    INDEX idx_detail_generation_result_link_result_id (generation_result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='详情页引用生成结果关联表';

CREATE TABLE IF NOT EXISTS detail_composition_quality_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    detail_composition_id BIGINT NOT NULL COMMENT '详情组合ID',
    tool_code VARCHAR(100) NOT NULL COMMENT '工具编码',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '验收状�?',
    issue_count INT DEFAULT 0 COMMENT '问题数量',
    issues_json JSON COMMENT '问题列表JSON',
    screenshot_path VARCHAR(500) COMMENT '截图路径',
    error_message TEXT COMMENT '错误信息',
    check_time DATETIME COMMENT '验收时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_detail_composition_quality_check_composition_id (detail_composition_id),
    INDEX idx_detail_composition_quality_check_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='详情长图视觉验收记录�?;

CREATE TABLE IF NOT EXISTS category_visual_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_code VARCHAR(100) NOT NULL COMMENT '类目编码',
    category_name VARCHAR(255) COMMENT '类目名称',
    model_policy VARCHAR(50) COMMENT '模特策略',
    model_consistency_level VARCHAR(50) COMMENT '模特一致性等�?',
    allowed_shot_types_json JSON COMMENT '允许图型JSON',
    required_main_images_json JSON COMMENT '主图要求JSON',
    detail_screen_count_range_json JSON COMMENT '详情屏数范围JSON',
    risk_rules_json JSON COMMENT '风险规则JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '策略状�?',
    version INT NOT NULL DEFAULT 1 COMMENT '版本�?',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_visual_policy_code (category_code),
    INDEX idx_category_visual_policy_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='类目视觉策略�?;

CREATE TABLE IF NOT EXISTS model_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    display_name VARCHAR(255) NOT NULL COMMENT '模特名称',
    front_image VARCHAR(500) COMMENT '正面�?',
    side_image VARCHAR(500) COMMENT '侧面�?',
    back_image VARCHAR(500) COMMENT '背面�?',
    height DECIMAL(10,2) COMMENT '身高',
    weight DECIMAL(10,2) COMMENT '体重',
    bust DECIMAL(10,2) COMMENT '胸围',
    waist DECIMAL(10,2) COMMENT '腰围',
    hip DECIMAL(10,2) COMMENT '臀�?',
    style_tags_json JSON COMMENT '风格标签JSON',
    category_scopes_json JSON COMMENT '可用类目JSON',
    authorization_status VARCHAR(50) COMMENT '授权状�?',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '模特状�?',
    version INT NOT NULL DEFAULT 1 COMMENT '版本�?',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_model_profile_status (status),
    INDEX idx_model_profile_authorization_status (authorization_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模特库表';

CREATE TABLE IF NOT EXISTS skc_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    policy_name VARCHAR(255) NOT NULL COMMENT '策略名称',
    category_code VARCHAR(100) COMMENT '类目编码',
    color_count INT COMMENT '颜色数量',
    spec_count INT COMMENT '规格数量',
    colors_json JSON COMMENT '颜色JSON',
    specs_json JSON COMMENT '规格JSON',
    render_mode VARCHAR(50) COMMENT '出图模式',
    variant_display_mode VARCHAR(50) COMMENT 'SKC展示模式',
    generation_rules_json JSON COMMENT '出图规则JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '策略状�?',
    version INT NOT NULL DEFAULT 1 COMMENT '版本�?',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_skc_policy_category_code (category_code),
    INDEX idx_skc_policy_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKC视觉策略�?;

CREATE TABLE IF NOT EXISTS prompt_workbench_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    entry_type VARCHAR(50) NOT NULL COMMENT 'prompt 工作台类�?',
    task_name VARCHAR(255) COMMENT '任务名称',
    tool_code VARCHAR(100) NOT NULL COMMENT '工具编码',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状�?',
    version INT NOT NULL DEFAULT 1 COMMENT '版本�?',
    input_json JSON COMMENT '输入JSON',
    output_json JSON COMMENT '输出JSON',
    output_text LONGTEXT COMMENT '输出文本',
    error_message TEXT COMMENT '错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_prompt_workbench_entry_type (entry_type),
    INDEX idx_prompt_workbench_tool_code (tool_code),
    INDEX idx_prompt_workbench_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt Workbench 记录�?;

CREATE TABLE IF NOT EXISTS visual_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_detail_id BIGINT COMMENT '商品详情ID',
    plan_name VARCHAR(255) NOT NULL COMMENT '视觉规划名称',
    category_code VARCHAR(100) COMMENT '类目编码',
    category_visual_policy_id BIGINT COMMENT '类目策略ID',
    model_profile_id BIGINT COMMENT '模特ID',
    skc_policy_id BIGINT COMMENT 'SKC 策略ID',
    prompt_workbench_entry_ids_json JSON COMMENT 'prompt 记录ID JSON',
    input_json JSON COMMENT '输入JSON',
    plan_json JSON COMMENT '计划JSON',
    confirmed_snapshot_json JSON COMMENT '确认快照JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '规划状�?',
    version INT NOT NULL DEFAULT 1 COMMENT '版本�?',
    confirmed_time DATETIME COMMENT '确认时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_visual_plan_product_detail_id (product_detail_id),
    INDEX idx_visual_plan_category_code (category_code),
    INDEX idx_visual_plan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视觉规划�?;

CREATE TABLE IF NOT EXISTS image_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    tool_code VARCHAR(100) NOT NULL COMMENT '工具编码',
    input_json JSON COMMENT '输入JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状�?,
    progress INT DEFAULT 0 COMMENT '进度',
    external_job_id VARCHAR(255) COMMENT '外部任务ID',
    error_message TEXT COMMENT '错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_image_job_tool_code (tool_code),
    INDEX idx_image_job_status (status),
    visual_plan_id BIGINT COMMENT 'visual plan id',
    slot VARCHAR(100) COMMENT 'slot',
    ratio VARCHAR(20) COMMENT 'ratio',
    prompt_version INT COMMENT 'prompt version',
    model_profile_id BIGINT COMMENT 'model profile id',
    source_snapshot_json JSON COMMENT 'source snapshot',
    INDEX idx_image_job_external_job_id (external_job_id),
    INDEX idx_image_job_visual_plan_id (visual_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生图任务�?;

CREATE TABLE IF NOT EXISTS generation_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    image_job_id BIGINT NOT NULL COMMENT '任务ID',
    result_url VARCHAR(500) COMMENT '结果URL',
    thumbnail_url VARCHAR(500) COMMENT '缩略图MRL',
    prompt TEXT COMMENT '提示�?,
    params_json JSON COMMENT '参数JSON',
    compliance_status VARCHAR(50) COMMENT '合规状�?,
    selected TINYINT(1) DEFAULT 0 COMMENT '是否精�?,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_generation_result_job_id (image_job_id),
    INDEX idx_generation_result_selected (selected),
    INDEX idx_generation_result_compliance_status (compliance_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生成结果�?;

CREATE TABLE IF NOT EXISTS audit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_detail_id BIGINT NOT NULL COMMENT '商品详情页ID',
    audit_type VARCHAR(50) COMMENT '审核类型',
    audit_status TINYINT NOT NULL COMMENT '审核状�?,
    risk_level TINYINT COMMENT '风险等级',
    risk_items TEXT COMMENT '风险项列�?,
    audit_comment TEXT COMMENT '审核意见',
    modification_suggestions TEXT COMMENT '修改建议',
    submitter VARCHAR(50) COMMENT '提交�?,
    submit_time DATETIME COMMENT '提交时间',
    auditor VARCHAR(50) COMMENT '审核�?,
    audit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    audit_duration INT COMMENT '审核耗时',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_product_detail_id (product_detail_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_auditor (auditor),
    INDEX idx_audit_time (audit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录�?;

CREATE TABLE IF NOT EXISTS export_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_detail_id BIGINT NOT NULL COMMENT '商品详情页ID',
    export_format VARCHAR(20) NOT NULL COMMENT '导出格式',
    file_path VARCHAR(500) COMMENT '导出文件路径',
    file_name VARCHAR(255) COMMENT '文件�?,
    file_size BIGINT COMMENT '文件大小',
    export_status TINYINT DEFAULT 0 COMMENT '导出状�?,
    error_message TEXT COMMENT '错误信息',
    exporter VARCHAR(50) COMMENT '导出�?,
    export_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '导出时间',
    detail_composition_id BIGINT COMMENT 'P3.12: �����������ID',
    manifest_json JSON COMMENT 'P3.12: ����ʱ�Ľ����嵥����',
    manifest_consistent TINYINT(1) DEFAULT 0 COMMENT 'P3.12: �嵥һ����У����',
    qa_check_id BIGINT COMMENT 'P3.12: ������QA���ID',
    qa_status VARCHAR(20) COMMENT 'P3.12: ����ʱ��QA״̬',
    deleted TINYINT DEFAULT 0 COMMENT '�߼�ɾ����ʶ',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '����ʱ��',
    INDEX idx_product_detail_id (product_detail_id),
    INDEX idx_export_status (export_status),
    INDEX idx_exporter (exporter),
    INDEX idx_export_time (export_time),
    INDEX idx_export_record_composition_id (detail_composition_id),
    INDEX idx_export_record_manifest_consistent (manifest_consistent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='������¼��';


CREATE TABLE IF NOT EXISTS cost_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_type VARCHAR(20) NOT NULL COMMENT 'MODEL or TOOL',
    provider_code VARCHAR(100) NOT NULL,
    unit_price DECIMAL(12,6),
    unit_type VARCHAR(20) DEFAULT 'PER_CALL' COMMENT 'PER_CALL, PER_TOKEN, PER_IMAGE, PER_SECOND',
    currency VARCHAR(10) DEFAULT 'USD',
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cost_config_provider (provider_type, provider_code),
    INDEX idx_cost_config_provider_type (provider_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS task_cost_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_type VARCHAR(50) NOT NULL COMMENT 'PROMPT_WORKBENCH, IMAGE_JOB, DETAIL_COMPOSITION',
    task_id BIGINT NOT NULL,
    tool_code VARCHAR(100),
    model_code VARCHAR(100),
    duration_ms BIGINT,
    invoke_count INT DEFAULT 1,
    cost_amount DECIMAL(12,6),
    cost_currency VARCHAR(10) DEFAULT 'USD',
    cost_source VARCHAR(20) COMMENT 'CONFIG, RECEIPT, MANUAL',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS publish_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '����ID',
    product_detail_id BIGINT NOT NULL COMMENT '��Ʒ����ID',
    check_type VARCHAR(50) NOT NULL COMMENT '�������: COPY_RISK, IMAGE_QA, ASSET_AUTH, MANIFEST, EXPORT_FILE, AUDIT_STATUS',
    target_type VARCHAR(50) COMMENT 'Ŀ������: TASK, ASSET, FIELD, COMPOSITION, EXPORT',
    target_id VARCHAR(255) COMMENT 'Ŀ��ID',
    target_field VARCHAR(100) COMMENT 'Ŀ���ֶ���',
    severity VARCHAR(20) NOT NULL DEFAULT 'HARD' COMMENT '������: HARD(��������) / SOFT(���澯)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '״̬: PASS, FAIL, WARN, PENDING',
    message TEXT COMMENT '�ɶ������Ϣ',
    details_json JSON COMMENT '�ṹ������',
    overridden TINYINT(1) DEFAULT 0 COMMENT '�Ƿ��ѱ��˹�����',
    override_reason TEXT COMMENT '����ԭ��',
    override_operator VARCHAR(100) COMMENT '���ǲ�����',
    override_time DATETIME COMMENT '����ʱ��',
    deleted TINYINT DEFAULT 0 COMMENT '�߼�ɾ����ʶ',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '����ʱ��',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '����ʱ��',
    INDEX idx_publish_check_detail_id (product_detail_id),
    INDEX idx_publish_check_type (check_type),
    INDEX idx_publish_check_status (status),
    INDEX idx_publish_check_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='����ǰ�ʼ����¼';

-- ============================================================
-- P4.3 Team Collaboration & Permissions
-- ============================================================

CREATE TABLE IF NOT EXISTS team_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    email VARCHAR(200),
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, DISABLED',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user_username (username),
    INDEX idx_team_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='�Ŷ��û���';

CREATE TABLE IF NOT EXISTS team_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='��ɫ��';

CREATE TABLE IF NOT EXISTS team_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL,
    permission_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Ȩ�ޱ�';

CREATE TABLE IF NOT EXISTS team_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user_role (user_id, role_id),
    INDEX idx_team_user_role_user (user_id),
    INDEX idx_team_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='�û���ɫ������';

CREATE TABLE IF NOT EXISTS team_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_role_permission (role_id, permission_id),
    INDEX idx_team_role_permission_role (role_id),
    INDEX idx_team_role_permission_perm (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='��ɫȨ�޹�����';

CREATE TABLE IF NOT EXISTS operation_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL COMMENT '������ID',
    operator_name VARCHAR(200) NOT NULL COMMENT '��������ʾ��',
    action VARCHAR(100) NOT NULL COMMENT '��������: CONFIRM_VISUAL_PLAN, TRIGGER_IMAGE_JOB, APPLY_RESULT, EXPORT, AUDIT, OVERRIDE ��',
    target_type VARCHAR(100) COMMENT 'Ŀ����Դ����',
    target_id BIGINT COMMENT 'Ŀ����ԴID',
    detail_json JSON COMMENT '��������JSON',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operation_audit_operator (operator_id),
    INDEX idx_operation_audit_action (action),
    INDEX idx_operation_audit_target (target_type, target_id),
    INDEX idx_operation_audit_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='���������־��';
CREATE TABLE IF NOT EXISTS post_process_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    source_generation_result_id BIGINT COMMENT '来源生成结果ID',
    source_image_path VARCHAR(500) NOT NULL COMMENT '输入图片路径',
    output_image_path VARCHAR(500) COMMENT '输出图片路径',
    tool_code VARCHAR(100) NOT NULL COMMENT '工具编码: grounded-sam, iopaint, real-esrgan, imagemagick',
    operation VARCHAR(100) NOT NULL COMMENT '操作名称: inpaint, cleanup-background, remove-object, segment, upscale, crop, resize',
    params_json JSON COMMENT '工具调用参数JSON',
    input_width INT COMMENT '输入图片宽度',
    input_height INT COMMENT '输入图片高度',
    input_file_size BIGINT COMMENT '输入文件大小',
    input_mime_type VARCHAR(100) COMMENT '输入MIME类型',
    output_width INT COMMENT '输出图片宽度',
    output_height INT COMMENT '输出图片高度',
    output_file_size BIGINT COMMENT '输出文件大小',
    output_mime_type VARCHAR(100) COMMENT '输出MIME类型',
    source_chain_json JSON COMMENT '来源链JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
    progress INT DEFAULT 0 COMMENT '进度',
    error_message TEXT COMMENT '错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_post_process_source_result_id (source_generation_result_id),
    INDEX idx_post_process_tool_code (tool_code),
    INDEX idx_post_process_status (status),
    INDEX idx_post_process_operation (operation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后处理任务表';
