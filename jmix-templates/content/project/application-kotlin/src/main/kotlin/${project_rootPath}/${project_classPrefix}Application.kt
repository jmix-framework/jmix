package ${project_rootPackage}

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import com.vaadin.flow.theme.aura.Aura
import io.jmix.flowui.theme.aura.JmixAura
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import javax.sql.DataSource

@Push
@StyleSheet(Aura.STYLESHEET)
@StyleSheet(JmixAura.STYLESHEET)
@StyleSheet("themes/${project_name}-aura/styles.css")
@PWA(name = "${project_projectPrintableName}", shortName = "${project_projectPrintableName}", offline = false)
@SpringBootApplication
open class ${project_classPrefix}Application : AppShellConfigurator {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(${project_classPrefix}Application::class.java, *args)
        }
    }

    @Autowired
    private val environment: Environment? = null

    @Bean
    @Primary
    @ConfigurationProperties("main.datasource")
    open fun dataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean
    @Primary
    @ConfigurationProperties("main.datasource.hikari")
    open fun dataSource(dataSourceProperties: DataSourceProperties): DataSource =
            dataSourceProperties.initializeDataSourceBuilder().build()

    @EventListener
    open fun printApplicationUrl(event: ApplicationStartedEvent?) {
        LoggerFactory.getLogger(${project_classPrefix}Application::class.java).info(
                "Application started at http://localhost:"
                        + (environment?.getProperty("local.server.port") ?: "")
                        + (environment?.getProperty("server.servlet.context-path") ?: ""))
    }
}
