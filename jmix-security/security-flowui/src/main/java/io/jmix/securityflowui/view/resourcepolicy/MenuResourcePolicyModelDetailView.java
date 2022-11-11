package io.jmix.securityflowui.view.resourcepolicy;

import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController("sec_MenuResourcePolicyModel.detail")
@ViewDescriptor("menu-resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class MenuResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {

    @ViewComponent
    private JmixComboBox<String> resourceField;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Subscribe
    public void onInit(InitEvent event) {
        FlowuiComponentUtils.setItemsMap(resourceField, resourcePolicyEditorUtils.getMenuItemOptionsMap());
    }

    @Subscribe(id = "resourcePolicyModelDc", target = Target.DATA_CONTAINER)
    public void onResourcePolicyModelDcItemPropertyChange(ItemPropertyChangeEvent<ResourcePolicyModel> event) {
        if ("resource".equals(event.getProperty())) {
            String policyGroup = resourcePolicyGroupResolver.resolvePolicyGroup(getEditedEntity().getTypeId(),
                    getEditedEntity().getResource());
            getEditedEntity().setPolicyGroup(policyGroup);
        }
    }
}
