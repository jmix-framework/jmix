package io.jmix.core.compatibility

import com.sample.addon1.TestAddon1Configuration
import com.sample.app.TestAppConfiguration
import io.jmix.core.JmixCoreConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [TestAppConfiguration, TestAddon1Configuration, JmixCoreConfiguration])
class AppPropertiesTest extends Specification {

    @Autowired
    AppProperties appProperties

    def "properties can be changed at runtime"() {
        when:

        appProperties.setProperty('prop3', 'changed_prop3')
        appProperties.setProperty('prop4', 'changed_prop4')

        then:

        appProperties.getProperty('prop3') == 'changed_prop3'
        appProperties.getProperty('prop4') == 'changed_prop4'

        when:

        appProperties.setProperty('prop3', null)
        appProperties.setProperty('prop4', null)

        then:

        appProperties.getProperty('prop3') == 'app_prop3'
        appProperties.getProperty('prop4') == null
    }
}
