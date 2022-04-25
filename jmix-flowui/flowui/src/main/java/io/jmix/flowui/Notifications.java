package io.jmix.flowui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.notification.JmixNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component("flowui_Notifications")
public class Notifications {
    private static final Logger log = LoggerFactory.getLogger(Notifications.class);

    protected FlowUiComponentProperties uiComponentProperties;

    @Autowired
    public Notifications(FlowUiComponentProperties uiComponentProperties) {
        this.uiComponentProperties = uiComponentProperties;
    }

    public void show(String text) {
        Preconditions.checkNotNullArgument(text);

        create(text).show();
    }

    public void show(String title, String message) {
        Preconditions.checkNotNullArgument(title);
        Preconditions.checkNotNullArgument(message);

        create(title, message).show();
    }

    public void show(Component component) {
        Preconditions.checkNotNullArgument(component);

        create(component).show();
    }

    public NotificationBuilder create(String text) {
        Preconditions.checkNotNullArgument(text);

        return new NotificationBuilder(text)
                .applyDefaults(uiComponentProperties);
    }

    public NotificationBuilder create(String title, String message) {
        Preconditions.checkNotNullArgument(title);
        Preconditions.checkNotNullArgument(message);

        return new NotificationBuilder(title, message)
                .applyDefaults(uiComponentProperties);
    }

    public NotificationBuilder create(Component component) {
        Preconditions.checkNotNullArgument(component);

        return new NotificationBuilder(component)
                .applyDefaults(uiComponentProperties);
    }

    public enum Type {
        DEFAULT,
        SUCCESS,
        ERROR,
        SYSTEM,
        WARNING
    }

    public static class NotificationBuilder {

        protected static final String CLOSEABLE_LAYOUT_STYLE_NAME = "jmix-closeable-layout";
        protected static final String CLOSE_BUTTON_STYLE_NAME = "close-button";

        protected static final String TEXT_LAYOUT_STYLE_NAME = "jmix-text-layout";
        protected static final String TITLE_STYLE_NAME = "title";
        protected static final String MESSAGE_STYLE_NAME = "message";

        protected static final String TEXT_CONTENT_STYLE_NAME = "text-content";
        protected static final String COMPONENT_CONTENT_STYLE_NAME = "component-content";

        protected static final String WARNING_THEME_NAME = "warning";

        protected JmixNotification notification;

        protected String text;
        protected String title;
        protected String message;
        protected Component component;

        protected Notification.Position position;
        protected int duration;
        protected Type type = Type.DEFAULT;
        protected Boolean closeable = null;
        protected NotificationVariant themeVariant;

        public NotificationBuilder(String text) {
            this.text = text;
        }

        public NotificationBuilder(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public NotificationBuilder(Component component) {
            this.component = component;
        }

        public NotificationBuilder withPosition(Notification.Position position) {
            Preconditions.checkNotNullArgument(position);

            this.position = position;
            return this;
        }

        public NotificationBuilder withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public NotificationBuilder withType(Type type) {
            Preconditions.checkNotNullArgument(type);

            this.type = type;
            return this;
        }

        public NotificationBuilder withCloseable(boolean closeable) {
            this.closeable = closeable;
            return this;
        }

        public NotificationBuilder withThemeVariant(NotificationVariant themeVariant) {
            Preconditions.checkNotNullArgument(themeVariant);

            this.themeVariant = themeVariant;
            return this;
        }

        public Notification.Position getPosition() {
            return position;
        }

        public int getDuration() {
            return duration;
        }

        public Type getType() {
            return type;
        }

        public boolean isCloseable() {
            return isInternalCloseable();
        }

        @Nullable
        public String getText() {
            return text;
        }

        @Nullable
        public String getTitle() {
            return title;
        }

        @Nullable
        public String getMessage() {
            return message;
        }

        @Nullable
        public Component getComponent() {
            return component;
        }

        public Notification build() {
            notification = createNotification();
            notification.setDuration(duration);
            notification.addThemeName(convertTypeToThemeName(type));

            if (themeVariant != null) {
                notification.addThemeVariants(themeVariant);
            }

            if (position != null) {
                position = type == Type.SYSTEM ? Notification.Position.TOP_STRETCH : position;
                notification.setPosition(position);
            }

            if (isInternalCloseable()) {
                log.debug("Notification is closeable so duration property will be ignored");
                notification.setDuration(0);
            }

            Component content = buildNotificationContent();
            if (content != null) {
                notification.add(content);
            }

            return notification;
        }

        public void show() {
            build().open();
        }

        protected NotificationBuilder applyDefaults(FlowUiComponentProperties properties) {
            this.duration = properties.getDefaultNotificationDuration();
            this.position = properties.getDefaultNotificationPosition();
            return this;
        }

        @Nullable
        protected Component buildNotificationContent() {
            HasComponents closeableLayout = isInternalCloseable()
                    ? createCloseableLayout()
                    : null;

            Component content;
            if (text != null) {
                content = new Text(text);
            } else if (title != null && message != null) {
                HasComponents textLayout = createTextLayout();
                textLayout.add(createTitleComponent(title));
                textLayout.add(createMessageComponent(message));
                content = (Component) textLayout;
            } else if (component != null) {
                content = component;
            } else {
                log.debug("No text message or component provided to the notification");
                return null;
            }

            if (closeableLayout != null) {
                closeableLayout.addComponentAsFirst(content);
                closeableLayout.getElement().getClassList().add(content instanceof Text
                        ? TEXT_CONTENT_STYLE_NAME
                        : COMPONENT_CONTENT_STYLE_NAME);
                return (Component) closeableLayout;
            }
            return content;
        }

        protected HasComponents createTextLayout() {
            Div div = new Div();
            div.setClassName(TEXT_LAYOUT_STYLE_NAME);
            return div;
        }

        protected Component createTitleComponent(String title) {
            H4 titleElement = new H4();
            titleElement.setText(title);
            titleElement.setClassName(TITLE_STYLE_NAME);
            return titleElement;
        }

        protected Component createMessageComponent(String message) {
            Paragraph messageElement = new Paragraph();
            messageElement.setClassName(MESSAGE_STYLE_NAME);
            messageElement.setText(message);
            return messageElement;
        }

        protected HasComponents createCloseableLayout() {
            Div div = new Div();
            div.setClassName(CLOSEABLE_LAYOUT_STYLE_NAME);
            div.add(createCloseButton());
            return div;
        }

        protected Button createCloseButton() {
            Button button = new Button();
            button.setClassName(CLOSE_BUTTON_STYLE_NAME);
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
            button.setIcon(new Icon(VaadinIcon.CLOSE_SMALL));
            button.addClickListener(this::onCloseButtonClick);
            return button;
        }

        protected void onCloseButtonClick(ClickEvent<Button> event) {
            notification.close();
        }

        protected boolean isInternalCloseable() {
            boolean internalCloseable = type == Type.ERROR || type == Type.WARNING || type == Type.SYSTEM
                    || duration == 0;
            return closeable != null ? closeable : internalCloseable;
        }

        protected JmixNotification createNotification() {
            return new JmixNotification();
        }

        protected String convertTypeToThemeName(Type type) {
            switch (type) {
                case DEFAULT:
                    return "";
                case SUCCESS:
                    return NotificationVariant.LUMO_SUCCESS.getVariantName();
                case ERROR:
                    return NotificationVariant.LUMO_ERROR.getVariantName();
                case SYSTEM:
                    return NotificationVariant.LUMO_CONTRAST.getVariantName();
                case WARNING:
                    return WARNING_THEME_NAME;
                default:
                    throw new IllegalStateException("Unsupported type " + type);
            }
        }
    }
}
