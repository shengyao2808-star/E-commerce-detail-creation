-- E-commerce Detail AI Workbench database schema

CREATE TABLE IF NOT EXISTS product_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    brand_id BIGINT COMMENT '鍝佺墝ID',
    brand_name VARCHAR(100) COMMENT '鍝佺墝鍚嶇О',
    product_name VARCHAR(255) NOT NULL COMMENT '鍟嗗搧鍚嶇О',
    product_sku VARCHAR(100) COMMENT '鍟嗗搧SKU',
    category VARCHAR(100) COMMENT '鍟嗗搧绫荤洰',
    price DECIMAL(10,2) COMMENT '鍟嗗搧浠锋牸',
    description TEXT COMMENT '鍟嗗搧鎻忚堪',
    images JSON COMMENT '鍟嗗搧鍥剧墖鍒楄〃',
    videos JSON COMMENT '鍟嗗搧瑙嗛鍒楄〃',
    documents JSON COMMENT '鍟嗗搧鏂囨。鍒楄〃',
    status TINYINT DEFAULT 1 COMMENT '璧勬枡鐘讹拷?,
    original_file_path VARCHAR(500) COMMENT '鍘熷鏂囦欢璺緞',
    file_type VARCHAR(50) COMMENT '鏂囦欢绫诲瀷',
    file_size BIGINT COMMENT '鏂囦欢澶у皬',
    uploader VARCHAR(50) COMMENT '涓婁紶锟?,
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '涓婁紶鏃堕棿',
    parse_status TINYINT DEFAULT 0 COMMENT '瑙ｆ瀽鐘讹拷?,
    parse_error TEXT COMMENT '瑙ｆ瀽閿欒淇℃伅',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_product_name (product_name),
    INDEX idx_product_sku (product_sku),
    INDEX idx_category (category),
    INDEX idx_brand_id (brand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍟嗗搧璧勬枡锟?;

CREATE TABLE IF NOT EXISTS brand_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    brand_id BIGINT COMMENT '鍝佺墝ID',
    brand_name VARCHAR(100) COMMENT '鍝佺墝鍚嶇О',
    template_name VARCHAR(100) NOT NULL COMMENT '妯℃澘鍚嶇О',
    template_type VARCHAR(50) COMMENT '妯℃澘绫诲瀷',
    template_content TEXT COMMENT '妯℃澘鍐呭',
    style_tags VARCHAR(500) COMMENT '椋庢牸鏍囩',
    style_description TEXT COMMENT '椋庢牸鎻忚堪',
    applicable_categories VARCHAR(500) COMMENT '閫傜敤绫荤洰',
    enabled TINYINT(1) DEFAULT 1 COMMENT '鏄惁鍚敤',
    usage_count INT DEFAULT 0 COMMENT '浣跨敤娆℃暟',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    creator VARCHAR(50) COMMENT '鍒涘缓锟?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updater VARCHAR(50) COMMENT '鏇存柊锟?,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_brand_id (brand_id),
    INDEX idx_brand_name (brand_name),
    INDEX idx_template_name (template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍝佺墝妯℃澘锟?;

CREATE TABLE IF NOT EXISTS product_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    material_id BIGINT COMMENT '鍟嗗搧璧勬枡ID',
    product_name VARCHAR(255) COMMENT '鍟嗗搧鍚嶇О',
    brand_id BIGINT COMMENT '鍝佺墝ID',
    brand_name VARCHAR(100) COMMENT '鍝佺墝鍚嶇О',
    title VARCHAR(255) COMMENT '鏍囬',
    subtitle VARCHAR(500) COMMENT '鍓爣锟?,
    selling_points TEXT COMMENT '鏍稿績鍗栫偣鍒楄〃',
    seo_keywords TEXT COMMENT 'SEO鍏抽敭璇嶅垪锟?,
    image_template_id BIGINT COMMENT '鍥剧墖妯℃澘ID',
    sku VARCHAR(100) COMMENT '鍟嗗搧SKU',
    category VARCHAR(100) COMMENT '鍒嗙被',
    price DECIMAL(10,2) COMMENT '浠锋牸',
    description TEXT COMMENT '鎻忚堪',
    ai_generated_content TEXT COMMENT 'AI鐢熸垚鍐呭',
    images JSON COMMENT '鍥剧墖鍒楄〃',
    videos JSON COMMENT '瑙嗛鍒楄〃',
    documents JSON COMMENT '鏂囨。鍒楄〃',
    risk_level VARCHAR(20) COMMENT '椋庨櫓绛夌骇',
    risk_description TEXT COMMENT '椋庨櫓鎻忚堪',
    audit_status TINYINT DEFAULT 0 COMMENT '瀹℃牳鐘讹拷?,
    auditor VARCHAR(50) COMMENT '瀹℃牳锟?,
    audit_time DATETIME COMMENT '瀹℃牳鏃堕棿',
    audit_comment TEXT COMMENT '瀹℃牳鎰忚',
    version INT DEFAULT 1 COMMENT '鐗堟湰锟?,
    is_current_version TINYINT(1) DEFAULT 1 COMMENT '鏄惁褰撳墠鐗堟湰',
    module_order JSON COMMENT 'module order',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    creator VARCHAR(50) COMMENT '鍒涘缓锟?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updater VARCHAR(50) COMMENT '鏇存柊锟?,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_material_id (material_id),
    INDEX idx_product_name (product_name),
    INDEX idx_sku (sku),
    INDEX idx_brand_id (brand_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍟嗗搧璇︽儏椤佃〃';

CREATE TABLE IF NOT EXISTS research_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    task_name VARCHAR(255) NOT NULL COMMENT '浠诲姟鍚嶇О',
    category VARCHAR(100) COMMENT '绫荤洰',
    owner VARCHAR(100) COMMENT '璐熻矗锟?,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '浠诲姟鐘讹拷?,
    input_json JSON COMMENT '杈撳叆JSON',
    result_json JSON COMMENT '缁撴灉JSON',
    error_message TEXT COMMENT '閿欒淇℃伅',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_research_task_name (task_name),
    INDEX idx_research_task_category (category),
    INDEX idx_research_task_owner (owner),
    INDEX idx_research_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='甯傚満璋冪爺浠诲姟锟?;

CREATE TABLE IF NOT EXISTS product_content_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    product_detail_id BIGINT NOT NULL COMMENT '鍟嗗搧璇︽儏ID',
    task_name VARCHAR(255) NOT NULL COMMENT '浠诲姟鍚嶇О',
    tool_code VARCHAR(100) NOT NULL COMMENT '宸ュ叿缂栫爜',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '浠诲姟鐘讹拷?,
    version INT NOT NULL DEFAULT 1 COMMENT '鐗堟湰锟?,
    input_json JSON COMMENT '杈撳叆蹇収JSON',
    output_json JSON COMMENT 'AI relay缁撴灉JSON',
    output_text LONGTEXT COMMENT 'AI relay鍘熷杈撳嚭',
    applied_fields_json JSON COMMENT '宸插簲鐢ㄥ瓧娈礘SON',
    applied_time DATETIME COMMENT '搴旂敤鏃堕棿',
    error_message TEXT COMMENT '閿欒淇℃伅',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_product_content_task_detail_id (product_detail_id),
    INDEX idx_product_content_task_status (status),
    INDEX idx_product_content_task_tool_code (tool_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍟嗗搧鍐呭鐢熸垚浠诲姟';

CREATE TABLE IF NOT EXISTS asset_ocr_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    material_id BIGINT COMMENT '璧勬枡ID',
    asset_name VARCHAR(255) COMMENT '绱犳潗鍚嶇О',
    asset_type VARCHAR(50) COMMENT '绱犳潗绫诲瀷',
    language VARCHAR(50) COMMENT 'OCR璇█',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '浠诲姟鐘讹拷?,
    progress INT DEFAULT 0 COMMENT '杩涘害',
    ocr_text TEXT COMMENT 'OCR锟侥憋拷',
    confidence DECIMAL(5,2) COMMENT '缃俊锟?,
    error_message TEXT COMMENT '閿欒淇℃伅',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_asset_ocr_material_id (material_id),
    INDEX idx_asset_ocr_status (status),
    INDEX idx_asset_ocr_language (language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绱犳潗OCR浠诲姟锟?;

CREATE TABLE IF NOT EXISTS design_draft (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    product_detail_id BIGINT COMMENT '鍟嗗搧璇︽儏椤礗D',
    product_material_id BIGINT COMMENT '鍟嗗搧璧勬枡ID',
    draft_name VARCHAR(255) NOT NULL COMMENT '鑽夌鍚嶇О',
    scene_json LONGTEXT COMMENT 'Excalidraw鍦烘櫙JSON',
    selected_assets_json JSON COMMENT '閫変腑绱犳潗JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '鑽夌鐘讹拷?,
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_design_draft_detail_id (product_detail_id),
    INDEX idx_design_draft_material_id (product_material_id),
    INDEX idx_design_draft_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璁捐鑽夌锟?;

CREATE TABLE IF NOT EXISTS detail_composition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    product_detail_id BIGINT NOT NULL COMMENT '鍟嗗搧璇︽儏椤礗D',
    task_name VARCHAR(255) NOT NULL COMMENT '浠诲姟鍚嶇О',
    tool_code VARCHAR(100) NOT NULL COMMENT '宸ュ叿缂栫爜',
    input_json JSON COMMENT '杈撳叆JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '浠诲姟鐘讹拷?,
    progress INT DEFAULT 0 COMMENT '杩涘害',
    external_job_id VARCHAR(255) COMMENT '澶栭儴浠诲姟ID',
    output_path VARCHAR(500) COMMENT '杈撳嚭璺緞',
    error_message TEXT COMMENT '閿欒淇℃伅',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_detail_composition_product_detail_id (product_detail_id),
    INDEX idx_detail_composition_status (status),
    INDEX idx_detail_composition_tool_code (tool_code),
    INDEX idx_detail_composition_external_job_id (external_job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍟嗗搧璇︽儏缁勫悎浠诲姟锟?;

CREATE TABLE IF NOT EXISTS detail_composition_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    detail_composition_id BIGINT NOT NULL COMMENT '缁勫悎浠诲姟ID',
    output_path VARCHAR(500) NOT NULL COMMENT '杈撳嚭璺緞',
    file_name VARCHAR(255) COMMENT '鏂囦欢锟?,
    file_size BIGINT COMMENT '鏂囦欢澶у皬',
    image_width INT COMMENT '鍥剧墖瀹藉害',
    image_height INT COMMENT '鍥剧墖楂樺害',
    mime_type VARCHAR(100) COMMENT 'MIME绫诲瀷',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    UNIQUE KEY uk_detail_composition_result_path (detail_composition_id, output_path),
    INDEX idx_detail_composition_result_composition_id (detail_composition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍟嗗搧璇︽儏缁勫悎缁撴灉锟?;

CREATE TABLE IF NOT EXISTS detail_generation_result_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    product_detail_id BIGINT NOT NULL COMMENT '鍟嗗搧璇︽儏ID',
    generation_result_id BIGINT NOT NULL COMMENT '鐢熸垚缁撴灉ID',
    result_url VARCHAR(500) NOT NULL COMMENT '鐪熷疄缁撴灉URL',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    UNIQUE KEY uk_detail_generation_result_link_pair (product_detail_id, generation_result_id),
    UNIQUE KEY uk_detail_generation_result_link_url (product_detail_id, result_url),
    INDEX idx_detail_generation_result_link_detail_id (product_detail_id),
    INDEX idx_detail_generation_result_link_result_id (generation_result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璇︽儏椤靛紩鐢ㄧ敓鎴愮粨鏋滃叧鑱旇〃';

CREATE TABLE IF NOT EXISTS detail_composition_quality_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    detail_composition_id BIGINT NOT NULL COMMENT '璇︽儏缁勫悎ID',
    tool_code VARCHAR(100) NOT NULL COMMENT '宸ュ叿缂栫爜',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '楠屾敹鐘讹拷?',
    issue_count INT DEFAULT 0 COMMENT '闂鏁伴噺',
    issues_json JSON COMMENT '闂鍒楄〃JSON',
    screenshot_path VARCHAR(500) COMMENT '鎴浘璺緞',
    error_message TEXT COMMENT '閿欒淇℃伅',
    check_time DATETIME COMMENT '楠屾敹鏃堕棿',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_detail_composition_quality_check_composition_id (detail_composition_id),
    INDEX idx_detail_composition_quality_check_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璇︽儏闀垮浘瑙嗚楠屾敹璁板綍锟?;

CREATE TABLE IF NOT EXISTS category_visual_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    category_code VARCHAR(100) NOT NULL COMMENT '绫荤洰缂栫爜',
    category_name VARCHAR(255) COMMENT '绫荤洰鍚嶇О',
    model_policy VARCHAR(50) COMMENT '妯＄壒绛栫暐',
    model_consistency_level VARCHAR(50) COMMENT '妯＄壒涓€鑷存€х瓑锟?',
    allowed_shot_types_json JSON COMMENT '鍏佽鍥惧瀷JSON',
    required_main_images_json JSON COMMENT '涓诲浘瑕佹眰JSON',
    detail_screen_count_range_json JSON COMMENT '璇︽儏灞忔暟鑼冨洿JSON',
    risk_rules_json JSON COMMENT '椋庨櫓瑙勫垯JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '绛栫暐鐘讹拷?',
    version INT NOT NULL DEFAULT 1 COMMENT '鐗堟湰锟?',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_category_visual_policy_code (category_code),
    INDEX idx_category_visual_policy_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绫荤洰瑙嗚绛栫暐锟?;

CREATE TABLE IF NOT EXISTS model_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    display_name VARCHAR(255) NOT NULL COMMENT '妯＄壒鍚嶇О',
    front_image VARCHAR(500) COMMENT '姝ｉ潰锟?',
    side_image VARCHAR(500) COMMENT '渚ч潰锟?',
    back_image VARCHAR(500) COMMENT '鑳岄潰锟?',
    height DECIMAL(10,2) COMMENT '韬珮',
    weight DECIMAL(10,2) COMMENT '浣撻噸',
    bust DECIMAL(10,2) COMMENT '鑳稿洿',
    waist DECIMAL(10,2) COMMENT '鑵板洿',
    hip DECIMAL(10,2) COMMENT '鑷€锟?',
    style_tags_json JSON COMMENT '椋庢牸鏍囩JSON',
    category_scopes_json JSON COMMENT '鍙敤绫荤洰JSON',
    authorization_status VARCHAR(50) COMMENT '鎺堟潈鐘讹拷?',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '妯＄壒鐘讹拷?',
    version INT NOT NULL DEFAULT 1 COMMENT '鐗堟湰锟?',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_model_profile_status (status),
    INDEX idx_model_profile_authorization_status (authorization_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='妯＄壒搴撹〃';

CREATE TABLE IF NOT EXISTS skc_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    policy_name VARCHAR(255) NOT NULL COMMENT '绛栫暐鍚嶇О',
    category_code VARCHAR(100) COMMENT '绫荤洰缂栫爜',
    color_count INT COMMENT '棰滆壊鏁伴噺',
    spec_count INT COMMENT '瑙勬牸鏁伴噺',
    colors_json JSON COMMENT '棰滆壊JSON',
    specs_json JSON COMMENT '瑙勬牸JSON',
    render_mode VARCHAR(50) COMMENT '鍑哄浘妯″紡',
    variant_display_mode VARCHAR(50) COMMENT 'SKC灞曠ず妯″紡',
    generation_rules_json JSON COMMENT '鍑哄浘瑙勫垯JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '绛栫暐鐘讹拷?',
    version INT NOT NULL DEFAULT 1 COMMENT '鐗堟湰锟?',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_skc_policy_category_code (category_code),
    INDEX idx_skc_policy_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKC瑙嗚绛栫暐锟?;

CREATE TABLE IF NOT EXISTS prompt_workbench_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    entry_type VARCHAR(50) NOT NULL COMMENT 'prompt 宸ヤ綔鍙扮被锟?',
    task_name VARCHAR(255) COMMENT '浠诲姟鍚嶇О',
    tool_code VARCHAR(100) NOT NULL COMMENT '宸ュ叿缂栫爜',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '浠诲姟鐘讹拷?',
    version INT NOT NULL DEFAULT 1 COMMENT '鐗堟湰锟?',
    input_json JSON COMMENT '杈撳叆JSON',
    output_json JSON COMMENT '杈撳嚭JSON',
    output_text LONGTEXT COMMENT '杈撳嚭鏂囨湰',
    error_message TEXT COMMENT '閿欒淇℃伅',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_prompt_workbench_entry_type (entry_type),
    INDEX idx_prompt_workbench_tool_code (tool_code),
    INDEX idx_prompt_workbench_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt Workbench 璁板綍锟?;

CREATE TABLE IF NOT EXISTS visual_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    product_detail_id BIGINT COMMENT '鍟嗗搧璇︽儏ID',
    plan_name VARCHAR(255) NOT NULL COMMENT '瑙嗚瑙勫垝鍚嶇О',
    category_code VARCHAR(100) COMMENT '绫荤洰缂栫爜',
    category_visual_policy_id BIGINT COMMENT '绫荤洰绛栫暐ID',
    model_profile_id BIGINT COMMENT '妯＄壒ID',
    skc_policy_id BIGINT COMMENT 'SKC 绛栫暐ID',
    prompt_workbench_entry_ids_json JSON COMMENT 'prompt 璁板綍ID JSON',
    input_json JSON COMMENT '杈撳叆JSON',
    plan_json JSON COMMENT '璁″垝JSON',
    confirmed_snapshot_json JSON COMMENT '纭蹇収JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '瑙勫垝鐘讹拷?',
    version INT NOT NULL DEFAULT 1 COMMENT '鐗堟湰锟?',
    confirmed_time DATETIME COMMENT '纭鏃堕棿',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_visual_plan_product_detail_id (product_detail_id),
    INDEX idx_visual_plan_category_code (category_code),
    INDEX idx_visual_plan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瑙嗚瑙勫垝锟?;

CREATE TABLE IF NOT EXISTS image_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    task_name VARCHAR(255) NOT NULL COMMENT '浠诲姟鍚嶇О',
    tool_code VARCHAR(100) NOT NULL COMMENT '宸ュ叿缂栫爜',
    input_json JSON COMMENT '杈撳叆JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '浠诲姟鐘讹拷?,
    progress INT DEFAULT 0 COMMENT '杩涘害',
    external_job_id VARCHAR(255) COMMENT '澶栭儴浠诲姟ID',
    error_message TEXT COMMENT '閿欒淇℃伅',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢熷浘浠诲姟锟?;

CREATE TABLE IF NOT EXISTS generation_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    image_job_id BIGINT NOT NULL COMMENT '浠诲姟ID',
    result_url VARCHAR(500) COMMENT '缁撴灉URL',
    thumbnail_url VARCHAR(500) COMMENT '缂╃暐鍥綧RL',
    prompt TEXT COMMENT '鎻愮ず锟?,
    params_json JSON COMMENT '鍙傛暟JSON',
    compliance_status VARCHAR(50) COMMENT '鍚堣鐘讹拷?,
    selected TINYINT(1) DEFAULT 0 COMMENT '鏄惁绮撅拷?,
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_generation_result_job_id (image_job_id),
    INDEX idx_generation_result_selected (selected),
    INDEX idx_generation_result_compliance_status (compliance_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢熸垚缁撴灉锟?;

CREATE TABLE IF NOT EXISTS audit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    product_detail_id BIGINT NOT NULL COMMENT '鍟嗗搧璇︽儏椤礗D',
    audit_type VARCHAR(50) COMMENT '瀹℃牳绫诲瀷',
    audit_status TINYINT NOT NULL COMMENT '瀹℃牳鐘讹拷?,
    risk_level TINYINT COMMENT '椋庨櫓绛夌骇',
    risk_items TEXT COMMENT '椋庨櫓椤瑰垪锟?,
    audit_comment TEXT COMMENT '瀹℃牳鎰忚',
    modification_suggestions TEXT COMMENT '淇敼寤鸿',
    submitter VARCHAR(50) COMMENT '鎻愪氦锟?,
    submit_time DATETIME COMMENT '鎻愪氦鏃堕棿',
    auditor VARCHAR(50) COMMENT '瀹℃牳锟?,
    audit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '瀹℃牳鏃堕棿',
    audit_duration INT COMMENT '瀹℃牳鑰楁椂',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    INDEX idx_product_detail_id (product_detail_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_auditor (auditor),
    INDEX idx_audit_time (audit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瀹℃牳璁板綍锟?;

CREATE TABLE IF NOT EXISTS export_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    product_detail_id BIGINT NOT NULL COMMENT '鍟嗗搧璇︽儏椤礗D',
    export_format VARCHAR(20) NOT NULL COMMENT '瀵煎嚭鏍煎紡',
    file_path VARCHAR(500) COMMENT '瀵煎嚭鏂囦欢璺緞',
    file_name VARCHAR(255) COMMENT '鏂囦欢锟?,
    file_size BIGINT COMMENT '鏂囦欢澶у皬',
    export_status TINYINT DEFAULT 0 COMMENT '瀵煎嚭鐘讹拷?,
    error_message TEXT COMMENT '閿欒淇℃伅',
    exporter VARCHAR(50) COMMENT '瀵煎嚭锟?,
    export_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '瀵煎嚭鏃堕棿',
    detail_composition_id BIGINT COMMENT 'P3.12: 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟絀D',
    manifest_json JSON COMMENT 'P3.12: 锟斤拷锟斤拷时锟侥斤拷锟斤拷锟藉单锟斤拷锟斤拷',
    manifest_consistent TINYINT(1) DEFAULT 0 COMMENT 'P3.12: 锟藉单一锟斤拷锟斤拷校锟斤拷锟斤拷',
    qa_check_id BIGINT COMMENT 'P3.12: 锟斤拷锟斤拷锟斤拷QA锟斤拷锟絀D',
    qa_status VARCHAR(20) COMMENT 'P3.12: 锟斤拷锟斤拷时锟斤拷QA状态',
    deleted TINYINT DEFAULT 0 COMMENT '锟竭硷拷删锟斤拷锟斤拷识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '锟斤拷锟斤拷时锟斤拷',
    INDEX idx_product_detail_id (product_detail_id),
    INDEX idx_export_status (export_status),
    INDEX idx_exporter (exporter),
    INDEX idx_export_time (export_time),
    INDEX idx_export_record_composition_id (detail_composition_id),
    INDEX idx_export_record_manifest_consistent (manifest_consistent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锟斤拷锟斤拷锟斤拷录锟斤拷';


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
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '锟斤拷锟斤拷ID',
    product_detail_id BIGINT NOT NULL COMMENT '锟斤拷品锟斤拷锟斤拷ID',
    check_type VARCHAR(50) NOT NULL COMMENT '锟斤拷锟斤拷锟斤拷锟? COPY_RISK, IMAGE_QA, ASSET_AUTH, MANIFEST, EXPORT_FILE, AUDIT_STATUS',
    target_type VARCHAR(50) COMMENT '目锟斤拷锟斤拷锟斤拷: TASK, ASSET, FIELD, COMPOSITION, EXPORT',
    target_id VARCHAR(255) COMMENT '目锟斤拷ID',
    target_field VARCHAR(100) COMMENT '目锟斤拷锟街讹拷锟斤拷',
    severity VARCHAR(20) NOT NULL DEFAULT 'HARD' COMMENT '锟斤拷锟斤拷锟斤拷: HARD(锟斤拷锟斤拷锟斤拷锟斤拷) / SOFT(锟斤拷锟芥警)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PASS, FAIL, WARN, PENDING',
    message TEXT COMMENT '锟缴讹拷锟斤拷锟斤拷锟较?,
    details_json JSON COMMENT '锟结构锟斤拷锟斤拷锟斤拷',
    overridden TINYINT(1) DEFAULT 0 COMMENT '锟角凤拷锟窖憋拷锟剿癸拷锟斤拷锟斤拷',
    override_reason TEXT COMMENT '锟斤拷锟斤拷原锟斤拷',
    override_operator VARCHAR(100) COMMENT '锟斤拷锟角诧拷锟斤拷锟斤拷',
    override_time DATETIME COMMENT '锟斤拷锟斤拷时锟斤拷',
    deleted TINYINT DEFAULT 0 COMMENT '锟竭硷拷删锟斤拷锟斤拷识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '锟斤拷锟斤拷时锟斤拷',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '锟斤拷锟斤拷时锟斤拷',
    INDEX idx_publish_check_detail_id (product_detail_id),
    INDEX idx_publish_check_type (check_type),
    INDEX idx_publish_check_status (status),
    INDEX idx_publish_check_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锟斤拷锟斤拷前锟绞硷拷锟斤拷锟铰?;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锟脚讹拷锟矫伙拷锟斤拷';

CREATE TABLE IF NOT EXISTS team_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锟斤拷色锟斤拷';

CREATE TABLE IF NOT EXISTS team_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL,
    permission_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权锟睫憋拷';

CREATE TABLE IF NOT EXISTS team_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user_role (user_id, role_id),
    INDEX idx_team_user_role_user (user_id),
    INDEX idx_team_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锟矫伙拷锟斤拷色锟斤拷锟斤拷锟斤拷';

CREATE TABLE IF NOT EXISTS team_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_role_permission (role_id, permission_id),
    INDEX idx_team_role_permission_role (role_id),
    INDEX idx_team_role_permission_perm (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锟斤拷色权锟睫癸拷锟斤拷锟斤拷';

CREATE TABLE IF NOT EXISTS operation_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL COMMENT '锟斤拷锟斤拷锟斤拷ID',
    operator_name VARCHAR(200) NOT NULL COMMENT '锟斤拷锟斤拷锟斤拷锟斤拷示锟斤拷',
    action VARCHAR(100) NOT NULL COMMENT '锟斤拷锟斤拷锟斤拷锟斤拷: CONFIRM_VISUAL_PLAN, TRIGGER_IMAGE_JOB, APPLY_RESULT, EXPORT, AUDIT, OVERRIDE 锟斤拷',
    target_type VARCHAR(100) COMMENT '目锟斤拷锟斤拷源锟斤拷锟斤拷',
    target_id BIGINT COMMENT '目锟斤拷锟斤拷源ID',
    detail_json JSON COMMENT '锟斤拷锟斤拷锟斤拷锟斤拷JSON',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operation_audit_operator (operator_id),
    INDEX idx_operation_audit_action (action),
    INDEX idx_operation_audit_target (target_type, target_id),
    INDEX idx_operation_audit_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锟斤拷锟斤拷锟斤拷锟斤拷锟街撅拷锟?;
CREATE TABLE IF NOT EXISTS post_process_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '涓婚敭ID',
    source_generation_result_id BIGINT COMMENT '鏉ユ簮鐢熸垚缁撴灉ID',
    source_image_path VARCHAR(500) NOT NULL COMMENT '杈撳叆鍥剧墖璺緞',
    output_image_path VARCHAR(500) COMMENT '杈撳嚭鍥剧墖璺緞',
    tool_code VARCHAR(100) NOT NULL COMMENT '宸ュ叿缂栫爜: grounded-sam, iopaint, real-esrgan, imagemagick',
    operation VARCHAR(100) NOT NULL COMMENT '鎿嶄綔鍚嶇О: inpaint, cleanup-background, remove-object, segment, upscale, crop, resize',
    params_json JSON COMMENT '宸ュ叿璋冪敤鍙傛暟JSON',
    input_width INT COMMENT '杈撳叆鍥剧墖瀹藉害',
    input_height INT COMMENT '杈撳叆鍥剧墖楂樺害',
    input_file_size BIGINT COMMENT '杈撳叆鏂囦欢澶у皬',
    input_mime_type VARCHAR(100) COMMENT '杈撳叆MIME绫诲瀷',
    output_width INT COMMENT '杈撳嚭鍥剧墖瀹藉害',
    output_height INT COMMENT '杈撳嚭鍥剧墖楂樺害',
    output_file_size BIGINT COMMENT '杈撳嚭鏂囦欢澶у皬',
    output_mime_type VARCHAR(100) COMMENT '杈撳嚭MIME绫诲瀷',
    source_chain_json JSON COMMENT '鏉ユ簮閾綣SON',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '浠诲姟鐘舵€?,
    progress INT DEFAULT 0 COMMENT '杩涘害',
    error_message TEXT COMMENT '閿欒淇℃伅',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎鏍囪瘑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_post_process_source_result_id (source_generation_result_id),
    INDEX idx_post_process_tool_code (tool_code),
    INDEX idx_post_process_status (status),
    INDEX idx_post_process_operation (operation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍚庡鐞嗕换鍔¤〃';

CREATE TABLE IF NOT EXISTS prompt_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    template_name VARCHAR(255) NOT NULL COMMENT '模板名称',
    category VARCHAR(100) NOT NULL COMMENT '分类: PRODUCT_MAIN, DETAIL_SCENE, MODEL_SHOT, FLAT_LAY, BACKGROUND, LIFESTYLE, BRAND_STORY',
    scene_type VARCHAR(100) COMMENT '场景类型: INDOOR, OUTDOOR, STUDIO, NATURE, URBAN',
    platform VARCHAR(50) COMMENT '适用平台: TAOBAO, JD, PINDUODUO, DOUYIN, AMAZON, SHOPIFY, GENERAL',
    style VARCHAR(100) COMMENT '风格: MINIMALIST, LUXURY, CUTE, TECH, VINTAGE, FRESH, DARK, BRIGHT',
    positive_prompt TEXT NOT NULL COMMENT '正向提示词',
    negative_prompt TEXT COMMENT '反向提示词',
    style_tags_json JSON COMMENT '风格标签JSON',
    constraints_json JSON COMMENT '约束条件JSON',
    description TEXT COMMENT '模板说明',
    preview_image_url VARCHAR(500) COMMENT '预览图地址',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    rating DECIMAL(3,2) DEFAULT 0.00 COMMENT '评分 0.00-5.00',
    source VARCHAR(50) DEFAULT 'CUSTOM' COMMENT '来源: SYSTEM, COMMUNITY, CUSTOM, GITHUB',
    source_ref VARCHAR(500) COMMENT '来源引用(URL/repo)',
    language VARCHAR(20) DEFAULT 'zh-CN' COMMENT '语言',
    author VARCHAR(100) COMMENT '作者',
    tags_json JSON COMMENT '标签JSON',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, DRAFT, ARCHIVED',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_prompt_template_category (category),
    INDEX idx_prompt_template_platform (platform),
    INDEX idx_prompt_template_style (style),
    INDEX idx_prompt_template_source (source),
    INDEX idx_prompt_template_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板库';
