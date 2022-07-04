package component_registration.screen;

import com.vaadin.flow.router.Route;
import component_registration.TestThirdJmixButton;
import io.jmix.flowui.view.ComponentId;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.UiController;
import io.jmix.flowui.view.UiDescriptor;

@Route("component-registrations-test-screen")
@UiController
@UiDescriptor("component-registrations-test-screen.xml")
public class ComponentRegistrationTestScreen extends StandardView {

    @ComponentId
    public TestThirdJmixButton button;
}
