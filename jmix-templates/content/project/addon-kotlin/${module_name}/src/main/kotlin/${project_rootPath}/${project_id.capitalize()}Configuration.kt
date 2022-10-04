package ${project_rootPackage}

import io.jmix.core.annotation.JmixModule
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.sys.UiControllersConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = [EclipselinkConfiguration::class, UiConfiguration::class])
@PropertySource(name = "${project_rootPackage}", value = ["classpath:/${project_rootPath}/module.properties"])
open class ${project_id.capitalize()}Configuration {

    @Bean("${project_id}_${project_id.capitalize()}UiControllers")
    open fun screens(applicationContext: ApplicationContext,
                metadataReaderFactory: AnnotationScanMetadataReaderFactory
    ): UiControllersConfiguration {
        return UiControllersConfiguration(applicationContext, metadataReaderFactory).apply {
            basePackages = listOf("${project_rootPackage}")
        }
    }
}