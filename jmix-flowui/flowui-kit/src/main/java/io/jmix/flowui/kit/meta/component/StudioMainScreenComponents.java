package io.jmix.flowui.kit.meta.component;

import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioMainScreenComponents {

    @StudioComponent(
            name = "ListMenu",
            classFqn = "io.jmix.flowui.component.main.JmixListMenu",
            category = "Main screen",
            xmlElement = "listMenu",
            properties = {
                    @StudioProperty(xmlAttribute = "className", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "loadMenuConfig", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "metaClass", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    ListMenu listMenu();
}
