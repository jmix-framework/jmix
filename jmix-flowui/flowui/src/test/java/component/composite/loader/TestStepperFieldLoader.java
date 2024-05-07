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

package component.composite.loader;

import component.composite.component.TestStepperField;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public class TestStepperFieldLoader extends AbstractComponentLoader<TestStepperField> {
    @Override
    protected TestStepperField createComponent() {
        return factory.create(TestStepperField.class);
    }

    @Override
    public void loadComponent() {
        // TODO: gg, implement
    }
}
