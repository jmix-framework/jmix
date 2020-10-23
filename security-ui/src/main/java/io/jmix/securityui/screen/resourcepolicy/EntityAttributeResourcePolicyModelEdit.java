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

package io.jmix.securityui.screen.resourcepolicy;

import com.google.common.base.Strings;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@UiController("sec_EntityAttributeResourcePolicyModel.edit")
@UiDescriptor("entity-attribute-resource-policy-model-edit.xml")
@EditedEntityContainer("resourcePolicyModelDc")
public class EntityAttributeResourcePolicyModelEdit extends StandardEditor<ResourcePolicyModel> {

    @Autowired
    private ComboBox<String> entityField;

    @Autowired
    private ComboBox<String> attributeField;

    @Autowired
    private ComboBox<String> actionField;

    @Autowired
    private ResourcePolicyEditorUtils resourcePolicyEditorUtils;

    @Autowired
    private MessageBundle messageBundle;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        entityField.setOptionsMap(resourcePolicyEditorUtils.getEntityOptionsMap());
        actionField.setOptionsList(Arrays.asList(
                EntityAttributePolicyAction.VIEW.getId(),
                EntityAttributePolicyAction.MODIFY.getId()));

        //fields with a null ValueSource are not disabled automatically by standard ViewAction
        entityField.setEditable(!this.isReadOnly());
        attributeField.setEditable(!this.isReadOnly());

        String resource = getEditedEntity().getResource();
        if (!Strings.isNullOrEmpty(resource)) {
            String entityName = resource.substring(0, resource.lastIndexOf("."));
            String attributeName = resource.substring(resource.lastIndexOf(".") + 1);

            entityField.setValue(entityName);

            fillAttributeField(entityName);
            attributeField.setValue(attributeName);
        }
    }

    @Subscribe("entityField")
    public void onEntityFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        fillAttributeField(event.getValue());
        attributeField.setValue(null);
        evaluateAndSetResource();
    }

    @Subscribe("attributeField")
    public void onAttributeFieldValueChange(HasValue.ValueChangeEvent event) {
        evaluateAndSetResource();
    }

    private void evaluateAndSetResource() {
        getEditedEntity().setResource(entityField.getValue() + "." + attributeField.getValue());
    }

    private void fillAttributeField(@Nullable String entityName) {
        Map<String, String> optionsMap;
        if ("*".equals(entityName)) {
            optionsMap = Collections.singletonMap(messageBundle.getMessage("allAttributes"), "*");
        } else {
            optionsMap = !Strings.isNullOrEmpty(entityName) ?
                    resourcePolicyEditorUtils.getEntityAttributeOptionsMap(entityName) :
                    new HashMap<>();
        }
        attributeField.setOptionsMap(optionsMap);
    }
}