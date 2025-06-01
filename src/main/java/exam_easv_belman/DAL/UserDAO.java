package exam_easv_belman.DAL;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import exam_easv_belman.BE.Role;
import exam_easv_belman.BE.User;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDataAccess {

    private DBConnector dbConnector;

    public UserDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    /**
     * finds a user by their username in the database
     * @param username the username of the user to find
     * @return the user that was found
     * @throws Exception if an error occurs during the database operation
     */
    @Override
    public User findByUsername(String username) throws Exception {
        String sql = "SELECT * FROM dbo.Users WHERE username = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User();

                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password_hash"));
                user.setRole(resultSet.getInt("role_id") == 1 ? Role.ADMIN : resultSet.getInt("role_id") == 2 ? Role.OPERATOR : Role.QC);
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setEmail(resultSet.getString("email"));
                user.setPhoneNumber(resultSet.getString("phone"));
                user.setSignaturePath(resultSet.getString("signature_path"));

                user.setQrKey(resultSet.getString("qr_key"));

                return user;
            }

        } catch (SQLException e) {
            throw new Exception();
        }
        return null;
    }

    /**
     * creates a new user in the database
     * @param user the user to create
     * @return the user that was created
     * @throws Exception if an error occurs during the database operation
     */
    @Override
    public User createUser(User user) throws Exception {

        String sql = "INSERT INTO Users (username, password_hash, role_id, first_Name, last_Name," +
                "email, phone, qr_key, signature_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try (Connection connection = dbConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());

            //ROLE IDS ARE ADMIN = 1, OPERATOR = 2, QC = 3, this is hardcoded.
            //ternary operator used instead of if statements, could alternatively be done with switch statements.
            int roleId = user.getRole() == Role.ADMIN ? 1 : user.getRole() == Role.OPERATOR ? 2 : 3;
            statement.setInt(3, roleId);

            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getLastName());
            statement.setString(6, user.getEmail());
            statement.setString(7, user.getPhoneNumber());
            statement.setString(8, user.getQrKey());
            statement.setString(9, user.getSignaturePath());


            statement.executeUpdate();

            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }

            return user;

        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    /**
     * deletes a user from the database
     * @param user the user to delete
     */
    @Override
    public void deleteUser(User user) {
        String sql = "DELETE FROM Users WHERE id = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, user.getId());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            AlertHelper.showAlert("Error", "Error Deleting User", Alert.AlertType.ERROR);
            e.printStackTrace();
        }

    }

    /**
     * gets all users from the database
     * @return a list of all users in the database
     * @throws Exception if an error occurs during the database operation
     */
    @Override
    public ObservableList<User> getAllUsers() throws Exception {

        String sql = "SELECT * FROM dbo.Users";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();
            ObservableList<User> users = FXCollections.observableArrayList();

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password_hash"));

                //ROLE IDS ARE ADMIN = 1, OPERATOR = 2, QC = 3, this is hardcoded.
                //ternary operator used instead of if statements, could alternatively be done with switch statements.
                int roleId = resultSet.getInt("role_id");
                user.setRole(roleId == 1 ? Role.ADMIN : roleId == 2 ? Role.OPERATOR : Role.QC);

                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setEmail(resultSet.getString("email"));
                user.setPhoneNumber(resultSet.getString("phone"));
                user.setQrKey(resultSet.getString("qr_key"));
                users.add(user);
            }
            return users;

        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    /**
     * Updates a user in the database.
     * @param user the user to update
     * @throws SQLServerException if an error occurs during the database operation
     */
    @Override
    public void updateUser(User user) throws SQLServerException {

        String sql = "UPDATE Users SET username = ?, password_hash = ?, role_id = ?, first_Name = ?, last_Name = ?, email = ?, phone = ? WHERE id = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setInt(3, user.getRole() == Role.ADMIN ? 1 : user.getRole() == Role.OPERATOR ? 2 : 3);
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getLastName());
            statement.setString(6, user.getEmail());
            statement.setString(7, user.getPhoneNumber());
            statement.setInt(8, user.getId());

            statement.executeUpdate();
            System.out.println("user updated");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attaches a signature to a user in the database.
     * @param user the user for which to attach the signature
     * @throws Exception if an error occurs during the database operation
     */
    public void attachSignature(User user) throws Exception {
        String sql = "UPDATE Users SET signature_path = ? WHERE id = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1,user.getSignaturePath());
            statement.setInt(2, user.getId());
            statement.executeUpdate();
        } catch (Exception e) {
            AlertHelper.showAlert("Error", "Error attaching signature", Alert.AlertType.ERROR);
            e.printStackTrace();
        }

    }
}
