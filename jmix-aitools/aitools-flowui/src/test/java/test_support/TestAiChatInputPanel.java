/*
 * Copyright 2026 Haulmont.
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

package test_support;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A message input panel that keeps its text and enabled state in fields.
 */
public class TestAiChatInputPanel extends Div implements AiChatInputPanel {

    @Nullable
    public Consumer<String> submitHandler;
    public String value = "";
    public boolean enabled = true;

    /**
     * Submits the text as the user would from the panel.
     *
     * @param text the submitted text
     */
    public void submit(String text) {
        if (submitHandler != null) {
            submitHandler.accept(text);
        }
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public void setSubmitHandler(@Nullable Consumer<String> submitHandler) {
        this.submitHandler = submitHandler;
    }

    @Override
    public void setPlaceholder(@Nullable String placeholder) {
    }

    @Override
    public void setInputEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isInputEnabled() {
        return enabled;
    }

    @Override
    public void focus() {
    }

    @Override
    public void restoreText(String text) {
        value = text;
    }
}
