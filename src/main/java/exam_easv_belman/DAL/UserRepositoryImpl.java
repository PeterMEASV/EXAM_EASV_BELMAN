package exam_easv_belman.DAL;

import exam_easv_belman.BE.User;
import exam_easv_belman.Repository.IUserRepository;
import javafx.collections.ObservableList;


public class UserRepositoryImpl implements IUserRepository {

    IUserDataAccess userDAO;

    public UserRepositoryImpl() throws Exception {
        userDAO = new UserDAO();
    }

    /**
     * finds a user by their username in the database
     * @param username the username of the user to find
     * @return
     * @throws Exception if an error occurs during the database operation
     */
    @Override
    public User findByUsername(String username) throws Exception {
        return userDAO.findByUsername(username);
    }

    /**
     * if id = 0 create a user else update a user
     * @param user the user to create or update
     * @return the user that was created or updated
     * @throws Exception if an error occurs during the database operation
     */
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

    /**
     * deletes a user from the database
     * @param user the user to delete
     * @throws Exception if an error occurs during the database operation
     */
    @Override
    public void delete(User user) throws Exception {
        userDAO.deleteUser(user);

    }

    /**
     * gets all users from the database
     * @return a list of all users in the database
     * @throws Exception if an error occurs during the database operation
     */
    @Override
    public ObservableList<User> findAll() throws Exception {
        return userDAO.getAllUsers();
    }

    /**
     * Attaches a signature to a user in the database.
     * @param user the user for which to attach the signature
     * @throws Exception if an error occurs during the database operation
     */
    @Override
    public void attachSignature(User user) throws Exception {
        userDAO.attachSignature(user);

    }
}
