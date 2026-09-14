package com.inventario.excel;

import com.inventario.dto.response.ImportFilaError;
import com.inventario.dto.response.ImportResultResponse;
import com.inventario.entity.CategoriaCatalogo;
import com.inventario.entity.Cliente;
import com.inventario.entity.Equipo;
import com.inventario.entity.EstadoEquipo;
import com.inventario.entity.EstadoServicioTaller;
import com.inventario.entity.Propiedad;
import com.inventario.entity.TipoCliente;
import com.inventario.entity.TipoMotorCatalogo;
import com.inventario.exception.ImportFileException;
import com.inventario.repository.CategoriaCatalogoRepository;
import com.inventario.repository.ClienteRepository;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.TipoMotorCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Importa los dos archivos reales del taller hacia el mismo modelo Equipo:
 * Inventario_Master.xlsx (equipos propios) e Inventario_SERVICIO_TALLER.xlsx
 * (equipos externos de clientes), cada uno con su propio mapeo de columnas.
 * <p>
 * Cada fila se guarda con su propia llamada a save() (sin @Transactional de
 * metodo envolviendo todo el archivo) para que una fila con error no revierta
 * las que ya se importaron bien: importacion tolerante a fallos, con reporte
 * de que fila fallo y por que.
 */
@Service
@RequiredArgsConstructor
public class EquipoExcelImportService {

    // Palabras clave -> nombre de categoria (se resuelve contra el catalogo
    // real en tiempo de importacion), usadas solo para el archivo de
    // Servicio Taller (no trae columna de categoria). Basado en los ejemplos
    // reales de "Maquinaria" que ya traia el dominio (apisonadoras,
    // porta-pallets, plumas grua, motobombas).
    private static final Map<String, String> PALABRAS_CLAVE_CATEGORIA = new LinkedHashMap<>();

    static {
        PALABRAS_CLAVE_CATEGORIA.put("PORTA PALLET", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("MOTOBOMBA", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("MOTO BOMBA", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("PLUMA", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("GRUA", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("APISONADORA", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("COMPACTADORA", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("RETROEXCAVADORA", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("MONTACARGA", "Maquinaria");
        PALABRAS_CLAVE_CATEGORIA.put("CAMIONETA", "Vehículo");
        PALABRAS_CLAVE_CATEGORIA.put("CAMION", "Vehículo");
        PALABRAS_CLAVE_CATEGORIA.put("VEHICULO", "Vehículo");
        PALABRAS_CLAVE_CATEGORIA.put("TRAILER", "Tráiler");
        PALABRAS_CLAVE_CATEGORIA.put("REMOLQUE", "Tráiler");
        PALABRAS_CLAVE_CATEGORIA.put("MULA", "Tráiler");
    }

    private static final Set<String> PALABRAS_CLAVE_EMPRESA = Set.of(
            "SAS", "LTDA", "CIA", "COMPANIA", "SOCIEDAD", "CONSTRUCTORA", "CONSTRUCTORES",
            "FUNDACION", "RESTAURANTE", "ZONA FRANCA", "AGRUPACION", "EMPRESA", "GRUPO",
            "CORPORACION", "ASOCIACION"
    );

    private final EquipoRepository equipoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaCatalogoRepository categoriaCatalogoRepository;
    private final TipoMotorCatalogoRepository tipoMotorCatalogoRepository;

    /** Columnas: Item | Codigo | Descripcion_Equipo | Categoria | Marca | N_Serie |
     *  Ubicacion | Fecha de registro | Estado | Check MTTO | Check H V | Check F.T */
    public ImportResultResponse importarMaster(InputStream inputStream) {
        List<ImportFilaError> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        int total = 0;
        int exitosas = 0;

        List<CategoriaCatalogo> categorias = categoriaCatalogoRepository.findByActivoTrueOrderByOrdenAscNombreAsc();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = obtenerHoja(workbook, "Master");
            int ultimaFila = sheet.getLastRowNum();

            for (int i = 1; i <= ultimaFila; i++) {
                Row row = sheet.getRow(i);
                int excelRowNum = i + 1;
                if (ExcelCellUtils.isRowEmpty(row)) {
                    continue;
                }
                total++;
                try {
                    Equipo equipo = mapearFilaMaster(row, excelRowNum, advertencias, categorias);
                    equipoRepository.save(equipo);
                    exitosas++;
                } catch (ImportRowException e) {
                    errores.add(new ImportFilaError(excelRowNum, e.getMessage()));
                } catch (DataIntegrityViolationException e) {
                    errores.add(new ImportFilaError(excelRowNum, "Conflicto de datos (posible código duplicado)"));
                } catch (RuntimeException e) {
                    errores.add(new ImportFilaError(excelRowNum, "Error inesperado: " + e.getMessage()));
                }
            }
        } catch (ImportFileException e) {
            throw e;
        } catch (IOException e) {
            throw new ImportFileException("No se pudo leer el archivo Excel: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new ImportFileException("El archivo no parece ser un Excel .xlsx válido: " + e.getMessage());
        }

        return new ImportResultResponse(total, exitosas, errores.size(), errores, advertencias);
    }

    /** Columnas: Item | Descripcion_Equipo | Numero_serie | Modelo | Marca | Motor |
     *  Codigo_Taller | Cliente | Accesorios | Fecha ingreso | Fecha diagnostico |
     *  Rotulado | Estado del equipo. Fila 1 = titulo, fila 2 = encabezados, datos desde fila 3. */
    public ImportResultResponse importarTaller(InputStream inputStream) {
        List<ImportFilaError> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        Map<String, Cliente> clienteCache = new HashMap<>();
        int total = 0;
        int exitosas = 0;

        List<CategoriaCatalogo> categorias = categoriaCatalogoRepository.findByActivoTrueOrderByOrdenAscNombreAsc();
        List<TipoMotorCatalogo> motores = tipoMotorCatalogoRepository.findByActivoTrueOrderByOrdenAscNombreAsc();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = obtenerHoja(workbook, "Hoja1");
            int ultimaFila = sheet.getLastRowNum();

            for (int i = 2; i <= ultimaFila; i++) {
                Row row = sheet.getRow(i);
                int excelRowNum = i + 1;
                if (ExcelCellUtils.isRowEmpty(row)) {
                    continue;
                }
                total++;
                try {
                    Equipo equipo = mapearFilaTaller(row, excelRowNum, advertencias, clienteCache, categorias, motores);
                    equipoRepository.save(equipo);
                    exitosas++;
                } catch (ImportRowException e) {
                    errores.add(new ImportFilaError(excelRowNum, e.getMessage()));
                } catch (DataIntegrityViolationException e) {
                    errores.add(new ImportFilaError(excelRowNum,
                            "Conflicto de datos (posible código de taller duplicado)"));
                } catch (RuntimeException e) {
                    errores.add(new ImportFilaError(excelRowNum, "Error inesperado: " + e.getMessage()));
                }
            }
        } catch (ImportFileException e) {
            throw e;
        } catch (IOException e) {
            throw new ImportFileException("No se pudo leer el archivo Excel: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new ImportFileException("El archivo no parece ser un Excel .xlsx válido: " + e.getMessage());
        }

        return new ImportResultResponse(total, exitosas, errores.size(), errores, advertencias);
    }

    private Sheet obtenerHoja(Workbook workbook, String nombrePreferido) {
        Sheet sheet = workbook.getSheet(nombrePreferido);
        if (sheet != null) {
            return sheet;
        }
        if (workbook.getNumberOfSheets() == 0) {
            throw new ImportFileException("El archivo Excel no tiene hojas");
        }
        return workbook.getSheetAt(0);
    }

    private Equipo mapearFilaMaster(Row row, int excelRowNum, List<String> advertencias, List<CategoriaCatalogo> categorias) {
        String descripcion = ExcelCellUtils.readString(row, 2);
        if (descripcion == null) {
            throw new ImportRowException("descripcionEquipo es obligatorio");
        }

        String categoriaRaw = ExcelCellUtils.readString(row, 3);
        CategoriaCatalogo categoria = matchCategoria(categorias, categoriaRaw);
        if (categoria == null) {
            throw new ImportRowException("categoria '" + categoriaRaw + "' no reconocida");
        }

        String numeroSerie = normalizeNumeroSerie(ExcelCellUtils.readString(row, 5));
        if (categoria.isRequiereNumeroSerie() && numeroSerie == null) {
            throw new ImportRowException("numeroSerie es obligatorio para categoría " + categoria.getNombre());
        }

        LocalDate fechaRegistro = ExcelCellUtils.readDate(row, 7);
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
            advertencias.add("Fila " + excelRowNum + ": sin fecha de registro válida, se usó la fecha de hoy");
        }

        String estadoRaw = ExcelCellUtils.readString(row, 8);
        EstadoEquipo estado = EstadoEquipo.ACTIVO;
        if (estadoRaw != null) {
            EstadoEquipo matched = ExcelCellUtils.matchEnum(EstadoEquipo.class, estadoRaw);
            if (matched != null) {
                estado = matched;
            } else {
                advertencias.add("Fila " + excelRowNum + ": estado '" + estadoRaw + "' no reconocido, se usó ACTIVO");
            }
        }

        return Equipo.builder()
                .codigo(ExcelCellUtils.readString(row, 1))
                .descripcionEquipo(descripcion)
                .categoria(categoria)
                .marca(ExcelCellUtils.readString(row, 4))
                .numeroSerie(numeroSerie)
                .ubicacion(ExcelCellUtils.readString(row, 6))
                .fechaRegistro(fechaRegistro)
                .estado(estado)
                .checkMtto(ExcelCellUtils.readCheckboxLike(row, 9))
                .checkHv(ExcelCellUtils.readCheckboxLike(row, 10))
                .checkFt(ExcelCellUtils.readCheckboxLike(row, 11))
                .propiedad(Propiedad.PROPIO)
                .build();
    }

    private Equipo mapearFilaTaller(Row row, int excelRowNum, List<String> advertencias,
                                     Map<String, Cliente> clienteCache, List<CategoriaCatalogo> categorias,
                                     List<TipoMotorCatalogo> motores) {
        String descripcion = ExcelCellUtils.readString(row, 1);
        if (descripcion == null) {
            throw new ImportRowException("descripcionEquipo es obligatorio");
        }

        String clienteNombre = ExcelCellUtils.readString(row, 7);
        if (clienteNombre == null) {
            throw new ImportRowException("cliente asociado es obligatorio para equipos externos");
        }

        String estadoRaw = ExcelCellUtils.readString(row, 12);
        EstadoServicioTaller estadoServicioTaller = ExcelCellUtils.matchEnum(EstadoServicioTaller.class, estadoRaw);
        if (estadoServicioTaller == null) {
            throw new ImportRowException("estado del equipo '" + estadoRaw + "' no reconocido");
        }

        CategoriaCatalogo categoria = inferirCategoria(descripcion, excelRowNum, advertencias, categorias);

        String numeroSerie = normalizeNumeroSerie(ExcelCellUtils.readString(row, 2));
        if (categoria.isRequiereNumeroSerie() && numeroSerie == null) {
            throw new ImportRowException("numeroSerie es obligatorio para categoría " + categoria.getNombre());
        }

        String motorRaw = ExcelCellUtils.readString(row, 5);
        TipoMotorCatalogo motor = null;
        if (motorRaw != null) {
            motor = matchMotor(motores, motorRaw);
            if (motor == null) {
                advertencias.add("Fila " + excelRowNum + ": motor '" + motorRaw + "' no reconocido, se dejó sin motor");
            }
        }

        LocalDate fechaIngreso = ExcelCellUtils.readDate(row, 9);
        if (fechaIngreso == null) {
            fechaIngreso = LocalDate.now();
            advertencias.add("Fila " + excelRowNum + ": sin fecha de ingreso válida, se usó la fecha de hoy");
        }

        Cliente cliente = resolverCliente(clienteNombre, clienteCache);

        return Equipo.builder()
                .codigoTaller(ExcelCellUtils.readString(row, 6))
                .descripcionEquipo(descripcion)
                .categoria(categoria)
                .marca(ExcelCellUtils.readString(row, 4))
                .numeroSerie(numeroSerie)
                .modelo(ExcelCellUtils.readString(row, 3))
                .motor(motor)
                .fechaRegistro(fechaIngreso)
                .estado(EstadoEquipo.ACTIVO)
                .propiedad(Propiedad.EXTERNO)
                .cliente(cliente)
                .accesorios(ExcelCellUtils.readString(row, 8))
                .fechaIngreso(fechaIngreso)
                .fechaDiagnostico(ExcelCellUtils.readDate(row, 10))
                .rotulado(ExcelCellUtils.readCheckboxLike(row, 11))
                .estadoServicioTaller(estadoServicioTaller)
                .build();
    }

    private CategoriaCatalogo inferirCategoria(String descripcion, int excelRowNum, List<String> advertencias,
                                                List<CategoriaCatalogo> categorias) {
        String normalizado = ExcelCellUtils.normalizeKey(descripcion);
        String conEspacios = normalizado == null ? "" : normalizado.replace('_', ' ');

        for (Map.Entry<String, String> entry : PALABRAS_CLAVE_CATEGORIA.entrySet()) {
            if (conEspacios.contains(entry.getKey())) {
                CategoriaCatalogo categoria = matchCategoria(categorias, entry.getValue());
                if (categoria != null) {
                    return categoria;
                }
            }
        }

        advertencias.add("Fila " + excelRowNum + ": no se pudo inferir categoría desde '" + descripcion
                + "', se usó la categoría por defecto");
        return categoriaPorDefecto(categorias);
    }

    private CategoriaCatalogo matchCategoria(List<CategoriaCatalogo> categorias, String raw) {
        String nombre = ExcelCellUtils.matchNombre(categorias.stream().map(CategoriaCatalogo::getNombre).toList(), raw);
        return nombre == null ? null : categorias.stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElse(null);
    }

    private TipoMotorCatalogo matchMotor(List<TipoMotorCatalogo> motores, String raw) {
        String nombre = ExcelCellUtils.matchNombre(motores.stream().map(TipoMotorCatalogo::getNombre).toList(), raw);
        return nombre == null ? null : motores.stream().filter(m -> m.getNombre().equals(nombre)).findFirst().orElse(null);
    }

    /** "Equipo Menor" si sigue existiendo en el catálogo; si no, la primera categoría activa por orden. */
    private CategoriaCatalogo categoriaPorDefecto(List<CategoriaCatalogo> categorias) {
        return categorias.stream().filter(c -> "Equipo Menor".equalsIgnoreCase(c.getNombre())).findFirst()
                .orElseGet(() -> categorias.stream().findFirst()
                        .orElseThrow(() -> new ImportFileException(
                                "No hay categorías de equipo activas configuradas; crea al menos una antes de importar")));
    }

    private Cliente resolverCliente(String nombreRaw, Map<String, Cliente> cache) {
        String nombre = nombreRaw.trim();
        String key = nombre.toUpperCase(Locale.ROOT);

        Cliente cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        Cliente cliente = clienteRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> clienteRepository.save(Cliente.builder()
                        .nombre(nombre)
                        .tipo(inferirTipoCliente(nombre))
                        .build()));

        cache.put(key, cliente);
        return cliente;
    }

    /** Heurística explícita en el spec: sin más contexto no hay forma exacta de saberlo. */
    private TipoCliente inferirTipoCliente(String nombre) {
        boolean todoMayusculas = nombre.equals(nombre.toUpperCase(Locale.ROOT))
                && !nombre.equals(nombre.toLowerCase(Locale.ROOT));
        if (todoMayusculas) {
            return TipoCliente.EMPRESA;
        }

        String normalizado = ExcelCellUtils.normalizeKey(nombre);
        String conEspacios = normalizado == null ? "" : normalizado.replace('_', ' ');
        for (String palabra : PALABRAS_CLAVE_EMPRESA) {
            if (conEspacios.contains(palabra)) {
                return TipoCliente.EMPRESA;
            }
        }

        return TipoCliente.NATURAL;
    }

    private String normalizeNumeroSerie(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("N/A") || trimmed.equalsIgnoreCase("NA")) {
            return null;
        }
        return trimmed;
    }
}
