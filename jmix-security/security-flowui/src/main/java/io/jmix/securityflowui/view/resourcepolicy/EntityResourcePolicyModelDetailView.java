package io.jmix.securityflowui.view.resourcepolicy;

import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController("sec_EntityResourcePolicyModel.detail")
@ViewDescriptor("entity-resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class EntityResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {

    @ViewComponent
    private JmixComboBox<String> entityField;
    @ViewComponent
    private JmixSelect<String> actionField;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;

    @Subscribe
    public void onInit(InitEvent event) {
        setReloadEdited(false);
        ComponentUtils.setItemsMap(entityField, resourcePolicyEditorUtils.getEntityOptionsMap());
        resourcePolicyEditorUtils.setEnumItemsAsString(actionField, EntityPolicyAction.class);
    }
}
