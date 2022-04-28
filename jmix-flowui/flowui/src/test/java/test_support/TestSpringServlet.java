package test_support;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.spring.SpringServlet;
import org.springframework.context.ApplicationContext;

public class TestSpringServlet extends SpringServlet {

    protected ApplicationContext applicationContext;

    public TestSpringServlet(ApplicationContext context, boolean rootMapping) {
        super(context, rootMapping);

        applicationContext = context;
    }

    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        TestSpringVaadinServletService service =
                new TestSpringVaadinServletService(this, deploymentConfiguration, applicationContext);
        service.init();
        return service;
    }
}
