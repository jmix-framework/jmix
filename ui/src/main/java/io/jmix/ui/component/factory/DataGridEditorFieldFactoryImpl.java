/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.factory;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.meta.EntityValueSource;

import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("ui_DataGridEditorFieldFactory")
public class DataGridEditorFieldFactoryImpl implements DataGridEditorFieldFactory {

    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;

    @Override
    public Field<?> createField(EntityValueSource valueSource, String property) {
        MetaClass metaClass = valueSource.getEntityMetaClass();

        ComponentGenerationContext context = new ComponentGenerationContext(metaClass, property)
                .setValueSource(valueSource)
                .setTargetClass(DataGrid.class);

        Component component = uiComponentsGenerator.generate(context);
        if (!(component instanceof Field)) {
            throw new IllegalStateException("Editor field must implement Field");
        }

        return (Field<?>) component;
    }
}
