/*
 * Copyright 2025 Haulmont.
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

package query_params

import io.jmix.core.DataManager
import io.jmix.core.LoadContext
import io.jmix.core.Metadata
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.sales.Customer

class ConditionWithoutParamValueTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    Metadata metadata

    def "test"() {
        when:
        def loadContext = new LoadContext(metadata.getClass(Customer)).setQuery(
                new LoadContext.Query('').setCondition(
                        PropertyCondition.createWithParameterName('name', PropertyCondition.Operation.EQUAL, 'name'))
        )
        dataManager.loadList(loadContext)

        then:
        noExceptionThrown()
    }
}
