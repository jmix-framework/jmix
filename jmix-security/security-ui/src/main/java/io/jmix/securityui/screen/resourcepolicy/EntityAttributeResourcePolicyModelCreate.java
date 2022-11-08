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
import io.jmix.core.Metadata;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.MessageBundle;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UiController("sec_EntityAttributeResourcePolicyModel.create")
@UiDescriptor("entity-attribute-resource-policy-model-create.xml")
public class EntityAttributeResourcePolicyModelCreate extends MultipleResourcePolicyModelCreateScreen {

    @Autowired
    private ComboBox<String> entityField;

    @Autowired
    private TextField<String> policyGroupField;

    @Autowired
    private ResourcePolicyEditorUtils resourcePolicyEditorUtils;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private CollectionContainer<AttributeResourceModel> attributesDc;

    @Autowired
    private Metadata metadata;

    private boolean hasChanges = false;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        entityField.setOptionsMap(resourcePolicyEditorUtils.getEntityOptionsMap());
    }

    @Subscribe("policyGroupField")
    public void onPolicyGroupFieldValueChange(HasValue.ValueChangeEvent<String> stringValueChangeEvent){
        this.hasChanges = true;
    }

    @Subscribe("entityField")
    public void onEntityFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String entityName = event.getValue();
        fillAttributesTable(entityName);
        policyGroupField.setValue(resourcePolicyGroupResolver.resolvePolicyGroup(ResourcePolicyType.ENTITY, entityName));
        hasChanges = true;
    }

    private String generateResourceString(String entityName, String attributeName) {
        return entityName + "." + attributeName;
    }

    private void fillAttributesTable(@Nullable String entityName) {
        Map<String, String> optionsMap;
        if ("*".equals(entityName)) {
            optionsMap = Collections.singletonMap(messageBundle.getMessage("allAttributes"), "*");
        } else {
            optionsMap = !Strings.isNullOrEmpty(entityName) ?
                    resourcePolicyEditorUtils.getEntityAttributeOptionsMap(entityName) :
                    null;
        }

        attributesDc.getMutableItems().clear();

        if (optionsMap != null) {
            for (Map.Entry<String, String> entry : optionsMap.entrySet()) {
                String attributeCaption = entry.getKey();
                String attributeName = entry.getValue();
                AttributeResourceModel attribute = metadata.create(AttributeResourceModel.class);
                attribute.setName(attributeName);
                attribute.setCaption(attributeCaption);
                attributesDc.getMutableItems().add(attribute);
            }
        }
    }

    @Subscribe(id = "attributesDc", target = Target.DATA_CONTAINER)
    public void onAttributesDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<AttributeResourceModel> event) {
        String property = event.getProperty();
        Object value = event.getValue();
        AttributeResourceModel item = event.getItem();
        if ("view".equals(property) && Boolean.TRUE.equals(value)) {
            item.setModify(false);
        }
        if ("modify".equals(property) && Boolean.TRUE.equals(value)) {
            item.setView(false);
        }
    }

    @Override
    public List<ResourcePolicyModel> getResourcePolicies() {
        List<ResourcePolicyModel> policies = new ArrayList<>();
        String entityName = entityField.getValue();
        for (AttributeResourceModel attribute : attributesDc.getItems()) {
            if (attribute.getModify() || attribute.getView()) {
                ResourcePolicyModel policy = metadata.create(ResourcePolicyModel.class);
                policy.setType(ResourcePolicyType.ENTITY_ATTRIBUTE);
                policy.setResource(generateResourceString(entityName, attribute.getName()));
                policy.setPolicyGroup(policyGroupField.getValue());
                policy.setEffect(ResourcePolicyEffect.ALLOW);
                if (attribute.getModify()) {
                    policy.setAction(EntityAttributePolicyAction.MODIFY.getId());
                } else {
                    policy.setAction(EntityAttributePolicyAction.VIEW.getId());
                }
                policies.add(policy);
            }
        }
        return policies;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return hasChanges;
    }

    @Override
    protected ValidationErrors validateScreen() {
        ValidationErrors validationErrors = new ValidationErrors();
        if (Strings.isNullOrEmpty(entityField.getValue())) {
            validationErrors.add(entityField, messageBundle.getMessage("EntityAttributeResourcePolicyModelCreate.selectEntity"));
        }
        return validationErrors;
    }
}
