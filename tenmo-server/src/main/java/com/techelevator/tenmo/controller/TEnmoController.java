package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize(("isAuthenticated()"))
public class TEnmoController {
    private UserDao userDao;
    private AuthenticationController.LoginResponse currentUser;


}
