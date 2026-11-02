package servicio;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Pago;
import util.FormatoFecha;

/**
 * Servicio encargado de la generación de comprobantes de pago en formato PDF
 * utilizando la librería iText 5.
 *
 * Incluye el historial de pagos registrados hasta el momento y el saldo restante
 * a pagar, adaptando el comprobante según la situación (Abono Parcial o Cancelación Total).
 */
public class GeneradorPdfService {

    private File ultimoArchivoGenerado;

    /**
     * Sobrecarga de compatibilidad: genera el comprobante consultando automáticamente
     * el historial de pagos del contrato si está disponible.
     */
    public File generarComprobante(Pago pago, Contrato contrato, Cliente cliente) {
        List<Pago> historial = null;
        if (contrato != null && contrato.getNro() != null) {
            try {
                historial = new dao.PagoDAO().listarPorContrato(contrato.getNro());
            } catch (Exception ignored) {
            }
        }
        return generarComprobante(pago, contrato, cliente, historial);
    }

    /**
     * Genera un comprobante unificado en PDF con el historial de pagos hasta el momento,
     * la liquidación del contrato y el saldo restante a pagar.
     *
     * @param pago Pago registrado (o actual)
     * @param contrato Contrato de alquiler asociado
     * @param cliente Cliente titular
     * @param historialPagos Lista completa o parcial de pagos del contrato
     * @return Archivo PDF generado
     */
    public File generarComprobante(Pago pago, Contrato contrato, Cliente cliente, List<Pago> historialPagos) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo para generar el comprobante.");
        }

        // Determinar el nombre del archivo en la carpeta del proyecto
        int idPago = pago.getId() != null ? pago.getId() : (pago.getP_id() != null ? pago.getP_id() : 0);
        String nombreArchivo = (idPago > 0) ? "comprobante_pago_" + idPago + ".pdf" : "comprobante_pago.pdf";
        File archivoDestino = new File(nombreArchivo);

        Document document = new Document(PageSize.A4, 45, 45, 45, 45);

        try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // Definición de fuentes tipográficas
            Font fontEncabezado = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
            Font fontSubtitulo = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(31, 41, 55));
            Font fontEtiqueta = new Font(Font.FontFamily.HELVETICA, 10.5f, Font.BOLD, BaseColor.BLACK);
            Font fontValor = new Font(Font.FontFamily.HELVETICA, 10.5f, Font.NORMAL, BaseColor.BLACK);
            Font fontTableHeader = new Font(Font.FontFamily.HELVETICA, 9.5f, Font.BOLD, new BaseColor(17, 24, 39));
            Font fontTableCell = new Font(Font.FontFamily.HELVETICA, 9.5f, Font.NORMAL, BaseColor.BLACK);
            Font fontTableCellBold = new Font(Font.FontFamily.HELVETICA, 9.5f, Font.BOLD, BaseColor.BLACK);
            Font fontPie = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.DARK_GRAY);

            // 1. Encabezado: Centrado, fuente grande y en negrita: 'RENT A CAR - COMPROBANTE DE PAGO'
            Paragraph encabezado = new Paragraph("RENT A CAR - COMPROBANTE DE PAGO", fontEncabezado);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            encabezado.setSpacingAfter(8f);
            document.add(encabezado);

            // 2. Línea separadora: Una línea horizontal
            LineSeparator separador = new LineSeparator();
            separador.setLineColor(new BaseColor(180, 180, 180));
            separador.setLineWidth(1f);
            document.add(new Chunk(separador));

            Paragraph esp1 = new Paragraph(" ");
            esp1.setSpacingBefore(2f);
            esp1.setSpacingAfter(4f);
            document.add(esp1);

            // 3. Datos del Recibo: Alineado a la izquierda
            String fechaTexto = "-";
            if (pago.getFec() != null && !pago.getFec().trim().isEmpty()) {
                fechaTexto = FormatoFecha.formatearFechaGui(pago.getFec());
            }
            Paragraph pFecha = new Paragraph();
            pFecha.setAlignment(Element.ALIGN_LEFT);
            pFecha.add(new Chunk("Fecha: ", fontEtiqueta));
            pFecha.add(new Chunk(fechaTexto, fontValor));
            pFecha.setSpacingAfter(3f);
            document.add(pFecha);

            String nroCompTexto = (idPago > 0) ? String.valueOf(idPago) : "-";
            Paragraph pNroComp = new Paragraph();
            pNroComp.setAlignment(Element.ALIGN_LEFT);
            pNroComp.add(new Chunk("N° de Comprobante: ", fontEtiqueta));
            pNroComp.add(new Chunk(nroCompTexto, fontValor));
            if (contrato != null && contrato.getNro() != null) {
                pNroComp.add(new Chunk("   |   Contrato N°: ", fontEtiqueta));
                pNroComp.add(new Chunk(String.valueOf(contrato.getNro()), fontValor));
            }
            pNroComp.setSpacingAfter(10f);
            document.add(pNroComp);

            // 4. Datos del Cliente y Contrato:
            String nombreCliente = "";
            String dniCliente = "";
            if (cliente != null) {
                String nom = cliente.getNom() != null ? cliente.getNom().trim() : "";
                String apl = cliente.getApl() != null ? cliente.getApl().trim() : "";
                nombreCliente = (nom + " " + apl).trim();
                if (cliente.getDni() != null) {
                    dniCliente = String.valueOf(cliente.getDni());
                }
            }
            if (nombreCliente.isEmpty() && contrato != null && contrato.getClienteNombre() != null) {
                nombreCliente = contrato.getClienteNombre().trim();
            }
            if (dniCliente.isEmpty() && contrato != null && contrato.getClienteDni() != null) {
                dniCliente = String.valueOf(contrato.getClienteDni());
            }
            if (nombreCliente.isEmpty()) {
                nombreCliente = "Cliente General";
            }
            String datosClienteTexto = nombreCliente + (!dniCliente.isEmpty() ? " - DNI: " + dniCliente : "");

            Paragraph pCliente = new Paragraph();
            pCliente.setAlignment(Element.ALIGN_LEFT);
            pCliente.add(new Chunk("Cliente: ", fontEtiqueta));
            pCliente.add(new Chunk(datosClienteTexto, fontValor));
            pCliente.setSpacingAfter(3f);
            document.add(pCliente);

            String vehiculoTexto = "";
            if (contrato != null) {
                String mod = contrato.getVehiculoModelo();
                String pat = contrato.getVehiculoPatente();

                // Si la patente no está seteada en el objeto Contrato, intentar resolverla mediante VehiculoDAO
                if ((pat == null || pat.trim().isEmpty() || pat.equals("-")) && contrato.getIdAsigna() != null) {
                    try {
                        modelo.Vehiculo v = new dao.VehiculoDAO().buscarPorId(contrato.getIdAsigna());
                        if (v != null) {
                            if (v.getPat() != null && !v.getPat().trim().isEmpty()) {
                                pat = v.getPat().trim();
                            }
                            if ((mod == null || mod.trim().isEmpty() || mod.equalsIgnoreCase("Sin asignar")) && v.getMod() != null) {
                                mod = ((v.getMarca() != null ? v.getMarca() : "") + " " + v.getMod()).trim();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                boolean tieneModelo = mod != null && !mod.trim().isEmpty() && !mod.equalsIgnoreCase("Sin asignar");
                boolean tienePatente = pat != null && !pat.trim().isEmpty() && !pat.equals("-");

                if (tieneModelo && tienePatente) {
                    vehiculoTexto = mod + " (Patente: " + pat + ")";
                } else if (tienePatente) {
                    vehiculoTexto = "Patente: " + pat;
                } else if (tieneModelo) {
                    vehiculoTexto = mod;
                }
            }
            if (vehiculoTexto.isEmpty()) {
                vehiculoTexto = "No asignado";
            }

            Paragraph pVehiculo = new Paragraph();
            pVehiculo.setAlignment(Element.ALIGN_LEFT);
            pVehiculo.add(new Chunk("Vehículo asociado: ", fontEtiqueta));
            pVehiculo.add(new Chunk(vehiculoTexto, fontValor));
            pVehiculo.setSpacingAfter(14f);
            document.add(pVehiculo);

            // 5. Historial de Pagos hasta el momento
            // Filtrar los pagos realizados hasta este pago inclusive (si el pago tiene ID asignado)
            List<Pago> pagosHastaElMomento = new ArrayList<>();
            if (historialPagos != null && !historialPagos.isEmpty()) {
                if (pago.getId() != null) {
                    boolean encontrado = false;
                    for (Pago p : historialPagos) {
                        pagosHastaElMomento.add(p);
                        if (p.getId() != null && p.getId().equals(pago.getId())) {
                            encontrado = true;
                            break;
                        }
                    }
                    if (!encontrado) {
                        pagosHastaElMomento.clear();
                        for (Pago p : historialPagos) {
                            if (p.getId() != null && p.getId() <= pago.getId()) {
                                pagosHastaElMomento.add(p);
                            }
                        }
                        if (pagosHastaElMomento.isEmpty()) {
                            pagosHastaElMomento.add(pago);
                        }
                    }
                } else {
                    pagosHastaElMomento.addAll(historialPagos);
                }
            } else {
                pagosHastaElMomento.add(pago);
            }

            Paragraph pTituloHistorial = new Paragraph("Historial de Pagos hasta el momento:", fontSubtitulo);
            pTituloHistorial.setSpacingAfter(6f);
            document.add(pTituloHistorial);

            // Tabla con columnas: N° Pago, Fecha, Medio de Pago, Importe
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100f);
            tabla.setWidths(new float[]{18f, 24f, 33f, 25f});
            tabla.setSpacingAfter(12f);

            // Cabeceras de tabla
            agregarCeldaCabecera(tabla, "N° Pago", fontTableHeader);
            agregarCeldaCabecera(tabla, "Fecha", fontTableHeader);
            agregarCeldaCabecera(tabla, "Medio de Pago", fontTableHeader);
            agregarCeldaCabecera(tabla, "Importe", fontTableHeader);

            double totalAbonadoAcumulado = 0.0;
            BaseColor colorBorde = new BaseColor(220, 224, 230);
            BaseColor colorFilaPar = new BaseColor(248, 250, 252);
            BaseColor colorFilaImpar = BaseColor.WHITE;

            for (int i = 0; i < pagosHastaElMomento.size(); i++) {
                Pago p = pagosHastaElMomento.get(i);
                BaseColor fondoFila = (i % 2 == 0) ? colorFilaPar : colorFilaImpar;

                String idP = (p.getId() != null) ? "#" + p.getId() : "-";
                String fecP = (p.getFec() != null) ? FormatoFecha.formatearFechaGui(p.getFec()) : "-";
                String medP = (p.getMedio() != null) ? p.getMedio().getDescripcion() : "Efectivo";
                double impP = (p.getImp() != null) ? p.getImp() : 0.0;
                totalAbonadoAcumulado += impP;

                agregarCeldaDato(tabla, idP, fontTableCell, fondoFila, colorBorde, Element.ALIGN_CENTER);
                agregarCeldaDato(tabla, fecP, fontTableCell, fondoFila, colorBorde, Element.ALIGN_CENTER);
                agregarCeldaDato(tabla, medP, fontTableCell, fondoFila, colorBorde, Element.ALIGN_LEFT);
                agregarCeldaDato(tabla, String.format("$%.2f", impP), fontTableCellBold, fondoFila, colorBorde, Element.ALIGN_RIGHT);
            }

            document.add(tabla);

            // 6. Detalle del Pago, Liquidación y Saldo Restante
            double totalContrato = 0.0;
            if (contrato != null) {
                double alq = contrato.getImpAlq() != null ? contrato.getImpAlq() : 0.0;
                double car = contrato.getCargos() != null ? contrato.getCargos() : 0.0;
                totalContrato = alq + car;
            }

            double saldoRestante = Math.max(0.0, totalContrato - totalAbonadoAcumulado);

            Paragraph pDetalleTitulo = new Paragraph("Liquidación del Contrato:", fontSubtitulo);
            pDetalleTitulo.setSpacingAfter(6f);
            document.add(pDetalleTitulo);

            double montoPagoActual = (pago.getImp() != null) ? pago.getImp() : 0.0;

            // Línea discontinua que conecta cada consigna con su monto alineado a la derecha
            DottedLineSeparator separadorPuntos = new DottedLineSeparator();
            separadorPuntos.setLineColor(new BaseColor(180, 185, 195));
            separadorPuntos.setGap(4.5f);
            separadorPuntos.setLineWidth(1.0f);

            agregarItemLiquidacion(document, "Monto total del contrato:", String.format("$%.2f", totalContrato), fontEtiqueta, fontValor, separadorPuntos, 3.5f);
            agregarItemLiquidacion(document, "Monto abonado:", String.format("$%.2f", montoPagoActual), fontEtiqueta, fontValor, separadorPuntos, 3.5f);
            agregarItemLiquidacion(document, "Total abonado hasta el momento:", String.format("$%.2f", totalAbonadoAcumulado), fontEtiqueta, fontValor, separadorPuntos, 3.5f);
            agregarItemLiquidacion(document, "Saldo restante a pagar:", String.format("$%.2f", saldoRestante), fontEtiqueta, fontValor, separadorPuntos, 18f);

            // 7. Pie de página: Centrado, 'Gracias por confiar en nosotros.'
            Paragraph piePagina = new Paragraph("Gracias por confiar en nosotros.", fontPie);
            piePagina.setAlignment(Element.ALIGN_CENTER);
            piePagina.setSpacingBefore(18f);
            document.add(piePagina);

            document.close();

            // Copia de respaldo en comprobante_pago.pdf
            try {
                java.nio.file.Files.copy(
                        archivoDestino.toPath(),
                        new File("comprobante_pago.pdf").toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            } catch (Exception ignored) {
            }

            this.ultimoArchivoGenerado = archivoDestino;
            return archivoDestino;
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error al generar el comprobante PDF con iText 5: " + e.getMessage(), e);
        }
    }

    private void agregarCeldaCabecera(PdfPTable tabla, String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(new BaseColor(241, 245, 249));
        celda.setBorderColor(new BaseColor(203, 213, 225));
        celda.setPadding(6f);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }

    private void agregarCeldaDato(PdfPTable tabla, String texto, Font font, BaseColor fondo, BaseColor borde, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(borde);
        celda.setPadding(5f);
        celda.setHorizontalAlignment(alineacion);
        tabla.addCell(celda);
    }

    private void agregarItemLiquidacion(Document doc, String etiqueta, String valor, Font fontEtiqueta, Font fontValor, DottedLineSeparator separador, float spacingAfter) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiqueta + " ", fontEtiqueta));
        p.add(new Chunk(separador));
        p.add(new Chunk(" " + valor, fontValor));
        p.setSpacingAfter(spacingAfter);
        doc.add(p);
    }

    public File getUltimoArchivoGenerado() {
        return ultimoArchivoGenerado;
    }
}
