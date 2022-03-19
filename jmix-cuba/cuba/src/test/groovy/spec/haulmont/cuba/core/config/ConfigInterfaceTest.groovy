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

package spec.haulmont.cuba.core.config

import com.haulmont.cuba.core.global.Configuration
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired

class ConfigInterfaceTest extends CoreTestSpecification {

    @Autowired
    private Configuration configuration


    def "default method in configuration interface"() {

        def config = configuration.getConfig(TestConfig)

        when: "no value provided for 'foo' property"

        def foo = config.getFooOrDefault()

        then: "default method returns value of 'bar' property"

        foo == 'bar-value'

        when: "after setting own value"

        config.setFoo('foo-value')
        foo = config.getFooOrDefault()

        then: "the own value is returned"

        foo == 'foo-value'

        cleanup:

        config.setFoo(null)
    }
}
