package io.jmix.securityflowui.view.resourcepolicy;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@ViewController("sec_EntityAttributeResourcePolicyModel.detail")
@ViewDescriptor("entity-attribute-resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class EntityAttributeResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {

    @ViewComponent
    private JmixComboBox<String> entityField;
    @ViewComponent
    private JmixComboBox<String> attributeField;
    @ViewComponent
    private JmixSelect<String> actionField;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;

    @Subscribe
    public void onInit(InitEvent event) {
        FlowuiComponentUtils.setItemsMap(entityField, resourcePolicyEditorUtils.getEntityOptionsMap());
        entityField.addValueChangeListener(this::onEntityFieldValueChange);
        attributeField.addValueChangeListener(this::onAttributeFieldValueChange);
        resourcePolicyEditorUtils.setEnumItemsAsString(actionField, EntityAttributePolicyAction.class);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        String resource = getEditedEntity().getResource();
        if (!Strings.isNullOrEmpty(resource)) {
            String entityName = resource.substring(0, resource.lastIndexOf("."));
            String attributeName = resource.substring(resource.lastIndexOf(".") + 1);

            entityField.setValue(entityName);

            fillAttributeField(entityName);
            attributeField.setValue(attributeName);
        }
    }

    private void onEntityFieldValueChange(ComponentValueChangeEvent<ComboBox<String>, String> event) {
        fillAttributeField(event.getValue());
        attributeField.clear();
        evaluateAndSetResource();
    }

    private void onAttributeFieldValueChange(ComponentValueChangeEvent<ComboBox<String>, String> event) {
        evaluateAndSetResource();
    }

    private void evaluateAndSetResource() {
        getEditedEntity().setResource(entityField.getValue() + "." + attributeField.getValue());
    }

    private void fillAttributeField(@Nullable String entityName) {
        FlowuiComponentUtils.setItemsMap(attributeField,
                resourcePolicyEditorUtils.getEntityAttributeOptionsMap(entityName));
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        entityField.setReadOnly(readOnly);
        attributeField.setReadOnly(readOnly);
    }
}
