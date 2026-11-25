package com.taller.inicializacion.data;

import net.datafaker.Faker;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catálogos y reglas deterministas para la generación de datos de demostración.
 *
 * <p>La coherencia se garantiza derivando unidad, categoría y costo del tipo de producto,
 * en lugar de elegirlos de forma independiente. Toda relación tipo → categoría es única.</p>
 */
public final class DemoData {

    private DemoData() {
    }

    /** El orden debe coincidir con la cantidad de categorías configurada en el seed. */
    public static final List<String> CATEGORIAS_BASE = List.of(
            "Lubricantes", "Filtros", "Frenos", "Suspensión", "Motor",
            "Eléctrico", "Transmisión", "Neumáticos", "Herramientas", "Consumibles",
            "Carrocería", "Refrigeración", "Escape", "Iluminación", "Seguridad",
            "Dirección", "Climatización", "Limpieza", "Pintura", "Accesorios");

    public record RangoCosto(long minimoCentimos, long maximoCentimos) {
        public RangoCosto {
            if (minimoCentimos < 0 || maximoCentimos < minimoCentimos) {
                throw new IllegalArgumentException("Rango de costo inválido");
            }
        }
    }

    public record RangoCantidad(int minimo, int maximo) {
        public RangoCantidad {
            if (minimo < 1 || maximo < minimo) {
                throw new IllegalArgumentException("Rango de cantidad inválido");
            }
        }
    }

    public record ServicioDemo(String finalidad, List<String> productosFrecuentes,
                               List<String> productosOpcionales) {
    }

    /** Unidad comercial plausible para cada tipo de producto. */
    public static final Map<String, List<String>> UNIDADES_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", List.of("LITRO", "GALON")),
            Map.entry("Aceite hidráulico", List.of("LITRO", "GALON")),
            Map.entry("Filtro de aceite", List.of("UNIDAD")),
            Map.entry("Filtro de aire", List.of("UNIDAD")),
            Map.entry("Filtro de combustible", List.of("UNIDAD")),
            Map.entry("Filtro de habitáculo", List.of("UNIDAD")),
            Map.entry("Pastillas de freno", List.of("JUEGO")),
            Map.entry("Discos de freno", List.of("UNIDAD", "PAR")),
            Map.entry("Zapatas de freno", List.of("JUEGO")),
            Map.entry("Líquido de frenos", List.of("LITRO")),
            Map.entry("Amortiguador", List.of("UNIDAD", "PAR")),
            Map.entry("Resorte de suspensión", List.of("UNIDAD", "PAR")),
            Map.entry("Bieleta", List.of("UNIDAD", "PAR")),
            Map.entry("Rótula", List.of("UNIDAD")),
            Map.entry("Bujía", List.of("UNIDAD", "CAJA")),
            Map.entry("Correa de distribución", List.of("UNIDAD", "KIT")),
            Map.entry("Junta de culata", List.of("UNIDAD")),
            Map.entry("Bomba de aceite", List.of("UNIDAD")),
            Map.entry("Batería", List.of("UNIDAD")),
            Map.entry("Alternador", List.of("UNIDAD")),
            Map.entry("Fusible", List.of("UNIDAD", "CAJA")),
            Map.entry("Foco automotriz", List.of("UNIDAD", "PAR")),
            Map.entry("Kit de embrague", List.of("JUEGO", "KIT")),
            Map.entry("Aceite de transmisión", List.of("LITRO", "GALON")),
            Map.entry("Neumático", List.of("UNIDAD")),
            Map.entry("Válvula de neumático", List.of("UNIDAD")),
            Map.entry("Llave de tuercas", List.of("UNIDAD")),
            Map.entry("Destornillador", List.of("UNIDAD")),
            Map.entry("Alicate", List.of("UNIDAD")),
            Map.entry("Juego de dados", List.of("JUEGO")),
            Map.entry("Limpiador de frenos", List.of("UNIDAD", "LITRO")),
            Map.entry("Silicona automotriz", List.of("UNIDAD")),
            Map.entry("Cinta aislante", List.of("UNIDAD", "ROLLO")),
            Map.entry("Abrazadera", List.of("UNIDAD", "CAJA")),
            Map.entry("Espejo retrovisor", List.of("UNIDAD", "PAR")),
            Map.entry("Parachoques", List.of("UNIDAD")),
            Map.entry("Radiador", List.of("UNIDAD")),
            Map.entry("Manguera de refrigeración", List.of("METRO", "UNIDAD")),
            Map.entry("Silenciador", List.of("UNIDAD")),
            Map.entry("Junta de escape", List.of("UNIDAD")),
            Map.entry("Faro", List.of("UNIDAD", "PAR")),
            Map.entry("Luz posterior", List.of("UNIDAD", "PAR")),
            Map.entry("Extintor vehicular", List.of("UNIDAD")),
            Map.entry("Triángulo de seguridad", List.of("UNIDAD")),
            Map.entry("Terminal de dirección", List.of("UNIDAD", "PAR")),
            Map.entry("Cremallera de dirección", List.of("UNIDAD")),
            Map.entry("Filtro de cabina", List.of("UNIDAD")),
            Map.entry("Gas refrigerante", List.of("LATA", "KILOGRAMO")),
            Map.entry("Shampoo para carrocería", List.of("LITRO", "GALON")),
            Map.entry("Desengrasante", List.of("LITRO", "GALON")),
            Map.entry("Pintura en aerosol", List.of("UNIDAD")),
            Map.entry("Masilla automotriz", List.of("KILOGRAMO")),
            Map.entry("Plumillas limpiaparabrisas", List.of("UNIDAD", "PAR")),
            Map.entry("Tapetes", List.of("JUEGO")));

    /** Traducción de las unidades históricas de texto a los códigos de negocio sembrados en V9. */
    public static final Map<String, String> CODIGO_UNIDAD_BASE = Map.ofEntries(
            Map.entry("UNIDAD", "UND"),
            Map.entry("CAJA", "UND"),
            Map.entry("KILOGRAMO", "UND"),
            Map.entry("METRO", "CM"),
            Map.entry("ROLLO", "CM"),
            Map.entry("PAR", "PAR"),
            Map.entry("JUEGO", "JUEGO"),
            Map.entry("KIT", "KIT"),
            Map.entry("LITRO", "LITRO"),
            Map.entry("GALON", "GALON"),
            Map.entry("LATA", "LATA"));

    /** Presentación y factor por tipo. KG no se usa en datos demo (unidad de negocio sin uso). */
    public static final Map<String, String> PRESENTACION_POR_TIPO = Map.ofEntries(
            Map.entry("Bujía", "CAJA"),
            Map.entry("Fusible", "CAJA"),
            Map.entry("Abrazadera", "CAJA"),
            Map.entry("Manguera de refrigeración", "METRO"),
            Map.entry("Cinta aislante", "ROLLO"));

    public static final Map<String, Integer> FACTOR_PRESENTACION_POR_TIPO = Map.ofEntries(
            Map.entry("Bujía", 20),
            Map.entry("Fusible", 50),
            Map.entry("Abrazadera", 100),
            Map.entry("Manguera de refrigeración", 100),
            Map.entry("Cinta aislante", 3000));

    /** Relación única tipo → categoría. Todas las categorías tienen al menos dos tipos. */
    public static final Map<String, String> CATEGORIA_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", "Lubricantes"),
            Map.entry("Aceite hidráulico", "Lubricantes"),
            Map.entry("Filtro de aceite", "Filtros"),
            Map.entry("Filtro de aire", "Filtros"),
            Map.entry("Filtro de combustible", "Filtros"),
            Map.entry("Filtro de habitáculo", "Filtros"),
            Map.entry("Pastillas de freno", "Frenos"),
            Map.entry("Discos de freno", "Frenos"),
            Map.entry("Zapatas de freno", "Frenos"),
            Map.entry("Líquido de frenos", "Frenos"),
            Map.entry("Amortiguador", "Suspensión"),
            Map.entry("Resorte de suspensión", "Suspensión"),
            Map.entry("Bieleta", "Suspensión"),
            Map.entry("Rótula", "Suspensión"),
            Map.entry("Bujía", "Motor"),
            Map.entry("Correa de distribución", "Motor"),
            Map.entry("Junta de culata", "Motor"),
            Map.entry("Bomba de aceite", "Motor"),
            Map.entry("Batería", "Eléctrico"),
            Map.entry("Alternador", "Eléctrico"),
            Map.entry("Fusible", "Eléctrico"),
            Map.entry("Foco automotriz", "Eléctrico"),
            Map.entry("Kit de embrague", "Transmisión"),
            Map.entry("Aceite de transmisión", "Transmisión"),
            Map.entry("Neumático", "Neumáticos"),
            Map.entry("Válvula de neumático", "Neumáticos"),
            Map.entry("Llave de tuercas", "Herramientas"),
            Map.entry("Destornillador", "Herramientas"),
            Map.entry("Alicate", "Herramientas"),
            Map.entry("Juego de dados", "Herramientas"),
            Map.entry("Limpiador de frenos", "Consumibles"),
            Map.entry("Silicona automotriz", "Consumibles"),
            Map.entry("Cinta aislante", "Consumibles"),
            Map.entry("Abrazadera", "Consumibles"),
            Map.entry("Espejo retrovisor", "Carrocería"),
            Map.entry("Parachoques", "Carrocería"),
            Map.entry("Radiador", "Refrigeración"),
            Map.entry("Manguera de refrigeración", "Refrigeración"),
            Map.entry("Silenciador", "Escape"),
            Map.entry("Junta de escape", "Escape"),
            Map.entry("Faro", "Iluminación"),
            Map.entry("Luz posterior", "Iluminación"),
            Map.entry("Extintor vehicular", "Seguridad"),
            Map.entry("Triángulo de seguridad", "Seguridad"),
            Map.entry("Terminal de dirección", "Dirección"),
            Map.entry("Cremallera de dirección", "Dirección"),
            Map.entry("Filtro de cabina", "Climatización"),
            Map.entry("Gas refrigerante", "Climatización"),
            Map.entry("Shampoo para carrocería", "Limpieza"),
            Map.entry("Desengrasante", "Limpieza"),
            Map.entry("Pintura en aerosol", "Pintura"),
            Map.entry("Masilla automotriz", "Pintura"),
            Map.entry("Plumillas limpiaparabrisas", "Accesorios"),
            Map.entry("Tapetes", "Accesorios"));

    /** Orden estable: no depender del orden de iteración de {@code Map.ofEntries}. */
    public static final List<String> TIPOS_PRODUCTO =
            CATEGORIA_POR_TIPO.keySet().stream().sorted().toList();

    /** Herramientas: solo se inventarían, nunca se consumen. */
    public static final Set<String> TIPOS_HERRAMIENTA = Set.of(
            "Llave de tuercas", "Destornillador", "Alicate", "Juego de dados");

    /** Costos simulados en céntimos de sol. */
    public static final Map<String, RangoCosto> COSTOS_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", new RangoCosto(1800, 4500)),
            Map.entry("Aceite hidráulico", new RangoCosto(2000, 5000)),
            Map.entry("Filtro de aceite", new RangoCosto(800, 3500)),
            Map.entry("Filtro de aire", new RangoCosto(1200, 5000)),
            Map.entry("Filtro de combustible", new RangoCosto(1200, 5500)),
            Map.entry("Filtro de habitáculo", new RangoCosto(1200, 5000)),
            Map.entry("Pastillas de freno", new RangoCosto(3500, 14000)),
            Map.entry("Discos de freno", new RangoCosto(6000, 22000)),
            Map.entry("Zapatas de freno", new RangoCosto(3000, 10000)),
            Map.entry("Líquido de frenos", new RangoCosto(1200, 4500)),
            Map.entry("Amortiguador", new RangoCosto(7000, 30000)),
            Map.entry("Resorte de suspensión", new RangoCosto(5000, 18000)),
            Map.entry("Bieleta", new RangoCosto(2500, 9000)),
            Map.entry("Rótula", new RangoCosto(2500, 10000)),
            Map.entry("Bujía", new RangoCosto(500, 3000)),
            Map.entry("Correa de distribución", new RangoCosto(4000, 18000)),
            Map.entry("Junta de culata", new RangoCosto(5000, 22000)),
            Map.entry("Bomba de aceite", new RangoCosto(7000, 25000)),
            Map.entry("Batería", new RangoCosto(18000, 48000)),
            Map.entry("Alternador", new RangoCosto(15000, 50000)),
            Map.entry("Fusible", new RangoCosto(100, 800)),
            Map.entry("Foco automotriz", new RangoCosto(500, 4500)),
            Map.entry("Kit de embrague", new RangoCosto(18000, 65000)),
            Map.entry("Aceite de transmisión", new RangoCosto(2200, 6000)),
            Map.entry("Neumático", new RangoCosto(15000, 55000)),
            Map.entry("Válvula de neumático", new RangoCosto(200, 1200)),
            Map.entry("Llave de tuercas", new RangoCosto(1500, 9000)),
            Map.entry("Destornillador", new RangoCosto(500, 5000)),
            Map.entry("Alicate", new RangoCosto(1000, 7000)),
            Map.entry("Juego de dados", new RangoCosto(5000, 25000)),
            Map.entry("Limpiador de frenos", new RangoCosto(1000, 4500)),
            Map.entry("Silicona automotriz", new RangoCosto(800, 3500)),
            Map.entry("Cinta aislante", new RangoCosto(300, 1500)),
            Map.entry("Abrazadera", new RangoCosto(100, 1000)),
            Map.entry("Espejo retrovisor", new RangoCosto(5000, 25000)),
            Map.entry("Parachoques", new RangoCosto(18000, 70000)),
            Map.entry("Radiador", new RangoCosto(18000, 65000)),
            Map.entry("Manguera de refrigeración", new RangoCosto(1000, 7000)),
            Map.entry("Silenciador", new RangoCosto(12000, 45000)),
            Map.entry("Junta de escape", new RangoCosto(800, 5000)),
            Map.entry("Faro", new RangoCosto(7000, 35000)),
            Map.entry("Luz posterior", new RangoCosto(5000, 25000)),
            Map.entry("Extintor vehicular", new RangoCosto(5000, 18000)),
            Map.entry("Triángulo de seguridad", new RangoCosto(1000, 5000)),
            Map.entry("Terminal de dirección", new RangoCosto(3000, 12000)),
            Map.entry("Cremallera de dirección", new RangoCosto(25000, 90000)),
            Map.entry("Filtro de cabina", new RangoCosto(1000, 5000)),
            Map.entry("Gas refrigerante", new RangoCosto(5000, 18000)),
            Map.entry("Shampoo para carrocería", new RangoCosto(1000, 5000)),
            Map.entry("Desengrasante", new RangoCosto(1000, 5000)),
            Map.entry("Pintura en aerosol", new RangoCosto(1500, 6000)),
            Map.entry("Masilla automotriz", new RangoCosto(2500, 9000)),
            Map.entry("Plumillas limpiaparabrisas", new RangoCosto(1500, 7000)),
            Map.entry("Tapetes", new RangoCosto(5000, 18000)));

    /** Cantidades por reposición, expresadas en la unidad de inventario del producto. */
    public static final Map<String, RangoCantidad> CANTIDADES_ENTRADA_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", new RangoCantidad(12, 40)),
            Map.entry("Aceite hidráulico", new RangoCantidad(6, 20)),
            Map.entry("Filtro de aceite", new RangoCantidad(10, 30)),
            Map.entry("Filtro de aire", new RangoCantidad(6, 20)),
            Map.entry("Filtro de combustible", new RangoCantidad(5, 15)),
            Map.entry("Pastillas de freno", new RangoCantidad(3, 10)),
            Map.entry("Bujía", new RangoCantidad(8, 24)),
            Map.entry("Batería", new RangoCantidad(2, 6)),
            Map.entry("Amortiguador", new RangoCantidad(2, 6)),
            Map.entry("Neumático", new RangoCantidad(2, 8)),
            Map.entry("Radiador", new RangoCantidad(1, 3)),
            Map.entry("Llave de tuercas", new RangoCantidad(1, 2)),
            Map.entry("Destornillador", new RangoCantidad(1, 4)),
            Map.entry("Manguera de refrigeración", new RangoCantidad(100, 500)),
            Map.entry("Cinta aislante", new RangoCantidad(1000, 5000)));

    /** Peso relativo de reposición: los artículos de alta rotación aparecen más a menudo. */
    public static final Map<String, Integer> PESO_ROTACION_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", 8), Map.entry("Filtro de aceite", 8),
            Map.entry("Filtro de aire", 6), Map.entry("Bujía", 6),
            Map.entry("Limpiador de frenos", 5), Map.entry("Líquido de frenos", 4),
            Map.entry("Pastillas de freno", 4), Map.entry("Cinta aislante", 3),
            Map.entry("Abrazadera", 3), Map.entry("Batería", 2),
            Map.entry("Amortiguador", 2), Map.entry("Radiador", 1),
            Map.entry("Alternador", 1), Map.entry("Cremallera de dirección", 1));

    public static final List<String> VEHICULOS_COMUNES = List.of(
            "Toyota Yaris", "Toyota Corolla", "Toyota Hilux", "Toyota RAV4",
            "Nissan Sentra", "Nissan Versa", "Nissan Frontier", "Hyundai Accent",
            "Hyundai Tucson", "Kia Rio", "Kia Sportage", "Chevrolet Onix",
            "Chevrolet Tracker", "Suzuki Swift", "Suzuki Vitara", "Mitsubishi L200",
            "Renault Duster", "Volkswagen Gol", "Volkswagen Virtus", "Kia Soluto");

    public static final List<ServicioDemo> SERVICIOS = List.of(
            new ServicioDemo("Mantenimiento preventivo",
                    List.of("Aceite de motor", "Filtro de aceite"),
                    List.of("Filtro de aire", "Filtro de combustible", "Bujía")),
            new ServicioDemo("Cambio de aceite",
                    List.of("Aceite de motor", "Filtro de aceite"),
                    List.of("Limpiador de frenos")),
            new ServicioDemo("Reparación de motor",
                    List.of("Bujía", "Junta de culata"),
                    List.of("Correa de distribución", "Bomba de aceite")),
            new ServicioDemo("Alineación y balanceo",
                    List.of("Terminal de dirección", "Válvula de neumático"),
                    List.of("Neumático")),
            new ServicioDemo("Cambio de frenos",
                    List.of("Pastillas de freno"),
                    List.of("Discos de freno", "Líquido de frenos", "Limpiador de frenos")),
            new ServicioDemo("Revisión de suspensión",
                    List.of("Amortiguador", "Bieleta"),
                    List.of("Resorte de suspensión", "Rótula")),
            new ServicioDemo("Cambio de batería",
                    List.of("Batería"), List.of("Fusible", "Cinta aislante")),
            new ServicioDemo("Diagnóstico eléctrico",
                    List.of("Fusible", "Foco automotriz"),
                    List.of("Batería", "Alternador")),
            new ServicioDemo("Reparación de transmisión",
                    List.of("Kit de embrague", "Aceite de transmisión"), List.of()),
            new ServicioDemo("Cambio de neumáticos",
                    List.of("Neumático", "Válvula de neumático"), List.of()),
            new ServicioDemo("Limpieza de inyectores",
                    List.of("Desengrasante"), List.of("Filtro de combustible")),
            new ServicioDemo("Reparación de aire acondicionado",
                    List.of("Gas refrigerante", "Filtro de cabina"), List.of()));

    public static final List<String> FINALIDADES = SERVICIOS.stream()
            .map(ServicioDemo::finalidad)
            .toList();

    public static final Map<String, List<String>> CATEGORIAS_POR_FINALIDAD = Map.ofEntries(
            Map.entry("Mantenimiento preventivo", List.of("Lubricantes", "Filtros", "Motor")),
            Map.entry("Cambio de aceite", List.of("Lubricantes", "Filtros")),
            Map.entry("Reparación de motor", List.of("Motor")),
            Map.entry("Alineación y balanceo", List.of("Dirección", "Neumáticos")),
            Map.entry("Cambio de frenos", List.of("Frenos", "Consumibles")),
            Map.entry("Revisión de suspensión", List.of("Suspensión")),
            Map.entry("Cambio de batería", List.of("Eléctrico")),
            Map.entry("Diagnóstico eléctrico", List.of("Eléctrico")),
            Map.entry("Reparación de transmisión", List.of("Transmisión")),
            Map.entry("Cambio de neumáticos", List.of("Neumáticos")),
            Map.entry("Limpieza de inyectores", List.of("Motor", "Limpieza")),
            Map.entry("Reparación de aire acondicionado", List.of("Climatización")));

    public static int cantidadConsumo(String tipo, Faker faker) {
        return switch (tipo) {
            case "Aceite de motor", "Aceite hidráulico", "Aceite de transmisión" ->
                    faker.number().numberBetween(3, 6);
            case "Filtro de aceite", "Filtro de aire", "Filtro de combustible", "Filtro de habitáculo",
                 "Filtro de cabina", "Batería", "Neumático", "Amortiguador", "Radiador" -> 1;
            case "Bujía", "Fusible", "Abrazadera", "Válvula de neumático" ->
                    faker.number().numberBetween(1, 5);
            case "Manguera de refrigeración", "Cinta aislante" ->
                    faker.number().numberBetween(30, 300);
            default -> faker.number().numberBetween(1, 3);
        };
    }

    public static int cantidadEntrada(String tipo, Faker faker) {
        RangoCantidad rango = CANTIDADES_ENTRADA_POR_TIPO.get(tipo);
        return rango == null
                ? faker.number().numberBetween(5, 20)
                : faker.number().numberBetween(rango.minimo(), rango.maximo());
    }
}
