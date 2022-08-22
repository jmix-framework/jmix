package io.jmix.securityflowui.view.resourcepolicy;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.securityflowui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import io.jmix.securityflowui.model.ResourcePolicyType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UiController("sec_EntityAttributeResourcePolicyModel.create")
@UiDescriptor("entity-attribute-resource-policy-model-create-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "40em")
public class EntityAttributeResourcePolicyModelCreateView extends MultipleResourcePolicyModelCreateView {

    @ComponentId
    private JmixComboBox<String> entityField;
    @ComponentId
    private TypedTextField<String> policyGroupField;
    @ComponentId
    private DataGrid<AttributeResourceModel> attributesTable;

    @ComponentId
    private CollectionContainer<AttributeResourceModel> attributesDc;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInit(InitEvent event) {
        FlowuiComponentUtils.setItemsMap(entityField, resourcePolicyEditorUtils.getEntityOptionsMap());
        entityField.addValueChangeListener(this::onEntityFieldValueChange);

        initTable();
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

        fillAttributesTable(entityName);
    }

    private void initTable() {
        attributesTable.addColumn(createPermissionChangeRenderer("view"))
                .setHeader(messageTools.getPropertyCaption(attributesDc.getEntityMetaClass(), "view"));
        attributesTable.addColumn(createPermissionChangeRenderer("modify"))
                .setHeader(messageTools.getPropertyCaption(attributesDc.getEntityMetaClass(), "modify"));
    }

    private ComponentRenderer<Checkbox, AttributeResourceModel> createPermissionChangeRenderer(String property) {
        return new ComponentRenderer<>(
                (SerializableFunction<AttributeResourceModel, Checkbox>) item -> {
                    Checkbox checkbox = new Checkbox();

                    switch (property) {
                        case "view":
                            checkbox.setValue(item.getView());
                            break;
                        case "modify":
                            checkbox.setValue(item.getModify());
                            break;
                    }

                    checkbox.addValueChangeListener(event -> {
                        switch (property) {
                            case "view":
                                item.setView(checkbox.getValue());
                                break;
                            case "modify":
                                item.setModify(checkbox.getValue());
                                break;
                        }
                        attributesDc.replaceItem(item);
                    });

                    return checkbox;
                });
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
}
