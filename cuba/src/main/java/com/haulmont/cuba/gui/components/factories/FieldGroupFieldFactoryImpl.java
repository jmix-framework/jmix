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

import com.haulmont.cuba.gui.components.FieldGroup;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.components.ComponentGenerationContext;
import com.haulmont.cuba.gui.components.FieldGroupFieldFactory;
import io.jmix.ui.components.UiComponentsGenerator;
import io.jmix.ui.dynamicattributes.DynamicAttributesUtils;

import javax.inject.Inject;

@org.springframework.stereotype.Component(FieldGroupFieldFactory.NAME)
public class FieldGroupFieldFactoryImpl implements FieldGroupFieldFactory {

//    @Inject
//    protected DynamicAttributes dynamicAttributes;

    @Inject
    protected UiComponentsGenerator uiComponentsGenerator;

    // todo dynamic attributes
//    @Inject
//    protected DynamicAttributeComponentsGenerator dynamicAttributeComponentsGenerator;

    @Override
    public GeneratedField createField(FieldGroup.FieldConfig fc) {
        return createFieldComponent(fc);
    }

    protected GeneratedField createFieldComponent(FieldGroup.FieldConfig fc) {
        MetaClass metaClass = null /* resolveMetaClass(fc.getTargetDatasource()) TODO: legacy-ui */;

        if (DynamicAttributesUtils.isDynamicAttribute(fc.getProperty())) {
            // todo dynamic attributes
//            CategoryAttribute attribute = dynamicAttributes.getAttributeForMetaClass(metaClass, fc.getProperty());
//            if (attribute != null && BooleanUtils.isTrue(attribute.getIsCollection())) {
//                //noinspection unchecked
//                DatasourceValueSource valueSource = new DatasourceValueSource(fc.getTargetDatasource(), fc.getProperty());
//                Component fieldComponent = dynamicAttributeComponentsGenerator.generateComponent(valueSource, attribute);
//                return new GeneratedField(fieldComponent);
//            }
        }

        ComponentGenerationContext context = new ComponentGenerationContext(metaClass, fc.getProperty())
                /*
                TODO: legacy-ui
                .setDatasource(fc.getTargetDatasource())
                .setOptionsDatasource(fc.getOptionsDatasource())*/
                .setXmlDescriptor(fc.getXmlDescriptor())
                .setComponentClass(FieldGroup.class);

        return new GeneratedField(uiComponentsGenerator.generate(context));
    }

    /*
    TODO: legacy-ui
    protected MetaClass resolveMetaClass(Datasource datasource) {
        // todo dynamic attributes
        return *//* datasource instanceof RuntimePropsDatasource ?
                ((RuntimePropsDatasource) datasource).resolveCategorizedEntityClass() : *//* datasource.getMetaClass();
    }*/
}
