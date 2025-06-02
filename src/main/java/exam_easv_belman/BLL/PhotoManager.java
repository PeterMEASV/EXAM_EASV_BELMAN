package exam_easv_belman.BLL;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.exceptions.BelmanBLLException;
import exam_easv_belman.DAL.IPhotoDataAccess;
import exam_easv_belman.DAL.PhotoDAO;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.List;

public class PhotoManager {
    private IPhotoDataAccess photoDataAccess;


    public PhotoManager() throws BelmanBLLException {
        try {
            photoDataAccess = new PhotoDAO();
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to Load PhotoDataAccess", e);
        }
    }

    public boolean saveImageAndPath(List<BufferedImage> images,
                                    List<String> fileNames,
                                    User uploader,
                                    String productNumber,
                                    String tag) throws BelmanBLLException {
        try {
            return photoDataAccess.saveImageAndPath(images, fileNames, uploader, productNumber, tag);
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to save image and path for product: " + productNumber, e);
        }
    }
    public ObservableList<Photo> getImagesForOrder(String orderNumber) throws BelmanBLLException {
        try {
            return photoDataAccess.getImagesForOrder(orderNumber);
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to get images for order: " + orderNumber, e);
        }
    }

    public void deleteImage(Photo photo) throws  BelmanBLLException {
        try {
            photoDataAccess.deleteImageFromDatabase(photo);
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to delete image with ID: " + (photo != null ? photo.getId() : "null"), e);
        }
    }

    public ObservableList<Photo> getImagesForProduct(String productNumber) throws BelmanBLLException{
        try {
            return photoDataAccess.getImagesForProduct(productNumber);
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to get images for product: " + productNumber, e);
        }
    }

    public void addCommentToPhoto(String comment, Photo photo) throws BelmanBLLException {
        try {
            photoDataAccess.addCommentToPhoto(comment, photo);
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to add comment to photo with ID: " + (photo != null ? photo.getId() : "null"), e);
        }
    }


    //Probably not the currect way of doing this
    public User getUserById(int userId) throws BelmanBLLException {
        try{
            UserManager userManager = new UserManager();
            ObservableList<User> allUsers = userManager.getAllUsers();

            for (User user : allUsers) {
                if (user.getId() == userId) {
                    return user;
                }
            }

            throw new BelmanBLLException("User not found for ID: " + userId);
        } catch (BelmanBLLException e){
            throw e;
        } catch (Exception e){throw new BelmanBLLException("Error retrieving user by ID: " + userId, e);
        }

    }

    public void changeVeirfyState(Photo photo, int approval) throws BelmanBLLException {
        try {
            photoDataAccess.changeVeirfyState(photo, approval);
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to change verify state for photo with ID: " + (photo != null ? photo.getId() : "null"), e);
        }
    }


}
