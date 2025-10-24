package ${packageName}

import com.google.common.base.Strings
import com.vaadin.flow.router.Route
import io.jmix.flowui.app.main.StandardMainView
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor

<%if (!updateLayoutProperty) {%>/*
 * To use the view as a main view don't forget to set
 * new value (see @ViewController) to 'jmix.ui.main-view-id' property.
 */
@Route(value = "${route}")<%} else {%>@Route("")<%}%>
@ViewController(id = "${id}")
@ViewDescriptor(path = "${descriptorName}.xml")
class ${controllerName} : StandardMainView() {

    override fun updateTitle() {
        super.updateTitle()

        val viewTitle: String = getTitleFromOpenedView()
        UiComponentUtils.findComponent(content, "viewHeaderBox").ifPresent { component ->
            component.isVisible = !Strings.isNullOrEmpty(viewTitle)
        }
    }
}