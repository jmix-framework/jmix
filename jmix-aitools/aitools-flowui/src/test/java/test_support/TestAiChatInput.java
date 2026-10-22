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
import io.jmix.aitoolsflowui.fragment.input.AiChatInput;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A message input that only remembers what the host told it and lets a test submit text.
 */
public class TestAiChatInput extends Div implements AiChatInput {

    @Nullable
    Consumer<String> submitHandler;
    String text = "";
    boolean inputEnabled = true;

    @Override
    public void setSubmitHandler(@Nullable Consumer<String> submitHandler) {
        this.submitHandler = submitHandler;
    }

    @Override
    public void focus() {
    }

    @Override
    public void clear() {
        text = "";
    }

    @Override
    public void restoreText(String text) {
        this.text = text;
    }

    @Override
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
    }

    @Override
    public Component asComponent() {
        return this;
    }

    public void submit(String text) {
        if (submitHandler != null) {
            submitHandler.accept(text);
        }
    }

    public String getText() {
        return text;
    }

    public boolean isInputEnabled() {
        return inputEnabled;
    }
}
