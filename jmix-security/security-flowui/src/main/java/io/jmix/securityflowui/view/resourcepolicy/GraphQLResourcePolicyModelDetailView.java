package io.jmix.securityflowui.view.resourcepolicy;

import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.ResourcePolicyModel;

@UiController("sec_GraphQLResourcePolicyModel.detail")
@UiDescriptor("graphql-resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class GraphQLResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {
}
