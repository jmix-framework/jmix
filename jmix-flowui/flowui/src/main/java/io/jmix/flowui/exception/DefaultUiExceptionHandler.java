package io.jmix.flowui.exception;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.InvalidLocationException;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.sys.BeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

@Internal
@Component("flowui_DefaultExceptionHandler")
public class DefaultUiExceptionHandler implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(DefaultUiExceptionHandler.class);

    protected ApplicationContext applicationContext;
    protected List<ExceptionDialogProvider> exceptionDialogProviders;

    public DefaultUiExceptionHandler(
            @Autowired(required = false) List<ExceptionDialogProvider> exceptionDialogProviders) {
        this.exceptionDialogProviders = exceptionDialogProviders;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public boolean handle(Throwable exception) {
        // Copied from com.vaadin.flow.server.DefaultErrorHandler#error(ErrorEvent)
        if (exception instanceof InvalidLocationException) {
            log.warn("", exception);
            return true;
        }

        if (UI.getCurrent() == null) {
            // There is no UI, just add error to log
            return false;
        }

        log.error("Unhandled exception", exception);

        openExceptionDialog(exception);

        return true;
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
