/*
 * Copyright 2020 Haulmont.
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

package io.jmix.grapesjs.widget.grapesjshtmleditorcomponent;


import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
import elemental.json.impl.JreJsonString;
import io.jmix.grapesjs.component.GjsBlock;
import io.jmix.grapesjs.component.GjsPlugin;
import io.jmix.ui.widget.WebJarResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


@WebJarResource(value = {
        "jquery:jquery.min.js",
        "grapesjs:dist/grapes.min.js",
        "grapesjs:dist/css/grapes.min.css",
        "grapesjs-plugin-ckeditor:dist/index.js",
        "grapesjs-custom-code:dist/index.js",
        "grapesjs-tabs:dist/grapesjs-tabs.min.js",
        "grapesjs-blocks-basic:dist/index.js",
        "grapesjs-blocks-flexbox:dist/index.js",
        "grapesjs-tui-image-editor:dist/index.js",
        "grapesjs-plugin-forms:dist/index.js",
        "grapesjs-style-filter:dist/index.js",
        "grapesjs-tooltip:dist/index.js",
        "grapesjs-parser-postcss:dist/index.js",
        "grapesjs-preset-webpage:dist/index.js",
        "grapesjs-preset-newsletter:dist/index.js",
})
@JavaScript({"vaadin://ckeditor/ckeditor.js",
        "grapesjshtmleditorcomponent-connector.js"})
@StyleSheet({"vaadin://styles/grapesjs-style.css"})
public class GrapesJsHtmlEditorComponent extends AbstractJavaScriptComponent {

    public GrapesJsHtmlEditorComponent() {
        addFunction("valueChanged", arguments -> {
            JreJsonString data = arguments.get(0);
            String value = data.getString();
            getState(false).html = value;
            if (listener != null) {
                listener.valueChanged(value);
            }
        });

        addFunction("fileUploaded", arguments -> {
            String fileName = arguments.getString(0);
            String fileBase64 = arguments.getString(1);

            if (fileUploadListener != null) {
                fileUploadListener.fileUploaded(fileName, fileBase64);
            }
        });
    }

    @Override
    protected GrapesJsHtmlEditorComponentState getState() {
        return (GrapesJsHtmlEditorComponentState) super.getState();
    }

    @Override
    protected GrapesJsHtmlEditorComponentState getState(boolean markAsDirty) {
        return (GrapesJsHtmlEditorComponentState) super.getState(markAsDirty);
    }

    public String getHtml() {
        return getState(false).html;
    }

    public void setPlugins(Collection<GjsPlugin> plugins) {
        getState(false).plugins = plugins;
    }

    public void setInlineCss(Boolean inlineCss) {
        getState(false).inlineCss = inlineCss;
    }

    public void setBlocks(Collection<GjsBlock> blocks) {
        getState(false).blocks = blocks;
    }

    public interface ValueChangeListener {
        void valueChanged(String value);
    }

    public interface CommandResultListener {
        void onResult(String result);
    }

    public interface FileUploadListener {
        void fileUploaded(String name, String fileBase64);
    }

    private ValueChangeListener listener;

    private FileUploadListener fileUploadListener;

    public void setValue(String value) {
        getState().html = value;
    }

    public String getValue() {
        return getState(false).html;
    }

    public void runCommand(String command) {
        callFunction("runCommand", command);
    }

    public void stopCommand(String command) {
        callFunction("stopCommand", command);
    }

    public ValueChangeListener getListener() {
        return listener;
    }

    public void setListener(ValueChangeListener listener) {
        this.listener = listener;
    }

    public FileUploadListener getFileUploadListener() {
        return fileUploadListener;
    }

    public void setFileUploadListener(FileUploadListener fileUploadListener) {
        this.fileUploadListener = fileUploadListener;
    }

    public void setDisabledBlocks(Collection<String> disabledBlocks) {
        getState(false).disabledBlocks = disabledBlocks != null ? new ArrayList<>(disabledBlocks) : Collections.emptyList();
    }

}