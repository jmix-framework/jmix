package io.jmix.flowui.sys;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import io.jmix.flowui.exception.UiExceptionHandlers;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("flowui_JmixServiceInitListener")
public class JmixServiceInitListener implements VaadinServiceInitListener, ApplicationContextAware {

    protected ApplicationContext applicationContext;
    protected UiExceptionHandlers uiExceptionHandlers;

    @Autowired
    public JmixServiceInitListener(UiExceptionHandlers uiExceptionHandlers) {
        this.uiExceptionHandlers = uiExceptionHandlers;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(this::onSessionInitEvent);
        event.getSource().addUIInitListener(this::onUIInitEvent);
    }

    protected void onUIInitEvent(UIInitEvent uiInitEvent) {
        UI ui = uiInitEvent.getUI();
        // retrieve ExtendedClientDetails to be cached
        ui.getPage().retrieveExtendedClientDetails(extendedClientDetails -> {});
    }

    protected void onSessionInitEvent(SessionInitEvent event) {
        event.getSession().setErrorHandler(uiExceptionHandlers);
    }
}
