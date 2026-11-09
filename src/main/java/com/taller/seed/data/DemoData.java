package com.taller.seed.data;

import java.util.List;

public final class DemoData {

    private DemoData() {
    }

    public static final List<String> CATEGORIAS_BASE = List.of(
            "Lubricantes",
            "Filtros",
            "Frenos",
            "Suspensión",
            "Motor",
            "Eléctrico",
            "Transmisión",
            "Neumáticos",
            "Herramientas",
            "Consumibles",
            "Carrocería",
            "Refrigeración",
            "Escape",
            "Iluminación",
            "Seguridad",
            "Dirección",
            "Climatización",
            "Limpieza",
            "Pintura",
            "Accesorios");

    public static final List<String> UNIDADES = List.of(
            "UNIDAD", "CAJA", "LITRO", "GALON", "KILOGRAMO", "METRO", "JUEGO", "PAR");

    public static final List<String> TIPOS_PRODUCTO = List.of(
            "Filtro de aceite",
            "Filtro de aire",
            "Filtro de combustible",
            "Pastillas de freno",
            "Discos de freno",
            "Aceite de motor",
            "Bujía",
            "Amortiguador",
            "Batería",
            "Correa de distribución",
            "Bomba de agua",
            "Radiador",
            "Alternador",
            "Faro",
            "Espejo retrovisor",
            "Neumático",
            "Kit de embrague",
            "Manguera",
            "Abrazadera",
            "Aceite hidráulico",
            "Grasa",
            "Limpiador de frenos",
            "Silicona",
            "Cinta aislante");

    public static final List<String> FINALIDADES = List.of(
            "Mantenimiento preventivo",
            "Cambio de aceite",
            "Reparación de motor",
            "Alineación y balanceo",
            "Cambio de frenos",
            "Revisión de suspensión",
            "Cambio de batería",
            "Diagnóstico eléctrico",
            "Reparación de transmisión",
            "Cambio de neumáticos",
            "Limpieza de inyectores",
            "Reparación de aire acondicionado");

    public static final int TAMANO_LOTE = 500;
}
