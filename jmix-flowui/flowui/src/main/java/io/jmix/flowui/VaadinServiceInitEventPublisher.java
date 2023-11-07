/*
 * Copyright (c) Haulmont 2023. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.jmix.flowui;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.EventObject;

/**
 * Vaadin service init listener. It handles session init event and propagate new global
 * {@link VaadinSessionInitEvent}.
 */
@Component("flowui_VaadinServiceInitEventPublisher")
public class VaadinServiceInitEventPublisher implements VaadinServiceInitListener {

    protected ApplicationEventPublisher eventPublisher;

    public VaadinServiceInitEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(this::onSessionInitListener);
    }

    protected void onSessionInitListener(SessionInitEvent event) {
        eventPublisher.publishEvent(new VaadinSessionInitEvent(this, event.getSession()));
    }

    /**
     * An event that is fired when {@link VaadinSession} is initialized.
     */
    public static class VaadinSessionInitEvent extends EventObject {
        protected VaadinSession session;

        public VaadinSessionInitEvent(Object source, VaadinSession session) {
            super(source);
            this.session = session;
        }

        /**
         * @return session
         */
        public VaadinSession getSession() {
            return session;
        }
    }
}
