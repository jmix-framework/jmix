/*
 * Copyright 2021 Haulmont.
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

package specific_policy_info_registry

import io.jmix.core.CoreConfiguration
import io.jmix.core.security.SpecificPolicyInfoRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration
import test_support.base.accesscontext.TestSpecificOperationAccessContext

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class SpecificPolicyInfoRegistryTest extends Specification {

    @Autowired
    SpecificPolicyInfoRegistry specificPolicyInfoRegistry

    def "specific policy info from TestSpecificOperationAccessContext is returned"() {

        when:
        def specificPolicyDefinitions = specificPolicyInfoRegistry.getSpecificPolicyInfos()

        then:

        specificPolicyDefinitions.size() > 0
        specificPolicyDefinitions.find {it.name == TestSpecificOperationAccessContext.NAME} != null
    }
}
