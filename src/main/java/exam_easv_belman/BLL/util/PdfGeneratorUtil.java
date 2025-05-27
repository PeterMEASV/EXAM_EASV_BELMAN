package exam_easv_belman.BLL.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.Paragraph;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.PhotoManager;
import exam_easv_belman.GUI.Models.ProductModel;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.CompressionConstants;
import java.io.File;

public class PdfGeneratorUtil {

    public static void generatePdf(String filePath,
                                   String email,
                                   String comment,
                                   String orderNumber,
                                   Boolean delete,
                                   Stage mainStage,
                                   List<String> headers,
                                   List<String> photoPaths,
                                   List<String> imageComments,
                                   ProductModel productModel,
                                   PhotoManager photoManager,
                                   String qcName,
                                   String qcSignaturePath,
                                   String opName) throws Exception {
        // Configure PDF writer with compression
        WriterProperties writerProperties = new WriterProperties()
                .setCompressionLevel(CompressionConstants.BEST_COMPRESSION);
        PdfWriter writer = new PdfWriter(filePath, writerProperties);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument, PageSize.A4);
        //document.setMargins(20, 20, 20, 20);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // TITLE
        document.add(new Paragraph("Quality Control Report")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" ")); // Add space

        // Metadata
        document.add(new Paragraph("Order Number: " + orderNumber).setFont(font).setFontSize(12));
        document.add(new Paragraph("Prepared by (QC): " + qcName).setFont(font).setFontSize(12));

        // Unique Operators
        document.add(new Paragraph("Operators: " + opName).setFont(font).setFontSize(12));


        // General Comments
        document.add(new Paragraph("General Comments: ").setFont(font).setFontSize(12).setItalic());
        document.add(new Paragraph(comment).setFont(font).setFontSize(10));
        document.add(new Paragraph(" ")); // Add space after comments

        // FETCH PRODUCTS AND ADD TO PDF
        ObservableList<Product> productsForOrder = productModel.getProductsForOrder(orderNumber);

        for (Product product : productsForOrder) {
            // Get photos for this product
            List<Photo> productPhotos = photoManager.getImagesForProduct(product.getProduct_number());
            if (productPhotos == null || productPhotos.isEmpty()) {
                continue; // Skip this product if it has no photos
            }

            // Add Product Header
            String productHeader = "Product: " + product.getProduct_number();
            document.add(new Paragraph(productHeader)
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setUnderline());

            // Add Photos and Related Data

            for (Photo photo : productPhotos) {
                try {
                    // Read and compress the image
                    BufferedImage originalImage = ImageIO.read(new File(photo.getFilepath()));

                    // Calculate new dimensions while maintaining aspect ratio
                    int targetWidth = 800;  // You can adjust these values
                    int targetHeight = (int) (originalImage.getHeight() * (targetWidth / (double) originalImage.getWidth()));

                    // Create compressed version
                    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
                    g.dispose();

                    // Save to temporary file with compression
                    File tempFile = File.createTempFile("compressed", ".jpg");
                    tempFile.deleteOnExit();
                    
                    // Set up the image writer with compression
                    ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                    ImageWriteParam imgWriteParam = imgWriter.getDefaultWriteParam();
                    imgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    imgWriteParam.setCompressionQuality(0.5f);

                    // Write the compressed image
                    try (ImageOutputStream ios = ImageIO.createImageOutputStream(tempFile)) {
                        imgWriter.setOutput(ios);
                        imgWriter.write(null, new IIOImage(resizedImage, null, null), imgWriteParam);
                    }
                    imgWriter.dispose();

                    // Add the compressed image to PDF
                    ImageData imageData = ImageDataFactory.create(tempFile.toPath().toString());
                    Image image = new Image(imageData)
                            .scaleToFit(350, 250)
                            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                    document.add(image);

                    // Add Photo Metadata
                    String uploaderName = "Unknown";
                    try {
                        User uploader = photoManager.getUserById(photo.getUploadedBy());
                        if (uploader != null) {
                            uploaderName = uploader.getFirstName() + " " + uploader.getLastName();
                        }
                    } catch (Exception ignored) {
                        // If user details cannot be fetched, continue with default "Unknown"
                    }

                    String uploadTime = photo.getUploadTime()
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                    document.add(new Paragraph("Uploaded by: " + uploaderName).setFont(font).setFontSize(10));
                    document.add(new Paragraph("Upload Time (EU): " + uploadTime).setFont(font).setFontSize(10));

                    // Add photo-specific comment
                    String imageComment = photo.getComment();
                    document.add(new Paragraph("Comment: " +
                            (imageComment != null && !imageComment.isBlank() ? imageComment : "No comment provided."))
                            .setFont(font).setFontSize(10));

                    // Add spacing after each photo
                    document.add(new Paragraph(" "));
                } catch (Exception e) {
                    document.add(new Paragraph("Failed to display photo.")
                            .setFontColor(ColorConstants.RED)
                            .setFont(font)
                            .setFontSize(10));
                    e.printStackTrace();
                }
            }

            // Add spacing after each product
            document.add(new Paragraph(" "));
        }

        /*
        // FOOTER
        document.add(new Paragraph("Report Generated for: " + email)
                .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT).setMarginTop(20));
        // QC Signature
        if (qcSignaturePath != null && !qcSignaturePath.isEmpty()) {
            ImageData imageData = ImageDataFactory.create(qcSignaturePath);
            Image signature = new Image(imageData)
                    .scaleToFit(300, 150)
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.LEFT);
            document.add(signature);
        }

         */
        addFooter(pdfDocument, email, qcSignaturePath);


        document.close();
    }

    private static void addFooter(PdfDocument pdfDocument, String email, String qcSignaturePath) {
        PdfPage page = pdfDocument.getLastPage();
        Rectangle pageSize = page.getPageSize();
        Canvas canvas = new Canvas(page, pageSize);

        // Add the footer text
        Paragraph footerText = new Paragraph("Report Generated for: " + email)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);

        // Position footer text at the bottom-right of the page
        canvas.showTextAligned(footerText,
                pageSize.getWidth() - 36, //right margin offset
                30, // bottom edge
                TextAlignment.RIGHT);

        // Add QC Signature, if present
        if (qcSignaturePath != null && !qcSignaturePath.isEmpty()) {
            try {
                // Create the QC signature image
                ImageData imageData = ImageDataFactory.create(qcSignaturePath);
                Image signature = new Image(imageData).scaleToFit(100, 50); // Scale the image (adjust size as needed)

                // Set "Signed by:" text
                Paragraph signedByParagraph = new Paragraph("Signed by:")
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.LEFT);

                // Define positions for "Signed by:" text and the QC signature
                float marginLeft = 36; // Starting left margin for both
                float signatureY = 15; // Y position for both elements (at the bottom of the page)
                float textX = marginLeft; // X position for "Signed by:" text
                float signatureX = marginLeft + 50; // X position for the signature (offset based on text width)

                // Draw "Signed by:" text
                canvas.showTextAligned(signedByParagraph,
                        textX, // Position to the left of the signature
                        signatureY + 15, // Slight alignment adjustment if needed
                        TextAlignment.LEFT);

                // Position the QC signature to the right of "Signed by:"
                signature.setFixedPosition(signatureX, signatureY); // X defines right of text
                canvas.add(signature);

            } catch (Exception e) {
                System.err.println("Error inserting QC signature: " + e.getMessage());
            }
        }


        // Close canvas after drawing
        canvas.close();
    }



    public static List<String> getUniqueOperators(ObservableList<Product> productsForOrder,
                                                  PhotoManager photoManager) throws Exception {
        Set<String> operatorNames = new HashSet<>();

        for (Product product : productsForOrder) {
            // Get photos for the product
            ObservableList<Photo> productPhotos = photoManager.getImagesForProduct(product.getProduct_number());

            // Extract uploader names
            for (Photo photo : productPhotos) {
                try {
                    User uploader = photoManager.getUserById(photo.getUploadedBy());
                    if (uploader != null) {
                        String fullName = uploader.getFirstName() + " " + uploader.getLastName();
                        operatorNames.add(fullName);
                    }
                } catch (Exception ignored) {
                    // Skip if uploader info is missing
                }
            }
        }

        // Return sorted list of operator names
        return operatorNames.stream().sorted().toList();
    }



}