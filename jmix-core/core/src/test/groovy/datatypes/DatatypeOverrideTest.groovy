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

package datatypes

import test_support.addon1.TestAddon1Configuration
import test_support.addon1.TestStringDatatype

import io.jmix.core.CoreConfiguration
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.datatype.impl.StringDatatype
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestAddon1Configuration])
class DatatypeOverrideTest extends Specification {

    @Autowired
    DatatypeRegistry registry

    def "TestStringDatatype is default for String java class"() {

        expect:

        registry.get('string').class == StringDatatype

        registry.get('string_mod').class == TestStringDatatype
        registry.find(String).class == TestStringDatatype
    }
}
