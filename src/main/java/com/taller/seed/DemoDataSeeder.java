package com.taller.seed;

import com.taller.auth.entity.Rol;
import com.taller.auth.entity.Usuario;
import com.taller.auth.repository.RolRepository;
import com.taller.auth.repository.UsuarioRepository;
import com.taller.catalogo.entity.Categoria;
import com.taller.catalogo.entity.Proveedor;
import com.taller.catalogo.repository.CategoriaRepository;
import com.taller.catalogo.repository.ProveedorRepository;
import com.taller.consumo.entity.Consumo;
import com.taller.consumo.repository.ConsumoRepository;
import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.movimiento.repository.MovimientoInventarioRepository;
import com.taller.producto.entity.Producto;
import com.taller.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "seed.demo", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements ApplicationRunner {

    private static final List<String> CATEGORIAS_BASE = List.of(
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

    private static final List<String> UNIDADES = List.of(
            "UNIDAD", "CAJA", "LITRO", "GALON", "KILOGRAMO", "METRO", "JUEGO", "PAR");

    private static final List<String> TIPOS_PRODUCTO = List.of(
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

    private static final List<String> FINALIDADES = List.of(
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

    private static final int TAMANO_LOTE = 500;

    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ConsumoRepository consumoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Value("${seed.demo.categorias}")
    private int categorias;

    @Value("${seed.demo.proveedores}")
    private int proveedores;

    @Value("${seed.demo.productos}")
    private int productos;

    @Value("${seed.demo.entradas}")
    private int entradas;

    @Value("${seed.demo.consumos}")
    private int consumos;

    @Value("${seed.demo.ajustes}")
    private int ajustes;

    @Value("${seed.demo.semilla}")
    private long semilla;

    @Value("${seed.demo.password}")
    private String password;

    @Value("${seed.demo.usuarios.jefe}")
    private String usuarioJefe;

    @Value("${seed.demo.usuarios.mecanicos}")
    private String usuariosMecanicos;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validarConfiguracion();

        Faker faker = new Faker(Locale.forLanguageTag("es"), new Random(semilla));

        Usuario jefe = obtenerOCrearUsuario(usuarioJefe, "JEFE_TALLER");
        List<Usuario> mecanicos = Arrays.stream(usuariosMecanicos.split(","))
                .map(String::trim)
                .filter(usuario -> !usuario.isBlank())
                .map(usuario -> obtenerOCrearUsuario(usuario, "MECANICO"))
                .toList();

        log.info("Usuarios demo asegurados: {} (JEFE_TALLER) y {} (MECANICO)",
                jefe.getUsername(), mecanicos.stream().map(Usuario::getUsername).toList());

        if (productoRepository.count() > 0) {
            log.info("Datos demo omitidos: ya existen productos en la base de datos");
            return;
        }

        List<Categoria> categoriasCreadas = crearCategorias(faker);
        List<Proveedor> proveedoresCreados = crearProveedores(faker);
        List<Producto> productosCreados = crearProductos(faker, categoriasCreadas, proveedoresCreados);

        Historial historial = generarHistorial(faker, jefe, mecanicos, productosCreados);

        log.info("Datos demo generados: {} categorías, {} proveedores, {} productos, {} entradas, "
                 + "{} consumos, {} ajustes, {} movimientos",
                categoriasCreadas.size(), proveedoresCreados.size(), productosCreados.size(),
                historial.entradas(), historial.consumos(), historial.ajustes(), historial.movimientos());
    }

    private void validarConfiguracion() {
        if (categorias < 1 || proveedores < 1 || productos < 1) {
            throw new IllegalStateException("FATAL: SEED_DEMO_CATEGORIAS, SEED_DEMO_PROVEEDORES "
                                            + "y SEED_DEMO_PRODUCTOS deben ser mayores que cero");
        }
        if (entradas < 1 || consumos < 1 || ajustes < 0) {
            throw new IllegalStateException("FATAL: SEED_DEMO_ENTRADAS y SEED_DEMO_CONSUMOS deben ser "
                                            + "mayores que cero y SEED_DEMO_AJUSTES no puede ser negativo");
        }
        if (usuarioJefe == null || usuarioJefe.isBlank()) {
            throw new IllegalStateException("FATAL: SEED_DEMO_JEFE es obligatorio");
        }
        if (usuariosMecanicos == null
            || Arrays.stream(usuariosMecanicos.split(",")).allMatch(String::isBlank)) {
            throw new IllegalStateException("FATAL: SEED_DEMO_MECANICOS debe contener al menos un usuario");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("FATAL: SEED_DEMO_PASSWORD es obligatorio");
        }
    }

    private Usuario obtenerOCrearUsuario(String username, String rolNombre) {
        String nombre = username.trim();
        return usuarioRepository.findByUsername(nombre)
                .orElseGet(() -> {
                    Rol rol = rolRepository.findByNombre(rolNombre)
                            .orElseThrow(() -> new IllegalStateException(
                                    "FATAL: el rol " + rolNombre + " no existe, revisar migración V1"));
                    return usuarioRepository.save(
                            new Usuario(nombre, passwordEncoder.encode(password), rol));
                });
    }

    private List<Categoria> crearCategorias(Faker faker) {
        List<Categoria> creadas = new ArrayList<>(categorias);
        for (int i = 0; i < categorias; i++) {
            String nombre = i < CATEGORIAS_BASE.size()
                    ? CATEGORIAS_BASE.get(i)
                    : CATEGORIAS_BASE.get(i % CATEGORIAS_BASE.size()) + " " + (i / CATEGORIAS_BASE.size() + 1);
            creadas.add(categoriaRepository.save(new Categoria(
                    nombreCategoriaUnico(nombre),
                    truncar(faker.lorem().sentence(6), 255))));
        }
        return creadas;
    }

    private String nombreCategoriaUnico(String base) {
        String nombre = base;
        int sufijo = 1;
        while (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            nombre = base + " " + sufijo++;
        }
        return nombre;
    }

    private List<Proveedor> crearProveedores(Faker faker) {
        List<Proveedor> creados = new ArrayList<>(proveedores);
        int contador = 1;
        for (int i = 0; i < proveedores; i++) {
            String ruc;
            do {
                ruc = String.format(Locale.ROOT, "20%09d", contador++);
            } while (proveedorRepository.existsByRuc(ruc));

            creados.add(proveedorRepository.save(new Proveedor(
                    ruc,
                    truncar(faker.company().name() + " S.A.C.", 150),
                    truncar(faker.name().fullName(), 100),
                    truncar(faker.phoneNumber().phoneNumber(), 30),
                    truncar(faker.internet().emailAddress(), 150),
                    truncar(faker.address().fullAddress(), 255))));
        }
        return creados;
    }

    private List<Producto> crearProductos(Faker faker, List<Categoria> categoriasCreadas,
                                          List<Proveedor> proveedoresCreados) {
        List<Producto> nuevos = new ArrayList<>(productos);
        for (int i = 0; i < productos; i++) {
            Producto producto = new Producto(
                    String.format(Locale.ROOT, "PRD-%06d", i + 1),
                    generarNombreProducto(faker),
                    faker.options().option(UNIDADES.toArray(String[]::new)),
                    faker.number().numberBetween(5, 50),
                    categoriasCreadas.get(faker.number().numberBetween(0, categoriasCreadas.size() - 1)),
                    proveedoresCreados.get(faker.number().numberBetween(0, proveedoresCreados.size() - 1)));
            producto.setDescripcion(truncar(faker.lorem().sentence(10), 500));
            producto.setCostoUltimaCompra(BigDecimal.valueOf(faker.number().numberBetween(500, 50_000), 2));
            nuevos.add(producto);
        }

        List<Producto> guardados = productoRepository.saveAll(nuevos);
        for (Producto producto : guardados) {
            producto.setSku(String.format(Locale.ROOT, "CP-%06d", producto.getId()));
        }
        return guardados;
    }

    private String generarNombreProducto(Faker faker) {
        String tipo = faker.options().option(TIPOS_PRODUCTO.toArray(String[]::new));
        String marca = faker.vehicle().manufacturer();
        String modelo = faker.vehicle().model();
        return truncar(tipo + " " + marca + " " + modelo, 150);
    }

    private Historial generarHistorial(Faker faker, Usuario jefe, List<Usuario> mecanicos,
                                       List<Producto> productosCreados) {
        Instant ahora = Instant.now(clock);
        Instant inicioEntradas = ahora.minus(540, ChronoUnit.DAYS);
        Instant inicioConsumos = ahora.minus(330, ChronoUnit.DAYS);
        Instant finEntradas = inicioConsumos.minus(1, ChronoUnit.DAYS);

        Map<Long, Integer> stock = new HashMap<>();
        for (Producto producto : productosCreados) {
            stock.put(producto.getId(), 0);
        }

        List<MovimientoInventario> movimientos = new ArrayList<>();
        int totalEntradas = generarEntradas(faker, jefe, productosCreados, stock, movimientos,
                inicioEntradas, finEntradas);

        List<EventoHistorial> eventos = new ArrayList<>();
        generarConsumos(faker, jefe, mecanicos, productosCreados, inicioConsumos, ahora, eventos);
        generarAjustes(faker, productosCreados, ahora.minus(90, ChronoUnit.DAYS), ahora, eventos);
        eventos.sort(Comparator.comparing(EventoHistorial::fecha));

        int totalConsumos = 0;
        int totalAjustes = 0;
        for (EventoHistorial evento : eventos) {
            if (evento instanceof EventoConsumo consumo) {
                totalConsumos += procesarConsumo(consumo, stock, movimientos);
            } else if (evento instanceof EventoAjuste ajuste) {
                totalAjustes += procesarAjuste(ajuste, jefe, stock, movimientos);
            }
        }

        for (Producto producto : productosCreados) {
            producto.setStockActual(stock.get(producto.getId()));
        }

        desactivarProductos(productosCreados);
        ajustarStockMinimo(faker, productosCreados, stock);

        guardarMovimientos(movimientos);
        productoRepository.flush();

        return new Historial(totalEntradas, totalConsumos, totalAjustes, movimientos.size());
    }

    private int generarEntradas(Faker faker, Usuario jefe, List<Producto> productosCreados,
                                Map<Long, Integer> stock, List<MovimientoInventario> movimientos,
                                Instant desde, Instant hasta) {
        List<EntradaProgramada> programadas = new ArrayList<>(entradas);
        int base = Math.min(entradas, productosCreados.size());
        for (int i = 0; i < base; i++) {
            programadas.add(new EntradaProgramada(
                    productosCreados.get(i),
                    fechaAleatoria(faker, desde, hasta),
                    faker.number().numberBetween(20, 100)));
        }
        for (int i = base; i < entradas; i++) {
            programadas.add(new EntradaProgramada(
                    productosCreados.get(faker.number().numberBetween(0, productosCreados.size() - 1)),
                    fechaAleatoria(faker, desde, hasta),
                    faker.number().numberBetween(20, 100)));
        }
        programadas.sort(Comparator.comparing(EntradaProgramada::fecha));

        for (EntradaProgramada entrada : programadas) {
            Producto producto = entrada.producto();
            int anterior = stock.get(producto.getId());
            int posterior = anterior + entrada.cantidad();
            MovimientoInventario movimiento = new MovimientoInventario(
                    TipoMovimiento.ENTRADA,
                    producto,
                    jefe,
                    anterior,
                    posterior,
                    entrada.cantidad(),
                    "Entrada de proveedor " + producto.getProveedor().getRazonSocial());
            movimiento.setFecha(entrada.fecha());
            movimiento.setCantidad(entrada.cantidad());
            movimiento.setCostoUnitario(producto.getCostoUltimaCompra());
            movimientos.add(movimiento);
            stock.put(producto.getId(), posterior);
        }
        return programadas.size();
    }

    private void generarConsumos(Faker faker, Usuario jefe, List<Usuario> mecanicos,
                                 List<Producto> productosCreados, Instant desde, Instant hasta,
                                 List<EventoHistorial> eventos) {
        for (int i = 0; i < consumos; i++) {
            Instant fecha = fechaAleatoria(faker, desde, hasta);
            Usuario usuario = elegirUsuarioConsumo(faker, jefe, mecanicos);
            int cantidadItems = faker.number().numberBetween(2, 4);
            List<ItemConsumo> items = new ArrayList<>(cantidadItems);
            Set<Long> usados = new HashSet<>();
            while (items.size() < cantidadItems && usados.size() < productosCreados.size()) {
                Producto producto = productosCreados.get(
                        faker.number().numberBetween(0, productosCreados.size() - 1));
                if (!usados.add(producto.getId())) {
                    continue;
                }
                items.add(new ItemConsumo(producto, faker.number().numberBetween(1, 5)));
            }
            eventos.add(new EventoConsumo(
                    fecha,
                    faker.options().option(FINALIDADES.toArray(String[]::new)),
                    "Placa " + faker.number().digits(3) + "-" + faker.number().digits(3),
                    usuario,
                    items));
        }
    }

    private Usuario elegirUsuarioConsumo(Faker faker, Usuario jefe, List<Usuario> mecanicos) {
        if (mecanicos.isEmpty() || faker.number().numberBetween(1, 100) <= 10) {
            return jefe;
        }
        return mecanicos.get(faker.number().numberBetween(0, mecanicos.size() - 1));
    }

    private void generarAjustes(Faker faker, List<Producto> productosCreados,
                                Instant desde, Instant hasta, List<EventoHistorial> eventos) {
        for (int i = 0; i < ajustes; i++) {
            Producto producto = productosCreados.get(
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
                                List<MovimientoInventario> movimientos) {
        List<ItemConsumo> aplicables = new ArrayList<>(evento.items().size());
        for (ItemConsumo item : evento.items()) {
            int disponible = stock.get(item.producto().getId());
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

        Consumo consumo = consumoRepository.save(new Consumo(
                evento.fecha(), evento.finalidad(), evento.referencia(), evento.usuario()));

        for (ItemConsumo item : aplicables) {
            Producto producto = item.producto();
            int anterior = stock.get(producto.getId());
            int posterior = anterior - item.cantidad();
            MovimientoInventario movimiento = new MovimientoInventario(
                    TipoMovimiento.CONSUMO,
                    producto,
                    evento.usuario(),
                    anterior,
                    posterior,
                    null,
                    evento.finalidad());
            movimiento.setFecha(evento.fecha());
            movimiento.setCantidad(item.cantidad());
            movimiento.setConsumoId(consumo.getId());
            movimientos.add(movimiento);
            stock.put(producto.getId(), posterior);
        }
        return 1;
    }

    private int procesarAjuste(EventoAjuste evento, Usuario jefe, Map<Long, Integer> stock,
                               List<MovimientoInventario> movimientos) {
        Producto producto = evento.producto();
        int anterior = stock.get(producto.getId());
        int posterior = evento.stockObjetivo();
        if (anterior == posterior) {
            posterior = anterior == 0 ? 1 : anterior - 1;
        }
        MovimientoInventario movimiento = new MovimientoInventario(
                TipoMovimiento.AJUSTE,
                producto,
                jefe,
                anterior,
                posterior,
                posterior - anterior,
                evento.motivo());
        movimiento.setFecha(evento.fecha());
        movimientos.add(movimiento);
        stock.put(producto.getId(), posterior);
        return 1;
    }

    private void desactivarProductos(List<Producto> productosCreados) {
        int objetivo = (int) Math.round(productosCreados.size() * 0.10);
        if (objetivo <= 0) {
            return;
        }
        List<Producto> mezclados = new ArrayList<>(productosCreados);
        Collections.shuffle(mezclados, new Random(semilla + 1));
        for (int i = 0; i < objetivo; i++) {
            mezclados.get(i).setActivo(false);
        }
    }

    private void ajustarStockMinimo(Faker faker, List<Producto> productosCreados,
                                    Map<Long, Integer> stock) {
        List<Producto> activos = productosCreados.stream()
                .filter(Producto::isActivo)
                .collect(Collectors.toCollection(ArrayList::new));
        int objetivo = (int) Math.round(activos.size() * 0.15);
        if (objetivo <= 0) {
            return;
        }
        Collections.shuffle(activos, new Random(semilla + 2));
        for (int i = 0; i < objetivo; i++) {
            Producto producto = activos.get(i);
            int actual = stock.get(producto.getId());
            producto.setStockMinimo(actual + faker.number().numberBetween(1, 20));
        }
    }

    private void guardarMovimientos(List<MovimientoInventario> movimientos) {
        for (int i = 0; i < movimientos.size(); i += TAMANO_LOTE) {
            movimientoRepository.saveAll(
                    movimientos.subList(i, Math.min(i + TAMANO_LOTE, movimientos.size())));
        }
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

    private record EntradaProgramada(Producto producto, Instant fecha, int cantidad) {
    }

    private record ItemConsumo(Producto producto, int cantidad) {
    }

    private sealed interface EventoHistorial permits EventoConsumo, EventoAjuste {
        Instant fecha();
    }

    private record EventoConsumo(Instant fecha, String finalidad, String referencia,
                                 Usuario usuario, List<ItemConsumo> items) implements EventoHistorial {
    }

    private record EventoAjuste(Instant fecha, Producto producto, int stockObjetivo,
                                String motivo) implements EventoHistorial {
    }
}
