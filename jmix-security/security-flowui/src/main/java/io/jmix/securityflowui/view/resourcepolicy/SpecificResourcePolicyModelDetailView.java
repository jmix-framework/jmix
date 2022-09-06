package io.jmix.securityflowui.view.resourcepolicy;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.GeneratedVaadinComboBox.CustomValueSetEvent;
import io.jmix.core.security.SpecificPolicyInfoRegistry;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@ViewController("sec_SpecificResourcePolicyModel.detail")
@ViewDescriptor("specific-resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class SpecificResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {

    @ViewComponent
    private JmixComboBox<String> resourceField;

    @Autowired
    private SpecificPolicyInfoRegistry specificPolicyInfoRegistry;

    @Subscribe
    public void onInit(InitEvent event) {
        List<String> specificPolicyNames = specificPolicyInfoRegistry.getSpecificPolicyInfos().stream()
                .map(SpecificPolicyInfoRegistry.SpecificPolicyInfo::getName)
                .sorted()
                .collect(Collectors.toList());

        resourceField.setItems(specificPolicyNames);
    }

    @Subscribe("resourceField")
    private void onCustomValueSet(CustomValueSetEvent<ComboBox<String>> event) {
        resourceField.setValue(event.getDetail());
    }
}
