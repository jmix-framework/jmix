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

package lazy_loading.disabled_lazy_loading

import cascade_operations.CascadeEventsTest
import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlans
import io.jmix.core.SaveContext
import io.jmix.data.PersistenceHints
import io.jmix.data.impl.EntityListenerManager
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import test_support.DataSpec
import test_support.entity.cascade_operations.JpaCascadeBar
import test_support.entity.cascade_operations.JpaCascadeEmbeddable
import test_support.entity.cascade_operations.JpaCascadeFoo
import test_support.entity.cascade_operations.JpaCascadeItem
import test_support.listeners.cascade_operations.TestCascadeBarEventListener
import test_support.listeners.cascade_operations.TestCascadeFooEventListener
import test_support.listeners.cascade_operations.TestCascadeItemEventListener



@TestPropertySource(properties = ["jmix.eclipselink.disableLazyLoading = true"])
class NoLLCascadeEventsTest extends CascadeEventsTest {
}
