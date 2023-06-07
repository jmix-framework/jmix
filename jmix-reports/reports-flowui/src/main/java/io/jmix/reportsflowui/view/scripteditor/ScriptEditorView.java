package io.jmix.reportsflowui.view.scripteditor;


import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.view.*;

@ViewController("report_ScriptEditor.view")
@ViewDescriptor("script-editor-view.xml")
public class ScriptEditorView extends StandardView {

    @ViewComponent
    private CodeEditor editor;

    public void setEditorMode(CodeEditorMode mode){
        editor.setMode(mode);
    }

}