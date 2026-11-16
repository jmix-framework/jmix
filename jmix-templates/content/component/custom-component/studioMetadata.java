package ${packageName};

import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
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
            // TODO describe your component's own XML attributes, e.g.:
            // properties = @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
            propertyGroups = {
                    StudioPropertyGroups.BaseSizedEnabledComponentWithClassName.class,
                    StudioPropertyGroups.ClickShortcut.class
            }
    )
    void ${xmlElement}();
}
