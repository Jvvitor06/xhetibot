# Cria o arquivo de configuracao do Chat IA em %USERPROFILE%\.chatia\config.properties
$pasta = Join-Path $env:USERPROFILE '.chatia'
$arquivo = Join-Path $pasta 'config.properties'

Write-Host ''
Write-Host 'Configuracao do Chat IA'
Write-Host '-----------------------'

if (Test-Path $arquivo) {
    $resp = Read-Host 'Ja existe uma configuracao neste computador. Substituir? (S/N)'
    if ($resp -notmatch '^[sS]') {
        Write-Host 'Nada foi alterado.'
        exit 0
    }
}

$uri = (Read-Host 'Cole a MONGODB_URI').Trim()
$chave = (Read-Host 'Cole a GROQ_API_KEY').Trim()

if (-not $uri -or -not $chave) {
    Write-Host 'ERRO: os dois valores sao obrigatorios. Nada foi salvo.'
    exit 1
}

New-Item -ItemType Directory -Force -Path $pasta | Out-Null
Set-Content -Path $arquivo -Encoding ascii -Value @("MONGODB_URI=$uri", "GROQ_API_KEY=$chave")

Write-Host ''
Write-Host "Pronto! Configuracao salva em: $arquivo"