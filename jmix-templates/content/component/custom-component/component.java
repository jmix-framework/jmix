package ${packageName};

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;<% if (generateFrontend) { %>
import com.vaadin.flow.component.dependency.JsModule;<% } %>
import io.jmix.core.Messages;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Tag("${tag}")<% if (generateFrontend) { %>
@JsModule("${jsModulePath}")<% } else { %>
// TODO add @JsModule/@NpmPackage pointing to your frontend module<% } %>
public class ${componentClassName} extends Component implements ClickNotifier<${componentClassName}>, HasEnabled, HasSize,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected Messages messages;

    // Spring calls setApplicationContext() and afterPropertiesSet() only for a component
    // created by UiComponents (from XML or with uiComponents.create()), not with the new operator.
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        // TODO get the Spring beans your component needs
        messages = applicationContext.getBean(Messages.class);
    }

    protected void initComponent() {
        // TODO initialize the component, the beans are not available in the constructor yet
    }
}
