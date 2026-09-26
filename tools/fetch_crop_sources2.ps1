$ProgressPreference = 'SilentlyContinue'
$ErrorActionPreference = 'Continue'
$outDir = Join-Path $PSScriptRoot 'crop_research'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$headers = @{ 'User-Agent' = 'nsuk-crop-research' }

function Save-Raw($name, $url) {
    $path = Join-Path $outDir $name
    Write-Host "GET $url"
    try {
        Invoke-WebRequest -Uri $url -Headers $headers -OutFile $path -UseBasicParsing
        Write-Host "  saved $name size=$((Get-Item $path).Length)"
    } catch {
        Write-Host "  FAIL $($_.Exception.Message)"
    }
}

function Save-Tree($name, $url) {
    $path = Join-Path $outDir "$name`_tree.txt"
    Write-Host "TREE $url"
    try {
        $json = Invoke-RestMethod -Uri $url -Headers $headers
        $json.tree | ForEach-Object { $_.path } | Set-Content $path -Encoding UTF8
        Write-Host ("  files=" + $json.tree.Count)
        $json.tree |
            Where-Object { $_.path -match '(?i)(Item|Block|Crop|Seed|registry)' -and $_.path -match '\.java$' } |
            ForEach-Object { Write-Host ("  " + $_.path) }
    } catch {
        $_.Exception.Message | Set-Content $path
        Write-Host "  FAIL $($_.Exception.Message)"
    }
}

Save-Raw 'ubes_Items.java' 'https://raw.githubusercontent.com/ChefMooon/ubes-delight/HEAD/common/src/main/java/com/chefmooon/ubesdelight/common/registry/UbesDelightItems.java'
Save-Raw 'ubes_Blocks.java' 'https://raw.githubusercontent.com/ChefMooon/ubes-delight/HEAD/common/src/main/java/com/chefmooon/ubesdelight/common/registry/UbesDelightBlocks.java'
Save-Raw 'ubes_UbeCrop.java' 'https://raw.githubusercontent.com/ChefMooon/ubes-delight/HEAD/common/src/main/java/com/chefmooon/ubesdelight/common/block/UbeCropBlock.java'
Save-Raw 'ubes_Lemongrass.java' 'https://raw.githubusercontent.com/ChefMooon/ubes-delight/HEAD/common/src/main/java/com/chefmooon/ubesdelight/common/block/LemongrassCropBlock.java'
Save-Raw 'ubes_LemongrassLeaf.java' 'https://raw.githubusercontent.com/ChefMooon/ubes-delight/HEAD/common/src/main/java/com/chefmooon/ubesdelight/common/block/LemongrassLeafCropBlock.java'
Save-Raw 'ubes_LemongrassStalk.java' 'https://raw.githubusercontent.com/ChefMooon/ubes-delight/HEAD/common/src/main/java/com/chefmooon/ubesdelight/common/block/LemongrassStalkCropBlock.java'
Save-Raw 'ubes_Garlic.java' 'https://raw.githubusercontent.com/ChefMooon/ubes-delight/HEAD/common/src/main/java/com/chefmooon/ubesdelight/common/block/GarlicCropBlock.java'

Save-Raw 'cd_Objects.java' 'https://raw.githubusercontent.com/mrsterner/Cultural-Delights-Fabric/HEAD/src/main/java/dev/sterner/culturaldelights/common/registry/CDObjects.java'
Save-Raw 'cd_Cucumbers.java' 'https://raw.githubusercontent.com/mrsterner/Cultural-Delights-Fabric/HEAD/src/main/java/dev/sterner/culturaldelights/common/block/CucumbersBlock.java'
Save-Raw 'cd_Eggplant.java' 'https://raw.githubusercontent.com/mrsterner/Cultural-Delights-Fabric/HEAD/src/main/java/dev/sterner/culturaldelights/common/block/EggplantBlock.java'
Save-Raw 'cd_Corn.java' 'https://raw.githubusercontent.com/mrsterner/Cultural-Delights-Fabric/HEAD/src/main/java/dev/sterner/culturaldelights/common/block/CornBlock.java'

$ed = Invoke-RestMethod -Uri 'https://api.github.com/repos/ianm1647/expandeddelight/contents/src/main/java/ianm1647/expandeddelight/common/registry' -Headers $headers
$ed | ForEach-Object { Write-Host ("ED REG " + $_.name) }
$ed | ForEach-Object {
    Save-Raw ("expanded_" + $_.name) $_.download_url
}

$edb = Invoke-RestMethod -Uri 'https://api.github.com/repos/ianm1647/expandeddelight/contents/src/main/java/ianm1647/expandeddelight/common/block' -Headers $headers
$edb | ForEach-Object { Write-Host ("ED BLOCK " + $_.name) }

# Croptopia probes
foreach ($u in @(
    'https://api.github.com/repos/thethonk/Croptopia',
    'https://api.github.com/users/thethonk/repos',
    'https://api.github.com/orgs/Croptopia-Development/repos',
    'https://api.github.com/repos/Hashalotte/Croptopia',
    'https://api.github.com/search/repositories?q=Croptopia+farmland+seed+in:readme'
)) {
    Write-Host "PROBE $u"
    try {
        $j = Invoke-RestMethod -Uri $u -Headers $headers
        if ($j.items) { $j.items | Select-Object -First 5 | ForEach-Object { Write-Host $_.full_name } }
        elseif ($j -is [array]) { $j | Select-Object -First 15 | ForEach-Object { Write-Host ($_.full_name + ' ' + $_.default_branch) } }
        else { Write-Host ($j.full_name + ' ' + $j.default_branch) }
    } catch { Write-Host $_.Exception.Message }
}
