package exam_easv_belman.BLL;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BE.Tag;
import exam_easv_belman.BE.User;
import exam_easv_belman.DAL.IPhotoDataAccess;
import exam_easv_belman.DAL.ITagDataAccess;
import exam_easv_belman.DAL.PhotoDAO;
import exam_easv_belman.DAL.TagDAO;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.List;

public class PhotoManager {
    private IPhotoDataAccess photoDataAccess;
    private ITagDataAccess tagDataAccess;

    public PhotoManager() throws Exception {
        try {
            photoDataAccess = new PhotoDAO();
            tagDataAccess = new TagDAO();
        } catch (Exception e) {
            throw new Exception();
            //TODO exception handling
        }
    }

    public boolean saveImageAndPath(List<BufferedImage> images,
                                    List<String> fileNames,
                                    User uploader,
                                    String productNumber,
                                    String tag) throws Exception {
        return photoDataAccess.saveImageAndPath(images, fileNames, uploader, productNumber, tag);
    }
    public ObservableList<Photo> getImagesForOrder(String orderNumber) throws SQLException {
        return photoDataAccess.getImagesForOrder(orderNumber);
    }

    public void deleteImage(Photo photo) throws SQLException {
        photoDataAccess.deleteImageFromDatabase(photo);
    }

    public ObservableList<Photo> getImagesForProduct(String productNumber) throws SQLException {
        return photoDataAccess.getImagesForProduct(productNumber);
    }

    public void addCommentToPhoto(String comment, Photo photo) throws SQLException {
        photoDataAccess.addCommentToPhoto(comment, photo);
    }

    public void addTagToPhoto(Photo photo, Tag tag) throws SQLException {
        tagDataAccess.addTagToPhoto(photo, tag);
    }
}
