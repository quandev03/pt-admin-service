package vn.vnsky.bcss.admin.dto;

import lombok.Data;

@Data
public class TestMessageDTO {

    private String title;

    private String content;

    private String uriRef;

    private String receiverId;

    private String receiverPreferredUsername;

}
