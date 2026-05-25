package com.ecommerce.detail.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 风险检测工具类
 * 提供敏感词检测、合规校验等功能
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Slf4j
@Component
public class RiskCheckUtil {

    // 广告法禁用词示例
    private static final Set<String> ADVERTISEMENT_BANNED_WORDS = new HashSet<>(Arrays.asList(
        "国家级", "最高级", "最佳", "最好", "最强", "最先进", "最新技术", 
        "最科学", "最新科学", "独一无二", "独家", "首选", "第一", "唯一",
        "金牌", "名牌", "优秀", "顶级", "极品", "极佳", "绝佳", "绝对",
        "终极", "极致", "完美", "万能", "永久", "永久有效", "史无前例",
        "前无古人", "后无来者", "绝版", "珍稀", "稀世", "空前", "绝后"
    ));

    // 医疗相关禁用词
    private static final Set<String> MEDICAL_BANNED_WORDS = new HashSet<>(Arrays.asList(
        "治疗", "治愈", "疗效", "消炎", "抗癌", "防癌", "降血压", "降血糖",
        "减肥", "瘦身", "美白", "祛斑", "祛痘", "去皱", "抗衰老", "延年益寿"
    ));

    // 虚假宣传关键词
    private static final Set<String> FALSE_ADVERTISING_WORDS = new HashSet<>(Arrays.asList(
        "100%有效", "无效退款", "零风险", " guaranteed", "保证", "承诺",
        "绝对安全", "无副作用", "纯天然", "绿色健康", "环保无污染"
    ));

    // 价格相关敏感词
    private static final Set<String> PRICE_SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
        "最低价", "全网最低", "史上最低", "抄底价", "跳楼价", "亏本甩卖",
        "原价", "原价出售", "折扣", "打折", "优惠", "特价", "促销价"
    ));

    /**
     * 检测文本中的风险内容
     * 
     * @param content 待检测文本
     * @return 风险检测结果
     */
    public RiskCheckResult checkRisk(String content) {
        RiskCheckResult result = new RiskCheckResult();
        result.setContent(content);
        
        List<String> issues = new ArrayList<>();
        Map<String, List<String>> issueDetails = new HashMap<>();
        
        // 检测广告法禁用词
        List<String> adIssues = detectBannedWords(content, ADVERTISEMENT_BANNED_WORDS, "广告法禁用词");
        if (!adIssues.isEmpty()) {
            issues.addAll(adIssues);
            issueDetails.put("advertisement", adIssues);
        }
        
        // 检测医疗相关禁用词
        List<String> medicalIssues = detectBannedWords(content, MEDICAL_BANNED_WORDS, "医疗禁用词");
        if (!medicalIssues.isEmpty()) {
            issues.addAll(medicalIssues);
            issueDetails.put("medical", medicalIssues);
        }
        
        // 检测虚假宣传关键词
        List<String> falseAdvIssues = detectBannedWords(content, FALSE_ADVERTISING_WORDS, "虚假宣传");
        if (!falseAdvIssues.isEmpty()) {
            issues.addAll(falseAdvIssues);
            issueDetails.put("falseAdvertising", falseAdvIssues);
        }
        
        // 检测价格敏感词
        List<String> priceIssues = detectBannedWords(content, PRICE_SENSITIVE_WORDS, "价格敏感词");
        if (!priceIssues.isEmpty()) {
            issues.addAll(priceIssues);
            issueDetails.put("price", priceIssues);
        }
        
        // 检测极限用语（正则匹配）
        List<String> extremeIssues = detectExtremeExpressions(content);
        if (!extremeIssues.isEmpty()) {
            issues.addAll(extremeIssues);
            issueDetails.put("extreme", extremeIssues);
        }
        
        result.setIssues(issues);
        result.setIssueDetails(issueDetails);
        result.setRiskLevel(calculateRiskLevel(issues.size()));
        result.setHasRisk(!issues.isEmpty());
        
        // 生成修改建议
        result.setSuggestions(generateSuggestions(issueDetails));
        
        log.debug("风险检测结果: 风险等级={}, 问题数量={}", result.getRiskLevel(), issues.size());
        
        return result;
    }

    /**
     * 检测自定义敏感词
     * 
     * @param content 待检测文本
     * @param sensitiveWords 敏感词列表
     * @return 检测到的敏感词列表
     */
    public List<String> detectSensitiveWords(String content, List<String> sensitiveWords) {
        List<String> found = new ArrayList<>();
        
        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                found.add(word);
            }
        }
        
        return found;
    }

    /**
     * 检测品牌违规词
     * 
     * @param content 待检测文本
     * @param brandBannedWords 品牌禁用词库
     * @return 检测结果
     */
    public RiskCheckResult checkBrandCompliance(String content, List<String> brandBannedWords) {
        RiskCheckResult result = new RiskCheckResult();
        result.setContent(content);
        
        List<String> issues = detectSensitiveWords(content, brandBannedWords);
        result.setIssues(issues);
        result.setHasRisk(!issues.isEmpty());
        result.setRiskLevel(calculateRiskLevel(issues.size()));
        
        if (!issues.isEmpty()) {
            Map<String, List<String>> details = new HashMap<>();
            details.put("brandBanned", issues);
            result.setIssueDetails(details);
            result.setSuggestions(Arrays.asList(
                "请移除或替换以下品牌违规词: " + String.join(", ", issues),
                "建议参考品牌规范文档进行文案调整"
            ));
        }
        
        return result;
    }

    /**
     * 检测文本相似度（防止抄袭）
     * 
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度百分比（0-100）
     */
    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        // 简单的字符级别相似度计算
        Set<Character> set1 = new HashSet<>();
        Set<Character> set2 = new HashSet<>();
        
        for (char c : text1.toCharArray()) {
            set1.add(c);
        }
        for (char c : text2.toCharArray()) {
            set2.add(c);
        }
        
        Set<Character> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<Character> union = new HashSet<>(set1);
        union.addAll(set2);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (intersection.size() * 100.0) / union.size();
    }

    /**
     * 检测极限用语（使用正则表达式）
     * 
     * @param content 待检测文本
     * @return 检测到的极限用语列表
     */
    private List<String> detectExtremeExpressions(String content) {
        List<String> issues = new ArrayList<>();
        
        // 匹配"最XXX"模式
        Pattern mostPattern = Pattern.compile("最[\\u4e00-\\u9fa5]{1,4}");
        java.util.regex.Matcher matcher = mostPattern.matcher(content);
        while (matcher.find()) {
            issues.add(matcher.group());
        }
        
        // 匹配"第X"模式
        Pattern firstPattern = Pattern.compile("第[一二三四五六七八九十百千万]+");
        matcher = firstPattern.matcher(content);
        while (matcher.find()) {
            issues.add(matcher.group());
        }
        
        return issues;
    }

    /**
     * 检测禁用词
     * 
     * @param content 待检测文本
     * @param bannedWords 禁用词集合
     * @param category 类别名称
     * @return 检测到的禁用词列表
     */
    private List<String> detectBannedWords(String content, Set<String> bannedWords, String category) {
        List<String> found = new ArrayList<>();
        
        for (String word : bannedWords) {
            if (content.contains(word)) {
                found.add(String.format("[%s] %s", category, word));
            }
        }
        
        return found;
    }

    /**
     * 计算风险等级
     * 
     * @param issueCount 问题数量
     * @return 风险等级
     */
    private String calculateRiskLevel(int issueCount) {
        if (issueCount == 0) {
            return "LOW";
        } else if (issueCount <= 3) {
            return "MEDIUM";
        } else if (issueCount <= 10) {
            return "HIGH";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * 生成修改建议
     * 
     * @param issueDetails 问题详情
     * @return 建议列表
     */
    private List<String> generateSuggestions(Map<String, List<String>> issueDetails) {
        List<String> suggestions = new ArrayList<>();
        
        if (issueDetails.containsKey("advertisement")) {
            suggestions.add("请移除广告法禁用词，如'最佳'、'最优'等极限用语");
        }
        
        if (issueDetails.containsKey("medical")) {
            suggestions.add("非医疗产品请勿使用医疗相关术语，避免违规宣传");
        }
        
        if (issueDetails.containsKey("falseAdvertising")) {
            suggestions.add("避免使用绝对化承诺，如'100%有效'、'零风险'等");
        }
        
        if (issueDetails.containsKey("price")) {
            suggestions.add("价格描述需真实准确，避免使用'最低价'等无法验证的表述");
        }
        
        if (issueDetails.containsKey("extreme")) {
            suggestions.add("请谨慎使用极限用语，确保有充分依据支撑");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("未发现明显风险，建议人工复核确认");
        }
        
        return suggestions;
    }

    /**
     * 风险检测结果类
     */
    public static class RiskCheckResult {
        private String content;
        private boolean hasRisk;
        private String riskLevel;
        private List<String> issues;
        private Map<String, List<String>> issueDetails;
        private List<String> suggestions;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isHasRisk() {
            return hasRisk;
        }

        public void setHasRisk(boolean hasRisk) {
            this.hasRisk = hasRisk;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public List<String> getIssues() {
            return issues;
        }

        public void setIssues(List<String> issues) {
            this.issues = issues;
        }

        public Map<String, List<String>> getIssueDetails() {
            return issueDetails;
        }

        public void setIssueDetails(Map<String, List<String>> issueDetails) {
            this.issueDetails = issueDetails;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions;
        }
    }
}
