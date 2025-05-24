package exam_easv_belman.BLL.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
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

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

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

        // QC Signature
        if (qcSignaturePath != null && !qcSignaturePath.isEmpty()) {
            ImageData imageData = ImageDataFactory.create(qcSignaturePath);
            Image signature = new Image(imageData)
                    .scaleToFit(100, 50)
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.LEFT);
            document.add(signature);
        }

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
                    // Add Photo Image
                    ImageData imageData = ImageDataFactory.create(photo.getFilepath());
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
                    document.add(new Paragraph("Failed to display photo.").setFontColor(ColorConstants.RED).setFont(font).setFontSize(10));
                }
            }

            // Add spacing after each product
            document.add(new Paragraph(" "));
        }

        // FOOTER
        document.add(new Paragraph("Report Generated for: " + email)
                .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT).setMarginTop(20));

        // CLOSE DOCUMENT
        document.close();
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
