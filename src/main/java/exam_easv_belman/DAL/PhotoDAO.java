package exam_easv_belman.DAL;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BE.User;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhotoDAO implements IPhotoDataAccess{

    private static final DateTimeFormatter FileNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private DBConnector dbConnector;
    private final Path baseRelativePath = Paths.get("QC_Images");

    public PhotoDAO() throws Exception {
        dbConnector = new DBConnector();

    }


    public boolean saveImageAndPath(List<BufferedImage> photos,
                                    List<String> fileNames,
                                    User uploader,
                                    String productNumber,
                                    String tag) throws Exception {

        //check if the lists are of the same size, if not throw an exception.
        if (photos.size() != fileNames.size()) {
            throw new IllegalArgumentException("Photos and paths must be of same size");
        }
        Connection connection = null;
        List<Path> persistedPaths = new ArrayList<>();

        try {
            connection = dbConnector.getConnection();
            connection.setAutoCommit(false);

            Path orderFolderPath = baseRelativePath.resolve(productNumber + "_Images");
            persistedPaths = saveImages(photos, fileNames, orderFolderPath);

            insertImagePathToDatabase(connection, persistedPaths, uploader, getProductFromNumber(productNumber), tag);

            connection.commit();
            return true;

        } catch (Exception e) {
            if (connection != null) {
                try {
                    //roll back the transaction if ANYTHING goes wrong.
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    //TODO exception handling
                }
            }
            deleteFiles(persistedPaths);
            throw e;
            //TODO exception handling
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    //TODO exception handling
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Path> saveImages(List<BufferedImage> photos,
                                  List<String> fileNames,
                                  Path orderFolderPath) throws IOException {
       //if the dir doesn't exist, then make it. if it does, do nothing(IDEMPOTENT).
       Files.createDirectories(orderFolderPath);

       Path tempDir = Files.createTempDirectory(orderFolderPath, "temp_images_");
       List<Path> tempFilePaths = new ArrayList<>(photos.size());
       List<Path> movedFilePaths = new ArrayList<>(photos.size());

       try {
           LocalDateTime now = LocalDateTime.now();
           for (int i = 0; i < photos.size(); i++) {
               //give the files unique names using DateTime + index + file extension.
               String uniqueFileName = fileNames.get(i) + "_" + FileNameFormatter.format(now) + "_" + i + ".png" ;
               Path tempFilePath = tempDir.resolve(uniqueFileName);
               ImageIO.write(photos.get(i), "png", tempFilePath.toFile());
               tempFilePaths.add(tempFilePath);
           }

           for (Path temp : tempFilePaths) {
               Path dest = orderFolderPath.resolve(temp.getFileName());
               if (Files.exists(dest)) {
                   throw new IOException("File already exists" + dest.toString());
               }
               Files.move(temp, dest, StandardCopyOption.ATOMIC_MOVE);
               movedFilePaths.add(dest);
           }
           Files.deleteIfExists(tempDir);
           return movedFilePaths;
       } catch (IOException e) {
           deleteFiles(movedFilePaths);
           deleteRecursively(tempDir);
           throw e;
       }
    }

    private void deleteRecursively(Path tempDir) {
        if (tempDir == null || Files.notExists(tempDir)) {
            return;
        }
        //auto close stream after use.
        try (Stream<Path> walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignoredException) {
                    //Deletion failure is what it is
                    //ideally its logged or flagged so it can be manually deleted later.
                }
            });
        } catch (IOException ignoredException) {
            //same as previous catch
        }
    }

    private void deleteFiles(List<Path> movedFilePaths) {
        for (Path path : movedFilePaths) {
             try {
                 Files.deleteIfExists(path);
             } catch (IOException ignoredException) {
                 //Deletion failure is what it is
                 //ideally its logged or flagged so it can be manually deleted later.
             }
        }
    }


    @Override
    public void insertImagePathToDatabase(Connection connection,
                                          List<Path> filePaths,
                                          User uploader,
                                          Product product,
                                          String tag) throws SQLException {

        String sql = "INSERT INTO Photos (product_id, file_path, uploaded_by, uploaded_at, tag) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Path path : filePaths) {
                statement.setInt(1, product.getId());
                statement.setString(2, path.toString());
                statement.setInt(3, uploader.getId());
                statement.setObject(4, LocalDateTime.now());
                statement.setString(5, tag);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public ObservableList<Photo> getImagesForProduct(String productNumber) throws SQLException {
        ObservableList<Photo> photos = javafx.collections.FXCollections.observableArrayList();
        String sql = "SELECT p.* FROM Photos p INNER JOIN Products pr ON p.product_id = pr.id WHERE pr.products_order_number = ?";


        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, productNumber);

            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                Photo tempImg = new Photo();
                tempImg.setId(rs.getInt("id"));
                tempImg.setOrderNumber(rs.getString("product_id"));
                tempImg.setFilepath(rs.getString("file_path"));
                tempImg.setUploadedBy(rs.getInt("uploaded_by"));
                tempImg.setUploadTime(rs.getObject("uploaded_at", LocalDateTime.class));
                tempImg.setComment(rs.getString("comment"));
                tempImg.setTag(rs.getString("tag"));
                photos.add(tempImg);
            }
            System.out.println("length:" + photos.size());
            return photos;
        }
    }

    @Override
    public ObservableList<Photo> getImagesForOrder(String orderNumber) throws SQLException {
        //todo: make this??
        ObservableList<Photo> photos = FXCollections.observableArrayList();

        // Query database for photos matching the order number
        String query = "SELECT * FROM Photos WHERE OrderNumber LIKE ?"; // Partial match for 'orderNumber' prefix
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, orderNumber + "%"); // Match all products related to the given order
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Photo photo = new Photo();
                photo.setId(rs.getInt("Id"));
                photo.setOrderNumber(rs.getString("OrderNumber"));
                photo.setFilepath(rs.getString("Filepath"));
                photo.setUploadedBy(rs.getInt("UploadedBy"));
                photo.setUploadTime(rs.getTimestamp("UploadTime").toLocalDateTime());
                photo.setComment(rs.getString("Comment"));

                photos.add(photo);
            }
        }

        // Sort photos by product number extracted from the order number
        return photos.stream()
                .sorted(Comparator.comparing(photo -> extractProductNumber(photo.getOrderNumber())))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private int extractProductNumber(String orderNumber) {
        try {

            String[] parts = orderNumber.split("-");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }



    @Override
    public Product getProductFromNumber(String productNumber) throws SQLException {
        Product product = new Product();
        String sql = "SELECT * FROM Products WHERE products_order_number = ?";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                product.setId(rs.getInt("id"));
                product.setOrder_id(rs.getInt("order_id"));
                product.setProduct_number(rs.getString("products_order_number"));
            }
            return product;
        }

    }

    @Override
    public void addCommentToPhoto(String comment, Photo photo) throws SQLException {
        String sql = "UPDATE Photos SET comment = ? WHERE id = ?";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, comment);
            ps.setInt(2, photo.getId());
            ps.executeUpdate();
            System.out.println("comment added to photo with id " + photo.getId());
        }
    }

    @Override
    public void deleteImageFromDatabase(Photo photo) throws SQLException {
        String sql = "DELETE FROM Photos WHERE id = ?";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, photo.getId());
            ps.executeUpdate();
            System.out.println("photo with id " + photo.getId() + " deleted from database.");
        }
        catch(SQLServerException e){
            throw new SQLException(e);
        }

        try{
            Files.deleteIfExists(Paths.get(photo.getFilepath()));
        } catch (IOException e) {
            //TODO Burde nok smide et eller andet.. kører bare runtime for nu.
            throw new RuntimeException(e);
        }
    }
}
