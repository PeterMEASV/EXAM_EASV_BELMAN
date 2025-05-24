package exam_easv_belman.BLL;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.util.PdfGeneratorUtil;
import exam_easv_belman.GUI.Models.ProductModel;
import exam_easv_belman.GUI.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QCReportManager {
    private PhotoManager photoManager;
    private ProductModel productModel;



    public QCReportManager() throws Exception {
        this.photoManager = new PhotoManager();
        this.productModel = new ProductModel();
    }

    /**
     * Generates a QC Report PDF based on the session details (current user, order, and product).
     *
     * @param outputFilePath The file path where the PDF will be saved.
     * @param email          The email to include in the PDF metadata or to send the report.
     * @param comment        Additional comments to include in the report.
     * @param mainStage      The main application stage, used for displaying dialogs or file previews.
     * @throws Exception If an error occurs during the generation process.
     */
    public void generateQCReportPDF(String outputFilePath,
                                    String email,
                                    String comment,
                                    Stage mainStage) throws Exception {
        // Retrieve session information
        String orderNumber = SessionManager.getInstance().getCurrentOrderNumber();
        String qcName = SessionManager.getInstance().getCurrentUser().getFirstName() + " " +
                SessionManager.getInstance().getCurrentUser().getLastName();
        String qcSignaturePath = SessionManager.getInstance().getCurrentUser().getSignaturePath();

        // Fetch all products in the order
        ObservableList<Product> productsForOrder = productModel.getProductsForOrder(orderNumber);

        // Filter out products with no associated images
        ObservableList<Product> filteredProducts = FXCollections.observableList(
                productsForOrder.stream()
                        .filter(product -> {
                            try {
                                return !photoManager.getImagesForProduct(product.getProduct_number()).isEmpty();
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        })
                        .collect(Collectors.toList())
        );

        // Retrieve unique operators for the filtered products
        List<String> uniqueOperators = PdfGeneratorUtil.getUniqueOperators(filteredProducts, photoManager);

        // Format the unique operator names for the PDF
        String formattedUniqueOperators = String.join(", ", uniqueOperators);

        // Pass the required data to the PDF generator
        PdfGeneratorUtil.generatePdf(
                outputFilePath,
                email,
                comment,
                orderNumber,
                true, // Option to delete files/resources if necessary
                mainStage,
                null, // Headers - unused in this flow
                null, // Photo paths - handled internally in PdfGeneratorUtil
                null, // Image comments - handled internally in PdfGeneratorUtil
                productModel,
                photoManager,
                qcName,
                qcSignaturePath,
                formattedUniqueOperators // Unique operator list
        );
    }

}

