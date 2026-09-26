$ProgressPreference = 'SilentlyContinue'
$headers = @{ 'User-Agent' = 'nsuk-crop-research' }
$out = Join-Path $PSScriptRoot 'crop_research'
New-Item -ItemType Directory -Force -Path $out | Out-Null
$files = @(
    @{ n = 'croptopia_Content.java'; u = 'https://raw.githubusercontent.com/ExcessiveAmountsOfZombies/Croptopia/v4/common/src/main/java/com/epherical/croptopia/register/Content.java' },
    @{ n = 'croptopia_FarmlandCrop.java'; u = 'https://raw.githubusercontent.com/ExcessiveAmountsOfZombies/Croptopia/v4/common/src/main/java/com/epherical/croptopia/register/helpers/FarmlandCrop.java' },
    @{ n = 'croptopia_CropBlock.java'; u = 'https://raw.githubusercontent.com/ExcessiveAmountsOfZombies/Croptopia/v4/common/src/main/java/com/epherical/croptopia/blocks/CroptopiaCropBlock.java' },
    @{ n = 'croptopia_SeedItem.java'; u = 'https://raw.githubusercontent.com/ExcessiveAmountsOfZombies/Croptopia/v4/common/src/main/java/com/epherical/croptopia/items/SeedItem.java' },
    @{ n = 'croptopia_ItemNames.java'; u = 'https://raw.githubusercontent.com/ExcessiveAmountsOfZombies/Croptopia/v4/common/src/main/java/com/epherical/croptopia/common/ItemNamesV2.java' },
    @{ n = 'croptopia_BlockNames.java'; u = 'https://raw.githubusercontent.com/ExcessiveAmountsOfZombies/Croptopia/v4/common/src/main/java/com/epherical/croptopia/common/BlockNames.java' },
    @{ n = 'croptopia_TreeCrop.java'; u = 'https://raw.githubusercontent.com/ExcessiveAmountsOfZombies/Croptopia/v4/common/src/main/java/com/epherical/croptopia/register/helpers/TreeCrop.java' },
    @{ n = 'ed_CropBlock.java'; u = 'https://raw.githubusercontent.com/ianm1647/expandeddelight/1.21-neo/src/main/java/ianm1647/expandeddelight/common/block/EDCropBlock.java' },
    @{ n = 'ed_Cranberry.java'; u = 'https://raw.githubusercontent.com/ianm1647/expandeddelight/1.21-neo/src/main/java/ianm1647/expandeddelight/common/block/CranberryPlantBlock.java' },
    @{ n = 'cd_Eggplant.java'; u = 'https://raw.githubusercontent.com/mrsterner/Cultural-Delights-Fabric/HEAD/src/main/java/dev/sterner/culturaldelights/common/block/EggplantBlock.java' }
)
foreach ($f in $files) {
    Write-Host $f.n
    try {
        Invoke-WebRequest -Uri $f.u -Headers $headers -OutFile (Join-Path $out $f.n) -UseBasicParsing
        Write-Host ("  " + (Get-Item (Join-Path $out $f.n)).Length)
    } catch {
        Write-Host $_.Exception.Message
    }
}
