$ProgressPreference = 'SilentlyContinue'
$ErrorActionPreference = 'Continue'
$outDir = Join-Path $PSScriptRoot 'crop_research'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$repos = @(
    @{ n = 'pineapple'; u = 'https://api.github.com/repos/AmarokIce/PineappleDelight/git/trees/1.21?recursive=1' },
    @{ n = 'ubes'; u = 'https://api.github.com/repos/ChefMooon/ubes-delight/git/trees/1.21.1?recursive=1' },
    @{ n = 'ubes-main'; u = 'https://api.github.com/repos/ChefMooon/ubes-delight/git/trees/main?recursive=1' },
    @{ n = 'veggies'; u = 'https://api.github.com/repos/MehdiNoui/VeggiesDelight/git/trees/master?recursive=1' },
    @{ n = 'corn'; u = 'https://api.github.com/repos/0999312/Corn-Delight/git/trees/1.20.x?recursive=1' },
    @{ n = 'corn-121'; u = 'https://api.github.com/repos/0999312/Corn-Delight/git/trees/1.21.x?recursive=1' },
    @{ n = 'expanded'; u = 'https://api.github.com/repos/ianm1647/expandeddelight/git/trees/1.21-neo?recursive=1' },
    @{ n = 'cultural'; u = 'https://api.github.com/repos/ncpicard/CulturalDelights/git/trees/1.21?recursive=1' },
    @{ n = 'cultural2'; u = 'https://api.github.com/repos/ncpicard/cultural-delights/git/trees/1.21?recursive=1' },
    @{ n = 'croptopia'; u = 'https://api.github.com/repos/thethonk/Croptopia/git/trees/1.21?recursive=1' },
    @{ n = 'croptopia-neo'; u = 'https://api.github.com/repos/thethonk/Croptopia/git/trees/1.21.1-neoforge?recursive=1' }
)

$headers = @{ 'User-Agent' = 'nsuk-crop-research' }
foreach ($r in $repos) {
    Write-Host "==== $($r.n) ===="
    $outFile = Join-Path $outDir "$($r.n)_tree.txt"
    try {
        $json = Invoke-RestMethod -Uri $r.u -Headers $headers
        if ($null -eq $json.tree) {
            "NO TREE: $($r.u)" | Tee-Object -FilePath $outFile
            continue
        }
        $paths = $json.tree |
            Where-Object { $_.path -match '\.(java|json)$' } |
            ForEach-Object { $_.path }
        $paths | Set-Content -Path $outFile -Encoding UTF8
        $paths |
            Where-Object { $_ -match '(?i)(crop|seed|ModItems|ModBlocks|registry|Block|Item)' } |
            Where-Object { $_ -notmatch 'models/|loot_table|recipe|advancement|lang/' } |
            Select-Object -First 100 |
            ForEach-Object { Write-Host $_ }
        Write-Host ("total files: " + $paths.Count)
    } catch {
        $_.Exception.Message | Tee-Object -FilePath $outFile
    }
}
