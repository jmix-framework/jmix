package io.jmix.securityflowui.view.resourcepolicy;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableFunction;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityflowui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.security.model.ResourcePolicyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ViewController("sec_EntityAttributeResourcePolicyModel.create")
@ViewDescriptor("entity-attribute-resource-policy-model-create-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "40em")
public class EntityAttributeResourcePolicyModelCreateView extends MultipleResourcePolicyModelCreateView {

    @ViewComponent
    private JmixComboBox<String> entityField;
    @ViewComponent
    private TypedTextField<String> policyGroupField;
    @ViewComponent
    private DataGrid<AttributeResourceModel> attributesTable;

    @ViewComponent
    private CollectionContainer<AttributeResourceModel> attributesDc;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private Metadata metadata;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private MessageBundle messageBundle;

    private boolean hasChanges = false;

    @Subscribe
    public void onInit(InitEvent event) {
        ComponentUtils.setItemsMap(entityField, resourcePolicyEditorUtils.getEntityOptionsMap());
        entityField.addValueChangeListener(this::onEntityFieldValueChange);
    }

    private void onEntityFieldValueChange(ComponentValueChangeEvent<ComboBox<String>, String> event) {
        String entityName = event.getValue();
        String policyGroup = resourcePolicyGroupResolver
                .resolvePolicyGroup(ResourcePolicyType.ENTITY, entityName);
        if (policyGroup != null) {
            policyGroupField.setValue(policyGroup);
        } else {
            policyGroupField.clear();
        }

        fillAttributesTable(entityName);

        hasChanges = true;
    }

    @Subscribe("policyGroupField")
    public void onPolicyGroupFieldValueChange(ComponentValueChangeEvent<TypedTextField<String>, String> event) {
        this.hasChanges = true;
    }

    private void fillAttributesTable(@Nullable String entityName) {
        Map<String, String> optionsMap = resourcePolicyEditorUtils.getEntityAttributeOptionsMap(entityName);

        attributesDc.getMutableItems().clear();

        if (optionsMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : optionsMap.entrySet()) {
            AttributeResourceModel attribute = metadata.create(AttributeResourceModel.class);
            attribute.setName(entry.getKey());
            attribute.setCaption(entry.getValue());

            attributesDc.getMutableItems().add(attribute);
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
    protected ValidationErrors validateView() {
        ValidationErrors validationErrors = new ValidationErrors();
        if (Strings.isNullOrEmpty(entityField.getValue())) {
            validationErrors.add(entityField,
                    messageBundle.getMessage("entityAttributeResourcePolicyModelCreateView.error.selectEntity"));
        }

        return validationErrors;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return hasChanges;
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

    private String generateResourceString(String entityName, String attributeName) {
        return entityName + "." + attributeName;
    }

    @Supply(to = "attributesTable.view", subject = "renderer")
    private Renderer<AttributeResourceModel> attributesTableViewRenderer() {
        return new ComponentRenderer<>(this::viewPermissionChangeUpdater);
    }

    @Supply(to = "attributesTable.modify", subject = "renderer")
    private Renderer<AttributeResourceModel> attributesTableModifyRenderer() {
        return new ComponentRenderer<>(this::modifyPermissionChangeUpdater);
    }

    private Checkbox viewPermissionChangeUpdater(AttributeResourceModel item) {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(item.getView());

        checkbox.addValueChangeListener(event -> {
            item.setView(checkbox.getValue());
            attributesDc.replaceItem(item);
        });

        return checkbox;
    }

    private Checkbox modifyPermissionChangeUpdater(AttributeResourceModel item) {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(item.getModify());

        checkbox.addValueChangeListener(event -> {
            item.setModify(checkbox.getValue());
            attributesDc.replaceItem(item);
        });

        return checkbox;
    }
}
