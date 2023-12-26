package ${packageName};

import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

/*
 * To use the view as a main view, please, don't forget to set 'jmix.ui.main-view-id' property
 * to new value (see @ViewController annotation).
 * Also, please, check that the route of default MainView is not the same
 * as the route of this view (see @Route annotation).
 */
@Route("")
@ViewController("${id}")
@ViewDescriptor("${descriptorName}.xml")
public class ${controllerName} extends StandardMainView {
}