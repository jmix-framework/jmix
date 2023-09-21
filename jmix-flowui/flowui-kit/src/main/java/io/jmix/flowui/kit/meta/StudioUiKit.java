/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.kit.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface StudioUiKit {

    /**
     * Describes dependencies from module that should be loaded into Studio classloader.
     * It may be required to display a component in the Designer Preview that uses
     * a custom return type in its meta description.
     * The dependency should look like {@code group:name}
     */
    String[] studioClassloaderDependencies() default {};

    /**
     * Describes the required dependencies.
     * Optional. If empty the ui kit will be used in Studio.
     * The dependency should look like {@code group:name}
     */
    String[] requiredDependencies() default {};

    /**
     * Describes the required dependencies strategy.
     * <p></p>
     * If strategy is {@link RequiredDependenciesStrategy#AND} (default value)
     * then all dependencies are required to use the ui kit in the Studio,
     * otherwise any of the dependencies is enough.
     */
    RequiredDependenciesStrategy requiredDependenciesStrategy() default RequiredDependenciesStrategy.AND;
}
