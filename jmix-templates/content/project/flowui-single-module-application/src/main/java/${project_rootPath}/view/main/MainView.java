package ${project_rootPackage}.view.main;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainScreen;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;

@Route("")
@UiController("${normalizedPrefix_underscore}MainView")
@UiDescriptor("main-view.xml")
public class MainView extends StandardMainScreen {
}
