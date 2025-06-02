package exam_easv_belman.BLL;

import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.exceptions.BelmanBLLException;
import exam_easv_belman.BLL.util.PBKDF2PasswordUtil;
import exam_easv_belman.DAL.UserRepositoryImpl;
import exam_easv_belman.GUI.util.AlertHelper;
import exam_easv_belman.Repository.IUserRepository;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.apache.http.auth.InvalidCredentialsException;

import java.util.List;

public class UserManager {

    private IUserRepository userRepo;

    public UserManager() throws BelmanBLLException {
        try {
            userRepo = new UserRepositoryImpl();
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to initialize UserRepository.", e);
        }
    }

    /**
     * Authenticates a user based on their username and raw (plain text) password.
     *
     * This method first attempts to find a user by the provided username using the repository.
     * If a user is found, it then verifies the provided raw password against the user's
     * stored hashed password using PBKDF2PasswordUtil.
     * If the user is not found or if the password verification fails, an
     * InvalidCredentialsException is thrown.
     *
     * @param username The username of the user attempting to authenticate.
     * @param rawPassword The plain text password provided by the user.
     * @return The authenticated User object if the credentials are valid.
     * @throws InvalidCredentialsException If the username is not found or the password does not match.
     * @throws Exception If an error occurs during the password verification process or while fetching the user from the repository.
     */
    public User authenticateUser(String username, String rawPassword) throws Exception {
        User user = userRepo.findByUsername(username);
        if (user == null || !PBKDF2PasswordUtil.verifyPassword(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return user;
    }

    /**
     * Authenticates a user based on a provided QR key.
     *
     * This method retrieves all users from the repository and iterates through them,
     * comparing the provided QRKey with each user's stored hashed QR key.
     * If a match is found, the corresponding User object is returned.
     * If no match is found after checking all users, an error alert is displayed,
     * and null is returned.
     *
     * @param QRKey The raw QR key string to be verified.
     * @return The authenticated User object if the QR key is valid, otherwise null.
     * @throws Exception If an error occurs during the password verification process or while fetching users.
     */
    public User authenticateUser(String QRKey) throws Exception {
        List<User> allUsers = userRepo.findAll();
        for(User user : allUsers) {
            if(user.getQrKey() != null) {
                if (PBKDF2PasswordUtil.verifyPassword(QRKey, user.getQrKey())) {
                    return user;
                }
            }
        }
        AlertHelper.showAlert("Error", "Invalid QR Key", Alert.AlertType.ERROR);
        return null;
    }

    public User createUser(User user) throws BelmanBLLException {
        try {
            hashPassword(user);
            return userRepo.save(user);
        } catch (BelmanBLLException e) {
            throw e;
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to create user: " + user.getUsername(), e);
        }
    }

    public ObservableList<User> getAllUsers() throws BelmanBLLException {
        try {
            return userRepo.findAll();
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to retrieve all users.", e);
        }
    }

    public void deleteUser(User user) throws BelmanBLLException {
        try {
            userRepo.delete(user);
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to delete user: " + (user != null ? user.getUsername() : "null"), e);
        }
    }

    public void attachSignature(User user) throws BelmanBLLException {
        try {
            userRepo.attachSignature(user);
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to attach signature for user: " + (user != null ? user.getUsername() : "null"), e);
        }
    }

    /**
     * Updates the selectedUsers password and hashes it before saving it.
     * @param selectedUser
     * @param passwordChanged
     * @throws BelmanBLLException
     */
    public void updateUser(User selectedUser, boolean passwordChanged) throws BelmanBLLException {
        try {
            if (passwordChanged) {
                hashPassword(selectedUser);
            }
            userRepo.save(selectedUser);
        } catch (BelmanBLLException e) {
            throw e;
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to update user: " + (selectedUser != null ? selectedUser.getUsername() : "null"), e);
        }
    }

    /**
     * Hashes the raw password and QR key of the provided User object.
     * This method retrieves the plain text password and QR key from the User object,
     * hashes them using the PBKDF2PasswordUtil, and then updates the User object
     * with these hashed values.
     *
     * @param user The User object whose password and QR key need to be hashed.
     * @throws BelmanBLLException If an error occurs during the hashing process.
     */
    private void hashPassword(User user) throws BelmanBLLException {
        try{
            String rawPwd = user.getPassword();
            String hashedPwd = PBKDF2PasswordUtil.hashPassword(rawPwd);
            user.setPassword(hashedPwd);

            String rawKey = user.getQrKey();
            String hashedKey = PBKDF2PasswordUtil.hashPassword(rawKey);
            user.setQrKey(hashedKey);
        } catch (Exception e){
            throw new BelmanBLLException("Failed to hash password", e);
        }

    }
}
