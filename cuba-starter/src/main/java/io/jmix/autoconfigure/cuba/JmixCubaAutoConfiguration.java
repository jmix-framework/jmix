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

package io.jmix.autoconfigure.cuba;

import com.haulmont.cuba.JmixCubaConfiguration;
import io.jmix.core.JmixCoreConfiguration;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.dynattr.JmixDynAttrConfiguration;
import io.jmix.dynattrui.JmixDynAttrUiConfiguration;
import io.jmix.security.JmixSecurityConfiguration;
import io.jmix.ui.JmixUiConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        JmixCoreConfiguration.class,
        JmixDataConfiguration.class,
        JmixSecurityConfiguration.class,
        JmixUiConfiguration.class,
        JmixDynAttrConfiguration.class,
        JmixDynAttrUiConfiguration.class,
        JmixCubaConfiguration.class})
public class JmixCubaAutoConfiguration {
}
