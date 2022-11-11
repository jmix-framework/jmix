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

package io.jmix.flowui.exception;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionDialog implements InitializingBean {

    protected static final String BASE_CLASS_NAME = "jmix-exception-dialog-window";
    protected static final String DIALOG_CONTENT_CLASS_NAME = BASE_CLASS_NAME + "-content";
    protected static final String HEADER_CLOSE_BUTTON_CLASS_NAME = BASE_CLASS_NAME + "-close-button";
    protected static final String STACKTRACE_TEXTAREA_CLASS_NAME = BASE_CLASS_NAME + "-stacktrace-textarea";
    protected static final String MESSAGE_TEXTAREA_CLASS_NAME = BASE_CLASS_NAME + "-message-textarea";

    protected static final String WIDTH = "40em";
    protected static final String EXPANDED_WIDTH = "45em";
    protected static final String EXPANDED_HEIGHT = "37.5em";

    protected Messages messages;
    protected ViewRegistry viewRegistry;
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
    public void setViewRegistry(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
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
    }

    /**
     * Opens the exception dialog.
     */
    public void open() {
        dialog.open();
    }

    protected Dialog createDialog() {
        return new Dialog();
    }

    protected void initDialog() {
        dialog = createDialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);

        dialog.setWidth(WIDTH);

        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        initLayout(dialog);
    }

    protected void initLayout(Dialog dialog) {
        initHeader(dialog);

        Component content = createContent();
        dialog.add(content);
    }

    protected void initHeader(Dialog dialog) {
        String viewTitle = messages.getMessage("exceptionDialog.title");
        dialog.setHeaderTitle(viewTitle);
        dialog.getHeader().add(createHeaderCloseButton());

        dialog.getElement().setAttribute("aria-label", viewTitle);
    }

    protected Button createHeaderCloseButton() {
        JmixButton closeButton = uiComponents.create(JmixButton.class);
        closeButton.setIcon(new Icon(VaadinIcon.CLOSE_SMALL));
        closeButton.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_CONTRAST
        );
        closeButton.setClassName(HEADER_CLOSE_BUTTON_CLASS_NAME);
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
        layout.setClassName(DIALOG_CONTENT_CLASS_NAME);

        Element messageTextArea = createMessageTextArea(getMessage(throwable));

        layout.getElement().appendChild(messageTextArea);

        layout.add(createButtonsPanel());

        stackTraceTextArea = createStackTraceTextArea(getStackTrace(throwable));
        layout.getElement().appendChild(stackTraceTextArea);

        return layout;
    }

    protected Element createMessageTextArea(String message) {
        Element textarea = ElementFactory.createTextarea();
        textarea.getClassList().add(MESSAGE_TEXTAREA_CLASS_NAME);
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

        textarea.getClassList().add(STACKTRACE_TEXTAREA_CLASS_NAME);
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
                    if (guiDevException.getFrameId() != null) {
                        String frameId = guiDevException.getFrameId();
                        params.put("Frame ID", frameId);
                        try {
                            params.put("XML descriptor",
                                    viewRegistry.getViewInfo(frameId).
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
