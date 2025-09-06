package vn.vnsky.bcss.admin.controller;

import lombok.extern.slf4j.Slf4j;
import vn.vnsky.bcss.admin.service.RoleService;

@Slf4j
public abstract class RoleControllerBase {

    protected final RoleService roleService;

    protected RoleControllerBase(RoleService roleService) {
        this.roleService = roleService;
    }

}
