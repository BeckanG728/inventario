package com.taller.inicializacion.data;

import net.datafaker.Faker;

import java.util.List;
import java.util.Map;

/**
 * Catálogos y reglas deterministas para la generación de datos de demostración.
 *
 * <p>La coherencia se garantiza derivando unidad, categoría y costo del tipo de producto,
 * en lugar de elegirlos de forma independiente. Toda relación tipo → categoría es única.</p>
 *
 * <p>La unidad base es siempre discreta: UND, o el envase de un producto líquido, graso o pastoso
 * (galón, botella, tubo...). Nunca es un volumen o un peso, porque el sobrante de un envase abierto no se
 * puede registrar con exactitud. La presentación agrupa unidades base (CAJA de 12 botellas, JUEGO de 4 UND).</p>
 */
public final class DemoData {

    private DemoData() {
    }

    /** Deben coincidir con las categorías sembradas en V10. El orden debe coincidir con la cantidad configurada en el seed. */
    public static final List<String> CATEGORIAS_BASE = List.of(
            "Lubricantes y aceites", "Refrigerantes y líquidos", "Grasas", "Filtros", "Frenos",
            "Componentes eléctricos", "Tornillería y fijaciones", "Abrasivos",
            "Limpieza y mantenimiento", "Adhesivos y selladores");

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

    /**
     * Unidad de inventario de un producto: la base (la que se almacena en el stock) y, opcionalmente, una
     * presentación con su factor de conversión a la base. Los códigos son los sembrados en V9/V10.
     */
    public record UnidadDemo(String base, String presentacion, Integer factor) {
        public UnidadDemo {
            if ((presentacion == null) != (factor == null)) {
                throw new IllegalArgumentException("La presentación y el factor deben indicarse juntos");
            }
        }
    }

    public record ServicioDemo(String finalidad, List<String> productosFrecuentes,
                               List<String> productosOpcionales) {
    }

    private static UnidadDemo unidad(String base, String presentacion, int factor) {
        return new UnidadDemo(base, presentacion, factor);
    }

    private static UnidadDemo sinPresentacion(String base) {
        return new UnidadDemo(base, null, null);
    }

    /**
     * Opciones de unidad plausibles por tipo de producto. Cada tipo tiene una única unidad base; las opciones
     * varían la presentación comercial. Las unidades están restringidas a las que V10 asocia a su categoría.
     */
    public static final Map<String, List<UnidadDemo>> UNIDADES_POR_TIPO = Map.ofEntries(
            // Lubricantes y aceites
            Map.entry("Aceite de motor", List.of(unidad("GALON", "CAJA", 4), sinPresentacion("GALON"))),
            Map.entry("Aceite de transmisión", List.of(unidad("BOTELLA", "CAJA", 12), sinPresentacion("BOTELLA"))),
            Map.entry("Aceite hidráulico", List.of(sinPresentacion("BIDON"))),
            // Refrigerantes y líquidos
            Map.entry("Refrigerante", List.of(unidad("GALON", "CAJA", 4), sinPresentacion("GALON"))),
            Map.entry("Líquido de frenos", List.of(
                    unidad("BOTELLA", "CAJA", 12), unidad("BOTELLA", "CAJA", 24), sinPresentacion("BOTELLA"))),
            Map.entry("Líquido de dirección", List.of(unidad("BOTELLA", "CAJA", 12), sinPresentacion("BOTELLA"))),
            // Grasas
            Map.entry("Grasa multipropósito", List.of(unidad("TUBO", "CAJA", 12), unidad("TUBO", "CAJA", 24))),
            Map.entry("Grasa de litio", List.of(unidad("POTE", "CAJA", 6), sinPresentacion("POTE"))),
            Map.entry("Grasa para chasis", List.of(sinPresentacion("BALDE"))),
            // Filtros
            Map.entry("Filtro de aceite", List.of(unidad("UND", "CAJA", 12), unidad("UND", "PAQUETE", 4))),
            Map.entry("Filtro de aire", List.of(unidad("UND", "CAJA", 10), unidad("UND", "PAQUETE", 2))),
            Map.entry("Filtro de combustible", List.of(unidad("UND", "CAJA", 10), unidad("UND", "PAQUETE", 2))),
            Map.entry("Filtro de habitáculo", List.of(unidad("UND", "CAJA", 10), unidad("UND", "PAQUETE", 2))),
            // Frenos
            Map.entry("Pastillas de freno", List.of(unidad("UND", "JUEGO", 4), unidad("UND", "CAJA", 12))),
            Map.entry("Zapatas de freno", List.of(unidad("UND", "JUEGO", 4), unidad("UND", "PAR", 2))),
            Map.entry("Discos de freno", List.of(unidad("UND", "PAR", 2), unidad("UND", "CAJA", 4))),
            // Componentes eléctricos
            Map.entry("Fusible", List.of(
                    unidad("UND", "CAJA", 100), unidad("UND", "PAQUETE", 50), unidad("UND", "BLISTER", 10))),
            Map.entry("Relé", List.of(unidad("UND", "CAJA", 20), unidad("UND", "PAQUETE", 5))),
            Map.entry("Bombilla", List.of(
                    unidad("UND", "CAJA", 10), unidad("UND", "PAQUETE", 5), unidad("UND", "BLISTER", 2))),
            // Tornillería y fijaciones
            Map.entry("Perno", List.of(
                    unidad("UND", "CAJA", 100), unidad("UND", "BOLSA", 50), unidad("UND", "PAQUETE", 25))),
            Map.entry("Tuerca", List.of(
                    unidad("UND", "CAJA", 200), unidad("UND", "BOLSA", 100), unidad("UND", "PAQUETE", 50))),
            Map.entry("Arandela", List.of(
                    unidad("UND", "CAJA", 500), unidad("UND", "BOLSA", 200), unidad("UND", "PAQUETE", 100))),
            // Abrasivos
            Map.entry("Lija", List.of(unidad("UND", "PAQUETE", 10), unidad("UND", "CAJA", 50))),
            Map.entry("Disco de corte", List.of(unidad("UND", "PAQUETE", 5), unidad("UND", "CAJA", 25))),
            Map.entry("Disco de desbaste", List.of(unidad("UND", "PAQUETE", 5), unidad("UND", "CAJA", 20))),
            // Limpieza y mantenimiento
            Map.entry("Desengrasante", List.of(unidad("GALON", "CAJA", 4), sinPresentacion("GALON"))),
            Map.entry("Limpiacontactos", List.of(unidad("AEROSOL", "CAJA", 12), unidad("AEROSOL", "CAJA", 24))),
            Map.entry("Limpiador de frenos", List.of(unidad("AEROSOL", "CAJA", 12), unidad("AEROSOL", "CAJA", 24))),
            // Adhesivos y selladores
            Map.entry("Silicona", List.of(unidad("CARTUCHO", "CAJA", 12), unidad("CARTUCHO", "CAJA", 24))),
            Map.entry("Pegamento", List.of(unidad("POTE", "CAJA", 6), sinPresentacion("POTE"))),
            Map.entry("Sellador de juntas", List.of(unidad("TUBO", "CAJA", 12), unidad("TUBO", "CAJA", 24))));

    /** Relación única tipo → categoría. Todas las categorías tienen al menos dos tipos. */
    public static final Map<String, String> CATEGORIA_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", "Lubricantes y aceites"),
            Map.entry("Aceite de transmisión", "Lubricantes y aceites"),
            Map.entry("Aceite hidráulico", "Lubricantes y aceites"),
            Map.entry("Refrigerante", "Refrigerantes y líquidos"),
            Map.entry("Líquido de frenos", "Refrigerantes y líquidos"),
            Map.entry("Líquido de dirección", "Refrigerantes y líquidos"),
            Map.entry("Grasa multipropósito", "Grasas"),
            Map.entry("Grasa de litio", "Grasas"),
            Map.entry("Grasa para chasis", "Grasas"),
            Map.entry("Filtro de aceite", "Filtros"),
            Map.entry("Filtro de aire", "Filtros"),
            Map.entry("Filtro de combustible", "Filtros"),
            Map.entry("Filtro de habitáculo", "Filtros"),
            Map.entry("Pastillas de freno", "Frenos"),
            Map.entry("Zapatas de freno", "Frenos"),
            Map.entry("Discos de freno", "Frenos"),
            Map.entry("Fusible", "Componentes eléctricos"),
            Map.entry("Relé", "Componentes eléctricos"),
            Map.entry("Bombilla", "Componentes eléctricos"),
            Map.entry("Perno", "Tornillería y fijaciones"),
            Map.entry("Tuerca", "Tornillería y fijaciones"),
            Map.entry("Arandela", "Tornillería y fijaciones"),
            Map.entry("Lija", "Abrasivos"),
            Map.entry("Disco de corte", "Abrasivos"),
            Map.entry("Disco de desbaste", "Abrasivos"),
            Map.entry("Desengrasante", "Limpieza y mantenimiento"),
            Map.entry("Limpiacontactos", "Limpieza y mantenimiento"),
            Map.entry("Limpiador de frenos", "Limpieza y mantenimiento"),
            Map.entry("Silicona", "Adhesivos y selladores"),
            Map.entry("Pegamento", "Adhesivos y selladores"),
            Map.entry("Sellador de juntas", "Adhesivos y selladores"));

    /** Orden estable: no depender del orden de iteración de {@code Map.ofEntries}. */
    public static final List<String> TIPOS_PRODUCTO =
            CATEGORIA_POR_TIPO.keySet().stream().sorted().toList();

    /** Costos simulados en céntimos de sol, por unidad base del tipo (galón, botella, tubo, unidad...). */
    public static final Map<String, RangoCosto> COSTOS_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", new RangoCosto(6500, 16000)),
            Map.entry("Aceite de transmisión", new RangoCosto(2200, 6000)),
            Map.entry("Aceite hidráulico", new RangoCosto(15000, 40000)),
            Map.entry("Refrigerante", new RangoCosto(2500, 6000)),
            Map.entry("Líquido de frenos", new RangoCosto(1200, 4500)),
            Map.entry("Líquido de dirección", new RangoCosto(1200, 3500)),
            Map.entry("Grasa multipropósito", new RangoCosto(1500, 3000)),
            Map.entry("Grasa de litio", new RangoCosto(2500, 6000)),
            Map.entry("Grasa para chasis", new RangoCosto(15000, 35000)),
            Map.entry("Filtro de aceite", new RangoCosto(800, 3500)),
            Map.entry("Filtro de aire", new RangoCosto(1200, 5000)),
            Map.entry("Filtro de combustible", new RangoCosto(1200, 5500)),
            Map.entry("Filtro de habitáculo", new RangoCosto(1200, 5000)),
            Map.entry("Pastillas de freno", new RangoCosto(900, 3500)),
            Map.entry("Zapatas de freno", new RangoCosto(750, 2500)),
            Map.entry("Discos de freno", new RangoCosto(6000, 22000)),
            Map.entry("Fusible", new RangoCosto(20, 100)),
            Map.entry("Relé", new RangoCosto(800, 3500)),
            Map.entry("Bombilla", new RangoCosto(500, 4500)),
            Map.entry("Perno", new RangoCosto(30, 300)),
            Map.entry("Tuerca", new RangoCosto(20, 200)),
            Map.entry("Arandela", new RangoCosto(5, 50)),
            Map.entry("Lija", new RangoCosto(100, 600)),
            Map.entry("Disco de corte", new RangoCosto(400, 1800)),
            Map.entry("Disco de desbaste", new RangoCosto(600, 2500)),
            Map.entry("Desengrasante", new RangoCosto(4000, 12000)),
            Map.entry("Limpiacontactos", new RangoCosto(1800, 3500)),
            Map.entry("Limpiador de frenos", new RangoCosto(1500, 3500)),
            Map.entry("Silicona", new RangoCosto(1000, 2500)),
            Map.entry("Pegamento", new RangoCosto(1200, 3500)),
            Map.entry("Sellador de juntas", new RangoCosto(1500, 3500)));

    /** Cantidades por reposición, expresadas en la unidad base del producto. */
    public static final Map<String, RangoCantidad> CANTIDADES_ENTRADA_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", new RangoCantidad(6, 24)),
            Map.entry("Aceite de transmisión", new RangoCantidad(6, 24)),
            Map.entry("Aceite hidráulico", new RangoCantidad(2, 8)),
            Map.entry("Refrigerante", new RangoCantidad(6, 24)),
            Map.entry("Líquido de frenos", new RangoCantidad(6, 24)),
            Map.entry("Líquido de dirección", new RangoCantidad(6, 18)),
            Map.entry("Grasa multipropósito", new RangoCantidad(6, 24)),
            Map.entry("Grasa de litio", new RangoCantidad(4, 12)),
            Map.entry("Grasa para chasis", new RangoCantidad(1, 4)),
            Map.entry("Filtro de aceite", new RangoCantidad(10, 30)),
            Map.entry("Filtro de aire", new RangoCantidad(6, 20)),
            Map.entry("Filtro de combustible", new RangoCantidad(5, 15)),
            Map.entry("Filtro de habitáculo", new RangoCantidad(5, 15)),
            Map.entry("Pastillas de freno", new RangoCantidad(8, 40)),
            Map.entry("Zapatas de freno", new RangoCantidad(8, 24)),
            Map.entry("Discos de freno", new RangoCantidad(4, 16)),
            Map.entry("Fusible", new RangoCantidad(50, 300)),
            Map.entry("Relé", new RangoCantidad(10, 40)),
            Map.entry("Bombilla", new RangoCantidad(10, 50)),
            Map.entry("Perno", new RangoCantidad(200, 1000)),
            Map.entry("Tuerca", new RangoCantidad(200, 1000)),
            Map.entry("Arandela", new RangoCantidad(300, 2000)),
            Map.entry("Lija", new RangoCantidad(20, 100)),
            Map.entry("Disco de corte", new RangoCantidad(10, 50)),
            Map.entry("Disco de desbaste", new RangoCantidad(10, 40)),
            Map.entry("Desengrasante", new RangoCantidad(4, 12)),
            Map.entry("Limpiacontactos", new RangoCantidad(12, 48)),
            Map.entry("Limpiador de frenos", new RangoCantidad(12, 48)),
            Map.entry("Silicona", new RangoCantidad(12, 48)),
            Map.entry("Pegamento", new RangoCantidad(6, 24)),
            Map.entry("Sellador de juntas", new RangoCantidad(6, 24)));

    /** Peso relativo de reposición: los artículos de alta rotación aparecen más a menudo. */
    public static final Map<String, Integer> PESO_ROTACION_POR_TIPO = Map.ofEntries(
            Map.entry("Aceite de motor", 8), Map.entry("Filtro de aceite", 8),
            Map.entry("Filtro de aire", 6), Map.entry("Limpiador de frenos", 5),
            Map.entry("Líquido de frenos", 4), Map.entry("Pastillas de freno", 4),
            Map.entry("Fusible", 4), Map.entry("Perno", 3),
            Map.entry("Tuerca", 3), Map.entry("Arandela", 3),
            Map.entry("Grasa multipropósito", 3), Map.entry("Discos de freno", 1));

    public static final List<String> VEHICULOS_COMUNES = List.of(
            "Toyota Yaris", "Toyota Corolla", "Toyota Hilux", "Toyota RAV4",
            "Nissan Sentra", "Nissan Versa", "Nissan Frontier", "Hyundai Accent",
            "Hyundai Tucson", "Kia Rio", "Kia Sportage", "Chevrolet Onix",
            "Chevrolet Tracker", "Suzuki Swift", "Suzuki Vitara", "Mitsubishi L200",
            "Renault Duster", "Volkswagen Gol", "Volkswagen Virtus", "Kia Soluto");

    public static final List<ServicioDemo> SERVICIOS = List.of(
            new ServicioDemo("Mantenimiento preventivo",
                    List.of("Aceite de motor", "Filtro de aceite"),
                    List.of("Filtro de aire", "Filtro de combustible", "Grasa multipropósito")),
            new ServicioDemo("Cambio de aceite",
                    List.of("Aceite de motor", "Filtro de aceite"),
                    List.of("Desengrasante")),
            new ServicioDemo("Cambio de frenos",
                    List.of("Pastillas de freno"),
                    List.of("Discos de freno", "Líquido de frenos", "Limpiador de frenos", "Grasa de litio")),
            new ServicioDemo("Reparación de transmisión",
                    List.of("Aceite de transmisión"),
                    List.of("Grasa de litio", "Sellador de juntas")),
            new ServicioDemo("Cambio de refrigerante",
                    List.of("Refrigerante"),
                    List.of("Sellador de juntas", "Perno")),
            new ServicioDemo("Revisión de dirección hidráulica",
                    List.of("Líquido de dirección", "Aceite hidráulico"),
                    List.of("Sellador de juntas")),
            new ServicioDemo("Diagnóstico eléctrico",
                    List.of("Fusible", "Bombilla"),
                    List.of("Relé", "Limpiacontactos")),
            new ServicioDemo("Reparación de carrocería",
                    List.of("Lija", "Pegamento"),
                    List.of("Disco de desbaste", "Disco de corte", "Silicona", "Perno")),
            new ServicioDemo("Ajuste y fijación de componentes",
                    List.of("Perno", "Tuerca", "Arandela"),
                    List.of("Grasa para chasis")),
            new ServicioDemo("Limpieza de inyectores",
                    List.of("Desengrasante"),
                    List.of("Filtro de combustible", "Limpiacontactos")),
            new ServicioDemo("Cambio de filtros",
                    List.of("Filtro de aire", "Filtro de habitáculo"),
                    List.of("Filtro de combustible")));

    public static final List<String> FINALIDADES = SERVICIOS.stream()
            .map(ServicioDemo::finalidad)
            .toList();

    public static final Map<String, List<String>> CATEGORIAS_POR_FINALIDAD = Map.ofEntries(
            Map.entry("Mantenimiento preventivo",
                    List.of("Lubricantes y aceites", "Filtros", "Grasas")),
            Map.entry("Cambio de aceite",
                    List.of("Lubricantes y aceites", "Filtros", "Limpieza y mantenimiento")),
            Map.entry("Cambio de frenos",
                    List.of("Frenos", "Refrigerantes y líquidos", "Limpieza y mantenimiento", "Grasas")),
            Map.entry("Reparación de transmisión",
                    List.of("Lubricantes y aceites", "Grasas", "Adhesivos y selladores")),
            Map.entry("Cambio de refrigerante",
                    List.of("Refrigerantes y líquidos", "Adhesivos y selladores", "Tornillería y fijaciones")),
            Map.entry("Revisión de dirección hidráulica",
                    List.of("Refrigerantes y líquidos", "Lubricantes y aceites", "Adhesivos y selladores")),
            Map.entry("Diagnóstico eléctrico",
                    List.of("Componentes eléctricos", "Limpieza y mantenimiento")),
            Map.entry("Reparación de carrocería",
                    List.of("Abrasivos", "Adhesivos y selladores", "Tornillería y fijaciones")),
            Map.entry("Ajuste y fijación de componentes",
                    List.of("Tornillería y fijaciones", "Grasas")),
            Map.entry("Limpieza de inyectores",
                    List.of("Limpieza y mantenimiento", "Filtros")),
            Map.entry("Cambio de filtros",
                    List.of("Filtros")));

    /** Cantidad consumida por servicio, expresada en la unidad base del producto. */
    public static int cantidadConsumo(String tipo, Faker faker) {
        return switch (tipo) {
            case "Pastillas de freno", "Zapatas de freno" -> 4;
            case "Discos de freno" -> 2;
            case "Fusible", "Lija" -> faker.number().numberBetween(1, 6);
            // Envases grandes (bidón, balde) y desengrasante: un envase entero por servicio como máximo.
            case "Aceite hidráulico", "Grasa para chasis", "Desengrasante" -> 1;
            case "Disco de corte", "Disco de desbaste" -> faker.number().numberBetween(1, 4);
            case "Perno", "Tuerca", "Arandela" -> faker.number().numberBetween(4, 21);
            default -> faker.number().numberBetween(1, 3);
        };
    }

    /** Stock objetivo de un ajuste por inventario físico, escalado al tamaño típico de reposición del tipo. */
    public static int stockObjetivoAjuste(String tipo, Faker faker) {
        RangoCantidad rango = CANTIDADES_ENTRADA_POR_TIPO.get(tipo);
        int maximo = rango == null ? 20 : rango.maximo();
        int bajo = rango == null ? 10 : Math.max(2, rango.minimo());
        return faker.number().numberBetween(1, 100) <= 60
                ? faker.number().numberBetween(0, bajo)
                : faker.number().numberBetween(0, maximo * 2);
    }

    public static int cantidadEntrada(String tipo, Faker faker) {
        RangoCantidad rango = CANTIDADES_ENTRADA_POR_TIPO.get(tipo);
        return rango == null
                ? faker.number().numberBetween(5, 20)
                : faker.number().numberBetween(rango.minimo(), rango.maximo());
    }
}
