package vn.vnsky.bcss.admin.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import vn.vnsky.bcss.admin.service.UserService;

@Slf4j
public abstract class UserControllerBase {

    protected final UserService userService;

    @Autowired
    protected UserControllerBase(UserService userService) {
        this.userService = userService;
    }

}
