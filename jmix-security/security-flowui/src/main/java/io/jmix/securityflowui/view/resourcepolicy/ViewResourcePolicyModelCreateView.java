package io.jmix.securityflowui.view.resourcepolicy;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.securityflowui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import io.jmix.securityflowui.model.ResourcePolicyType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@ViewController("sec_ViewResourcePolicyModel.create")
@ViewDescriptor("view-resource-policy-model-create-view.xml")
@DialogMode(width = "32em")
public class ViewResourcePolicyModelCreateView extends MultipleResourcePolicyModelCreateView {

    @ViewComponent
    private JmixComboBox<String> viewField;
    @ViewComponent
    private TypedTextField<String> policyGroupField;
    @ViewComponent
    private JmixTextArea menuItemField;
    @ViewComponent
    private JmixCheckbox menuAccessField;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private Metadata metadata;
    @Autowired
    private MessageBundle messageBundle;

    private String menuItemId;

    @Subscribe
    public void onInit(InitEvent event) {
        FlowuiComponentUtils.setItemsMap(viewField, resourcePolicyEditorUtils.getViewsOptionsMap());
        viewField.addValueChangeListener(this::onViewFieldValueChange);
    }

    private void onViewFieldValueChange(ComponentValueChangeEvent<ComboBox<String>, String> event) {
        String viewId = event.getValue();
        String policyGroup = resourcePolicyGroupResolver
                .resolvePolicyGroup(ResourcePolicyType.VIEW.getId(), viewId);
        if (policyGroup != null) {
            policyGroupField.setValue(policyGroup);
        } else {
            policyGroupField.clear();
        }

        menuItemId = null;
        MenuItem menuItem = null;
        if (viewId != null) {
            menuItem = resourcePolicyEditorUtils.findMenuItemByView(viewId);
            if (menuItem != null) {
                menuItemId = menuItem.getId();
            }
        }

        if (this.menuItemId == null) {
            menuItemField.clear();
            menuAccessField.setValue(Boolean.FALSE);
            menuAccessField.setReadOnly(true);
        } else if (menuItem != null) {
            menuItemField.setValue(resourcePolicyEditorUtils.getMenuTitle(menuItem));
            menuAccessField.setReadOnly(false);
        }
    }

    @Override
    protected ValidationErrors validateView() {
        ValidationErrors validationErrors = new ValidationErrors();
        if (Strings.isNullOrEmpty(viewField.getValue())) {
            validationErrors.add(viewField,
                    messageBundle.getMessage("viewResourcePolicyModelCreateView.error.selectView"));
        }

        return validationErrors;
    }

    @Override
    public List<ResourcePolicyModel> getResourcePolicies() {
        List<ResourcePolicyModel> policies = new ArrayList<>();
        String viewId = viewField.getValue();

        ResourcePolicyModel viewPolicy = metadata.create(ResourcePolicyModel.class);
        viewPolicy.setType(ResourcePolicyType.VIEW);
        viewPolicy.setResource(viewId);
        viewPolicy.setPolicyGroup(policyGroupField.getValue());
        viewPolicy.setAction(ResourcePolicy.DEFAULT_ACTION);
        viewPolicy.setEffect(ResourcePolicyEffect.ALLOW);
        policies.add(viewPolicy);

        if (Boolean.TRUE.equals(menuAccessField.getValue()) && menuItemId != null) {
            ResourcePolicyModel menuPolicy = metadata.create(ResourcePolicyModel.class);
            menuPolicy.setType(ResourcePolicyType.MENU);
            menuPolicy.setResource(menuItemId);
            menuPolicy.setPolicyGroup(policyGroupField.getValue());
            menuPolicy.setAction(ResourcePolicy.DEFAULT_ACTION);
            menuPolicy.setEffect(ResourcePolicyEffect.ALLOW);
            policies.add(menuPolicy);
        }

        return policies;
    }
}
