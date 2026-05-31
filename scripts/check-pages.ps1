# Frontend Pages Check Script
$pages = @(
    @{Route="/"; Page="HomeWorkbenchPage.tsx"; Path="frontend/src/pages/workbench/HomeWorkbenchPage.tsx"},
    @{Route="/login"; Page="LoginPage.tsx"; Path="frontend/src/pages/auth/LoginPage.tsx"},
    @{Route="/research"; Page="ResearchCenterPage.tsx"; Path="frontend/src/pages/research/ResearchCenterPage.tsx"},
    @{Route="/research/new"; Page="NewResearchTaskPage.tsx"; Path="frontend/src/pages/research/NewResearchTaskPage.tsx"},
    @{Route="/research/tasks/:id"; Page="ResearchTaskDetailPage.tsx"; Path="frontend/src/pages/research/ResearchTaskDetailPage.tsx"},
    @{Route="/research/competitors"; Page="CompetitorLibraryPage.tsx"; Path="frontend/src/pages/research/CompetitorLibraryPage.tsx"},
    @{Route="/research/reports/:id"; Page="ResearchReportPage.tsx"; Path="frontend/src/pages/research/ResearchReportPage.tsx"},
    @{Route="/materials"; Page="MaterialListPage.tsx"; Path="frontend/src/pages/materials/MaterialListPage.tsx"},
    @{Route="/materials/new"; Page="MaterialCreatePage.tsx"; Path="frontend/src/pages/materials/MaterialCreatePage.tsx"},
    @{Route="/materials/:id"; Page="MaterialDetailPage.tsx"; Path="frontend/src/pages/materials/MaterialDetailPage.tsx"},
    @{Route="/generate"; Page="GenerateWorkbenchPage.tsx"; Path="frontend/src/pages/generate/GenerateWorkbenchPage.tsx"},
    @{Route="/generate/:taskId"; Page="GenerateTaskDetailPage.tsx"; Path="frontend/src/pages/generate/GenerateTaskDetailPage.tsx"},
    @{Route="/assets"; Page="AssetLibraryPage.tsx"; Path="frontend/src/pages/assets/AssetLibraryPage.tsx"},
    @{Route="/results"; Page="ResultsPreviewPage.tsx"; Path="frontend/src/pages/results/ResultsPreviewPage.tsx"},
    @{Route="/details/:id"; Page="DetailEditorPage.tsx"; Path="frontend/src/pages/details/DetailEditorPage.tsx"},
    @{Route="/details/:id/review"; Page="DetailReviewPlaceholder.tsx"; Path="frontend/src/pages/detail-review/index.tsx"},
    @{Route="/audit"; Page="AuditCenterPage.tsx"; Path="frontend/src/pages/audit/AuditCenterPage.tsx"},
    @{Route="/exports"; Page="ExportRecordsPage.tsx"; Path="frontend/src/pages/exports/ExportRecordsPage.tsx"},
    @{Route="/post-process"; Page="PostProcessTasksPage.tsx"; Path="frontend/src/pages/post-process/PostProcessTasksPage.tsx"},
    @{Route="/tools"; Page="ToolCenterPage.tsx"; Path="frontend/src/pages/tools/ToolCenterPage.tsx"},
    @{Route="/tools/imports"; Page="DataImportPage.tsx"; Path="frontend/src/pages/tools/DataImportPage.tsx"},
    @{Route="/tools/design-draft"; Page="DesignDraftPage.tsx"; Path="frontend/src/pages/tools/DesignDraftPage.tsx"},
    @{Route="/tools/:toolCode"; Page="ToolDetailPage.tsx"; Path="frontend/src/pages/tools/ToolDetailPage.tsx"},
    @{Route="/visual/category-policies"; Page="CategoryVisualPoliciesPage.tsx"; Path="frontend/src/pages/visual/CategoryVisualPoliciesPage.tsx"},
    @{Route="/visual/model-profiles"; Page="ModelProfilesPage.tsx"; Path="frontend/src/pages/visual/ModelProfilesPage.tsx"},
    @{Route="/visual/prompt-workbench"; Page="PromptWorkbenchPage.tsx"; Path="frontend/src/pages/visual/PromptWorkbenchPage.tsx"},
    @{Route="/visual/prompt-templates"; Page="PromptTemplatePage.tsx"; Path="frontend/src/pages/visual/PromptTemplatePage.tsx"},
    @{Route="/visual/plans"; Page="VisualPlansPage.tsx"; Path="frontend/src/pages/visual/VisualPlansPage.tsx"},
    @{Route="/cost"; Page="CostManagementPage.tsx"; Path="frontend/src/pages/cost/CostManagementPage.tsx"},
    @{Route="/system/diagnostics"; Page="DiagnosticsPage.tsx"; Path="frontend/src/pages/system/DiagnosticsPage.tsx"},
    @{Route="/system/team"; Page="TeamManagementPage.tsx"; Path="frontend/src/pages/system/TeamManagementPage.tsx"},
    @{Route="/system/audit-log"; Page="AuditLogPage.tsx"; Path="frontend/src/pages/system/AuditLogPage.tsx"}
)

$results = @()

foreach ($page in $pages) {
    $filePath = "C:\Users\Administrator\IdeaProjects\E-commerce detail creation\$($page.Path)"
    $result = @{
        Route = $page.Route
        Page = $page.Page
        Exists = $false
        HasChinese = $false
        HasI18n = $false
        IsPlaceholder = $false
        HasLayout = $false
        Issues = @()
    }
    
    if (Test-Path $filePath) {
        $result.Exists = $true
        $content = Get-Content $filePath -Encoding UTF8 -Raw
        
        if ($content -match '[\u4e00-\u9fff]') { $result.HasChinese = $true }
        if ($content -match 'useLang|from.*i18n') { $result.HasI18n = $true }
        if ($content -match 'P0Scaffold') { 
            $result.IsPlaceholder = $true 
            $result.Issues += "Uses P0Scaffold placeholder"
        }
        if ($content -match 'export default function|export default') { $result.HasLayout = $true }
        
        $englishHardcoded = [regex]::Matches($content, '"[A-Z][a-z]+(?: [A-Z][a-z]+)*"')
        if ($englishHardcoded.Count -gt 5) {
            $result.Issues += "Has $($englishHardcoded.Count) English hardcoded strings"
        }
        
        if ($content -notmatch 'ErrorState|error.*message') {
            $result.Issues += "Missing error state handling"
        }
        
        if ($content -notmatch 'LoadingState|loading.*state|isLoading') {
            $result.Issues += "Missing loading state handling"
        }
    } else {
        $result.Issues += "File does not exist"
    }
    
    $results += $result
}

Write-Output "=" * 80
Write-Output "Frontend Pages Check Report"
Write-Output "=" * 80
Write-Output ""

$placeholderCount = ($results | Where-Object { $_.IsPlaceholder }).Count
$noChineseCount = ($results | Where-Object { -not $_.HasChinese -and $_.Exists }).Count
$noI18nCount = ($results | Where-Object { -not $_.HasI18n -and $_.Exists }).Count

Write-Output "Total: $($results.Count) pages"
Write-Output "Placeholder: $placeholderCount pages"
Write-Output "No Chinese: $noChineseCount pages"
Write-Output "No i18n: $noI18nCount pages"
Write-Output ""

foreach ($result in $results) {
    Write-Output "Page: $($result.Page)"
    Write-Output "Route: $($result.Route)"
    Write-Output "Exists: $(if ($result.Exists) { 'Yes' } else { 'No' })"
    
    if ($result.Exists) {
        Write-Output "Chinese: $(if ($result.HasChinese) { 'Yes' } else { 'No' })"
        Write-Output "i18n: $(if ($result.HasI18n) { 'Used' } else { 'Not Used' })"
        Write-Output "Placeholder: $(if ($result.IsPlaceholder) { 'Yes' } else { 'No' })"
        Write-Output "Layout: $(if ($result.HasLayout) { 'OK' } else { 'Error' })"
    }
    
    if ($result.Issues.Count -gt 0) {
        Write-Output "Issues:"
        foreach ($issue in $result.Issues) {
            Write-Output "  - $issue"
        }
    }
    
    Write-Output "-" * 40
}