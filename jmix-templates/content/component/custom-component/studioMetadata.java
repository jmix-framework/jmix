package ${packageName};

import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface ${studioMetadataClassName} {

    @StudioComponent(
            name = "${componentClassName}",
            classFqn = "${componentFqn}",
            category = "Components",
            xmlElement = "${xmlElement}",
            xmlns = "${namespace}",
            xmlnsAlias = "${namespaceAlias}",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST, category = StudioProperty.Category.LOOK_AND_FEEL)
            }
    )
    void ${xmlElement}();
}
