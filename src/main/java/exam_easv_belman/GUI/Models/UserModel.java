package exam_easv_belman.GUI.Models;

import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.UserManager;
import javafx.collections.ObservableList;

public class UserModel {

    private UserManager userManager;

    public UserModel() throws Exception {
        userManager = new UserManager();
    }

    public User authenticateUser(String username, String rawPassword) throws Exception {
        return userManager.authenticateUser(username, rawPassword);
    }

    public User createUser(User user) throws Exception {
        return userManager.createUser(user);
    }

    public ObservableList<User> getAllUsers() throws Exception {
        return userManager.getAllUsers();
    }

    public void deleteUser(User user) throws Exception {
        userManager.deleteUser(user);
    }

    public User authenticateUser(String qrKey) throws Exception {
        return userManager.authenticateUser(qrKey);
    }

    public void attachSignature(User user) throws Exception {
        userManager.attachSignature(user);

    }

    public void updateUser(User selectedUser, boolean passwordChanged) throws Exception {
        userManager.updateUser(selectedUser, passwordChanged);
    }
}


