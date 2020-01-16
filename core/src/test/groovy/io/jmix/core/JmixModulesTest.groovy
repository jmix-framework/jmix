package io.jmix.core

import com.sample.addon1.TestAddon1Configuration
import com.sample.app.TestAppConfiguration
import com.sample.app.TestBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [TestAppConfiguration, TestAddon1Configuration, JmixCoreConfiguration])
class JmixModulesTest extends Specification {

    @Autowired
    private JmixModules components

    @Autowired
    private TestBean testBean

    @Autowired
    private Environment environment

    def "test dependencies"() {
        expect:

        components != null
        components.components.size() == 3
        components.components[0].id == 'io.jmix.core'

        def jmixCore = components.get('io.jmix.core')
        def addon1 = components.get('com.sample.addon1')
        def app = components.get('com.sample.app')

        addon1.dependsOn(jmixCore)
        app.dependsOn(addon1)
        app.dependsOn(jmixCore)
    }

    def "configuration properties of components"() {
        expect:

        def jmixCore = components.get('io.jmix.core')
        def addon1 = components.get('com.sample.addon1')
        def app = components.get('com.sample.app')

        jmixCore.getProperty('jmix.viewsConfig') == 'io/jmix/core/views.xml'
        addon1.getProperty('jmix.viewsConfig') == 'com/sample/addon1/views.xml'
        app.getProperty('jmix.viewsConfig') == 'com/sample/app/views.xml'

    }

    def "resulting configuration properties"() {
        expect:

        components.getProperty('jmix.viewsConfig') == 'io/jmix/core/views.xml com/sample/addon1/views.xml com/sample/app/views.xml'
        components.getProperty('prop1') == 'addon1_prop1 app_prop1'
        components.getProperty('prop2') == 'app_prop2'
        components.getProperty('prop3') == 'app_prop3'
    }

    def "using configuration properties"() {
        expect:

        testBean.prop1 == 'addon1_prop1 app_prop1'
    }

    def "app property file overrides JmixProperty"() {
        expect:

        environment.getProperty('prop_to_override') == 'app_properties_file_prop3'
    }
}
