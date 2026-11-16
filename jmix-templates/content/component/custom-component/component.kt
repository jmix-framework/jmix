package ${packageName}

import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasEnabled
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.Tag<% if (generateFrontend) { %>
import com.vaadin.flow.component.dependency.JsModule<% } %>
import io.jmix.core.Messages
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

@Tag("${tag}")<% if (generateFrontend) { %>
@JsModule("${jsModulePath}")<% } else { %>
// TODO add @JsModule/@NpmPackage pointing to your frontend module<% } %>
class ${componentClassName} : Component(), ClickNotifier<${componentClassName}>, HasEnabled, HasSize,
    ApplicationContextAware, InitializingBean {

    private lateinit var applicationContext: ApplicationContext

    private lateinit var messages: Messages

    // Spring calls setApplicationContext() and afterPropertiesSet() only for a component
    // created by UiComponents (from XML or with uiComponents.create()), not with a constructor call.
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun afterPropertiesSet() {
        autowireDependencies()
        initComponent()
    }

    private fun autowireDependencies() {
        // TODO get the Spring beans your component needs
        messages = applicationContext.getBean(Messages::class.java)
    }

    private fun initComponent() {
        // TODO initialize the component, the beans are not available in the constructor yet
    }
}
