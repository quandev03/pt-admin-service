package vn.vnsky.bcss.admin.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.vnsky.bcss.admin.constant.AuthConstants;

@Slf4j
@UtilityClass
public class RequestUtil {

    private final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getRequestOrigin() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String origin = null;
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            origin = request.getHeader(HttpHeaders.ORIGIN);
        }
        return origin;
    }

    public static String getClientIP() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            for (String header : IP_HEADER_CANDIDATES) {
                String ipList = servletRequestAttributes.getRequest().getHeader(header);
                if (StringUtils.hasText(ipList) && !"unknown".equalsIgnoreCase(ipList)) {
                    log.info("Get ip header {} -> IP Address: {}", header, ipList);
                    return ipList.split(",")[0];
                }
            }
        }
        return null;
    }

    public static String getUserAgent() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            servletRequestAttributes.getRequest().getHeader(HttpHeaders.USER_AGENT);
        }
        return null;
    }

    public static String getLanguageTag() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String languageTag = AuthConstants.VI_LANGUAGE;
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            languageTag = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        }
        return languageTag;
    }

}
