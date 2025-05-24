package exam_easv_belman.DAL;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BE.User;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IPhotoDataAccess {

    //TODO add the mandatory methods that have been implemented in PhotoDAO


    void insertImagePathToDatabase(Connection connection, List<Path> filePath, User uploader, Product product, String tag) throws SQLException;

    void deleteImageFromDatabase(Photo photo) throws SQLException;

    boolean saveImageAndPath(List<BufferedImage> photos, List<String> fileNames, User uploader, String productNumber, String tag) throws Exception;

    ObservableList<Photo> getImagesForProduct(String productNumber) throws SQLException;

    ObservableList<Photo> getImagesForOrder(String orderNumber) throws SQLException;

    Product getProductFromNumber(String photoNumber) throws SQLException;

    void addCommentToPhoto(String comment, Photo photo) throws SQLException;
}
