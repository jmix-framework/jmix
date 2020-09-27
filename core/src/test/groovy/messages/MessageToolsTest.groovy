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


import test_support.addon1.TestAddon1Configuration

import test_support.app.TestAppConfiguration
import test_support.app.entity.Pet
import io.jmix.core.CoreConfiguration
import io.jmix.core.MessageTools
import io.jmix.core.Metadata
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import test_support.base.TestBaseConfiguration

import static test_support.TestLocales.*

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MessageToolsTest extends Specification {

    @Autowired
    MessageTools messageTools

    @Autowired
    Metadata metadata

    def "test loadString"() {
        expect:

        messageTools.loadString('msg://test_support.app.entity/Pet.name', LOC_EN) == 'Name'
        messageTools.loadString('msg://test_support.app.entity/Pet.name', LOC_RU) == 'Имя'

        messageTools.loadString('test_support.app.entity','msg://Pet.name', LOC_EN) == 'Name'
        messageTools.loadString('test_support.app.entity','msg://Pet.name', LOC_RU) == 'Имя'

        messageTools.loadString('msg://menuCaption', LOC_EN) == 'Application'
        messageTools.loadString('msg://menuCaption', LOC_RU) == 'Приложение'
    }

    def "test getEntityCaption"() {
        expect:

        messageTools.getEntityCaption(metadata.getClass(Pet), LOC_EN) == 'Domestic Animal'
        messageTools.getEntityCaption(metadata.getClass(Pet), LOC_RU) == 'Домашнее животное'
    }

    def "test getPropertyCaption"() {
        expect:

        messageTools.getPropertyCaption(metadata.getClass(Pet), 'nick', LOC_EN) == 'Nickname'
        messageTools.getPropertyCaption(metadata.getClass(Pet), 'nick', LOC_RU) == 'Кличка'

        messageTools.getPropertyCaption(metadata.getClass(Pet).getProperty('nick'), LOC_EN) == 'Nickname'
        messageTools.getPropertyCaption(metadata.getClass(Pet).getProperty('nick'), LOC_RU) == 'Кличка'
    }
}
