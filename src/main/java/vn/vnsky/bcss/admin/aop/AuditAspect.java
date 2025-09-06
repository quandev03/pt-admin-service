package vn.vnsky.bcss.admin.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaOperations;
import vn.vnsky.bcss.admin.annotation.AuditAction;
import vn.vnsky.bcss.admin.annotation.AuditDetail;
import vn.vnsky.bcss.admin.annotation.AuditId;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.dto.AuditLogDTO;
import vn.vnsky.bcss.admin.util.RequestUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Aspect
public class AuditAspect {

    private static final String AUDIT_LOG_QUEUE = "system-audit-log";

    private static final String SUB_SYSTEM_ADMIN = "admin-service";

    private final KafkaOperations<String, Object> kafkaOperations;

    public AuditAspect(KafkaOperations<String, Object> kafkaOperations) {
        this.kafkaOperations = kafkaOperations;
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("@annotation(vn.vnsky.bcss.admin.annotation.AuditAction)")
    public void annotationPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice.
     * @return result.
     * @throws Throwable throws {@link IllegalArgumentException}.
     */
    @Around("annotationPackagePointcut()")
    public Object auditAround(ProceedingJoinPoint joinPoint) throws Throwable {
        AuditAction auditAction = this.getAuditActionAnnotation(joinPoint);
        log.info("[SYSTEM_AUDIT_LOG] Auditing {} -> {}", auditAction.targetType(), auditAction.actionType());
        Object preValueWrapper = null;
        Object postValueWrapper = null;
        switch (auditAction.actionType()) {
            case CREATE:
                postValueWrapper = joinPoint.proceed();
                this.processAndSendAuditMessage(preValueWrapper, postValueWrapper, auditAction);
                return postValueWrapper;
            case UPDATE:
                preValueWrapper = this.retrievePreValue(joinPoint, auditAction);
                postValueWrapper = joinPoint.proceed();
                this.processAndSendAuditMessage(preValueWrapper, postValueWrapper, auditAction);
                return postValueWrapper;
            case DELETE:
                preValueWrapper = this.retrievePreValue(joinPoint, auditAction);
                this.processAndSendAuditMessage(preValueWrapper, postValueWrapper, auditAction);
                return joinPoint.proceed();
            default:
                return joinPoint.proceed();
        }
    }

    private Object retrievePreValue(ProceedingJoinPoint joinPoint, AuditAction auditAction) {
        try {
            List<Object> args = new ArrayList<>();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method auditActionMethod = signature.getMethod();
            Annotation[][] annotatedParameterTypes = auditActionMethod.getParameterAnnotations();
            for (int i = 0; i < annotatedParameterTypes.length; i++) {
                Annotation[] annotatedParameterType = annotatedParameterTypes[i];
                for (Annotation annotation : annotatedParameterType) {
                    if (annotation instanceof AuditId) {
                        args.add(joinPoint.getArgs()[i]);
                    }
                }
            }
            for (Method declaredMethod : joinPoint.getTarget().getClass().getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(AuditDetail.class)) {
                    AuditDetail auditDetail = declaredMethod.getAnnotation(AuditDetail.class);
                    if (Objects.equals(auditDetail.targetType(), auditAction.targetType())) {
                        return declaredMethod.invoke(joinPoint.getTarget(), args.toArray());
                    }
                }
            }
        } catch (Exception ex) {
            log.error("[AUDIT_ASPECT] Error while retrieving pre value", ex);
            return null;
        }
        return null;
    }

    private void processAndSendAuditMessage(Object preValueWrapper, Object postValueWrapper, AuditAction auditAction) {
        AuditLogDTO auditLogDTO = AuditLogDTO.builder()
                .subSystem(SUB_SYSTEM_ADMIN)
                .actionTime(ZonedDateTime.now())
                .targetType(auditAction.targetType())
                .actionType(auditAction.actionType().name())
                .preValue(preValueWrapper instanceof ResponseEntity<?> responseEntity ? responseEntity.getBody() : preValueWrapper)
                .postValue(postValueWrapper instanceof ResponseEntity<?> responseEntity ? responseEntity.getBody() : postValueWrapper)
                .status(AuthConstants.ModelStatus.ACTIVE)
                .clientIp(RequestUtil.getClientIP())
                .build();
        if (log.isDebugEnabled()) {
            log.debug("[SYSTEM_AUDIT_LOG] Start send message to topic {}, message: {}", AUDIT_LOG_QUEUE, auditLogDTO);
        }
        String msgKey = UUID.randomUUID().toString();
        this.kafkaOperations.send(AUDIT_LOG_QUEUE, msgKey, auditLogDTO).handleAsync((result, throwable) -> {
            if (throwable != null) {
                log.error("[SYSTEM_AUDIT_LOG] Send message to topic {} failed, error: ", AUDIT_LOG_QUEUE, throwable);
            } else {
                if (log.isDebugEnabled()) {
                    log.info("[SYSTEM_AUDIT_LOG] Send message to topic {} successfully", AUDIT_LOG_QUEUE);
                }
            }
            return null;
        });
    }

    private AuditAction getAuditActionAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(AuditAction.class);
    }

}
