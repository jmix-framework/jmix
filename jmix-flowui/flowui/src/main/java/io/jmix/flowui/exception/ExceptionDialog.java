package io.jmix.flowui.exception;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.screen.ScreenRegistry;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionDialog implements InitializingBean {

    protected static final String BASE_STYLE_NAME = "jmix-exception-dialog-window";
    protected static final String DIALOG_OVERLAY_STYLE_NAME = BASE_STYLE_NAME + "-overlay";
    protected static final String HEADER_STYLE_NAME = BASE_STYLE_NAME + "-header";
    protected static final String HEADER_CLOSE_BUTTON_STYLE_NAME = BASE_STYLE_NAME + "-close-button";
    protected static final String TITLE_STYLE_NAME = BASE_STYLE_NAME + "-title";
    protected static final String STACKTRACE_TEXTAREA_STYLE_NAME = BASE_STYLE_NAME + "-stacktrace-textarea";
    protected static final String MESSAGE_TEXTAREA_STYLE_NAME = BASE_STYLE_NAME + "-message-textarea";

    protected static final String MIN_WIDTH = "400px";
    protected static final String WIDTH = "640px";
    protected static final String EXPANDED_WIDTH = "750px";
    protected static final String EXPANDED_HEIGHT = "600px";

    protected Messages messages;
    protected ScreenRegistry screenRegistry;
    protected UiComponents uiComponents;
    protected Notifications notifications;

    protected Dialog dialog;
    protected Throwable throwable;

    protected JmixButton copyButton;
    protected JmixButton detailsButton;
    protected Element stackTraceTextArea;

    protected boolean isStackTraceVisible = false;

    public ExceptionDialog(Throwable throwable) {
        this.throwable = throwable;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setScreenRegistry(ScreenRegistry screenRegistry) {
        this.screenRegistry = screenRegistry;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initDialog();
        initLayout();
    }

    /**
     * Opens the exception dialog.
     */
    public void open() {
        dialog.open();
    }

    protected void initDialog() {
        dialog = createDialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);

        dialog.setMinWidth(MIN_WIDTH);
        dialog.setWidth(WIDTH);

        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
    }

    protected Dialog createDialog() {
        return new Dialog();
    }

    protected void initLayout() {
        VerticalLayout dialogOverlay = createDialogOverlay();
        dialogOverlay.add(createHeader());
        dialogOverlay.add(createContent());

        dialog.add(dialogOverlay);
    }

    protected VerticalLayout createDialogOverlay() {
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);
        dialogContent.setHeightFull();
        dialogContent.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogContent.setClassName(DIALOG_OVERLAY_STYLE_NAME);
        return dialogContent;
    }

    protected Header createHeader() {
        Header header = new Header();
        header.addClassNames(HEADER_STYLE_NAME, "draggable");

        String screenTitle = messages.getMessage("exceptionDialog.title");

        H2 title = new H2();
        title.setText(screenTitle);
        title.setClassName(TITLE_STYLE_NAME);
        header.add(title);

        dialog.getElement().setAttribute("aria-label", screenTitle);

        Button closeButton = createHeaderCloseButton();
        header.add(closeButton);

        return header;
    }

    protected Button createHeaderCloseButton() {
        JmixButton closeButton = uiComponents.create(JmixButton.class);
        closeButton.setIcon(new Icon(VaadinIcon.CLOSE_SMALL));
        closeButton.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_CONTRAST
        );
        closeButton.setClassName(HEADER_CLOSE_BUTTON_STYLE_NAME);
        closeButton.setTitle(messages.getMessage("exceptionDialog.header.closeButton.description"));
        closeButton.addClickListener(this::onHeaderCloseButtonClick);
        return closeButton;
    }

    protected void onHeaderCloseButtonClick(ClickEvent<Button> event) {
        dialog.close();
    }

    protected Component createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setHeightFull();

        Element messageTextArea = createMessageTextArea(getMessage(throwable));

        layout.getElement().appendChild(messageTextArea);

        layout.add(createButtonsPanel());

        stackTraceTextArea = createStackTraceTextArea(getStackTrace(throwable));
        layout.getElement().appendChild(stackTraceTextArea);

        return layout;
    }

    protected Element createMessageTextArea(String message) {
        Element textarea = ElementFactory.createTextarea();
        textarea.getClassList().add(MESSAGE_TEXTAREA_STYLE_NAME);
        textarea.setAttribute("readonly", "");
        textarea.setProperty("value", message);
        return textarea;
    }

    protected HorizontalLayout createButtonsPanel() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.add(createCloseButton());

        detailsButton = createDetailsButton();
        layout.add(detailsButton);

        layout.addAndExpand(new Label());

        copyButton = createCopyButton();
        layout.add(copyButton);
        return layout;
    }

    protected JmixButton createCloseButton() {
        JmixButton closeBtn = uiComponents.create(JmixButton.class);
        closeBtn.setText(messages.getMessage("exceptionDialog.closeButton.text"));
        closeBtn.setTitle(messages.getMessage("exceptionDialog.closeButton.description"));
        closeBtn.addClickListener(this::onCloseButtonClick);
        return closeBtn;
    }

    protected void onCloseButtonClick(ClickEvent<Button> event) {
        dialog.close();
    }

    protected JmixButton createDetailsButton() {
        JmixButton detailsBtn = uiComponents.create(JmixButton.class);
        detailsBtn.setText(messages.getMessage("exceptionDialog.detailsButton.closed.text"));
        detailsBtn.setTitle(messages.getMessage("exceptionDialog.detailsButton.closed.description"));
        detailsBtn.addClickListener(this::onDetailsButtonClick);
        return detailsBtn;
    }

    protected void onDetailsButtonClick(ClickEvent<Button> event) {
        setStackTraceVisible(!isStackTraceVisible);
    }

    protected void setStackTraceVisible(boolean visible) {
        isStackTraceVisible = visible;

        if (isStackTraceVisible) {
            detailsButton.setText(messages.getMessage("exceptionDialog.detailsButton.opened.text"));
            detailsButton.setTitle(messages.getMessage("exceptionDialog.detailsButton.opened.description"));
            copyButton.setVisible(true);
            stackTraceTextArea.setVisible(true);

            focusTextArea(stackTraceTextArea);
            setTextAreaSelectionRange(stackTraceTextArea, 0, 0);

            dialog.setHeight(EXPANDED_HEIGHT);
            dialog.setWidth(EXPANDED_WIDTH);
            dialog.setResizable(true);
        } else {
            detailsButton.setText(messages.getMessage("exceptionDialog.detailsButton.closed.text"));
            detailsButton.setTitle(messages.getMessage("exceptionDialog.detailsButton.closed.description"));
            copyButton.setVisible(false);
            stackTraceTextArea.setVisible(false);

            dialog.setHeight(null);
            dialog.setWidth(WIDTH);
            dialog.setResizable(false);
        }
    }

    protected JmixButton createCopyButton() {
        JmixButton copyBtn = uiComponents.create(JmixButton.class);
        copyBtn.setIcon(new Icon(VaadinIcon.COPY_O));
        copyBtn.setVisible(false);
        copyBtn.setTitle(messages.getMessage("exceptionDialog.copyButton.description"));
        copyBtn.addClickListener(this::onCopyButtonClick);
        return copyBtn;
    }

    protected void onCopyButtonClick(ClickEvent<Button> event) {
        stackTraceTextArea.callJsFunction("select");
        stackTraceTextArea.executeJs("document.execCommand('copy');");
        notifications.create(messages.getMessage("exceptionDialog.copingSuccessful"))
                .withType(Notifications.Type.SUCCESS)
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }

    protected Element createStackTraceTextArea(String stackTrace) {
        Element textarea = ElementFactory.createTextarea();
        textarea.getClassList().add(STACKTRACE_TEXTAREA_STYLE_NAME);
        textarea.setAttribute("readonly", "");
        textarea.setProperty("value", stackTrace);
        textarea.setProperty("wrap", "off");
        textarea.setVisible(false);
        return textarea;
    }

    protected String getStackTrace(Throwable throwable) {
        return ExceptionUtils.getStackTrace(throwable);
    }

    protected String getMessage(Throwable rootCause) {
        StringBuilder msg = new StringBuilder();

        if (msg.length() == 0) {
            msg.append(rootCause.getClass().getSimpleName());
            if (!StringUtils.isBlank(rootCause.getMessage())) {
                msg.append(": ").append(rootCause.getMessage());
            }

            if (rootCause instanceof DevelopmentException) {
                Map<String, Object> params = new LinkedHashMap<>();
                if (rootCause instanceof GuiDevelopmentException) {
                    GuiDevelopmentException guiDevException = (GuiDevelopmentException) rootCause;
                    ComponentLoader.Context context = guiDevException.getContext();
                    // todo implement in ComponentLoader
                    /*if (context instanceof ComponentLoader.CompositeComponentContext) {
                        Class<?> componentClass = ((ComponentLoader.CompositeComponentContext) context).getComponentClass();
                        params.put("Component Class", componentClass);
                        CompositeDescriptor template = componentClass.getAnnotation(CompositeDescriptor.class);
                        if (template != null) {
                            params.put("XML descriptor", template.value());
                        }
                    } else*/
                    if (guiDevException.getFrameId() != null) {
                        String frameId = guiDevException.getFrameId();
                        params.put("Frame ID", frameId);
                        try {
                            params.put("XML descriptor",
                                    screenRegistry.getScreenInfo(frameId).
                                            getTemplatePath().orElse(null));
                        } catch (Exception e) {
                            params.put("XML descriptor", "not found for " + frameId);
                        }
                    }
                }
                params.putAll(((DevelopmentException) rootCause).getParams());

                if (!params.isEmpty()) {
                    msg.append("\n\n");
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        msg.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                }
            }
        }
        return msg.toString();
    }

    protected void setTextAreaSelectionRange(Element textArea, int start, int end) {
        textArea.callJsFunction("setSelectionRange", start, end);
    }

    protected void focusTextArea(Element textArea) {
        // see com.vaadin.flow.component.Focusable#focus()
        textArea.executeJs("setTimeout(function(){$0.focus()},0)", textArea);
    }
}
