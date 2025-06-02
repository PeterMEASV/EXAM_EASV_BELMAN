package exam_easv_belman.BLL;

import exam_easv_belman.BE.Product;
import exam_easv_belman.BLL.exceptions.BelmanBLLException;
import exam_easv_belman.BLL.util.PdfGeneratorUtil;
import exam_easv_belman.GUI.Models.ProductModel;
import exam_easv_belman.BLL.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class QCReportManager {
    private PhotoManager photoManager;
    private ProductModel productModel;



    public QCReportManager() throws BelmanBLLException {
        try {
            this.photoManager = new PhotoManager();
            this.productModel = new ProductModel();
        } catch (BelmanBLLException e) {
            throw e;
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to initialize QCReportManager components.", e);
        }
    }

    /**
     * Generates a QC Report PDF based on the session details (current user, order, and product).
     *
     * @param outputFilePath The file path where the PDF will be saved.
     * @param email          The email to include in the PDF metadata or to send the report.
     * @param comment        Additional comments to include in the report.
     * @param mainStage      The main application stage, used for displaying dialogs or file previews.
     * @throws BelmanBLLException If an error occurs during the generation process.
     */
    public void generateQCReportPDF(String outputFilePath,
                                    String email,
                                    String comment,
                                    Stage mainStage) throws BelmanBLLException {
        try {
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
                    true,
                    mainStage,
                    null,
                    null,
                    null,
                    productModel,
                    photoManager,
                    qcName,
                    qcSignaturePath,
                    formattedUniqueOperators
            );
        } catch (BelmanBLLException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof BelmanBLLException) {
                throw (BelmanBLLException) e.getCause();
            }
            throw new BelmanBLLException("Error during QC report PDF generation: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BelmanBLLException("An unexpected error occurred during QC report PDF generation.", e);
        }

    }

}

