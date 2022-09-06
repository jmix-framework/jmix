package io.jmix.securityflowui.view.resourcepolicy;

import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.ResourcePolicyModel;

@ViewController("sec_GraphQLResourcePolicyModel.detail")
@ViewDescriptor("graphql-resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class GraphQLResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {
}
