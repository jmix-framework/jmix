/*
 * Copyright 2026 Haulmont.
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

package actions_configuration_sorter

import io.jmix.core.CoreConfiguration
import io.jmix.core.JmixModuleDescriptor
import io.jmix.core.JmixModules
import io.jmix.core.impl.JmixModulesSorter
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.flowui.FlowuiConfiguration
import io.jmix.flowui.sys.ActionsConfiguration
import io.jmix.flowui.sys.ActionsConfigurationSorter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.FlowuiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, FlowuiConfiguration, DataConfiguration,
        EclipselinkConfiguration, FlowuiTestConfiguration])
class ActionsConfigurationSorterTest extends Specification {

    @Autowired
    Environment environment

    @Autowired
    ActionsConfigurationSorter actionsConfigurationSorter

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
        actionsConfigurationSorter = new ActionsConfigurationSorter(jmixModules)
    }

    def "test ActionsConfigurations sorting"() {
        def appActionsConfiguration = new ActionsConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appActionsConfiguration.basePackages = ["com.company.app.action"]

        def appAddonActionsConfiguration1 = new ActionsConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonActionsConfiguration1.basePackages = ["com.company.app.addon1.action"]

        def appAddonActionsConfiguration2 = new ActionsConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonActionsConfiguration2.basePackages = ["com.company.app.addon2.action"]

        def appAddonActionsConfiguration3 = new ActionsConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appAddonActionsConfiguration3.basePackages = ["com.company.app.addon3.action"]

        def jmixAddonActionsConfiguration = new ActionsConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        jmixAddonActionsConfiguration.basePackages = ["io.jmix.addon1.action"]

        def configurations = [
                appActionsConfiguration, jmixAddonActionsConfiguration, appAddonActionsConfiguration3,
                appAddonActionsConfiguration2, appAddonActionsConfiguration1
        ]

        when:
        def sorted = actionsConfigurationSorter.sort(configurations)

        then:
        sorted.indexOf(jmixAddonActionsConfiguration) < sorted.indexOf(appAddonActionsConfiguration1)
        sorted.indexOf(appAddonActionsConfiguration1) < sorted.indexOf(appActionsConfiguration)
        sorted.indexOf(appAddonActionsConfiguration2) < sorted.indexOf(appActionsConfiguration)
        sorted.indexOf(appAddonActionsConfiguration3) < sorted.indexOf(appActionsConfiguration)
    }

    def "test ActionsConfigurations sorting with unknown base package"() {
        def appActionsConfiguration = new ActionsConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        appActionsConfiguration.basePackages = ["com.company.app.action"]

        def jmixAddonActionsConfiguration = new ActionsConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        jmixAddonActionsConfiguration.basePackages = ["io.jmix.addon1.action"]

        def unknownActionsConfiguration = new ActionsConfiguration(applicationContext, annotationScanMetadataReaderFactory)
        unknownActionsConfiguration.basePackages = ["com.unknown.action"]

        def configurations = [unknownActionsConfiguration, appActionsConfiguration, jmixAddonActionsConfiguration]

        when:
        def sorted = actionsConfigurationSorter.sort(configurations)

        then:
        sorted.indexOf(jmixAddonActionsConfiguration) < sorted.indexOf(appActionsConfiguration)
        sorted.indexOf(appActionsConfiguration) < sorted.indexOf(unknownActionsConfiguration)
        sorted.indexOf(jmixAddonActionsConfiguration) < sorted.indexOf(unknownActionsConfiguration)
    }
}
