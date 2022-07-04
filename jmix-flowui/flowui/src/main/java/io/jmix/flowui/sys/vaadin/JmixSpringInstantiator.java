package io.jmix.flowui.sys.vaadin;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.SpringInstantiator;
import io.jmix.flowui.view.View;
import io.jmix.flowui.sys.ViewSupport;
import org.springframework.context.ApplicationContext;

public class JmixSpringInstantiator extends SpringInstantiator {

    protected ApplicationContext applicationContext;

    /**
     * Creates a new spring instantiator instance.
     *
     * @param service the service to use
     * @param context the application context
     */
    public JmixSpringInstantiator(VaadinService service, ApplicationContext context) {
        super(service, context);
        this.applicationContext = context;
    }

    @Override
    public <T> T getOrCreate(Class<T> type) {
        final T instance = super.getOrCreate(type);
        init(type, instance);

        return instance;
    }

    protected <T> void init(Class<T> type, T instance) {
        if (View.class.isAssignableFrom(type)) {
            getViewSupport().initView(((View) instance));
        }
    }

    private ViewSupport getViewSupport() {
        return applicationContext.getBean(ViewSupport.class);
    }
}
