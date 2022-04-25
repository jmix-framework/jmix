package test_support;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.spring.SpringVaadinServletService;
import org.springframework.context.ApplicationContext;

public class TestSpringVaadinServletService extends SpringVaadinServletService {

    public TestSpringVaadinServletService(VaadinServlet servlet,
                                          DeploymentConfiguration deploymentConfiguration,
                                          ApplicationContext context) {
        super(servlet, deploymentConfiguration, context);
    }

    @Override
    protected boolean isAtmosphereAvailable() {
        return false;
    }
}
