/*
 * Copyright 2019 Haulmont.
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

package messages

import spock.lang.Unroll
import test_support.addon1.TestAddon1Configuration

import test_support.app.TestAppConfiguration
import io.jmix.core.CoreConfiguration
import org.springframework.context.MessageSource
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import test_support.base.TestBaseConfiguration

import static test_support.TestLocales.*

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MessageSourceTest extends Specification {


    @Autowired
    MessageSource messageSource

    @Autowired
    Environment environment

    @Unroll
    def "messages in module base package"() {
        expect:

        messageSource.getMessage(code, null, locale) == expectedMessage

        where:

        code                                               | locale || expectedMessage
        'test_support.addon1.entity/TestAddon1Entity.name' | LOC_EN || 'Name'
        'test_support.addon1.entity/TestAddon1Entity.name' | LOC_RU || 'Наименование'

        'test_support.app.entity/Pet.name'                 | LOC_EN || 'Name'
        'test_support.app.entity/Pet.name'                 | LOC_RU || 'Имя'
    }

    def "message override"() {
        expect:

        messageSource.getMessage('test_support.addon1.entity/messageToOverride', null, LOC_EN) == 'app value'
        messageSource.getMessage('test_support.addon1.entity/messageToOverride', null, LOC_RU) == 'значение приложения'
    }

    def "additional message file in application"() {
        expect:

        messageSource.getMessage('test-key1', null, LOC_EN) == 'test message 1'
    }

    def "messages override in conf"() {
        setup:

        File file = overrideConfigurationWith(
            '/test_support/app/messages.properties',
            'messageToOverrideByConf', 'conf value'
        )

        expect:

        messageSource.getMessage('messageToOverrideByConf', null, LOC_EN) == 'conf value'

        cleanup:

        file.delete()
        clearMessageSourceCache()
    }

    protected clearMessageSourceCache() {
        ((ReloadableResourceBundleMessageSource) messageSource).clearCache()
    }

    protected File overrideConfigurationWith(String configurationFile, String overrideAttribute, String overrideValue) {
        def properties = new Properties()
        properties.load(new InputStreamReader(getClass().getResourceAsStream(configurationFile), 'UTF-8'))

        properties.setProperty(overrideAttribute , overrideValue)
        def dir = new File(environment.getProperty('jmix.core.conf-dir'), 'test_support/app')
        dir.mkdirs()
        def file = new File(dir, 'messages.properties')
        def stream = new FileOutputStream(file)
        try {
            properties.store(stream, '')
        } finally {
            stream.close()
        }
        clearMessageSourceCache()
        file
    }
}

