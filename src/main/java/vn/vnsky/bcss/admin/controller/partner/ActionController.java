package vn.vnsky.bcss.admin.controller.partner;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vnsky.bcss.admin.controller.ActionControllerBase;
import vn.vnsky.bcss.admin.service.ActionService;

@Slf4j
@Tag(name = "Partner Action API")
@RestController("partnerActionController")
@RequestMapping("${application.partner-web-oAuth2-client-info.api-prefix}/api/actions")
public class ActionController extends ActionControllerBase {

    @Autowired
    public ActionController(ActionService actionService) {
        super(actionService);
    }

}