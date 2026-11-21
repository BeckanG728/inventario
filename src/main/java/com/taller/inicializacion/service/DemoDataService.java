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

        List<ProductoDemo> productosDemo = new ArrayList<>(props.getProductos());
        for (int i = 0; i < props.getProductos(); i++) {
            String nombre = generarNombreProducto(faker);
            String unidadMedida = faker.options().option(DemoData.UNIDADES.toArray(String[]::new));
            Integer stockMinimo = faker.number().numberBetween(5, 50);
            String descripcion = truncar(faker.lorem().sentence(10), 500);
            BigDecimal costo = BigDecimal.valueOf(faker.number().numberBetween(500, 50_000), 2);
            Long categoriaId = categoriaIds.get(faker.number().numberBetween(0, categoriaIds.size() - 1));
            Long proveedorId = proveedorIds.get(faker.number().numberBetween(0, proveedorIds.size() - 1));
            productosDemo.add(new ProductoDemo(
                    String.format(Locale.ROOT, "PRD-%06d", i + 1),
                    nombre, descripcion, unidadMedida, stockMinimo, costo, categoriaId, proveedorId));
        }
        List<ProductoCreado> productos = preparacionCatalogo.crearProductos(productosDemo);

        Historial historial = generarHistorial(
                faker, usuarios, productos, razonSocialPorProveedor);

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

    private String generarNombreProducto(Faker faker) {
        String tipo = faker.options().option(DemoData.TIPOS_PRODUCTO.toArray(String[]::new));
        String marca = faker.vehicle().manufacturer();
        String modelo = faker.vehicle().model();
        return truncar(tipo + " " + marca + " " + modelo, 150);
    }

    private Historial generarHistorial(Faker faker, UsuariosDemo usuarios,
                                       List<ProductoCreado> productosCreados,
                                       Map<Long, String> razonSocialPorProveedor) {
        Instant ahora = Instant.now(clock);
        Instant inicioEntradas = ahora.minus(540, ChronoUnit.DAYS);
        Instant inicioConsumos = ahora.minus(330, ChronoUnit.DAYS);
        Instant finEntradas = inicioConsumos.minus(1, ChronoUnit.DAYS);

        Map<Long, Integer> stock = new HashMap<>();
        for (ProductoCreado producto : productosCreados) {
            stock.put(producto.id(), 0);
        }

        List<MovimientoDemo> movimientos = new ArrayList<>();
        int totalEntradas = generarEntradas(faker, usuarios.jefe(), productosCreados,
                razonSocialPorProveedor, stock, movimientos, inicioEntradas, finEntradas);

        List<EventoHistorial> eventos = new ArrayList<>();
        generarConsumos(faker, usuarios, productosCreados, inicioConsumos, ahora, eventos);
        generarAjustes(faker, productosCreados, ahora.minus(90, ChronoUnit.DAYS), ahora, eventos);
        eventos.sort(Comparator.comparing(EventoHistorial::fecha));

        int totalConsumos = 0;
        int totalAjustes = 0;
        for (EventoHistorial evento : eventos) {
            if (evento instanceof EventoConsumo consumo) {
                totalConsumos += procesarConsumo(consumo, stock, movimientos);
            } else if (evento instanceof EventoAjuste ajuste) {
                totalAjustes += procesarAjuste(ajuste, usuarios.jefe(), stock, movimientos);
            }
        }

        preparacionCatalogo.aplicarEstado(estadosFinales(faker, productosCreados, stock));
        preparacionInventario.guardarMovimientos(movimientos);

        return new Historial(totalEntradas, totalConsumos, totalAjustes, movimientos.size());
    }

    private int generarEntradas(Faker faker, UsuarioDemo jefe, List<ProductoCreado> productosCreados,
                                Map<Long, String> razonSocialPorProveedor, Map<Long, Integer> stock,
                                List<MovimientoDemo> movimientos, Instant desde, Instant hasta) {
        List<EntradaProgramada> programadas = new ArrayList<>(props.getEntradas());
        int base = Math.min(props.getEntradas(), productosCreados.size());
        for (int i = 0; i < base; i++) {
            programadas.add(new EntradaProgramada(
                    productosCreados.get(i),
                    fechaAleatoria(faker, desde, hasta),
                    faker.number().numberBetween(20, 100)));
        }
        for (int i = base; i < props.getEntradas(); i++) {
            programadas.add(new EntradaProgramada(
                    productosCreados.get(faker.number().numberBetween(0, productosCreados.size() - 1)),
                    fechaAleatoria(faker, desde, hasta),
                    faker.number().numberBetween(20, 100)));
        }
        programadas.sort(Comparator.comparing(EntradaProgramada::fecha));

        for (EntradaProgramada entrada : programadas) {
            ProductoCreado producto = entrada.producto();
            int anterior = stock.get(producto.id());
            int posterior = anterior + entrada.cantidad();
            movimientos.add(new MovimientoDemo(
                    TipoMovimiento.ENTRADA,
                    entrada.fecha(),
                    producto.id(),
                    jefe.id(),
                    entrada.cantidad(),
                    entrada.cantidad(),
                    anterior,
                    posterior,
                    producto.costoUltimaCompra(),
                    "Entrada de proveedor " + razonSocialPorProveedor.get(producto.proveedorId()),
                    null));
            stock.put(producto.id(), posterior);
        }
        return programadas.size();
    }

    private void generarConsumos(Faker faker, UsuariosDemo usuarios,
                                 List<ProductoCreado> productosCreados, Instant desde, Instant hasta,
                                 List<EventoHistorial> eventos) {
        for (int i = 0; i < props.getConsumos(); i++) {
            Instant fecha = fechaAleatoria(faker, desde, hasta);
            UsuarioDemo usuario = elegirUsuarioConsumo(faker, usuarios);
            int cantidadItems = faker.number().numberBetween(2, 4);
            List<ItemConsumo> items = new ArrayList<>(cantidadItems);
            Set<Long> usados = new HashSet<>();
            while (items.size() < cantidadItems && usados.size() < productosCreados.size()) {
                ProductoCreado producto = productosCreados.get(
                        faker.number().numberBetween(0, productosCreados.size() - 1));
                if (!usados.add(producto.id())) {
                    continue;
                }
                items.add(new ItemConsumo(producto, faker.number().numberBetween(1, 5)));
            }
            eventos.add(new EventoConsumo(
                    fecha,
                    faker.options().option(DemoData.FINALIDADES.toArray(String[]::new)),
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
                                Instant desde, Instant hasta, List<EventoHistorial> eventos) {
        for (int i = 0; i < props.getAjustes(); i++) {
            ProductoCreado producto = productosCreados.get(
                    faker.number().numberBetween(0, productosCreados.size() - 1));
            int objetivo = faker.number().numberBetween(1, 100) <= 60
                    ? faker.number().numberBetween(0, 10)
                    : faker.number().numberBetween(0, 200);
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
                    null,
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
                null,
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

    private record EntradaProgramada(ProductoCreado producto, Instant fecha, int cantidad) {
    }

    private record ItemConsumo(ProductoCreado producto, int cantidad) {
    }

    private sealed interface EventoHistorial permits EventoConsumo, EventoAjuste {
        Instant fecha();
    }

    private record EventoConsumo(Instant fecha, String finalidad, String referencia,
                                 UsuarioDemo usuario, List<ItemConsumo> items) implements EventoHistorial {
    }

    private record EventoAjuste(Instant fecha, ProductoCreado producto, int stockObjetivo,
                                String motivo) implements EventoHistorial {
    }
}
