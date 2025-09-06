package vn.vnsky.bcss.admin.config.kafka;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaAuthContextHolder {

    private static final ThreadLocal<KafkaUserDTO> CURRENT = new ThreadLocal<>();

    public static KafkaUserDTO getUser() {
        return CURRENT.get();
    }

    public static void setCurrent(KafkaUserDTO userDTO) {
        CURRENT.set(userDTO);
    }

    public static void clear() {
        CURRENT.remove();
    }

}
