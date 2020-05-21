/*
 * Copyright 2020 Haulmont.
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

package metadata


import com.haulmont.cuba.core.model.City
import com.haulmont.cuba.core.model.Foo
import com.haulmont.cuba.core.model.Owner
import com.haulmont.cuba.core.testsupport.CoreTestConfiguration
import io.jmix.core.InstanceNameProvider
import io.jmix.core.Metadata

/*
 * Copyright 2020 Haulmont.
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

import io.jmix.core.security.impl.AuthenticatorImpl
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Collectors

@ContextConfiguration(classes = [CoreTestConfiguration])
class InstanceNameTest extends Specification {

    @Autowired
    Metadata metadata

    @Autowired
    InstanceNameProvider instanceNameProvider

    @Autowired
    AuthenticatorImpl authenticator

    def "test name pattern annotation"() {

        def city = metadata.create(City)
        city.name = "Samara"

        expect:

        instanceNameProvider.getInstanceName(city) == "Samara"
        instanceNameProvider.getInstanceNameRelatedProperties(metadata.getClass(City),true).stream()
                .map{p->p.getName()}
                .collect(Collectors.toSet()) == ["name"] as Set
    }

    def "test instance name property"() {

        def foo = metadata.create(Foo)
        foo.name = "Foo"

        expect:

        instanceNameProvider.getInstanceName(foo) == "Foo"
    }

    def "test name pattern method"() {

        def owner = metadata.create(Owner)
        owner.name = "John"

        expect:

        instanceNameProvider.getInstanceName(owner) == "John"
        instanceNameProvider.getInstanceNameRelatedProperties(metadata.getClass(Owner),true).stream()
                .map{p->p.getName()}
                .collect(Collectors.toSet()) == ["name"] as Set
    }
}
