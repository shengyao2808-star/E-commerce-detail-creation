-- Initial sample data

INSERT INTO brand_template (
    brand_id, brand_name, template_name, template_type, template_content,
    style_tags, style_description, applicable_categories, enabled, usage_count, creator
) VALUES
(1001, '示例品牌A', '标准详情页模板', 'STANDARD',
 '{"modules":["商品介绍","核心卖点","使用场景","规格参数","FAQ"]}',
 '专业,清晰,可信', '语气专业克制，突出可信参数和真实使用场景', '电子产品,家居用品', 1, 0, 'admin'),
(1002, '示例品牌B', '促销详情页模板', 'PROMOTION',
 '{"modules":["活动利益点","核心卖点","对比说明","售后保障"]}',
 '促销,直接,转化', '语气直接，强调购买理由和售后保障，避免绝对化承诺', '日用品,服饰', 1, 0, 'admin');

INSERT INTO product_material (
    brand_id, brand_name, product_name, product_sku, category, price, description,
    images, videos, documents, status, uploader, parse_status
) VALUES
(1001, '示例品牌A', '智能手机X1', 'SKU001', '电子产品', 2999.00,
 '高性能智能手机，搭载新一代处理器，适合日常办公和娱乐。',
 '["https://example.com/image1.jpg","https://example.com/image2.jpg"]',
 '["https://example.com/video1.mp4"]',
 '["https://example.com/doc1.pdf"]',
 1, 'user1', 0),
(1002, '示例品牌B', '便携办公笔记本Pro', 'SKU002', '电子产品', 5999.00,
 '轻薄便携笔记本，适合办公和学习场景。',
 '["https://example.com/laptop1.jpg","https://example.com/laptop2.jpg"]',
 '[]',
 '["https://example.com/manual.pdf"]',
 1, 'user2', 0);
