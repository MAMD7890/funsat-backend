package com.inventario.excel;

import com.inventario.repository.CategoriaCatalogoRepository;
import com.inventario.repository.TipoMotorCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Collectors;

/**
 * Genera las plantillas de ejemplo (.xlsx) para los dos formatos que acepta
 * la importacion, con el mismo mapeo de columnas que
 * {@link EquipoExcelImportService} para que nunca queden desincronizadas.
 * Las categorias y tipos de motor validos se listan en las instrucciones
 * leyendo el catalogo real (son editables por el admin), no un texto fijo.
 */
@Service
@RequiredArgsConstructor
public class PlantillaExcelService {

    private final CategoriaCatalogoRepository categoriaCatalogoRepository;
    private final TipoMotorCatalogoRepository tipoMotorCatalogoRepository;

    public byte[] generarPlantillaMaster() {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = estiloEncabezado(workbook);

            Sheet datos = workbook.createSheet("Master");
            String[] encabezados = {
                    "Item", "Codigo", "Descripcion_Equipo", "Categoria", "Marca", "N_Serie",
                    "Ubicacion", "Fecha de registro", "Estado", "Check MTTO", "Check H V", "Check F.T"
            };
            escribirFila(datos, 0, headerStyle, encabezados);
            escribirFila(datos, 1, null,
                    "1", "EJEMPLO-001", "Pulidora 7\"", "Equipo Menor", "Dewalt", "",
                    "Bodega principal", "2026-01-15", "Activo", "Y", "Y", "N");
            escribirFila(datos, 2, null,
                    "2", "EJEMPLO-002", "Camioneta Ford F-150", "Vehiculo", "Ford", "1FTFW1E5XNFA12345",
                    "Patio taller", "2026-02-10", "Activo", "Y", "N", "N");
            autoajustarColumnas(datos, encabezados.length);

            Sheet instrucciones = workbook.createSheet("Instrucciones");
            String[][] filas = {
                    {"Como usar esta plantilla"},
                    {""},
                    {"1. La fila 1 es el encabezado (no la borres ni la muevas); los datos empiezan en la fila 2."},
                    {"2. Descripcion_Equipo y Categoria son obligatorios en cada fila."},
                    {"3. Categoria valida: " + nombresCategorias() + " (no distingue mayusculas ni acentos)."},
                    {"4. N_Serie es obligatorio solo para las categorias marcadas como \"(requiere N° serie)\" arriba."},
                    {"5. Fecha de registro en formato AAAA-MM-DD. Si se deja vacia o no es valida, se usa la fecha de hoy."},
                    {"6. Estado valido: Activo, Inactivo, En Mantenimiento, Dado de Baja. Si se deja vacio o no se reconoce, se usa Activo."},
                    {"7. Check MTTO / Check H V / Check F.T: usa \"Y\" para si, o deja la celda vacia."},
                    {"8. Codigo debe ser unico; si se repite un codigo ya existente, esa fila queda reportada con error y el resto se sigue importando."}
            };
            escribirInstrucciones(instrucciones, filas);

            return aBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] generarPlantillaTaller() {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = estiloEncabezado(workbook);
            CellStyle tituloStyle = estiloTitulo(workbook);

            Sheet datos = workbook.createSheet("Hoja1");
            Row filaTitulo = datos.createRow(0);
            Cell titulo = filaTitulo.createCell(0);
            titulo.setCellValue("SERVICIO TALLER - INVENTARIO EQUIPOS EXTERNOS");
            titulo.setCellStyle(tituloStyle);
            datos.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

            String[] encabezados = {
                    "Item", "Descripcion_Equipo", "Numero_serie", "Modelo", "Marca", "Motor",
                    "Codigo_Taller", "Cliente", "Accesorios", "Fecha ingreso", "Fecha diagnostico",
                    "Rotulado", "Estado del equipo"
            };
            escribirFila(datos, 1, headerStyle, encabezados);
            escribirFila(datos, 2, null,
                    "1", "Retroexcavadora CAT 320", "RX-2024-001", "320D", "Caterpillar", "Diesel",
                    "EJEMPLO-RT-001", "Constructora ABC SAS", "Balde, martillo hidraulico",
                    "2026-01-10", "2026-01-12", "Y", "Pendiente por diagnostico");
            escribirFila(datos, 3, null,
                    "2", "Pulidora de piso 5\"", "", "5 pulgadas", "Makita", "Electrico 110V",
                    "EJEMPLO-RT-002", "Juan Perez", "",
                    "2026-01-11", "", "N", "Por iniciar servicio");
            autoajustarColumnas(datos, encabezados.length);

            Sheet instrucciones = workbook.createSheet("Instrucciones");
            String[][] filas = {
                    {"Como usar esta plantilla"},
                    {""},
                    {"1. La fila 1 es un titulo libre (se ignora al importar); la fila 2 es el encabezado; los datos empiezan en la fila 3."},
                    {"2. Descripcion_Equipo, Cliente y Estado del equipo son obligatorios en cada fila."},
                    {"3. La Categoria no se pide: se infiere automaticamente por palabras clave en Descripcion_Equipo "
                            + "(camioneta/camion -> Vehiculo; trailer/remolque/mula -> Trailer; grua/pluma/motobomba/"
                            + "apisonadora/compactadora/retroexcavadora/montacarga/porta pallet -> Maquinaria; si no "
                            + "coincide nada, se usa Equipo Menor)."},
                    {"4. Numero_serie es obligatorio solo si la categoria inferida es de las que exigen N° de serie "
                            + "(ver punto 3 de la plantilla Master)."},
                    {"5. Cliente: si el nombre no existe todavia se crea automaticamente. Se guarda como Empresa si el "
                            + "nombre esta en MAYUSCULAS SOSTENIDAS o contiene palabras como SAS, LTDA, CIA, etc.; "
                            + "en caso contrario se guarda como Natural."},
                    {"6. Motor valido (opcional): " + nombresMotores() + "."},
                    {"7. Fechas en formato AAAA-MM-DD. Si Fecha ingreso se deja vacia o no es valida, se usa la fecha de hoy."},
                    {"8. Rotulado: usa \"Y\" para si, o deja la celda vacia."},
                    {"9. Estado del equipo valido: Pendiente por Diagnostico, Por Iniciar Servicio, Por Aprobar, Por Definir, "
                            + "Stand By, Sin Cancelar y Reparado, Pendiente Remision, Pendiente por Factura y Entrega, Reparada por Retirar."},
                    {"10. Codigo_Taller debe ser unico si se usa; si se repite uno ya existente, esa fila queda reportada "
                            + "con error (\"conflicto de datos\") y el resto del archivo se sigue importando."}
            };
            escribirInstrucciones(instrucciones, filas);

            return aBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String nombresCategorias() {
        return categoriaCatalogoRepository.findByActivoTrueOrderByOrdenAscNombreAsc().stream()
                .map(c -> c.getNombre() + (c.isRequiereNumeroSerie() ? " (requiere N° serie)" : ""))
                .collect(Collectors.joining(", "));
    }

    private String nombresMotores() {
        return tipoMotorCatalogoRepository.findByActivoTrueOrderByOrdenAscNombreAsc().stream()
                .map(com.inventario.entity.TipoMotorCatalogo::getNombre)
                .collect(Collectors.joining(", "));
    }

    private void escribirFila(Sheet sheet, int filaIndex, CellStyle estilo, String... valores) {
        Row row = sheet.createRow(filaIndex);
        for (int i = 0; i < valores.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(valores[i]);
            if (estilo != null) {
                cell.setCellStyle(estilo);
            }
        }
    }

    private void escribirInstrucciones(Sheet sheet, String[][] filas) {
        Font tituloFont = sheet.getWorkbook().createFont();
        tituloFont.setBold(true);
        tituloFont.setFontHeightInPoints((short) 12);
        CellStyle tituloStyle = sheet.getWorkbook().createCellStyle();
        tituloStyle.setFont(tituloFont);

        for (int i = 0; i < filas.length; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(filas[i][0]);
            if (i == 0) {
                cell.setCellStyle(tituloStyle);
            }
        }
        sheet.setColumnWidth(0, 255 * 120);
    }

    private CellStyle estiloEncabezado(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.BLUE_GREY.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle estiloTitulo(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private void autoajustarColumnas(Sheet sheet, int cantidadColumnas) {
        for (int i = 0; i < cantidadColumnas; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < 255 * 12) {
                sheet.setColumnWidth(i, 255 * 12);
            }
        }
    }

    private byte[] aBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
