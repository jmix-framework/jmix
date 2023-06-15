package io.jmix.reportsflowui.view.scripteditor;


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.view.*;

import java.util.function.Consumer;

@ViewController("report_ScriptEditor.view")
@ViewDescriptor("script-editor-view.xml")
@DialogMode(width = "75em", height = "45em")
public class ScriptEditorView extends StandardView {

    @ViewComponent
    protected CodeEditor editor;
    @ViewComponent
    protected JmixButton codeEditorHelpBtn;

    protected String title;
    protected String value;
    protected CodeEditorMode mode;
    protected Consumer<String> okButtonConsumer;
    protected ComponentEventListener<ClickEvent<Button>> helpBtnClickListener;

    public String getValue() {
        return editor.getValue();
    }

    public void setEditorMode(CodeEditorMode mode){
        editor.setMode(mode);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setValue(String value) {
        UiComponentUtils.setValue(editor, value);
    }

    public void setOkButtonConsumer(Consumer<String> okButtonConsumer) {
        this.okButtonConsumer = okButtonConsumer;
    }

    public void setHelpBtnClickListener(ComponentEventListener<ClickEvent<Button>> helpBtnClickListener) {
        this.helpBtnClickListener = helpBtnClickListener;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        codeEditorHelpBtn.addClickListener(helpBtnClickListener);
    }

    @Subscribe("okAction")
    public void onOkAction(final ActionPerformedEvent event) {
        okButtonConsumer.accept(editor.getValue());

        close(StandardOutcome.CLOSE);
    }
}