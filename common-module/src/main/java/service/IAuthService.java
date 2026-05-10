package service;

import domain.User;
import dtos.RegisterRequestDTO;
import exceptions.AppException;

/**
 * Authentication and observer wiring. The smallest contract a client needs
 * before it can do anything else on the server.
 */
public interface IAuthService {

    User loginUser(String username, String password, IObserver client) throws AppException;
    void logoutUser(String username, IObserver client);
    User registerUser(RegisterRequestDTO request, IObserver client) throws AppException;
    void setObserver(IObserver clientObserver);
}
