package io.jmix.reportsflowui.view.scripteditor;


import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.view.*;

import java.util.function.Consumer;

@ViewController("report_ScriptEditorView")
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
    protected Runnable helpBtnClickListener;

    public String getValue() {
        return editor.getValue();
    }

    public void setValue(String value) {
        UiComponentUtils.setValue(editor, value);
    }

    public void setEditorMode(CodeEditorMode mode) {
        editor.setMode(mode);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOkButtonConsumer(Consumer<String> okButtonConsumer) {
        this.okButtonConsumer = okButtonConsumer;
    }

    public void setHelpBtnClickListener(Runnable helpBtnClickListener) {
        this.helpBtnClickListener = helpBtnClickListener;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        codeEditorHelpBtn.addClickListener(listener -> helpBtnClickListener.run());
    }

    @Subscribe("okAction")
    public void onOkAction(final ActionPerformedEvent event) {
        okButtonConsumer.accept(editor.getValue());

        close(StandardOutcome.CLOSE);
    }
}