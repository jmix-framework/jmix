package io.jmix.flowui.sys.vaadin;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.SpringInstantiator;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.sys.ScreenSupport;
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
        if (Screen.class.isAssignableFrom(type)) {
            getScreenSupport().initScreen(((Screen) instance));
        }
    }

    private ScreenSupport getScreenSupport() {
        return applicationContext.getBean(ScreenSupport.class);
    }
}
