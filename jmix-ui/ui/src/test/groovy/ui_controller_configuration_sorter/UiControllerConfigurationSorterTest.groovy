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
        def appAddonDescriptor = new JmixModuleDescriptor("app-addon-1", "com.company.app.addon")
        def jmixAddonDescriptor = new JmixModuleDescriptor("jmix-addon-1", "io.jmix.addon1")

        appAddonDescriptor.addDependency(jmixAddonDescriptor)
        appDescriptor.addDependency(appAddonDescriptor)
        appDescriptor.addDependency(jmixAddonDescriptor)

        def sortedModules = JmixModulesSorter.sort([appDescriptor, jmixAddonDescriptor, appAddonDescriptor])

        def jmixModules = new JmixModules(sortedModules, environment)
        uiControllersConfigurationSorter = new UiControllersConfigurationSorter(jmixModules)
    }

    def "test UiControllerConfigurations sorting"() {
        def appUiConfiguration = new UiControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appUiConfiguration.basePackages = ["com.company.app.screen"]

        def appAddonUiConfiguration = new UiControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonUiConfiguration.basePackages = ["com.company.app.addon.screen"]

        def jmixAddonUiConfiguration = new UiControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        jmixAddonUiConfiguration.basePackages = ["io.jmix.addon1.screen"]

        def configurations = [appUiConfiguration, appAddonUiConfiguration, jmixAddonUiConfiguration]

        when:
        def sorted = uiControllersConfigurationSorter.sort(configurations)

        then:
        sorted.indexOf(jmixAddonUiConfiguration) < sorted.indexOf(appAddonUiConfiguration)
        sorted.indexOf(appAddonUiConfiguration) < sorted.indexOf(appUiConfiguration)
    }
}
