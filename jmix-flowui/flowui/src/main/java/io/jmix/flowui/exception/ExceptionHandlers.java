package io.jmix.flowui.exception;

import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("flowui_ExceptionHandlers")
public class ExceptionHandlers implements ErrorHandler, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlers.class);

    protected ApplicationContext applicationContext;

    protected List<ExceptionHandler> handlers;

    @Autowired
    public ExceptionHandlers(List<ExceptionHandler> handlers) {
        this.handlers = CollectionUtils.isNotEmpty(handlers) ? handlers : Collections.emptyList();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void error(ErrorEvent event) {
        for (ExceptionHandler handler : handlers) {
            if (handler.handle(event)) {
                return;
            }
        }

        log.error("There is no {} can handle the exception", ExceptionHandler.class.getName(), event.getThrowable());
    }
}
