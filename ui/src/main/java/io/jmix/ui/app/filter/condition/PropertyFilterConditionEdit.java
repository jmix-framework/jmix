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

package io.jmix.ui.app.filter.condition;

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.entity.PropertyFilterCondition;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("ui_PropertyFilterCondition.edit")
@UiDescriptor("property-filter-condition-edit.xml")
@EditedEntityContainer("filterConditionDc")
public class PropertyFilterConditionEdit extends FilterConditionEdit<PropertyFilterCondition> {

    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected InstanceContainer<PropertyFilterCondition> filterConditionDc;

    @Autowired
    protected TextField<String> propertyField;
    @Autowired
    protected TextField<String> captionField;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected TextField<String> parameterNameField;

    @Override
    public InstanceContainer<PropertyFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Install(to = "operationField", subject = "optionCaptionProvider")
    protected String operationFieldOptionCaptionProvider(PropertyFilter.Operation operation) {
        return propertyFilterSupport.getOperationCaption(operation);
    }
}
