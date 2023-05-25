package io.jmix.reportsflowui.view.scripteditor;


import io.jmix.flowui.view.DefaultMainViewParent;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "ScriptEditorView", layout = DefaultMainViewParent.class)
@ViewController("report_ScriptEditor.view")
@ViewDescriptor("script-editor-view.xml")
public class ScriptEditorView extends StandardView {
}