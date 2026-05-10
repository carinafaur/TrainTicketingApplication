package service;

import domain.User;
import domain.UserRole;
import dtos.RegisterRequestDTO;
import exceptions.AlreadyExistsException;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;

public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_USERNAME_LENGTH = 30;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User loginUser(String username, String password) throws NotFoundException {
        User foundUser = userRepository.findUserByUsername(username);
        if (foundUser == null) {
            throw new NotFoundException("User with username " + username + " not found.");
        }
        if (BCrypt.checkpw(password, foundUser.getPassword())) {
            return foundUser;
        }
        throw new NotFoundException("Incorrect password for user " + username);
    }

    public User register(RegisterRequestDTO request) throws ValidationException, AlreadyExistsException {
        validate(request);

        User existing = userRepository.findUserByUsername(request.getUsername().trim());
        if (existing != null) {
            throw new AlreadyExistsException("Username '" + request.getUsername() + "' is already taken.");
        }

        UserRole role = request.getRole() == null ? UserRole.CUSTOMER : request.getRole();
        String hashed = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        User user = new User(
                request.getUsername().trim(),
                hashed,
                request.getEmail().trim(),
                role
        );
        return userRepository.save(user);
    }

    private static void validate(RegisterRequestDTO request) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            errors.append("Username is required. ");
        } else if (username.trim().length() > MAX_USERNAME_LENGTH) {
            errors.append("Username can be at most ").append(MAX_USERNAME_LENGTH).append(" characters. ");
        } else if (!username.trim().matches("[A-Za-z0-9._-]+")) {
            errors.append("Username can only contain letters, digits, '.', '_', '-'. ");
        }

        String password = request.getPassword();
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            errors.append("Password must be at least ").append(MIN_PASSWORD_LENGTH).append(" characters long. ");
        }

        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            errors.append("Email is required. ");
        } else if (!email.trim().matches(EMAIL_REGEX)) {
            errors.append("Email is not a valid email address. ");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString().trim());
        }
    }
}
