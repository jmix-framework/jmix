/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package view_controller_configuration_sorter

import io.jmix.core.CoreConfiguration
import io.jmix.core.JmixModuleDescriptor
import io.jmix.core.JmixModules
import io.jmix.core.impl.JmixModulesSorter
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.flowui.FlowuiConfiguration
import io.jmix.flowui.sys.ViewControllersConfiguration
import io.jmix.flowui.sys.ViewControllersConfigurationSorter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.FlowuiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, FlowuiConfiguration, DataConfiguration,
        EclipselinkConfiguration, FlowuiTestConfiguration])
class ViewControllerConfigurationSorterTest extends Specification {

    @Autowired
    Environment environment

    @Autowired
    ViewControllersConfigurationSorter viewControllersConfigurationSorter

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
        viewControllersConfigurationSorter = new ViewControllersConfigurationSorter(jmixModules)
    }

    def "test ViewControllerConfigurations sorting"() {
        def appUiConfiguration = new ViewControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appUiConfiguration.basePackages = ["com.company.app.screen"]

        def appAddonUiConfiguration1 = new ViewControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonUiConfiguration1.basePackages = ["com.company.app.addon1.screen"]

        def appAddonUiConfiguration2 = new ViewControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonUiConfiguration2.basePackages = ["com.company.app.addon2.screen"]

        def appAddonUiConfiguration3 = new ViewControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonUiConfiguration3.basePackages = ["com.company.app.addon3.screen"]

        def jmixAddonUiConfiguration = new ViewControllersConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        jmixAddonUiConfiguration.basePackages = ["io.jmix.addon1.screen"]

        def configurations = [
                appUiConfiguration, jmixAddonUiConfiguration, appAddonUiConfiguration3, appAddonUiConfiguration2, appAddonUiConfiguration1
        ]

        when:
        def sorted = viewControllersConfigurationSorter.sort(configurations)

        then:
        sorted.indexOf(jmixAddonUiConfiguration) < sorted.indexOf(appAddonUiConfiguration1)
        sorted.indexOf(appAddonUiConfiguration1) < sorted.indexOf(appUiConfiguration)
        sorted.indexOf(appAddonUiConfiguration2) < sorted.indexOf(appUiConfiguration)
        sorted.indexOf(appAddonUiConfiguration3) < sorted.indexOf(appUiConfiguration)
    }
}
