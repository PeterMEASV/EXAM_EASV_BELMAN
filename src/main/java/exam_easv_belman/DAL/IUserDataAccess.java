package exam_easv_belman.DAL;

import exam_easv_belman.BE.User;
import javafx.collections.ObservableList;

import java.util.List;

public interface IUserDataAccess {

    User findByUsername(String username) throws Exception;

    User createUser(User user) throws Exception;

    void deleteUser(User user);

    ObservableList<User> getAllUsers() throws Exception;

    void updateUser(User user);

    void attachSignature(User user) throws Exception;
}
