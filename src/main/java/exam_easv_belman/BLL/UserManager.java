package exam_easv_belman.BLL;

import exam_easv_belman.BE.User;
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

    public UserManager() throws Exception {
        userRepo = new UserRepositoryImpl();
    }

    public User authenticateUser(String username, String rawPassword) throws Exception {
        User user = userRepo.findByUsername(username);
        if (user == null || !PBKDF2PasswordUtil.verifyPassword(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return user;
    }

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

    public User createUser(User user) throws Exception {
        hashPassword(user);
        return userRepo.save(user);
    }

    public ObservableList<User> getAllUsers() throws Exception {
         return userRepo.findAll();
    }

    public void deleteUser(User user) throws Exception {
        userRepo.delete(user);
    }

    public void attachSignature(User user) throws Exception {
        userRepo.attachSignature(user);
    }

    public void updateUser(User selectedUser, boolean passwordChanged) throws Exception {
        if(passwordChanged){
            hashPassword(selectedUser);
        }
        userRepo.save(selectedUser);
    }

    private void hashPassword(User user) throws Exception {
        String rawPwd = user.getPassword();
        String hashedPwd = PBKDF2PasswordUtil.hashPassword(rawPwd);
        user.setPassword(hashedPwd);

        String rawKey = user.getQrKey();
        String hashedKey = PBKDF2PasswordUtil.hashPassword(rawKey);
        user.setQrKey(hashedKey);
    }
}
