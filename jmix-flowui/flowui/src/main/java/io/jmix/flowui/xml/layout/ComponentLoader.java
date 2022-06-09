package io.jmix.flowui.xml.layout;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.model.ScreenData;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenActions;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Optional;

public interface ComponentLoader<T extends Component> {

    interface Context {
        String getMessageGroup();
    }

    interface ComponentContext extends Context {

        ScreenData getScreenData();

        ScreenActions getScreenActions();

        Optional<ComponentContext> getParent();

        String getFullFrameId();

        String getCurrentFrameId();

        Screen<?> getScreen();

        void addInitTask(InitTask task);

        void executeInitTasks();
    }

    /**
     * InitTasks are used to perform deferred initialization of visual components.
     */
    interface InitTask {
        /**
         * This method will be invoked after screen initialization.
         *
         * @param context loader context
         * @param screen  screen
         */
        void execute(ComponentContext context, Screen<?> screen);
    }

    Context getContext();

    void setContext(Context context);

    UiComponents getFactory();

    void setFactory(UiComponents factory);

    LoaderResolver getLoaderResolver();

    void setLoaderResolver(LoaderResolver loaderResolver);

    LoaderSupport getLoaderSupport();

    void setLoaderSupport(LoaderSupport loaderSupport);

    Element getElement(Element element);

    void setElement(Element element);

    void setApplicationContext(ApplicationContext applicationContext);

    void setEnvironment(Environment environment);

    /**
     * Creates result component by XML-element
     */
    void initComponent();

    /**
     * Loads component properties by XML definition.
     *
     * @see #getElement(Element)
     */
    void loadComponent();

    /**
     * Returns previously created instance of component.
     *
     * @see #initComponent()
     */
    T getResultComponent();
}
