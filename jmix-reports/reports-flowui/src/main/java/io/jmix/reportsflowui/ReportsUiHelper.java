package io.jmix.reportsflowui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Component("reportsflowui_ReportsUiHelper")
public class ReportsUiHelper {

    public static final String FIELD_ICON_CLASS_NAME = "reports-field-icon";
    public static final String FIELD_ICON_SIZE_CLASS_NAME = "reports-field-icon-size";

    protected final UiComponents uiComponents;
    protected final Messages messages;

    public ReportsUiHelper(UiComponents uiComponents, Messages messages) {
        this.uiComponents = uiComponents;
        this.messages = messages;
    }

    public void showScriptEditorDialog(String headerText,
                                       String value,
                                       Consumer<String> okButtonClickHandler,
                                       CodeEditorMode mode,
                                       @Nullable ComponentEventListener<ClickEvent<Icon>> helpButtonClickListener) {
        Dialog dialog = uiComponents.create(Dialog.class);
        dialog.setHeaderTitle(headerText);
        dialog.setWidth("75em");
        dialog.setHeight("45em");
        dialog.setCloseOnOutsideClick(false);
        dialog.setResizable(true);
        dialog.setDraggable(true);

        VerticalLayout layout = uiComponents.create(VerticalLayout.class);
        layout.setWidthFull();
        layout.setHeightFull();
        layout.setPadding(false);

        CodeEditor codeEditor = uiComponents.create(CodeEditor.class);
        codeEditor.setValue(value);
        codeEditor.setWidthFull();
        codeEditor.setHeightFull();

        if (mode != null) {
            codeEditor.setMode(mode);
        }

        Div div = uiComponents.create(Div.class);
        div.setWidthFull();
        div.setHeightFull();
        div.addClassNames("flex", "items-start");
        div.add(codeEditor);

        if (helpButtonClickListener != null) {
            Icon helpIcon = VaadinIcon.QUESTION_CIRCLE.create();
            helpIcon.addClassNames("icon", "tertiary-inline");
            helpIcon.addClickListener(helpButtonClickListener);
            div.add(helpIcon);
        }
        layout.add(div);
        HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);
        actionsLayout.setWidthFull();
        actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button okBtn = uiComponents.create(Button.class);
        okBtn.setText(messages.getMessage("scriptEditorDialog.okButton.text"));
        okBtn.setIcon(VaadinIcon.CHECK.create());
        okBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        okBtn.addClickListener(okClickEvent -> {
            dialog.close();
            okButtonClickHandler.accept(codeEditor.getValue());
        });
        Button cancelBtn = uiComponents.create(Button.class);
        cancelBtn.setText(messages.getMessage("scriptEditorDialog.cancelButton.text"));
        cancelBtn.setIcon(VaadinIcon.BAN.create());
        cancelBtn.addClickListener(okClickEvent -> dialog.close());

        actionsLayout.add(okBtn, cancelBtn);
        layout.add(actionsLayout);

        dialog.add(layout);
        dialog.open();
    }
}
