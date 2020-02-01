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

package app_context_events

import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.compatibility.AppContext
import io.jmix.core.event.AppContextInitializedEvent
import io.jmix.core.event.AppContextStartedEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification
import test_support.AppContextTestExecutionListener
import test_support.singlerun.AppContextLifecycleListener
import test_support.singlerun.TestSingleRunConfiguration

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestSingleRunConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class AppContextLifecycleEventsTest extends Specification {

    @Inject
    AppContextLifecycleListener listenerBean

    def "test"() {
        expect:

        listenerBean.events[0] instanceof ContextRefreshedEvent
        listenerBean.events[1] instanceof AppContextInitializedEvent
        listenerBean.events[2] instanceof AppContextStartedEvent

        AppContext.isStarted()
        AppContext.isReady()
    }
}
