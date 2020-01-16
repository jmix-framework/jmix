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
import com.sample.app.entity.PetType
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MessagesTest extends Specification {

    @Inject
    Messages messages

    static final LOC_EN = Locale.ENGLISH
    static final Locale LOC_RU = Locale.forLanguageTag('ru')

    def "get message"() {
        expect:

        messages.getMessage('com.sample.app.entity/Pet.name', LOC_EN) == 'Name'
        messages.getMessage('com.sample.app.entity/Pet.name', LOC_RU) == 'Имя'

        messages.getMessage('com.sample.app.entity', 'Pet.name', LOC_EN) == 'Name'
        messages.getMessage('com.sample.app.entity', 'Pet.name', LOC_RU) == 'Имя'

        messages.getMessage(Pet, 'Pet.name', LOC_EN) == 'Name'
        messages.getMessage(Pet, 'Pet.name', LOC_RU) == 'Имя'

        messages.getMessage(PetType.BIRD, LOC_EN) == 'Bird'
        messages.getMessage(PetType.BIRD, LOC_RU) == 'Птица'
    }

    def "get nonexistent message"() {
        expect:

        messages.getMessage('nonexistent', LOC_EN) == 'nonexistent'
        messages.getMessage('nonexistent', LOC_RU) == 'nonexistent'

        messages.getMessage('com.sample.app.entity', 'nonexistent', LOC_EN) == 'nonexistent'
        messages.getMessage('com.sample.app.entity', 'nonexistent', LOC_RU) == 'nonexistent'

        messages.getMessage(Pet, 'nonexistent', LOC_EN) == 'nonexistent'
        messages.getMessage(Pet, 'nonexistent', LOC_RU) == 'nonexistent'

        messages.getMessage(PetType.FISH, LOC_EN) == 'PetType.FISH'
        messages.getMessage(PetType.FISH, LOC_RU) == 'PetType.FISH'
    }

    def "find message"() {
        expect:

        messages.findMessage('com.sample.app.entity', 'Pet.name', LOC_EN) == 'Name'
        messages.findMessage('com.sample.app.entity', 'nonexistent', LOC_EN) == null

        messages.findMessage('nonGroupedMessage', LOC_EN) == 'value'
        messages.findMessage('nonexistent', LOC_EN) == null
    }

    def "format message"() {
        expect:

        messages.formatMessage('formattedMessage', LOC_EN, 'abc') == 'value abc'
        messages.formatMessage('formattedMessage', LOC_RU, 'abc') == 'значение abc'
    }
}
