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

package io.jmix.cuba

import com.haulmont.cuba.JmixCubaConfiguration
import com.haulmont.cuba.core.global.Messages
import com.sample.app.foo.Bar
import io.jmix.core.JmixCoreConfiguration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixCubaConfiguration])
class MessagesTest extends Specification {

    @Inject
    Messages messages

    static final LOC_EN = Locale.ENGLISH
    static final Locale LOC_RU = Locale.forLanguageTag('ru')

    def "test"() {
        expect:

        messages.getMessage(Bar, 'key1', LOC_EN) == 'value1'
        messages.getMessage(Bar, 'key2', LOC_EN) == 'value2'

        messages.getMessage(Bar, 'key1', LOC_RU) == 'значение1'
        messages.getMessage(Bar, 'key2', LOC_RU) == 'значение2'
    }
}
