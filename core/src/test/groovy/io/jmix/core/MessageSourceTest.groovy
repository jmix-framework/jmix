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

package io.jmix.core

import com.sample.addon1.TestAddon1Configuration
import com.sample.app.TestAppConfiguration
import io.jmix.core.JmixCoreConfiguration
import org.springframework.context.MessageSource
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MessageSourceTest extends Specification {

    static final LOC_EN = Locale.ENGLISH
    static final Locale LOC_RU = Locale.forLanguageTag('ru')

    @Inject
    MessageSource messageSource

    @Inject
    Environment environment

    def "messages in module base package"() {
        expect:

        messageSource.getMessage('io.jmix.core.entity/StandardEntity.version', null, LOC_EN) == 'Version'
        messageSource.getMessage('io.jmix.core.entity/StandardEntity.version', null, LOC_RU) == 'Версия'

        messageSource.getMessage('com.sample.addon1.entity/TestAddon1Entity.name', null, LOC_EN) == 'Name'
        messageSource.getMessage('com.sample.addon1.entity/TestAddon1Entity.name', null, LOC_RU) == 'Наименование'

        messageSource.getMessage('com.sample.app.entity/Pet.name', null, LOC_EN) == 'Name'
        messageSource.getMessage('com.sample.app.entity/Pet.name', null, LOC_RU) == 'Имя'
    }

    def "message override"() {
        expect:

        messageSource.getMessage('com.sample.addon1.entity/messageToOverride', null, LOC_EN) == 'app value'
        messageSource.getMessage('com.sample.addon1.entity/messageToOverride', null, LOC_RU) == 'значение приложения'
    }

    def "additional message file in application"() {
        expect:

        messageSource.getMessage('test-key1', null, LOC_EN) == 'test message 1'
    }

    def "messages override in conf"() {
        setup:

        def properties = new Properties()
        properties.load(new InputStreamReader(getClass().getResourceAsStream('/com/sample/app/messages.properties'), 'UTF-8'))
        properties.setProperty('messageToOverrideByConf', 'conf value')
        def dir = new File(environment.getProperty('jmix.confDir'), 'com/sample/app')
        dir.mkdirs()
        def file = new File(dir, 'messages.properties')
        def stream = new FileOutputStream(file)
        try {
            properties.store(stream, '')
        } finally {
            stream.close()
        }
        ((ReloadableResourceBundleMessageSource) messageSource).clearCache()

        expect:

        messageSource.getMessage('messageToOverrideByConf', null, LOC_EN) == 'conf value'

        cleanup:

        file.delete()
        ((ReloadableResourceBundleMessageSource) messageSource).clearCache()
    }
}

