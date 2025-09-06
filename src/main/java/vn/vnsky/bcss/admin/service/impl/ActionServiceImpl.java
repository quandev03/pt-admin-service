package vn.vnsky.bcss.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.vnsky.bcss.admin.dto.ActionDTO;
import vn.vnsky.bcss.admin.mapper.ActionMapper;
import vn.vnsky.bcss.admin.repository.ActionRepository;
import vn.vnsky.bcss.admin.service.ActionService;

import java.util.List;

@Service
public class ActionServiceImpl implements ActionService {

    private final ActionRepository actionRepository;

    private final ActionMapper actionMapper;

    @Autowired
    public ActionServiceImpl(ActionRepository actionRepository, ActionMapper actionMapper) {
        this.actionRepository = actionRepository;
        this.actionMapper = actionMapper;
    }

    @Override
    public List<ActionDTO> getActionList() {
        return actionMapper.toDto(actionRepository.findAll());
    }
}