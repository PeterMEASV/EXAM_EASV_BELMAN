package exam_easv_belman.DAL;

import exam_easv_belman.BE.User;
import exam_easv_belman.Repository.IUserRepository;
import javafx.collections.ObservableList;


public class UserRepositoryImpl implements IUserRepository {

    IUserDataAccess userDAO;

    public UserRepositoryImpl() throws Exception {
        userDAO = new UserDAO();
    }

    @Override
    public User findByUsername(String username) throws Exception {
        return userDAO.findByUsername(username);
    }

    @Override
    public User save(User user) throws Exception {
        if(user.getId() == 0) {
            return userDAO.createUser(user);
        }
        else
        {
            userDAO.updateUser(user);
            return user;
        }
    }

    @Override
    public void delete(User user) throws Exception {
        userDAO.deleteUser(user);

    }

    @Override
    public ObservableList<User> findAll() throws Exception {
        return userDAO.getAllUsers();
    }

    @Override
    public void attachSignature(User user) throws Exception {
        userDAO.attachSignature(user);

    }
}
