package ${packageName};

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.tabbedmode.app.main.StandardTabbedModeMainView;

<%if (!updateLayoutProperty) {%>/*
 * To use the view as a main view don't forget to set
 * new value (see @ViewController) to 'jmix.ui.main-view-id' property.
 * Also, the route of this view (see @Route) must differ from the route of default MainView.
 */<%}%>
@Route("")
@ViewController(id = "${id}")
@ViewDescriptor(path = "${descriptorName}.xml")
public class ${controllerName} extends StandardTabbedModeMainView {
}