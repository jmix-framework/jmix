package component_registration.screen;

import com.vaadin.flow.router.Route;
import component_registration.TestThirdJmixButton;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("component-registrations-test-screen")
@ViewController
@ViewDescriptor("component-registrations-test-screen.xml")
public class ComponentRegistrationTestScreen extends StandardView {

    @ViewComponent
    public TestThirdJmixButton button;
}
