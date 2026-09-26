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
        if ($null -eq $json.tree) {
            "NO TREE" | Set-Content $path
            Write-Host "  no tree"
            return
        }
        $json.tree | ForEach-Object { $_.path } | Set-Content $path -Encoding UTF8
        Write-Host ("  files=" + $json.tree.Count)
        $json.tree |
            Where-Object { $_.path -match '(?i)(ModItems|ModBlocks|ItemList|BlockList|ItemRegistry|BlockRegistry|CropBlock|_crop|_seed)' -and $_.path -match '\.java$' } |
            ForEach-Object { Write-Host ("  " + $_.path) }
    } catch {
        $_.Exception.Message | Set-Content $path
        Write-Host "  FAIL $($_.Exception.Message)"
    }
}

# Pineapple
Save-Raw 'pineapple_ItemList.java' 'https://raw.githubusercontent.com/AmarokIce/PineappleDelight/1.21/src/main/java/club/someoneice/pineapple/init/ItemList.java'
Save-Raw 'pineapple_BlockList.java' 'https://raw.githubusercontent.com/AmarokIce/PineappleDelight/1.21/src/main/java/club/someoneice/pineapple/init/BlockList.java'
Save-Raw 'pineapple_Crop.java' 'https://raw.githubusercontent.com/AmarokIce/PineappleDelight/1.21/src/main/java/club/someoneice/pineapple/common/BlockPineappleCrop.java'

# Veggies
Save-Raw 'veggies_ModItems.java' 'https://raw.githubusercontent.com/MehdiNoui/VeggiesDelight/master/src/main/java/net/mehdinoui/veggiesdelight/common/registry/ModItems.java'
Save-Raw 'veggies_ModBlocks.java' 'https://raw.githubusercontent.com/MehdiNoui/VeggiesDelight/master/src/main/java/net/mehdinoui/veggiesdelight/common/registry/ModBlocks.java'
Save-Raw 'veggies_Zucchini.java' 'https://raw.githubusercontent.com/MehdiNoui/VeggiesDelight/master/src/main/java/net/mehdinoui/veggiesdelight/common/block/crops/ZucchiniCropBlock.java'
Save-Raw 'veggies_Garlic.java' 'https://raw.githubusercontent.com/MehdiNoui/VeggiesDelight/master/src/main/java/net/mehdinoui/veggiesdelight/common/block/crops/GarlicCropBlock.java'
Save-Raw 'veggies_villager_seeds.json' 'https://raw.githubusercontent.com/MehdiNoui/VeggiesDelight/master/src/generated/resources/data/minecraft/tags/items/villager_plantable_seeds.json'

# Corn
Save-Raw 'corn_ItemRegistry.java' 'https://raw.githubusercontent.com/0999312/Corn-Delight/1.20.x/src/main/java/cn/mcmod/corn_delight/item/ItemRegistry.java'
Save-Raw 'corn_BlockRegistry.java' 'https://raw.githubusercontent.com/0999312/Corn-Delight/1.20.x/src/main/java/cn/mcmod/corn_delight/block/BlockRegistry.java'
Save-Raw 'corn_CornCrop.java' 'https://raw.githubusercontent.com/0999312/Corn-Delight/1.20.x/src/main/java/cn/mcmod/corn_delight/block/CornCrop.java'

# Expanded
Save-Tree 'expanded-java' 'https://api.github.com/repos/ianm1647/expandeddelight/git/trees/1.21-neo?recursive=1'
Save-Raw 'expanded_villager_seeds.json' 'https://raw.githubusercontent.com/ianm1647/expandeddelight/1.21-neo/src/generated/resources/data/minecraft/tags/item/villager_plantable_seeds.json'

# Probe other repos
Save-Tree 'ubes-default' 'https://api.github.com/repos/ChefMooon/ubes-delight/git/trees/HEAD?recursive=1'
Save-Tree 'cultural-buebrigade' 'https://api.github.com/repos/BueBrigade/CulturalDelights/git/trees/HEAD?recursive=1'
Save-Tree 'cultural-ncp' 'https://api.github.com/repos/NCP-Studio/Cultural-Delights/git/trees/HEAD?recursive=1'
Save-Tree 'cultural-fabric' 'https://api.github.com/repos/mrsterner/Cultural-Delights-Fabric/git/trees/HEAD?recursive=1'
Save-Tree 'croptopia-head' 'https://api.github.com/repos/thethonk/Croptopia/git/trees/HEAD?recursive=1'
Save-Tree 'croptopia-neo' 'https://api.github.com/repos/Croptopia-Development/Croptopia/git/trees/HEAD?recursive=1'
Save-Tree 'gan' 'https://api.github.com/repos/GanZhiXiong/gan_delight_reborn/git/trees/HEAD?recursive=1'

# GitHub search
try {
    $q = Invoke-RestMethod -Uri 'https://api.github.com/search/repositories?q=cultural+delights+minecraft' -Headers $headers
    $q.items | Select-Object -First 8 | ForEach-Object { Write-Host ("CULTURAL REPO " + $_.full_name + " " + $_.default_branch) }
} catch { Write-Host $_.Exception.Message }
try {
    $q = Invoke-RestMethod -Uri 'https://api.github.com/search/repositories?q=ubes-delight+minecraft' -Headers $headers
    $q.items | Select-Object -First 8 | ForEach-Object { Write-Host ("UBE REPO " + $_.full_name + " " + $_.default_branch) }
} catch { Write-Host $_.Exception.Message }
try {
    $q = Invoke-RestMethod -Uri 'https://api.github.com/search/repositories?q=croptopia+neoforge+1.21' -Headers $headers
    $q.items | Select-Object -First 8 | ForEach-Object { Write-Host ("CROP REPO " + $_.full_name + " " + $_.default_branch) }
} catch { Write-Host $_.Exception.Message }
try {
    $q = Invoke-RestMethod -Uri 'https://api.github.com/search/repositories?q=gan_delight_reborn' -Headers $headers
    $q.items | Select-Object -First 8 | ForEach-Object { Write-Host ("GAN REPO " + $_.full_name + " " + $_.default_branch) }
} catch { Write-Host $_.Exception.Message }
try {
    $q = Invoke-RestMethod -Uri 'https://api.github.com/search/repositories?q=corn-delight+1.21+neoforge' -Headers $headers
    $q.items | Select-Object -First 8 | ForEach-Object { Write-Host ("CORN REPO " + $_.full_name + " " + $_.default_branch) }
} catch { Write-Host $_.Exception.Message }
