package component_registration.screen;

import com.vaadin.flow.router.Route;
import component_registration.TestThirdJmixButton;
import io.jmix.flowui.screen.ComponentId;
import io.jmix.flowui.screen.StandardScreen;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;

@Route("component-registrations-test-screen")
@UiController
@UiDescriptor("component-registrations-test-screen.xml")
public class ComponentRegistrationTestScreen extends StandardScreen {

    @ComponentId
    public TestThirdJmixButton button;
}
