/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.testassist;

import org.springframework.context.ApplicationContext;

/**
 * Interface to be implemented by classes for managing application authentication.
 * <p>
 * To provide global implementation for all test classes with {@link JmixUiTestExtension} create a spring bean
 * that implements this interface. Before all tests extension will get bean from the context and
 * will use it for setting/removing authentication.
 * <p>
 * To provide implementation only for concrete test class with {@link JmixUiTestExtension} create a class with default
 * constructor that implements this interface. To set implementation use
 * {@link JmixUiTestExtension#withTestAuthenticator(UiTestAuthenticator)} or {@link UiTest#authenticator()}.
 *
 * @see JmixUiTestExtension
 * @see UiTest
 */
public interface UiTestAuthenticator {

    /**
     * Invoked before each test.
     *
     * @param context application context
     */
    void setupAuthentication(ApplicationContext context);

    /**
     * Invoked after each test.
     *
     * @param context application context
     */
    void removeAuthentication(ApplicationContext context);
}
