package io.jmix.flowui.kit.meta.palette;

import com.vaadin.flow.component.details.Details;
import io.jmix.flowui.kit.meta.StudioFlowComponent;
import io.jmix.flowui.kit.meta.StudioFlowComponents;
import io.jmix.flowui.kit.meta.StudioFlowProperty;
import io.jmix.flowui.kit.meta.StudioFlowPropertyType;

@StudioFlowComponents
public interface StudioFlowPaletteContainers {

    @StudioFlowComponent(
            name = "Details",
            classFqn = "com.vaadin.flow.component.details.Details",
            category = "Containers",
            xmlElement = "details",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "summaryText", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"filled", "reverse", "small"}),
                    @StudioFlowProperty(xmlAttribute = "visible", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "width", type = StudioFlowPropertyType.SIZE)
            }
    )
    Details details();
}
