package vn.vnsky.bcss.admin.tracing;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.task.SimpleAsyncTaskExecutorCustomizer;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import vn.vnsky.bcss.admin.constant.TracingContextEnum;

public class AsyncTaskTracingCustomizer implements SimpleAsyncTaskExecutorCustomizer {

    @Override
    public void customize(SimpleAsyncTaskExecutor taskExecutor) {
        taskExecutor.setTaskDecorator(command -> {
            final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
            return () -> {
                try {
                    ThreadContext.put(TracingContextEnum.X_CORRELATION_ID.getThreadKey(), corrId);
                    command.run();
                } finally {
                    ThreadContext.clearAll();
                }
            };
        });
    }
}
