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

package data_components

import io.jmix.core.Metadata
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.element_collection.EcAlpha
import test_support.spec.DataContextSpec

class DataContextElementCollectionTest extends DataContextSpec {

    @Autowired
    DataComponents factory
    @Autowired
    Metadata metadata

    def "track changes in element collection"() {

        def dataContext = factory.createDataContext()

        when:

        EcAlpha alpha = metadata.create(EcAlpha)
        alpha.name = 'cust 1'
        alpha.tags = ['t1', 't2']
        entityStates.makeDetached(alpha)

        def mergedAlpha = dataContext.merge(alpha)

        then:

        dataContext.contains(alpha)
        !dataContext.isModified(alpha)

        when:

        mergedAlpha.tags.add('t3')

        then:

        dataContext.isModified(mergedAlpha)
        mergedAlpha.tags == ['t1', 't2', 't3']
    }
}
