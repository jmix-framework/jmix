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
import test_support.app.entity.PetType
import io.jmix.core.CoreConfiguration
import io.jmix.core.Messages
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import test_support.base.TestBaseConfiguration

import static test_support.TestLocales.*

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MessagesTest extends Specification {

    @Autowired
    Messages messages

    def "get message"() {
        expect:

        messages.getMessage('test_support.app.entity/Pet.name', LOC_EN) == 'Name'
        messages.getMessage('test_support.app.entity/Pet.name', LOC_RU) == 'Имя'

        messages.getMessage('test_support.app.entity', 'Pet.name', LOC_EN) == 'Name'
        messages.getMessage('test_support.app.entity', 'Pet.name', LOC_RU) == 'Имя'

        messages.getMessage(Pet, 'Pet.name', LOC_EN) == 'Name'
        messages.getMessage(Pet, 'Pet.name', LOC_RU) == 'Имя'

        messages.getMessage(PetType.BIRD, LOC_EN) == 'Bird'
        messages.getMessage(PetType.BIRD, LOC_RU) == 'Птица'
    }


    def "get nonexistent message"() {
        expect:

        messages.getMessage('nonexistent', LOC_EN) == 'nonexistent'
        messages.getMessage('nonexistent', LOC_RU) == 'nonexistent'

        messages.getMessage('test_support.app.entity', 'nonexistent', LOC_EN) == 'nonexistent'
        messages.getMessage('test_support.app.entity', 'nonexistent', LOC_RU) == 'nonexistent'

        messages.getMessage(Pet, 'nonexistent', LOC_EN) == 'nonexistent'
        messages.getMessage(Pet, 'nonexistent', LOC_RU) == 'nonexistent'

        messages.getMessage(PetType.FISH, LOC_EN) == 'PetType.FISH'
        messages.getMessage(PetType.FISH, LOC_RU) == 'PetType.FISH'
    }

    def "find message"() {
        expect:

        messages.findMessage('test_support.app.entity', 'Pet.name', LOC_EN) == 'Name'
        messages.findMessage('test_support.app.entity', 'nonexistent', LOC_EN) == null

        messages.findMessage('nonGroupedMessage', LOC_EN) == 'value'
        messages.findMessage('nonexistent', LOC_EN) == null
    }

    def "format message"() {
        expect:

        messages.formatMessage('', 'formattedMessage', LOC_EN, 'abc') == 'value abc'
        messages.formatMessage('', 'formattedMessage', LOC_RU, 'abc') == 'значение abc'
    }

    def "language tag with country"() {
        expect:

        messages.getMessage('localeDisplayName.fr_CA', Locale.forLanguageTag('fr-CA')) == 'Français (Canada)'
        messages.getMessage('messageInFr', Locale.forLanguageTag('fr-CA')) == 'message in fr'
        messages.getMessage('messageInFrCA', Locale.forLanguageTag('fr-CA')) == 'message in fr CA'
    }
}
