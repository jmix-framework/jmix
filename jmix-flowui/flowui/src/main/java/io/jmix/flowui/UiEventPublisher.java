/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * Sends application events that should be handled in the components (e.g. views). To enable handling
 * application events in the {@link View}, annotate a method with {@link EventListener}. For instance:
 * <pre>
 *     &#064;EventListener
 *     public void customUiEventHandler(CustomUiEvent event) {
 *         // handle event
 *     }
 * </pre>
 * To correctly update the UI, class that implements {@link AppShellConfigurator} should contain the {@link Push}
 * annotation. It can be the main Spring Boot application class:
 * <pre>
 *     &#064;Push
 *     &#064;SpringBootApplication
 *     public class MyDemoProjectApplication implements AppShellConfigurator {
 *        // configuration
 *     }
 * </pre>
 */
@Component("flowui_UiEventPublisher")
public class UiEventPublisher {

    protected ApplicationContext applicationContext;

    @Autowired
    public UiEventPublisher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Publishes event only for the current UI.
     *
     * @param event application event
     */
    public void publishEventForCurrentUI(ApplicationEvent event) {
        publish(Collections.singletonList(UI.getCurrent()), event);
    }

    /**
     * Publishes event for all UIs in the current session.
     *
     * @param event application event
     */
    public void publishEvent(ApplicationEvent event) {
        publish(Collections.emptyList(), event);
    }

    protected void publish(Collection<UI> uis, ApplicationEvent event) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            applicationContext.getBean(UiEventsManager.class).publish(uis, event);
        } else {
            throw new IllegalStateException("Event cannot be sent since there is no active Session instance");
        }
    }
}
