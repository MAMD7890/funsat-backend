package com.inventario.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;

/**
 * Lectura defensiva de celdas para datos reales de Excel: numeros que en
 * realidad son texto (numero de serie), fechas guardadas como serial
 * numerico, checks tipo "y"/vacio, y texto libre con acentos/mayusculas
 * inconsistentes que hay que normalizar antes de mapear a un enum.
 */
final class ExcelCellUtils {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();
    private static final Set<String> VALORES_VERDADEROS = Set.of("Y", "YES", "SI", "SÍ", "X", "1", "TRUE");

    private ExcelCellUtils() {
    }

    static String readString(Row row, int colIndex) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        String value = DATA_FORMATTER.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    static LocalDate readDate(Row row, int colIndex) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String text = DATA_FORMATTER.formatCellValue(cell).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static boolean readCheckboxLike(Row row, int colIndex) {
        String value = readString(row, colIndex);
        return value != null && VALORES_VERDADEROS.contains(value.toUpperCase(Locale.ROOT));
    }

    /** "Pendiente remisión " -> "PENDIENTE_REMISION" (sin acentos, sin espacios de sobra). */
    static String normalizeKey(String raw) {
        if (raw == null) {
            return null;
        }
        String sinAcentos = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String key = sinAcentos.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        return key.replaceAll("^_+|_+$", "");
    }

    static <E extends Enum<E>> E matchEnum(Class<E> enumType, String raw) {
        String key = normalizeKey(raw);
        if (key == null || key.isEmpty()) {
            return null;
        }
        for (E constant : enumType.getEnumConstants()) {
            if (constant.name().equals(key)) {
                return constant;
            }
        }
        return null;
    }

    /**
     * Igual que {@link #matchEnum}, pero contra una lista dinamica de nombres
     * de catalogo (categoria/tipo de motor) en vez de constantes de enum.
     * Devuelve el nombre "canonico" tal como esta en el catalogo, para que el
     * caller pueda buscarlo por nombre.
     */
    static String matchNombre(java.util.Collection<String> nombresValidos, String raw) {
        String key = normalizeKey(raw);
        if (key == null || key.isEmpty()) {
            return null;
        }
        for (String nombre : nombresValidos) {
            if (key.equals(normalizeKey(nombre))) {
                return nombre;
            }
        }
        return null;
    }

    static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !DATA_FORMATTER.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
