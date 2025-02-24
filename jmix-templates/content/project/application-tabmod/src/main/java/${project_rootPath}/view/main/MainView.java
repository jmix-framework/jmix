package ${project_rootPackage}.view.main;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.tabbedmode.app.main.StandardTabbedModeMainView;

@Route("")
@ViewController(id = "${normalizedPrefix_underscore}MainView")
@ViewDescriptor(path = "main-view.xml")
public class MainView extends StandardTabbedModeMainView {
}
