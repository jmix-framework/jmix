package io.jmix.reportsflowui.view.scripteditor;


import io.jmix.reportsflowui.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "ScriptEditorView", layout = MainView.class)
@ViewController("ScriptEditorView")
@ViewDescriptor("script-editor-view.xml")
public class ScriptEditorView extends StandardView {
}