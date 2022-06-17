package io.jmix.flowui.kit.meta.palette;

import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.kit.meta.StudioFlowComponent;
import io.jmix.flowui.kit.meta.StudioFlowComponents;
import io.jmix.flowui.kit.meta.StudioFlowProperty;
import io.jmix.flowui.kit.meta.StudioFlowPropertyType;

@StudioFlowComponents
public interface StudioFlowPaletteMainComponents {

    @StudioFlowComponent(
            name = "ListMenu",
            classFqn = "io.jmix.flowui.component.main.JmixListMenu",
            category = "Main",
            xmlElement = "listMenu",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "loadMenuConfig", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "metaClass", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "visible", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "width", type = StudioFlowPropertyType.SIZE)
            }
    )
    ListMenu listMenu();
}
