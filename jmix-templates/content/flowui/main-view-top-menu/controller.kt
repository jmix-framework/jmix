package ${packageName}

import com.google.common.base.Strings
import com.vaadin.flow.router.Route
import io.jmix.flowui.app.main.StandardMainView
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor

/*
 * To use the view as a main view don't forget to set
 * new value (see @ViewController) to 'jmix.ui.main-view-id' property.
 * Also, the route of this view (see @Route) must differ from the route of default MainView.
 */
@Route("")
@ViewController("${id}")
@ViewDescriptor("${descriptorName}.xml")
class ${controllerName} : StandardMainView() {

    override fun updateTitle() {
        super.updateTitle()

        val viewTitle: String = getTitleFromOpenedView()
        UiComponentUtils.findComponent(content, "viewHeaderBox").ifPresent { component ->
            component.isVisible = !Strings.isNullOrEmpty(viewTitle)
        }
    }
}