package com.inventario.pdf;

import com.inventario.dto.response.OrdenSalidaItemResponse;
import com.inventario.dto.response.OrdenSalidaResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Genera el PDF de una orden de salida (equipos que salen a un cliente
 * externo por prestamo o alquiler entre empresas). Se apoya en OpenPDF
 * (com.lowagie.text, fork libre de iText 4) para no depender de una
 * libreria con licencia AGPL/comercial.
 */
@Service
public class OrdenSalidaPdfService {

    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font FONT_NUMERO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font FONT_VALOR = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font FONT_TABLA_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONT_TABLA_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font FONT_ACCESORIOS = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.DARK_GRAY);
    private static final Font FONT_FIRMA = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Color COLOR_MARCA = new Color(59, 95, 226);

    public byte[] generar(OrdenSalidaResponse orden) {
        Document document = new Document(PageSize.LETTER, 40, 40, 45, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            agregarEncabezado(document, orden);
            agregarInfoGeneral(document, orden);
            agregarTablaEquipos(document, orden);
            agregarObservaciones(document, orden);
            agregarFirmas(document);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF de la orden de salida", e);
        }
    }

    private void agregarEncabezado(Document document, OrdenSalidaResponse orden) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3, 2});

        PdfPCell izquierda = new PdfPCell();
        izquierda.setBorder(0);
        Paragraph titulo = new Paragraph("ORDEN DE SALIDA DE EQUIPOS", FONT_TITULO);
        Paragraph subtitulo = new Paragraph("FUNSAT", FONT_SUBTITULO);
        izquierda.addElement(titulo);
        izquierda.addElement(subtitulo);
        header.addCell(izquierda);

        PdfPCell derecha = new PdfPCell();
        derecha.setBorder(0);
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph numero = new Paragraph(orden.numero(), FONT_NUMERO);
        numero.setAlignment(Element.ALIGN_RIGHT);
        Paragraph fecha = new Paragraph("Fecha de salida: " + orden.fechaSalida().format(FECHA_FORMATO), FONT_VALOR);
        fecha.setAlignment(Element.ALIGN_RIGHT);
        derecha.addElement(numero);
        derecha.addElement(fecha);
        header.addCell(derecha);

        document.add(header);

        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        linea.setSpacingBefore(6);
        linea.setSpacingAfter(12);
        PdfPCell celdaLinea = new PdfPCell();
        celdaLinea.setFixedHeight(2);
        celdaLinea.setBackgroundColor(COLOR_MARCA);
        celdaLinea.setBorder(0);
        linea.addCell(celdaLinea);
        document.add(linea);
    }

    private void agregarInfoGeneral(Document document, OrdenSalidaResponse orden) throws Exception {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(14);

        agregarFilaInfo(tabla, "Destino (cliente):", orden.cliente().nombre()
                + (orden.cliente().telefono() != null ? " · " + orden.cliente().telefono() : ""));
        agregarFilaInfo(tabla, "Responsable:", orden.responsable().nombre());
        agregarFilaInfo(tabla, "Estado:", estadoLegible(orden.estado()));
        agregarFilaInfo(tabla, "Generado por:", orden.creadoPorNombre() != null ? orden.creadoPorNombre() : "—");

        document.add(tabla);
    }

    private void agregarFilaInfo(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, FONT_ETIQUETA));
        celdaEtiqueta.setBorder(0);
        celdaEtiqueta.setPaddingBottom(4);
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, FONT_VALOR));
        celdaValor.setBorder(0);
        celdaValor.setPaddingBottom(4);
        tabla.addCell(celdaValor);
    }

    private void agregarTablaEquipos(Document document, OrdenSalidaResponse orden) throws Exception {
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{2, 4, 2, 2.2f, 3});
        tabla.setSpacingBefore(4);

        for (String encabezado : new String[]{"Código", "Descripción", "Marca", "N° Serie", "Observación"}) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FONT_TABLA_HEADER));
            celda.setBackgroundColor(COLOR_MARCA);
            celda.setPadding(5);
            tabla.addCell(celda);
        }

        List<OrdenSalidaItemResponse> items = orden.items();
        for (OrdenSalidaItemResponse item : items) {
            tabla.addCell(celdaTabla(item.equipoCodigo() != null ? item.equipoCodigo() : "—"));
            tabla.addCell(celdaDescripcionConAccesorios(item));
            tabla.addCell(celdaTabla(defecto(item.equipoMarca())));
            tabla.addCell(celdaTabla(defecto(item.equipoNumeroSerie())));
            tabla.addCell(celdaTabla(defecto(item.observacionSalida())));
        }

        document.add(tabla);
    }

    private PdfPCell celdaTabla(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FONT_TABLA_CELDA));
        celda.setPadding(5);
        return celda;
    }

    /** Descripción del equipo, con una segunda línea en cursiva listando los accesorios que salen con él (si hay). */
    private PdfPCell celdaDescripcionConAccesorios(OrdenSalidaItemResponse item) {
        PdfPCell celda = new PdfPCell();
        celda.setPadding(5);

        celda.addElement(new Paragraph(item.equipoDescripcion(), FONT_TABLA_CELDA));

        if (!item.accesorios().isEmpty()) {
            String listado = item.accesorios().stream()
                    .map(a -> a.cantidad() > 1 ? a.nombre() + " (x" + a.cantidad() + ")" : a.nombre())
                    .collect(Collectors.joining(", "));
            Paragraph accesoriosParrafo = new Paragraph("Accesorios: " + listado, FONT_ACCESORIOS);
            accesoriosParrafo.setSpacingBefore(2);
            celda.addElement(accesoriosParrafo);
        }

        return celda;
    }

    private String defecto(String valor) {
        return (valor == null || valor.isBlank()) ? "—" : valor;
    }

    private String estadoLegible(String estado) {
        return switch (estado) {
            case "ABIERTA" -> "Abierta (sin devoluciones)";
            case "PARCIAL" -> "Parcial (algunos equipos devueltos)";
            case "CERRADA" -> "Cerrada (todos los equipos devueltos)";
            default -> estado;
        };
    }

    private void agregarObservaciones(Document document, OrdenSalidaResponse orden) throws Exception {
        if (orden.observaciones() == null || orden.observaciones().isBlank()) {
            return;
        }
        Paragraph etiqueta = new Paragraph("Observaciones generales", FONT_ETIQUETA);
        etiqueta.setSpacingBefore(14);
        document.add(etiqueta);
        document.add(new Paragraph(orden.observaciones(), FONT_VALOR));
    }

    private void agregarFirmas(Document document) throws Exception {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(40);
        tabla.setWidths(new float[]{1, 1});

        tabla.addCell(celdaFirma("Entrega"));
        tabla.addCell(celdaFirma("Recibe"));

        document.add(tabla);
    }

    private PdfPCell celdaFirma(String etiqueta) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(0);
        celda.setPaddingTop(30);

        Paragraph linea = new Paragraph("_______________________________", FONT_FIRMA);
        Paragraph texto = new Paragraph(etiqueta + " (firma, nombre y C.C.)", FONT_FIRMA);

        celda.addElement(linea);
        celda.addElement(texto);
        return celda;
    }
}
