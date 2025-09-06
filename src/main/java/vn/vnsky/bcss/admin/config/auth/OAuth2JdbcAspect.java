package vn.vnsky.bcss.admin.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@Slf4j
@Aspect
public class OAuth2JdbcAspect {

    private final PlatformTransactionManager platformTransactionManager;

    public OAuth2JdbcAspect(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    /**
     * Pointcut that matches Spring bean RegisteredClientRepository .save()
     */
    @Pointcut("(bean(registeredClientRepository) && execution(* save(..)))")
    public void jdbcOAuth2RegisteredClientServicePointCut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches Spring bean OAuth2AuthorizationServicePointCut .save() and .remove()
     */
    @Pointcut("(bean(oAuth2AuthorizationService) && (execution(* save(..)) || execution(* remove(..))))")
    public void jdbcOAuth2AuthorizationServicePointCut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches Spring bean OAuth2AuthorizationConsentService .save() and .remove()
     */
    @Pointcut("(bean(oAuth2AuthorizationConsentService) && (execution(* save(..)) || execution(* remove(..))))")
    public void jdbcOAuth2AuthorizationConsentServicePointCut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    @Around("jdbcOAuth2RegisteredClientServicePointCut() || jdbcOAuth2AuthorizationServicePointCut() || jdbcOAuth2AuthorizationConsentServicePointCut()")
    public Object cutAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[JDBC_TRANSACTION] Start transaction for ");
        TransactionStatus transactionStatus = null;
        try {
            transactionStatus = this.platformTransactionManager.getTransaction(null);
            return joinPoint.proceed();
        } finally {
            if (transactionStatus != null && !transactionStatus.isCompleted()) {
                this.platformTransactionManager.commit(transactionStatus);
                log.info("[JDBC_TRANSACTION] Transaction pending, need to commit for JdbcOAuth2AuthorizationService");
            } else {
                log.info("[JDBC_TRANSACTION] Transaction completed, no need to commit for JdbcOAuth2AuthorizationService");
            }
        }

    }

}
