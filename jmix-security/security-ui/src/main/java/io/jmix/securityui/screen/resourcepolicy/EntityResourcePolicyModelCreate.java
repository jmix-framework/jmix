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
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.CheckBoxGroup;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.screen.MessageBundle;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UiController("sec_EntityResourcePolicyModel.create")
@UiDescriptor("entity-resource-policy-model-create.xml")
public class EntityResourcePolicyModelCreate extends MultipleResourcePolicyModelCreateScreen {

    @Autowired
    private ComboBox<String> entityField;
    @Autowired
    private CheckBox allCheckBox;
    @Autowired
    private CheckBoxGroup<EntityPolicyAction> actionsCheckBoxGroup;

    @Autowired
    private TextField<String> policyGroupField;

    @Autowired
    private ResourcePolicyEditorUtils resourcePolicyEditorUtils;

    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private Metadata metadata;

    @Autowired
    private MessageBundle messageBundle;

    private boolean hasChanges = false;

    @Subscribe
    public void onInit(InitEvent event) {
        entityField.setOptionsMap(resourcePolicyEditorUtils.getEntityOptionsMap());
    }

    @Subscribe("policyGroupField")
    public void onPolicyGroupFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        hasChanges = true;
    }

    @Subscribe("entityField")
    public void onEntityFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String entityName = event.getValue();
        policyGroupField.setValue(resourcePolicyGroupResolver.resolvePolicyGroup(ResourcePolicyType.ENTITY, entityName));
        hasChanges = true;
    }

    private Set<String> getPolicyActions() {
        Set<String> actions = actionsCheckBoxGroup.getValue() != null ? new HashSet<>(actionsCheckBoxGroup.getValue()
                .stream().map(EntityPolicyAction::getId).collect(Collectors.toList())) : Collections.emptySet();
        return actions;
    }

    @Subscribe("actionsCheckBoxGroup")
    public void onActionsCheckBoxGroupValueChange(HasValue.ValueChangeEvent<Collection<EntityPolicyAction>> event) {
        allCheckBox.setEditable(false);
        if (event.getValue() != null && event.getValue().size() == EntityPolicyAction.values().length) {
            allCheckBox.setValue(true);
        } else {
            allCheckBox.setValue(false);
        }
        allCheckBox.setEditable(true);
        hasChanges = true;
    }

    @Subscribe("allCheckBox")
    public void onAllActionsCheckBoxValueChange(HasValue.ValueChangeEvent<Boolean> booleanValueChangeEvent) {

        Boolean allIsChecked = Boolean.TRUE.equals(booleanValueChangeEvent.getValue());
        if (booleanValueChangeEvent.isUserOriginated()) {
            if (allIsChecked) {
                actionsCheckBoxGroup.setValue(Arrays.stream(EntityPolicyAction.values())
                        .collect(Collectors.toList()));
            } else {
                actionsCheckBoxGroup.clear();
            }
        }
        hasChanges = true;
    }

    @Override
    protected ValidationErrors validateScreen() {
        ValidationErrors validationErrors = new ValidationErrors();
        if (Strings.isNullOrEmpty(entityField.getValue())) {
            validationErrors.add(entityField, messageBundle.getMessage("EntityResourcePolicyModelCreate.selectEntity"));
        }
        if (getPolicyActions().isEmpty()) {
            validationErrors.add(entityField, messageBundle.getMessage("EntityResourcePolicyModelCreate.selectActions"));
        }
        return validationErrors;
    }

    @Override
    public List<ResourcePolicyModel> getResourcePolicies() {
        List<ResourcePolicyModel> policies = new ArrayList<>();
        String entityName = entityField.getValue();
        for (String action : getPolicyActions()) {
            ResourcePolicyModel policy = metadata.create(ResourcePolicyModel.class);
            policy.setType(ResourcePolicyType.ENTITY);
            policy.setResource(entityName);
            policy.setPolicyGroup(policyGroupField.getValue());
            policy.setAction(action);
            policy.setEffect(ResourcePolicyEffect.ALLOW);
            policies.add(policy);
        }
        return policies;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return hasChanges;
    }
}
