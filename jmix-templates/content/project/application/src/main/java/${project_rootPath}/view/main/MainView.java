package ${project_rootPackage}.view.main;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({"USER", "ADMIN"})
@Route("")
@ViewController("${normalizedPrefix_underscore}MainView")
@ViewDescriptor("main-view.xml")
public class MainView extends StandardMainView {
}
