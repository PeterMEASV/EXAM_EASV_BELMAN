package exam_easv_belman.DAL;

import exam_easv_belman.BE.User;

import java.util.List;

public interface IUserDataAccess {

    User findByUsername(String username) throws Exception;

    User createUser(User user) throws Exception;

    void deleteUser(User user);

    List<User> getAllUsers() throws Exception;

}
