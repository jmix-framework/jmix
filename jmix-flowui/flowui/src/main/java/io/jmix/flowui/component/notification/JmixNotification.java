package io.jmix.flowui.component.notification;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.notification.Notification;

@Tag("jmix-notification")
@JsModule("./src/notification/jmix-notification.js")
public class JmixNotification extends Notification implements ClickNotifier<Notification> {
}
