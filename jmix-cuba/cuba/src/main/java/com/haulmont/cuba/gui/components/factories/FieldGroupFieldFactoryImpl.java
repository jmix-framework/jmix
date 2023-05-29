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

package com.haulmont.cuba.gui.components.factories;

import com.haulmont.cuba.gui.components.ComponentGenerationContext;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.components.FieldGroupFieldFactory;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.RuntimePropsDatasource;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.UiComponentsGenerator;

import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component(FieldGroupFieldFactory.NAME)
public class FieldGroupFieldFactoryImpl implements FieldGroupFieldFactory {
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;

    @Override
    public GeneratedField createField(FieldGroup.FieldConfig fc) {
        return createFieldComponent(fc);
    }

    protected GeneratedField createFieldComponent(FieldGroup.FieldConfig fc) {
        MetaClass metaClass = resolveMetaClass(fc.getTargetDatasource());

        ComponentGenerationContext context = new ComponentGenerationContext(metaClass, fc.getProperty())
                .setDatasource(fc.getTargetDatasource())
                .setOptionsDatasource(fc.getOptionsDatasource());

        context.setXmlDescriptor(fc.getXmlDescriptor());
        context.setComponentClass(FieldGroup.class);

        return new GeneratedField(uiComponentsGenerator.generate(context));
    }

    protected MetaClass resolveMetaClass(Datasource datasource) {
        return datasource instanceof RuntimePropsDatasource ?
                ((RuntimePropsDatasource) datasource).resolveCategorizedEntityClass()
                : datasource.getMetaClass();
    }
}
