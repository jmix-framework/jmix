package ${project_rootPackage}.test_support;

import ${project_rootPackage}.${project_classPrefix}Application;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@UiTest
@SpringBootTest(classes = {${project_classPrefix}Application.class, FlowuiTestAssistConfiguration.class})
public abstract class UiIntegrationTest {

    @Autowired
    ViewNavigators viewNavigators;

    protected ViewNavigators getViewNavigators() {
        return viewNavigators;
    }

    /**
     * Returns instance of currently navigated view.
     */
    protected <T extends View<?>> T getCurrentView() {
        return UiTestUtils.getCurrentView();
    }

    /**
     * Returns a component defined in the screen by the component id.
     * Throws an exception if not found.
     */
    @SuppressWarnings("unchecked")
    protected <T> T findComponent(View<?> view, String componentId) {
        Optional<Component> component = UiComponentUtils.findComponent(view, componentId);
        Assertions.assertTrue(component.isPresent());
        return (T) component.get();
    }
}
