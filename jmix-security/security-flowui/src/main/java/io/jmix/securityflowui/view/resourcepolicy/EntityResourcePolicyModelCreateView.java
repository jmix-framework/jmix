package io.jmix.securityflowui.view.resourcepolicy;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.data.items.EnumDataProvider;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.securityflowui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import io.jmix.securityflowui.model.ResourcePolicyType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ViewController("sec_EntityResourcePolicyModel.create")
@ViewDescriptor("entity-resource-policy-model-create-view.xml")
@DialogMode(width = "32em")
public class EntityResourcePolicyModelCreateView extends MultipleResourcePolicyModelCreateView {

    private static final Set<EntityPolicyAction> ALL_ACTION =
            Sets.newHashSet(EntityPolicyAction.class.getEnumConstants());

    @ViewComponent
    private JmixComboBox<String> entityField;
    @ViewComponent
    private TypedTextField<String> policyGroupField;
    @ViewComponent
    private JmixCheckbox allActions;
    @ViewComponent
    private JmixCheckboxGroup<EntityPolicyAction> actionsGroup;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private Metadata metadata;
    @Autowired
    private MessageBundle messageBundle;

    @Subscribe
    public void onInit(InitEvent event) {
        FlowuiComponentUtils.setItemsMap(entityField, resourcePolicyEditorUtils.getEntityOptionsMap());
        entityField.addValueChangeListener(this::onEntityFieldValueChange);

        actionsGroup.setItems(new EnumDataProvider<>(EntityPolicyAction.class));
        actionsGroup.addValueChangeListener(this::onActionGroupValueChange);

        allActions.addValueChangeListener(this::onAllActionValueChange);
    }

    private void onEntityFieldValueChange(ComponentValueChangeEvent<ComboBox<String>, String> event) {
        String entityName = event.getValue();
        String policyGroup = resourcePolicyGroupResolver
                .resolvePolicyGroup(ResourcePolicyType.ENTITY.getId(), entityName);
        if (policyGroup != null) {
            policyGroupField.setValue(policyGroup);
        } else {
            policyGroupField.clear();
        }
    }

    private void onActionGroupValueChange(
            ComponentValueChangeEvent<CheckboxGroup<EntityPolicyAction>, Set<EntityPolicyAction>> event) {
        long size = actionsGroup.getListDataView().getItems().count();

        if (event.getValue().size() == size) {
            allActions.setValue(true);
            allActions.setIndeterminate(false);
        } else if (event.getValue().isEmpty()) {
            allActions.setValue(false);
            allActions.setIndeterminate(false);
        } else {
            allActions.setIndeterminate(true);
        }
    }

    private void onAllActionValueChange(ComponentValueChangeEvent<Checkbox, Boolean> event) {
        if (Boolean.TRUE.equals(event.getValue())) {
            actionsGroup.setValue(ALL_ACTION);
        } else {
            actionsGroup.deselectAll();
        }
    }

    @Override
    protected ValidationErrors validateView() {
        ValidationErrors validationErrors = new ValidationErrors();

        if (Strings.isNullOrEmpty(entityField.getValue())) {
            validationErrors.add(entityField,
                    messageBundle.getMessage("entityResourcePolicyModelCreateView.error.selectEntity"));
        }

        if (getPolicyActions().isEmpty()) {
            validationErrors.add(entityField,
                    messageBundle.getMessage("entityResourcePolicyModelCreateView.error.selectActions"));
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

    private Set<String> getPolicyActions() {
        return actionsGroup.getValue().stream()
                .map(EntityPolicyAction::getId)
                .collect(Collectors.toSet());
    }
}
