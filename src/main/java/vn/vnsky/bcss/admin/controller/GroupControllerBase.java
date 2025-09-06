package vn.vnsky.bcss.admin.controller;

import lombok.extern.slf4j.Slf4j;
import vn.vnsky.bcss.admin.entity.GroupEntity;
import vn.vnsky.bcss.admin.service.GroupService;

/**
 * REST controller for managing {@link GroupEntity}.
 */
@Slf4j
public abstract class GroupControllerBase {

    protected final GroupService groupsService;

    protected GroupControllerBase(GroupService groupsService) {
        this.groupsService = groupsService;
    }

}
