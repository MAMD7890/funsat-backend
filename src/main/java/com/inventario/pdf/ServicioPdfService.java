package com.inventario.pdf;

import com.inventario.dto.response.ServicioChecklistRespuestaResponse;
import com.inventario.dto.response.ServicioRepuestoResponse;
import com.inventario.dto.response.ServicioResponse;
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
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Genera el PDF de cierre de un servicio/mantenimiento: info del equipo,
 * trabajo realizado, repuestos usados (con precio y cantidad cargados
 * libremente por el usuario) y checklist. Mismo enfoque que
 * {@link OrdenSalidaPdfService} (OpenPDF, sin dependencia AGPL/comercial).
 */
@Service
public class ServicioPdfService {

    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font FONT_NUMERO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font FONT_VALOR = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font FONT_TABLA_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONT_TABLA_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_FIRMA = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Color COLOR_MARCA = new Color(59, 95, 226);
    private static final Color COLOR_OK = new Color(40, 167, 69);
    private static final Color COLOR_PENDIENTE = new Color(220, 53, 69);

    private static final Map<String, String> TIPO_SERVICIO_LABEL = Map.of(
            "ALISTAMIENTO", "Alistamiento",
            "PREVENTIVO", "Preventivo",
            "CORRECTIVO", "Correctivo"
    );

    private static final Map<String, String> ESTADO_SERVICIO_LABEL = Map.of(
            "REGISTRADO", "Por hacer",
            "EN_PROGRESO", "En progreso",
            "EN_REVISION", "En revisión",
            "COMPLETADO", "Completado"
    );

    public byte[] generar(ServicioResponse servicio) {
        Document document = new Document(PageSize.LETTER, 40, 40, 45, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            agregarEncabezado(document, servicio);
            agregarInfoGeneral(document, servicio);
            agregarDescripcion(document, servicio);
            agregarTablaRepuestos(document, servicio);
            agregarResumenCostos(document, servicio);
            agregarChecklist(document, servicio);
            agregarFirmas(document);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del servicio", e);
        }
    }

    private void agregarEncabezado(Document document, ServicioResponse servicio) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3, 2});

        PdfPCell izquierda = new PdfPCell();
        izquierda.setBorder(0);
        izquierda.addElement(new Paragraph("REPORTE DE SERVICIO / MANTENIMIENTO", FONT_TITULO));
        izquierda.addElement(new Paragraph("FUNSAT", FONT_SUBTITULO));
        header.addCell(izquierda);

        PdfPCell derecha = new PdfPCell();
        derecha.setBorder(0);
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph numero = new Paragraph(servicio.numero(), FONT_NUMERO);
        numero.setAlignment(Element.ALIGN_RIGHT);
        Paragraph fecha = new Paragraph("Fecha: " + servicio.fecha().format(FECHA_FORMATO), FONT_VALOR);
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

    private void agregarInfoGeneral(Document document, ServicioResponse servicio) throws Exception {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(14);

        String equipoDescripcion = servicio.equipo().descripcionEquipo()
                + (servicio.equipo().codigo() != null ? " (" + servicio.equipo().codigo() + ")" : "");
        agregarFilaInfo(tabla, "Equipo:", equipoDescripcion);
        agregarFilaInfo(tabla, "Categoría:", servicio.equipo().categoria().nombre());
        agregarFilaInfo(tabla, "Marca / Modelo:", defecto(servicio.equipo().marca()) + " / " + defecto(servicio.equipo().modelo()));
        agregarFilaInfo(tabla, "N° de serie:", defecto(servicio.equipo().numeroSerie()));
        agregarFilaInfo(tabla, "Tipo de servicio:", etiqueta(TIPO_SERVICIO_LABEL, servicio.tipoServicio().name()));
        agregarFilaInfo(tabla, "Estado:", etiqueta(ESTADO_SERVICIO_LABEL, servicio.estado().name()));
        agregarFilaInfo(tabla, "Técnico responsable:",
                servicio.tecnicoResponsable() != null ? servicio.tecnicoResponsable().nombre() : "—");
        agregarFilaInfo(tabla, "Costo mano de obra:", formatoMoneda(servicio.costoValorizado()));

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

    private void agregarDescripcion(Document document, ServicioResponse servicio) throws Exception {
        Paragraph etiqueta = new Paragraph("Descripción del trabajo realizado", FONT_ETIQUETA);
        etiqueta.setSpacingAfter(4);
        document.add(etiqueta);

        Paragraph texto = new Paragraph(servicio.descripcion(), FONT_VALOR);
        texto.setSpacingAfter(14);
        document.add(texto);
    }

    private void agregarTablaRepuestos(Document document, ServicioResponse servicio) throws Exception {
        List<ServicioRepuestoResponse> repuestos = servicio.repuestos();
        if (repuestos.isEmpty()) {
            return;
        }

        Paragraph titulo = new Paragraph("Repuestos utilizados", FONT_ETIQUETA);
        titulo.setSpacingAfter(4);
        document.add(titulo);

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4, 2, 2, 2});
        tabla.setSpacingAfter(4);

        for (String encabezado : new String[]{"Repuesto", "Cantidad", "Costo unitario", "Subtotal"}) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FONT_TABLA_HEADER));
            celda.setBackgroundColor(COLOR_MARCA);
            celda.setPadding(5);
            tabla.addCell(celda);
        }

        for (ServicioRepuestoResponse r : repuestos) {
            tabla.addCell(celdaTabla(r.nombre(), Element.ALIGN_LEFT));
            tabla.addCell(celdaTabla(r.cantidad().stripTrailingZeros().toPlainString(), Element.ALIGN_RIGHT));
            tabla.addCell(celdaTabla(formatoMoneda(r.costoUnitario()), Element.ALIGN_RIGHT));
            tabla.addCell(celdaTabla(formatoMoneda(r.costoTotal()), Element.ALIGN_RIGHT));
        }

        document.add(tabla);
    }

    private PdfPCell celdaTabla(String texto, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FONT_TABLA_CELDA));
        celda.setPadding(5);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

    private void agregarResumenCostos(Document document, ServicioResponse servicio) throws Exception {
        BigDecimal totalRepuestos = servicio.repuestos().stream()
                .map(ServicioRepuestoResponse::costoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGeneral = servicio.costoValorizado().add(totalRepuestos);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(50);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setWidths(new float[]{3, 2});
        tabla.setSpacingBefore(4);
        tabla.setSpacingAfter(14);

        agregarFilaResumen(tabla, "Mano de obra", formatoMoneda(servicio.costoValorizado()), FONT_VALOR);
        agregarFilaResumen(tabla, "Repuestos", formatoMoneda(totalRepuestos), FONT_VALOR);
        agregarFilaResumen(tabla, "Total general", formatoMoneda(totalGeneral), FONT_TOTAL);

        document.add(tabla);
    }

    private void agregarFilaResumen(PdfPTable tabla, String etiqueta, String valor, Font fontValor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, FONT_ETIQUETA));
        celdaEtiqueta.setBorder(0);
        celdaEtiqueta.setPaddingBottom(3);
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fontValor));
        celdaValor.setBorder(0);
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaValor.setPaddingBottom(3);
        tabla.addCell(celdaValor);
    }

    private void agregarChecklist(Document document, ServicioResponse servicio) throws Exception {
        List<ServicioChecklistRespuestaResponse> checklist = servicio.checklist();
        if (checklist.isEmpty()) {
            return;
        }

        Paragraph titulo = new Paragraph("Checklist", FONT_ETIQUETA);
        titulo.setSpacingAfter(4);
        document.add(titulo);

        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1, 3, 4});
        tabla.setSpacingAfter(14);

        for (String encabezado : new String[]{"", "Ítem", "Observación"}) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FONT_TABLA_HEADER));
            celda.setBackgroundColor(COLOR_MARCA);
            celda.setPadding(5);
            tabla.addCell(celda);
        }

        for (ServicioChecklistRespuestaResponse c : checklist) {
            Font fontEstado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,
                    c.completado() ? COLOR_OK : COLOR_PENDIENTE);
            PdfPCell celdaEstado = new PdfPCell(new Phrase(c.completado() ? "OK" : "—", fontEstado));
            celdaEstado.setPadding(5);
            celdaEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celdaEstado);

            tabla.addCell(celdaTabla(c.nombreItem(), Element.ALIGN_LEFT));
            tabla.addCell(celdaTabla(defecto(c.observacion()), Element.ALIGN_LEFT));
        }

        document.add(tabla);
    }

    private void agregarFirmas(Document document) throws Exception {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(30);
        tabla.setWidths(new float[]{1, 1});

        tabla.addCell(celdaFirma("Técnico responsable"));
        tabla.addCell(celdaFirma("Cliente / Recibido conforme"));

        document.add(tabla);
    }

    private PdfPCell celdaFirma(String etiqueta) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(0);
        celda.setPaddingTop(30);

        celda.addElement(new Paragraph("_______________________________", FONT_FIRMA));
        celda.addElement(new Paragraph(etiqueta + " (firma, nombre y C.C.)", FONT_FIRMA));
        return celda;
    }

    private String etiqueta(Map<String, String> mapa, String clave) {
        return mapa.getOrDefault(clave, clave);
    }

    private String defecto(String valor) {
        return (valor == null || valor.isBlank()) ? "—" : valor;
    }

    private static final java.text.NumberFormat FORMATO_COP =
            java.text.NumberFormat.getNumberInstance(new java.util.Locale("es", "CO"));
    static {
        FORMATO_COP.setMaximumFractionDigits(0);
        FORMATO_COP.setMinimumFractionDigits(0);
    }

    private String formatoMoneda(BigDecimal valor) {
        return "$" + FORMATO_COP.format(valor);
    }
}
