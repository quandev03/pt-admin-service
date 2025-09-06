package vn.vnsky.bcss.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.web.bind.annotation.*;
import vn.vnsky.bcss.admin.dto.TestMessageDTO;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/test",
        "${application.vnsky-web-oAuth2-client-info.api-prefix}/api/test",
        "${application.partner-web-oAuth2-client-info.api-prefix}/api/test"})
public class TestController {

    private final KafkaOperations<String, Object> kafkaOperations;

    @Autowired
    public TestController(KafkaOperations<String, Object> kafkaOperations) {
        this.kafkaOperations = kafkaOperations;
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> helloRes = new HashMap<>();
        helloRes.put("message", "hello");
        return ResponseEntity.ok(helloRes);
    }

    @GetMapping("/trace")
    public ResponseEntity<Map<String, String>> trace(@RequestHeader Map<String, String> headers) {
        return ResponseEntity.ok(headers);
    }

    @PostMapping("/message")
    public ResponseEntity<Void> trace(@RequestBody TestMessageDTO testMessageDTO) {
        this.kafkaOperations.send("system-push-notification", testMessageDTO);
        return ResponseEntity.ok().build();
    }

}
