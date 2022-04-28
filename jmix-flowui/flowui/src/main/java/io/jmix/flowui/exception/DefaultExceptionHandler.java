package io.jmix.flowui.exception;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.InvalidLocationException;
import com.vaadin.flow.server.ErrorEvent;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.sys.BeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("flowui_DefaultExceptionHandler")
public class DefaultExceptionHandler implements ExceptionHandler, ApplicationContextAware, Ordered {
    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    protected ApplicationContext applicationContext;
    protected List<ExceptionDialogProvider> exceptionDialogProviders;

    public DefaultExceptionHandler(
            @Autowired(required = false) List<ExceptionDialogProvider> exceptionDialogProviders) {
        this.exceptionDialogProviders = exceptionDialogProviders;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean handle(ErrorEvent event) {
        Throwable throwable = event.getThrowable();

        // Copied from com.vaadin.flow.server.DefaultErrorHandler#error(ErrorEvent)
        if (throwable instanceof InvalidLocationException) {
            log.warn("", throwable);
            return true;
        }

        if (UI.getCurrent() == null) {
            // There is no UI, just add error to log
            return false;
        }

        if (throwable != null) {
            log.error("Unhandled exception", throwable);

            openExceptionDialog(throwable);
        }
        return true;
    }

    @Override
    public int getOrder() {
        return JmixOrder.LOWEST_PRECEDENCE;
    }

    protected void openExceptionDialog(Throwable throwable) {
        ExceptionDialog exceptionDialog = getExceptionDialog(throwable);
        exceptionDialog.open();
    }

    protected ExceptionDialog getExceptionDialog(Throwable throwable) {
        if (CollectionUtils.isNotEmpty(exceptionDialogProviders)) {
            for (ExceptionDialogProvider provider : exceptionDialogProviders) {
                if (provider.supports(throwable)) {
                    return provider.getExceptionDialogOpener(throwable);
                }
            }
        }

        return getDefaultExceptionDialog(throwable);
    }

    protected ExceptionDialog getDefaultExceptionDialog(Throwable throwable) {
        Throwable rootCause = ExceptionUtils.getRootCause(throwable);
        if (rootCause == null) {
            rootCause = throwable;
        }

        ExceptionDialog exceptionDialog = new ExceptionDialog(rootCause);

        BeanUtil.autowireContext(applicationContext, exceptionDialog);

        return exceptionDialog;
    }
}
