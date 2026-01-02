package com.group18.greengrocer.util;

import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Order;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.InputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;

public class PDFGenerator {

    /**
     * Generates a PDF invoice for the given order and returns it as a Base64 encoded String.
     * This String can be safely stored in the database as CLOB/LONGTEXT.
     *
     * @param order The order for which the invoice will be generated.
     * @return Base64 encoded PDF invoice.
     */
    public static String generateInvoice(Order order) {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            // Add Logo
            // Add Logo
            String logoPath = "/com/group18/greengrocer/images/logo.png";
            System.out.println("Attempting to load logo from: " + logoPath);
            try (InputStream logoStream = PDFGenerator.class.getResourceAsStream(logoPath)) {
                if (logoStream != null) {
                    ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = logoStream.read(buffer)) != -1) {
                        imageBuffer.write(buffer, 0, len);
                    }
                    byte[] logoBytes = imageBuffer.toByteArray();
                    
                    PDImageXObject logoImage = PDImageXObject.createFromByteArray(document, logoBytes, "logo");
                    // Draw at top right corner (A4 width ~595, height ~842)
                    content.drawImage(logoImage, 450, 750, 100, 50);
                    System.out.println("SUCCESS: Logo added to invoice PDF.");
                } else {
                    System.err.println("ERROR: Logo stream is null. File not found at resource path: " + logoPath);
                }
            } catch (Exception e) {
                System.err.println("EXCEPTION: Could not load logo for invoice. " + e.getMessage());
                e.printStackTrace();
            }

            content.setFont(PDType1Font.HELVETICA_BOLD, 16);
            content.beginText();
            content.setLeading(18f);
            content.newLineAtOffset(50, 770);
            content.showText("GREEN GROCER - INVOICE");
            content.newLine();
            content.newLine();

            content.setFont(PDType1Font.HELVETICA, 12);

            content.showText("Order ID: " + order.getId());
            content.newLine();

            content.showText("Customer ID: " + order.getCustomerId());
            content.newLine();

            content.showText("Status: " + sanitizeText(order.getStatus().toString()));
            content.newLine();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            content.showText("Order Date: " + sdf.format(order.getOrderTime()));
            content.newLine();
            content.newLine();

            content.showText("--------------------------------------------");
            content.newLine();

            // Table Header
            content.showText(String.format("%-20s %-8s %-8s %-8s",
                    "Product", "Qty", "Price", "Total"));
            content.newLine();
            content.showText("--------------------------------------------");
            content.newLine();

            double subtotal = 0.0;

            for (CartItem item : order.getItems()) {
                double lineTotal = item.getTotalPrice();
                subtotal += lineTotal;

                content.showText(String.format("%-20s %-8.2f %-8.2f %-8.2f",
                        sanitizeText(item.getProduct().getName()),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        lineTotal));
                content.newLine();
            }

            content.newLine();
            content.showText("--------------------------------------------");
            content.newLine();

            double vat = subtotal * 0.18;
            double finalTotal = order.getTotalCost();
            double discount = subtotal + vat - finalTotal;

            content.showText(String.format("Subtotal: %.2f", subtotal));
            content.newLine();

            content.showText(String.format("VAT (18%%): %.2f", vat));
            content.newLine();

            content.showText(String.format("Discount: -%.2f", discount));
            content.newLine();
            content.newLine();

            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.showText(String.format("TOTAL: %.2f", finalTotal));

            content.endText();
            content.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }


    private static String sanitizeText(String input) {
        if (input == null) return "";
        return input.replace("ı", "i")
                    .replace("İ", "I")
                    .replace("ğ", "g")
                    .replace("Ğ", "G")
                    .replace("ü", "u")
                    .replace("Ü", "U")
                    .replace("ş", "s")
                    .replace("Ş", "S")
                    .replace("ö", "o")
                    .replace("Ö", "O")
                    .replace("ç", "c")
                    .replace("Ç", "C");
    }
}
