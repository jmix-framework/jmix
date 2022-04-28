package test_support;

import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.EventListener;

/**
 * Mocked servlet context. Provides ability to add {@link ServletContextListener} and invoke them.
 *
 * @see com.vaadin.flow.spring.VaadinServletContextInitializer
 */
public class TestServletContext extends MockServletContext {

    protected EventListener eventListener;

    @Override
    public <T extends EventListener> void addListener(T listener) {
        eventListener = listener;
    }

    public void fireServletContextInitialized() {
        if (eventListener instanceof ServletContextListener) {
            ((ServletContextListener) eventListener).contextInitialized(new ServletContextEvent(this));
        }
    }
}
