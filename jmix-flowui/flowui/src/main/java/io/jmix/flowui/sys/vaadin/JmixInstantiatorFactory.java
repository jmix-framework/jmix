package io.jmix.flowui.sys.vaadin;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.InstantiatorFactory;
import com.vaadin.flow.server.VaadinService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("flowui_JmixInstantiatorFactory")
public class JmixInstantiatorFactory implements InstantiatorFactory {

    protected ApplicationContext applicationContext;

    public JmixInstantiatorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Instantiator createInstantitor(VaadinService service) {
        return new JmixSpringInstantiator(service, applicationContext);
    }
}
