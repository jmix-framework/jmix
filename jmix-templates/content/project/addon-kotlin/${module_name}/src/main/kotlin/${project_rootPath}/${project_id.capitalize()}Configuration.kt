package ${project_rootPackage}

import io.jmix.core.annotation.JmixModule
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.flowui.FlowuiConfiguration
import io.jmix.flowui.sys.ActionsConfiguration
import io.jmix.flowui.sys.ViewControllersConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = [EclipselinkConfiguration::class, FlowuiConfiguration::class])
@PropertySource(name = "${project_rootPackage}", value = ["classpath:/${project_rootPath}/module.properties"])
open class ${project_id.capitalize()}Configuration {

    @Bean("${project_id}_${project_id.capitalize()}ViewControllers")
    open fun screens(applicationContext: ApplicationContext,
                     metadataReaderFactory: AnnotationScanMetadataReaderFactory
    ): ViewControllersConfiguration {
        return ViewControllersConfiguration(applicationContext, metadataReaderFactory).apply {
            basePackages = listOf("${project_rootPackage}")
        }
    }

    @Bean("${project_id}_${project_id.capitalize()}Actions")
    open fun actions(applicationContext: ApplicationContext,
                     metadataReaderFactory: AnnotationScanMetadataReaderFactory
    ): ActionsConfiguration {
        return ActionsConfiguration(applicationContext, metadataReaderFactory).apply {
            basePackages = listOf("${project_rootPackage}")
        }
    }
}
