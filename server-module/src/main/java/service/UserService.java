package service;

import domain.User;
import exceptions.NotFoundException;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User loginUser(String username, String password) throws NotFoundException {
        User foundUser= userRepository.findUserByUsername(username);
        if (foundUser == null) {
            throw new NotFoundException("User with username " + username + " not found.");
        }
        if (BCrypt.checkpw(password, foundUser.getPassword())) {
            return foundUser;
        } else {
            throw new NotFoundException("Incorrect password for user " + username);
        }
    }
}
