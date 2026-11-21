# Pruebas de permisos y reglas de negocio contra el gateway (puerto 8080)
$base = "http://localhost:8080/delicias/v1"
$ok = 0
$fallos = 0

function Login($correo, $password) {
    $cuerpo = @{ correo = $correo; password = $password } | ConvertTo-Json
    (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $cuerpo).token
}

function Llamar($metodo, $ruta, $token, $cuerpo) {
    $p = @{ Method = $metodo; Uri = "$base$ruta"; UseBasicParsing = $true }
    if ($token) { $p.Headers = @{ Authorization = "Bearer $token" } }
    if ($cuerpo) { $p.Body = $cuerpo; $p.ContentType = "application/json" }
    try {
        return [int](Invoke-WebRequest @p).StatusCode
    } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode.value__ }
        return 0
    }
}

function Verificar($descripcion, $esperado, $obtenido) {
    if ($esperado -eq $obtenido) {
        $script:ok++
        Write-Host "OK     $descripcion (HTTP $obtenido)" -ForegroundColor Green
    } else {
        $script:fallos++
        Write-Host "FALLO  $descripcion (esperado $esperado, obtenido $obtenido)" -ForegroundColor Red
    }
}

$admin = Login "admin@deliciasdelsur.cl" "admin123"
$vendedor = Login "vendedor@deliciasdelsur.cl" "vendedor123"
$cliente = Login "cliente@deliciasdelsur.cl" "cliente123"

$compra = '{"items":[{"productoId":1,"cantidad":1}]}'
$compraExcesiva = '{"items":[{"productoId":1,"cantidad":99999}]}'

Write-Host "--- Sin sesión ---"
$rutaReal = $base.Replace("/delicias/v1", "/api/v1")
try {
    $codigoReal = [int](Invoke-WebRequest -UseBasicParsing -Method GET -Uri "$rutaReal/productos").StatusCode
} catch {
    $codigoReal = 0
    if ($_.Exception.Response) { $codigoReal = [int]$_.Exception.Response.StatusCode.value__ }
}
Verificar "La ruta real /api/v1 no está expuesta en el gateway" 404 $codigoReal
Verificar "Listar productos" 200 (Llamar "GET" "/productos" $null $null)
Verificar "Listar categorías" 200 (Llamar "GET" "/categorias" $null $null)
Verificar "Crear categoría sin token" 403 (Llamar "POST" "/categorias" $null '{"nombre":"X"}')
Verificar "Ver boletas sin token" 403 (Llamar "GET" "/boletas" $null $null)

Write-Host "--- Vendedor ---"
Verificar "Listar productos" 200 (Llamar "GET" "/productos" $vendedor $null)
Verificar "Ver detalle de producto" 200 (Llamar "GET" "/productos/1" $vendedor $null)
Verificar "Listar órdenes" 200 (Llamar "GET" "/boletas" $vendedor $null)
Verificar "Crear producto" 403 (Llamar "POST" "/productos" $vendedor '{"nombre":"X"}')
Verificar "Editar producto" 403 (Llamar "PUT" "/productos/1" $vendedor '{"nombre":"X"}')
Verificar "Eliminar producto" 403 (Llamar "DELETE" "/productos/1" $vendedor $null)
Verificar "Ver usuarios" 403 (Llamar "GET" "/usuarios" $vendedor $null)
Verificar "Crear una compra" 403 (Llamar "POST" "/boletas" $vendedor $compra)

Write-Host "--- Cliente ---"
Verificar "Listar productos" 200 (Llamar "GET" "/productos" $cliente $null)
Verificar "Crear una compra" 201 (Llamar "POST" "/boletas" $cliente $compra)
Verificar "Compra con stock insuficiente" 400 (Llamar "POST" "/boletas" $cliente $compraExcesiva)
Verificar "Ver sus boletas" 200 (Llamar "GET" "/boletas/mis-boletas" $cliente $null)
Verificar "Ver todas las boletas" 403 (Llamar "GET" "/boletas" $cliente $null)
Verificar "Ver usuarios" 403 (Llamar "GET" "/usuarios" $cliente $null)
Verificar "Crear categoría" 403 (Llamar "POST" "/categorias" $cliente '{"nombre":"X"}')

Write-Host "--- Administrador ---"
$nombreCategoria = "Prueba-" + (Get-Date -Format "HHmmss")
$cuerpoCategoria = '{"nombre":"' + $nombreCategoria + '","descripcion":"temporal"}'
Verificar "Ver usuarios" 200 (Llamar "GET" "/usuarios" $admin $null)
Verificar "Ver todas las boletas" 200 (Llamar "GET" "/boletas" $admin $null)
Verificar "Crear categoría" 201 (Llamar "POST" "/categorias" $admin $cuerpoCategoria)
$idCategoria = ((Invoke-RestMethod "$base/categorias") | Where-Object { $_.nombre -eq $nombreCategoria }).id
Verificar "Eliminar la categoría creada" 204 (Llamar "DELETE" "/categorias/$idCategoria" $admin $null)

Write-Host ""
Write-Host "Resultado: $ok correctas, $fallos con fallo"