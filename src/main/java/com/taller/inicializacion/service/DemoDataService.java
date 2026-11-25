package com.taller.inicializacion.service;

import com.taller.catalogo.api.PreparacionCatalogo;
import com.taller.catalogo.api.PreparacionCatalogo.CategoriaDemo;
import com.taller.catalogo.api.PreparacionCatalogo.EstadoProductoDemo;
import com.taller.catalogo.api.PreparacionCatalogo.ProductoCreado;
import com.taller.catalogo.api.PreparacionCatalogo.ProductoDemo;
import com.taller.catalogo.api.PreparacionCatalogo.ProveedorDemo;
import com.taller.inventario.api.PreparacionInventario;
import com.taller.inventario.api.PreparacionInventario.ConsumoDemo;
import com.taller.inventario.api.PreparacionInventario.MovimientoDemo;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inicializacion.config.DemoSeedProperties;
import com.taller.inicializacion.data.DemoData;
import com.taller.seguridad.api.PreparacionSeguridad;
import com.taller.seguridad.api.PreparacionSeguridad.UsuarioDemo;
import com.taller.seguridad.api.PreparacionSeguridad.UsuariosDemo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orquesta el perfil {@code dev}: decide qué datos generar y delega la persistencia en los
 * contratos de preparación de cada dominio. No crea entidades JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataService {

    private final PreparacionSeguridad preparacionSeguridad;
    private final PreparacionCatalogo preparacionCatalogo;
    private final PreparacionInventario preparacionInventario;
    private final DemoSeedProperties props;
    private final Clock clock;

    @Transactional
    public void run(ApplicationArguments args) {
        validarConfiguracion();

        Faker faker = new Faker(Locale.forLanguageTag("es"), new Random(props.getSemilla()));

        List<String> mecanicos = Arrays.stream(props.getUsuarios().getMecanicos().split(","))
                .map(String::trim)
                .filter(usuario -> !usuario.isBlank())
                .toList();
        UsuariosDemo usuarios = preparacionSeguridad.asegurarUsuariosDemo(
                props.getUsuarios().getJefe(), mecanicos, props.getPassword());

        log.info("Usuarios demo asegurados: {} (JEFE_TALLER) y {} (MECANICO)",
                usuarios.jefe().username(),
                usuarios.mecanicos().stream().map(UsuarioDemo::username).toList());

        if (preparacionCatalogo.hayProductos()) {
            log.info("Datos demo omitidos: ya existen productos en la base de datos");
            return;
        }

        List<CategoriaDemo> categoriasDemo = new ArrayList<>(props.getCategorias());
        for (int i = 0; i < props.getCategorias(); i++) {
            String nombre = i < DemoData.CATEGORIAS_BASE.size()
                    ? DemoData.CATEGORIAS_BASE.get(i)
                    : DemoData.CATEGORIAS_BASE.get(i % DemoData.CATEGORIAS_BASE.size()) + " "
                    + (i / DemoData.CATEGORIAS_BASE.size() + 1);
            categoriasDemo.add(new CategoriaDemo(nombre, truncar(faker.lorem().sentence(6), 255)));
        }
        List<Long> categoriaIds = preparacionCatalogo.crearCategorias(categoriasDemo);

        List<ProveedorDemo> proveedoresDemo = new ArrayList<>(props.getProveedores());
        for (int i = 0; i < props.getProveedores(); i++) {
            proveedoresDemo.add(new ProveedorDemo(
                    truncar(faker.company().name() + " S.A.C.", 150),
                    truncar(faker.name().fullName(), 100),
                    truncar(faker.phoneNumber().phoneNumber(), 30),
                    truncar(faker.internet().emailAddress(), 150),
                    truncar(faker.address().fullAddress(), 255)));
        }
        List<Long> proveedorIds = preparacionCatalogo.crearProveedores(proveedoresDemo);

        Map<Long, String> razonSocialPorProveedor = new LinkedHashMap<>();
        for (int i = 0; i < proveedorIds.size(); i++) {
            razonSocialPorProveedor.put(proveedorIds.get(i), proveedoresDemo.get(i).razonSocial());
        }

        Map<String, Long> idPorCategoriaBase = new HashMap<>();
        for (int i = 0; i < categoriaIds.size(); i++) {
            idPorCategoriaBase.put(DemoData.CATEGORIAS_BASE.get(i), categoriaIds.get(i));
        }

        // Las relaciones categoría-unidad vienen de la migración V10; el demo no las crea.
        Map<String, List<String>> tiposPorCategoria = new HashMap<>();
        for (String tipo : DemoData.TIPOS_PRODUCTO) {
            String nombreCategoria = DemoData.CATEGORIA_POR_TIPO.get(tipo);
            if (nombreCategoria != null && idPorCategoriaBase.containsKey(nombreCategoria)) {
                tiposPorCategoria.computeIfAbsent(nombreCategoria, clave -> new ArrayList<>()).add(tipo);
            }
        }
        List<String> tiposPlanificados = planificarTipos(
                faker, tiposPorCategoria, DemoData.CATEGORIAS_BASE.subList(0, categoriaIds.size()));

        List<ProductoDemo> productosDemo = new ArrayList<>(props.getProductos());
        for (int i = 0; i < props.getProductos(); i++) {
            String tipo = tiposPlanificados.get(i);
            List<DemoData.UnidadDemo> opcionesUnidad = DemoData.UNIDADES_POR_TIPO.get(tipo);
            DemoData.RangoCosto rangoCosto = DemoData.COSTOS_POR_TIPO.get(tipo);
            if (opcionesUnidad == null || opcionesUnidad.isEmpty() || rangoCosto == null) {
                throw new IllegalStateException("FATAL: catálogo demo incompleto para el tipo " + tipo);
            }
            DemoData.UnidadDemo unidadDemo = opcionesUnidad.get(
                    faker.number().numberBetween(0, opcionesUnidad.size()));
            // El stock mínimo se expresa en la unidad base, así que se escala con el tamaño típico de reposición.
            DemoData.RangoCantidad rangoEntrada = DemoData.CANTIDADES_ENTRADA_POR_TIPO.get(tipo);
            int minimoBase = rangoEntrada == null ? 5 : Math.max(1, rangoEntrada.minimo() / 2);
            Integer stockMinimo = faker.number().numberBetween(minimoBase, minimoBase * 2 + 1);
            String descripcion = truncar(faker.lorem().sentence(10), 500);
            BigDecimal costo = BigDecimal.valueOf(faker.number().numberBetween(
                    (int) rangoCosto.minimoCentimos(), (int) rangoCosto.maximoCentimos()), 2);
            Long categoriaId = idPorCategoriaBase.get(DemoData.CATEGORIA_POR_TIPO.get(tipo));
            if (categoriaId == null) {
                throw new IllegalStateException(
                        "FATAL: la categoría del tipo " + tipo + " no está configurada en el seed");
            }
            Long proveedorId = proveedorIds.get(faker.number().numberBetween(0, proveedorIds.size() - 1));
            productosDemo.add(new ProductoDemo(
                    generarNombreProducto(faker, tipo), descripcion,
                    stockMinimo, costo, categoriaId, proveedorId,
                    unidadDemo.base(), unidadDemo.presentacion(), unidadDemo.factor()));
        }
        List<ProductoCreado> productos = preparacionCatalogo.crearProductos(productosDemo);

        Map<Long, String> tipoPorProducto = new HashMap<>();
        for (int i = 0; i < productos.size(); i++) {
            tipoPorProducto.put(productos.get(i).id(), tiposPlanificados.get(i));
        }

        Historial historial = generarHistorial(
                faker, usuarios, productos, razonSocialPorProveedor, tipoPorProducto);

        log.info("Datos demo generados: {} categorías, {} proveedores, {} productos, {} entradas, "
                        + "{} consumos, {} ajustes, {} movimientos",
                categoriaIds.size(), proveedorIds.size(), productos.size(),
                historial.entradas(), historial.consumos(), historial.ajustes(), historial.movimientos());
    }

    private void validarConfiguracion() {
        if (props.getCategorias() < 1 || props.getProveedores() < 1 || props.getProductos() < 1) {
            throw new IllegalStateException("FATAL: SEED_DEMO_CATEGORIAS, SEED_DEMO_PROVEEDORES "
                    + "y SEED_DEMO_PRODUCTOS deben ser mayores que cero");
        }
        if (props.getEntradas() < 1 || props.getConsumos() < 1 || props.getAjustes() < 0) {
            throw new IllegalStateException("FATAL: SEED_DEMO_ENTRADAS y SEED_DEMO_CONSUMOS deben ser "
                    + "mayores que cero y SEED_DEMO_AJUSTES no puede ser negativo");
        }
        if (props.getCategorias() > DemoData.CATEGORIAS_BASE.size()) {
            throw new IllegalStateException("FATAL: SEED_DEMO_CATEGORIAS no puede superar "
                    + DemoData.CATEGORIAS_BASE.size());
        }
        if (props.getProductos() < 2 * props.getCategorias()) {
            throw new IllegalStateException("FATAL: SEED_DEMO_PRODUCTOS debe ser al menos el doble de "
                    + "SEED_DEMO_CATEGORIAS para garantizar dos productos por categoría");
        }
        if (props.getUsuarios().getJefe() == null || props.getUsuarios().getJefe().isBlank()) {
            throw new IllegalStateException("FATAL: SEED_DEMO_JEFE es obligatorio");
        }
        if (props.getUsuarios().getMecanicos() == null
                || Arrays.stream(props.getUsuarios().getMecanicos().split(",")).allMatch(String::isBlank)) {
            throw new IllegalStateException("FATAL: SEED_DEMO_MECANICOS debe contener al menos un usuario");
        }
        if (props.getPassword() == null || props.getPassword().isBlank()) {
            throw new IllegalStateException("FATAL: SEED_DEMO_PASSWORD es obligatorio");
        }
    }

    /** Garantiza al menos dos tipos por categoría antes de completar con tipos aleatorios. */
    private List<String> planificarTipos(Faker faker, Map<String, List<String>> tiposPorCategoria,
                                         List<String> categorias) {
        List<String> planificados = new ArrayList<>(props.getProductos());
        for (String categoria : categorias) {
            List<String> tipos = tiposPorCategoria.get(categoria);
            if (tipos == null || tipos.isEmpty()) {
                throw new IllegalStateException("FATAL: no hay tipos de producto para la categoría " + categoria);
            }
            for (int j = 0; j < 2; j++) {
                planificados.add(tipos.get(faker.number().numberBetween(0, tipos.size())));
            }
        }
        // Solo tipos cuyas categorías están configuradas; si no, el id de categoría sería nulo.
        List<String> todos = tiposPorCategoria.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        while (planificados.size() < props.getProductos()) {
            planificados.add(faker.options().option(todos.toArray(String[]::new)));
        }
        Collections.shuffle(planificados, new Random(props.getSemilla() + 3));
        return planificados;
    }

    private String generarNombreProducto(Faker faker, String tipo) {
        String vehiculo = faker.options().option(DemoData.VEHICULOS_COMUNES.toArray(String[]::new));
        return truncar(tipo + " " + vehiculo, 150);
    }

    private Historial generarHistorial(Faker faker, UsuariosDemo usuarios,
                                       List<ProductoCreado> productosCreados,
                                       Map<Long, String> razonSocialPorProveedor,
                                       Map<Long, String> tipoPorProducto) {
        Instant ahora = Instant.now(clock);
        Instant inicioHistorial = ahora.minus(540, ChronoUnit.DAYS);

        Map<Long, Integer> stock = new HashMap<>();
        for (ProductoCreado producto : productosCreados) {
            stock.put(producto.id(), 0);
        }

        // Todas las operaciones se planifican primero y se procesan en orden cronológico,
        // de modo que entradas y salidas se intercalen y los stocks anterior/posterior sean coherentes.
        List<EventoHistorial> eventos = new ArrayList<>();
        int totalEntradas = generarEntradas(faker, productosCreados, razonSocialPorProveedor,
                tipoPorProducto, eventos, inicioHistorial, ahora);
        generarConsumos(faker, usuarios, productosCreados, tipoPorProducto,
                inicioHistorial.plus(30, ChronoUnit.DAYS), ahora, eventos);
        generarAjustes(faker, productosCreados, tipoPorProducto, ahora.minus(90, ChronoUnit.DAYS), ahora, eventos);
        eventos.sort(Comparator.comparing(EventoHistorial::fecha));

        List<MovimientoDemo> movimientos = new ArrayList<>();
        int totalConsumos = 0;
        int totalAjustes = 0;
        for (EventoHistorial evento : eventos) {
            if (evento instanceof EventoEntrada entrada) {
                procesarEntrada(entrada, usuarios.jefe(), stock, movimientos);
            } else if (evento instanceof EventoConsumo consumo) {
                totalConsumos += procesarConsumo(consumo, stock, movimientos);
            } else if (evento instanceof EventoAjuste ajuste) {
                totalAjustes += procesarAjuste(ajuste, usuarios.jefe(), stock, movimientos);
            }
        }

        preparacionCatalogo.aplicarEstado(estadosFinales(faker, productosCreados, stock));
        preparacionInventario.guardarMovimientos(movimientos);

        return new Historial(totalEntradas, totalConsumos, totalAjustes, movimientos.size());
    }

    private int generarEntradas(Faker faker, List<ProductoCreado> productosCreados,
                                Map<Long, String> razonSocialPorProveedor,
                                Map<Long, String> tipoPorProducto, List<EventoHistorial> eventos,
                                Instant desde, Instant hasta) {
        int base = Math.min(props.getEntradas(), productosCreados.size());

        // Apertura: una entrada por producto al inicio de la línea de tiempo para garantizar stock inicial.
        Instant finApertura = desde.plus(60, ChronoUnit.DAYS);
        for (int i = 0; i < base; i++) {
            ProductoCreado producto = productosCreados.get(i);
            eventos.add(new EventoEntrada(
                    fechaAleatoria(faker, desde, finApertura),
                    producto,
                    DemoData.cantidadEntrada(tipoPorProducto.get(producto.id()), faker),
                    "Entrada inicial de " + razonSocialPorProveedor.get(producto.proveedorId())));
        }

        // El resto se reparte a lo largo de todo el periodo, intercalado con consumos y ajustes.
        for (int i = base; i < props.getEntradas(); i++) {
            ProductoCreado producto = productosCreados.get(
                    faker.number().numberBetween(0, productosCreados.size() - 1));
            eventos.add(new EventoEntrada(
                    fechaAleatoria(faker, desde, hasta),
                    producto,
                    DemoData.cantidadEntrada(tipoPorProducto.get(producto.id()), faker),
                    "Entrada de proveedor " + razonSocialPorProveedor.get(producto.proveedorId())));
        }
        return props.getEntradas();
    }

    private void procesarEntrada(EventoEntrada evento, UsuarioDemo jefe,
                                Map<Long, Integer> stock, List<MovimientoDemo> movimientos) {
        ProductoCreado producto = evento.producto();
        int anterior = stock.get(producto.id());
        int posterior = anterior + evento.cantidad();
        movimientos.add(new MovimientoDemo(
                TipoMovimiento.ENTRADA,
                evento.fecha(),
                producto.id(),
                jefe.id(),
                evento.cantidad(),
                evento.cantidad(),
                anterior,
                posterior,
                producto.costoUltimaCompra(),
                evento.motivo(),
                null));
        stock.put(producto.id(), posterior);
    }

    private void generarConsumos(Faker faker, UsuariosDemo usuarios,
                                 List<ProductoCreado> productosCreados,
                                 Map<Long, String> tipoPorProducto, Instant desde, Instant hasta,
                                 List<EventoHistorial> eventos) {
        Map<String, List<ProductoCreado>> productosPorTipo = new HashMap<>();
        for (ProductoCreado producto : productosCreados) {
            String tipo = tipoPorProducto.get(producto.id());
            if (tipo == null) {
                continue;
            }
            productosPorTipo.computeIfAbsent(tipo, clave -> new ArrayList<>()).add(producto);
        }

        DemoData.ServicioDemo[] servicios = DemoData.SERVICIOS.toArray(DemoData.ServicioDemo[]::new);
        for (int i = 0; i < props.getConsumos(); i++) {
            DemoData.ServicioDemo servicio = faker.options().option(servicios);
            Instant fecha = fechaAleatoria(faker, desde, hasta);
            UsuarioDemo usuario = elegirUsuarioConsumo(faker, usuarios);

            List<String> tipos = new ArrayList<>(servicio.productosFrecuentes());
            tipos.addAll(servicio.productosOpcionales());

            List<ItemConsumo> items = new ArrayList<>();
            Set<Long> usados = new HashSet<>();
            for (String tipo : tipos) {
                List<ProductoCreado> candidatos = productosPorTipo.get(tipo);
                if (candidatos == null || candidatos.isEmpty()) {
                    continue;
                }
                ProductoCreado producto = candidatos.get(faker.number().numberBetween(0, candidatos.size()));
                if (!usados.add(producto.id())) {
                    continue;
                }
                items.add(new ItemConsumo(producto, DemoData.cantidadConsumo(tipo, faker)));
            }
            if (items.isEmpty()) {
                continue;
            }
            eventos.add(new EventoConsumo(
                    fecha,
                    servicio.finalidad(),
                    "Placa " + faker.number().digits(3) + "-" + faker.number().digits(3),
                    usuario,
                    items));
        }
    }

    private UsuarioDemo elegirUsuarioConsumo(Faker faker, UsuariosDemo usuarios) {
        if (usuarios.mecanicos().isEmpty() || faker.number().numberBetween(1, 100) <= 10) {
            return usuarios.jefe();
        }
        return usuarios.mecanicos().get(
                faker.number().numberBetween(0, usuarios.mecanicos().size() - 1));
    }

    private void generarAjustes(Faker faker, List<ProductoCreado> productosCreados,
                                Map<Long, String> tipoPorProducto,
                                Instant desde, Instant hasta, List<EventoHistorial> eventos) {
        for (int i = 0; i < props.getAjustes(); i++) {
            ProductoCreado producto = productosCreados.get(
                    faker.number().numberBetween(0, productosCreados.size() - 1));
            int objetivo = DemoData.stockObjetivoAjuste(tipoPorProducto.get(producto.id()), faker);
            eventos.add(new EventoAjuste(
                    fechaAleatoria(faker, desde, hasta),
                    producto,
                    objetivo,
                    "Ajuste por inventario físico"));
        }
    }

    private int procesarConsumo(EventoConsumo evento, Map<Long, Integer> stock,
                                List<MovimientoDemo> movimientos) {
        List<ItemConsumo> aplicables = new ArrayList<>(evento.items().size());
        for (ItemConsumo item : evento.items()) {
            int disponible = stock.get(item.producto().id());
            if (disponible <= 0) {
                continue;
            }
            int cantidad = Math.min(item.cantidad(), disponible);
            if (cantidad > 0) {
                aplicables.add(new ItemConsumo(item.producto(), cantidad));
            }
        }
        if (aplicables.isEmpty()) {
            return 0;
        }

        Long consumoId = preparacionInventario.crearConsumo(new ConsumoDemo(
                evento.fecha(), evento.finalidad(), evento.referencia(), evento.usuario().id()));

        for (ItemConsumo item : aplicables) {
            int anterior = stock.get(item.producto().id());
            int posterior = anterior - item.cantidad();
            movimientos.add(new MovimientoDemo(
                    TipoMovimiento.CONSUMO,
                    evento.fecha(),
                    item.producto().id(),
                    evento.usuario().id(),
                    item.cantidad(),
                    null,
                    anterior,
                    posterior,
                    item.producto().costoUltimaCompra(),
                    evento.finalidad(),
                    consumoId));
            stock.put(item.producto().id(), posterior);
        }
        return 1;
    }

    private int procesarAjuste(EventoAjuste evento, UsuarioDemo jefe, Map<Long, Integer> stock,
                               List<MovimientoDemo> movimientos) {
        ProductoCreado producto = evento.producto();
        int anterior = stock.get(producto.id());
        int posterior = evento.stockObjetivo();
        if (anterior == posterior) {
            posterior = anterior == 0 ? 1 : anterior - 1;
        }
        movimientos.add(new MovimientoDemo(
                TipoMovimiento.AJUSTE,
                evento.fecha(),
                producto.id(),
                jefe.id(),
                null,
                posterior - anterior,
                anterior,
                posterior,
                producto.costoUltimaCompra(),
                evento.motivo(),
                null));
        stock.put(producto.id(), posterior);
        return 1;
    }

    private List<EstadoProductoDemo> estadosFinales(Faker faker, List<ProductoCreado> productosCreados,
                                                    Map<Long, Integer> stock) {
        Set<Long> desactivados = new HashSet<>();
        int objetivoDesactivados = (int) Math.round(productosCreados.size() * 0.10);
        if (objetivoDesactivados > 0) {
            List<ProductoCreado> mezclados = new ArrayList<>(productosCreados);
            Collections.shuffle(mezclados, new Random(props.getSemilla() + 1));
            for (int i = 0; i < objetivoDesactivados; i++) {
                desactivados.add(mezclados.get(i).id());
            }
        }

        Map<Long, Integer> minimosAjustados = new HashMap<>();
        List<ProductoCreado> activos = productosCreados.stream()
                .filter(p -> !desactivados.contains(p.id()))
                .collect(Collectors.toCollection(ArrayList::new));
        int objetivoMinimos = (int) Math.round(activos.size() * 0.15);
        if (objetivoMinimos > 0) {
            Collections.shuffle(activos, new Random(props.getSemilla() + 2));
            for (int i = 0; i < objetivoMinimos; i++) {
                ProductoCreado producto = activos.get(i);
                minimosAjustados.put(producto.id(),
                        stock.get(producto.id()) + faker.number().numberBetween(1, 20));
            }
        }

        return productosCreados.stream()
                .map(p -> new EstadoProductoDemo(
                        p.id(),
                        stock.get(p.id()),
                        minimosAjustados.getOrDefault(p.id(), p.stockMinimo()),
                        !desactivados.contains(p.id())))
                .toList();
    }

    private Instant fechaAleatoria(Faker faker, Instant desde, Instant hasta) {
        long dias = ChronoUnit.DAYS.between(desde, hasta);
        if (dias <= 0) {
            return desde;
        }
        return desde.plus(faker.number().numberBetween(0, (int) dias), ChronoUnit.DAYS);
    }

    private static String truncar(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }

    private record Historial(int entradas, int consumos, int ajustes, int movimientos) {
    }

    private record ItemConsumo(ProductoCreado producto, int cantidad) {
    }

    private sealed interface EventoHistorial permits EventoEntrada, EventoConsumo, EventoAjuste {
        Instant fecha();
    }

    private record EventoEntrada(Instant fecha, ProductoCreado producto, int cantidad,
                                 String motivo) implements EventoHistorial {
    }

    private record EventoConsumo(Instant fecha, String finalidad, String referencia,
                                 UsuarioDemo usuario, List<ItemConsumo> items) implements EventoHistorial {
    }

    private record EventoAjuste(Instant fecha, ProductoCreado producto, int stockObjetivo,
                                String motivo) implements EventoHistorial {
    }
}
