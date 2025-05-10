package exam_easv_belman.BLL.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfPreviewUtil {

    public static void showPdfPreview(File pdfFile, Stage ownerStage) throws IOException{
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        VBox imageContainer = new VBox(10);
        imageContainer.setStyle("-fx-background-color: white, - fx-padding: 10;");


        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 150);
            Image fximage = SwingFXUtils.toFXImage(bufferedImage, null);
            ImageView imageView = new ImageView(fximage);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            imageContainer.getChildren().add(imageView);
        }
        document.close();

        ScrollPane scrollPane = new ScrollPane(imageContainer);
        scrollPane.setFitToWidth(true);

        Stage previewStage = new Stage();
        previewStage.initStyle(StageStyle.UNDECORATED);
        previewStage.initOwner(ownerStage);
        previewStage.initModality(Modality.NONE);
        previewStage.setScene(new Scene(scrollPane, 640, 800));
        previewStage.show();

        ownerStage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (previewStage.isShowing()){
                previewStage.close();

            }
        });

    }


}
