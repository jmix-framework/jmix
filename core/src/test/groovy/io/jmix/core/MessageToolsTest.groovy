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
import com.sample.app.entity.Pet
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MessageToolsTest extends Specification {

    @Inject
    MessageTools messageTools

    @Inject
    Metadata metadata

    static final LOC_EN = Locale.ENGLISH
    static final Locale LOC_RU = Locale.forLanguageTag('ru')

    def "test loadString"() {
        expect:

        messageTools.loadString('msg://com.sample.app.entity/Pet.name', LOC_EN) == 'Name'
        messageTools.loadString('msg://com.sample.app.entity/Pet.name', LOC_RU) == 'Имя'

        messageTools.loadString('com.sample.app.entity','msg://Pet.name', LOC_EN) == 'Name'
        messageTools.loadString('com.sample.app.entity','msg://Pet.name', LOC_RU) == 'Имя'
    }

    def "test getEntityCaption"() {
        expect:

        messageTools.getEntityCaption(metadata.getClassNN(Pet), LOC_EN) == 'Domestic Animal'
        messageTools.getEntityCaption(metadata.getClassNN(Pet), LOC_RU) == 'Домашнее животное'
    }

    def "test getPropertyCaption"() {
        expect:

        messageTools.getPropertyCaption(metadata.getClassNN(Pet), 'nick', LOC_EN) == 'Nickname'
        messageTools.getPropertyCaption(metadata.getClassNN(Pet), 'nick', LOC_RU) == 'Кличка'

        messageTools.getPropertyCaption(metadata.getClassNN(Pet).getPropertyNN('nick'), LOC_EN) == 'Nickname'
        messageTools.getPropertyCaption(metadata.getClassNN(Pet).getPropertyNN('nick'), LOC_RU) == 'Кличка'
    }
}
