param(
    [Parameter(Mandatory = $true)]
    [string]$Title,

    [Parameter(Mandatory = $true)]
    [string]$Action,

    [Parameter(Mandatory = $true)]
    [string]$NextStep,

    [string[]]$Completed = @(),
    [string[]]$Blockers = @("None."),
    [string[]]$FilesTouched = @(),
    [string[]]$Verification = @(),
    [string]$Branch,
    [string]$Head,
    [datetime]$Timestamp = (Get-Date)
)

function Get-GitValue {
    param(
        [string[]]$Arguments,
        [string]$Fallback
    )

    try {
        $value = git @Arguments 2>$null
        if ($LASTEXITCODE -eq 0 -and $value) {
            return ($value | Select-Object -First 1).Trim()
        }
    } catch {
    }

    return $Fallback
}

function Format-Bullets {
    param(
        [string[]]$Items,
        [string]$DefaultItem
    )

    $values = if ($Items.Count -gt 0) { $Items } else { @($DefaultItem) }
    return ($values | ForEach-Object { "  - $_" }) -join [Environment]::NewLine
}

if (-not $Branch) {
    $Branch = Get-GitValue -Arguments @("branch", "--show-current") -Fallback "UNKNOWN_BRANCH"
}

if (-not $Head) {
    $Head = Get-GitValue -Arguments @("rev-parse", "--short", "HEAD") -Fallback "UNKNOWN_HEAD"
}

$stamp = $Timestamp.ToString("yyyy-MM-dd HH:mm:ss zzz")
$header = "## $stamp | $Title | Branch $Branch | HEAD $Head"

$completedBlock = Format-Bullets -Items $Completed -DefaultItem "TODO"
$blockersBlock = Format-Bullets -Items $Blockers -DefaultItem "None."
$filesBlock = Format-Bullets -Items $FilesTouched -DefaultItem "TODO"
$verificationBlock = Format-Bullets -Items $Verification -DefaultItem "TODO"

$entry = @"
$header
- Action: $Action
- Completed:
$completedBlock
- Exact next step:
  - $NextStep
- Blockers:
$blockersBlock
- Files touched:
$filesBlock
- Verification:
$verificationBlock
"@

"# HANDOFF.md snippet"
$entry
""
"# HANDOFF_LOG.md snippet"
$entry
