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

import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

/**
 * The annotation is used for testing Flow UI views on JUnit.
 * <p>
 * Base example:
 * <pre>
 * &#64;UiTest
 * &#64;SpringBootTest(classes = {DemoApplication.class, FlowuiTestAssistConfiguration.class})
 * public class UserViewsTest {
 *     &#64;Autowired
 *     private ViewNavigators viewNavigators;
 *
 *     &#64;Test
 *     public void navigateToUserListView() {
 *         viewNavigators.view(UserListView.class)
 *                 .navigate();
 *
 *         UserListView view = UiTestUtils.getCurrentView();
 *
 *         CollectionContainer<User> usersDc = ViewControllerUtils.getViewData(view)
 *                 .getContainer("usersDc");
 *
 *         Assertions.assertTrue(usersDc.getItems().size() > 0);
 *     }
 * }
 * </pre>
 *
 * @see JmixUiTestExtension
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({SpringExtension.class, JmixUiTestExtension.class})
@Documented
@Inherited
public @interface UiTest {

    /**
     * Views under these packages will be available in test.
     * <p>
     * Note that depending on the test's configuration all application views may be available.
     *
     * @return array of view packages should be registered
     */
    String[] viewBasePackages() default {};

    /**
     * Class providing authentication management in tests.
     * <p>
     * By default, {@link SystemAuthenticator} is used for authentication.
     *
     * @return class that implements {@link UiTestAuthenticator}
     */
    Class<? extends UiTestAuthenticator> authenticator() default DefaultUiTestAuthenticator.class;

    /**
     * Dummy class. By default, the {@link JmixUiTestExtension} will use {@link SystemAuthenticator}.
     */
    abstract class DefaultUiTestAuthenticator implements UiTestAuthenticator {
    }
}
