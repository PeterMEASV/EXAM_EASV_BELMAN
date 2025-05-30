package exam_easv_belman.Repository;

import exam_easv_belman.BE.User;
import javafx.collections.ObservableList;

public interface IUserRepository {

        User findByUsername(String username) throws Exception;
        User save(User user) throws Exception;
        void delete(User user) throws Exception;
        ObservableList<User> findAll() throws Exception;
        void attachSignature(User user) throws Exception;
}
