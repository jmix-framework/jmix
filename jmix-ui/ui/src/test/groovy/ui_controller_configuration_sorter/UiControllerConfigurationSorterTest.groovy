package ui_controller_configuration_sorter

import io.jmix.core.CoreConfiguration
import io.jmix.core.JmixModuleDescriptor
import io.jmix.core.JmixModules
import io.jmix.core.impl.JmixModulesSorter
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.sys.UiControllersConfiguration
import io.jmix.ui.sys.UiControllersConfigurationSorter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.TestContextInititalizer
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration],
        initializers = [TestContextInititalizer])
class UiControllerConfigurationSorterTest extends Specification {

    @Autowired
    Environment environment

    @Autowired
    UiControllersConfigurationSorter uiControllersConfigurationSorter

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    AnnotationScanMetadataReaderFactory annotationScanMetadataReaderFactory

    def setup() {
        def appDescriptor = new JmixModuleDescriptor("app", "com.company.app")
        def appAddonDescriptor1 = new JmixModuleDescriptor("app-addon-1", "com.company.app.addon1")
        def appAddonDescriptor2 = new JmixModuleDescriptor("app-addon-2", "com.company.app.addon2")
        def appAddonDescriptor3 = new JmixModuleDescriptor("app-addon-3", "com.company.app.addon3")
        def jmixAddonDescriptor = new JmixModuleDescriptor("jmix-addon-1", "io.jmix.addon1")

        appAddonDescriptor1.addDependency(jmixAddonDescriptor)
        appDescriptor.addDependency(appAddonDescriptor1)
        appDescriptor.addDependency(appAddonDescriptor2)
        appDescriptor.addDependency(appAddonDescriptor3)
        appDescriptor.addDependency(jmixAddonDescriptor)

        def sortedModules = JmixModulesSorter.sort([
                appDescriptor, jmixAddonDescriptor,
                appAddonDescriptor1, appAddonDescriptor2, appAddonDescriptor3
        ])

        def jmixModules = new JmixModules(sortedModules, environment)
        uiControllersConfigurationSorter = new UiControllersConfigurationSorter(jmixModules)
    }

    def "test UiControllerConfigurations sorting"() {
        def appUiConfiguration = new UiControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appUiConfiguration.basePackages = ["com.company.app.screen"]

        def appAddonUiConfiguration1 = new UiControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonUiConfiguration1.basePackages = ["com.company.app.addon1.screen"]

        def appAddonUiConfiguration2 = new UiControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonUiConfiguration2.basePackages = ["com.company.app.addon2.screen"]

        def appAddonUiConfiguration3 = new UiControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonUiConfiguration3.basePackages = ["com.company.app.addon3.screen"]

        def jmixAddonUiConfiguration = new UiControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        jmixAddonUiConfiguration.basePackages = ["io.jmix.addon1.screen"]

        def configurations = [
                appUiConfiguration, jmixAddonUiConfiguration, appAddonUiConfiguration3, appAddonUiConfiguration2, appAddonUiConfiguration1
        ]

        when:
        def sorted = uiControllersConfigurationSorter.sort(configurations)

        then:
        sorted.indexOf(jmixAddonUiConfiguration) < sorted.indexOf(appAddonUiConfiguration1)
        sorted.indexOf(appAddonUiConfiguration1) < sorted.indexOf(appUiConfiguration)
        sorted.indexOf(appAddonUiConfiguration2) < sorted.indexOf(appUiConfiguration)
        sorted.indexOf(appAddonUiConfiguration3) < sorted.indexOf(appUiConfiguration)
    }
}
