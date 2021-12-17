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

package io.jmix.ui.testassist.junit;

import io.jmix.ui.Screens;
import io.jmix.ui.UiProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

/**
 * The annotation is used for testing Jmix screens on JUnit.
 * <p>
 * For instance:
 * <pre>
 * &#64;UiTest(authenticatedUser = "admin", mainScreenId = "MainScreen", screenBasePackages = "com.company.demo.screen")
 * &#64;ContextConfiguration(classes = {DemoApplication.class, UiTestAssistConfiguration.class})
 * public class UserBrowseTest {
 *
 *     &#64;Test
 *     protected void openUserBrowse(Screens screens) {
 *         UserBrowse screen = screens.create(UserBrowse.class);
 *         screen.show();
 *     }
 * }
 * </pre>
 * {@link Screens} bean can be obtained from method parameters or via {@link ApplicationContext#getBean(Class)}.
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
     * The username that should be authenticated before each test. If username is not set, authentication will be
     * performed by system user.
     */
    String authenticatedUser() default "";

    /**
     * The main screen id that should be opened before each test. The screen with given id should be placed under
     * one of packages that are provided by {@link #screenBasePackages()}.
     * <p>
     * If main screen id is not set, the {@link UiProperties#getMainScreenId()} will be used.
     */
    String mainScreenId() default "";

    /**
     * Screens under these packages will be available in test. If packages are not set, all application screens
     * will be available depending on the test's configuration.
     */
    String[] screenBasePackages() default {};
}
