/*
 * Copyright 2024 Haulmont.
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

package component.composite.component;

import io.jmix.flowui.component.composite.CompositeComponent;
import io.jmix.flowui.component.textfield.TypedTextField;

public class TestTypedTextField extends CompositeComponent<TypedTextField<String>> {

    private boolean isPostInitListenerFired = false;

    public TestTypedTextField() {
        addPostInitListener(event -> {

        });
    }

    public boolean isPostInitListenerFired() {
        return isPostInitListenerFired;
    }

    @Override
    protected TypedTextField<String> initContent() {
        TypedTextField<String> textField = super.initContent();
        textField.setClearButtonVisible(true);

        return textField;
    }
}