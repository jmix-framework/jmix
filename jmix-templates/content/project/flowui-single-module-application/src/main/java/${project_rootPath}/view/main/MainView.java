package ${project_rootPackage}.view.main;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.view.UiController;
import io.jmix.flowui.view.UiDescriptor;

@Route("")
@UiController("${normalizedPrefix_underscore}MainView")
@UiDescriptor("main-view.xml")
public class MainView extends StandardMainView {
}
