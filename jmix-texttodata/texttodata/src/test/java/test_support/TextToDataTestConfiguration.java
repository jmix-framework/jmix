/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.MessageSourceBasenames;
import io.jmix.data.DataConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.texttodata.TextToDataConfiguration;
import io.jmix.texttodata.introspection.introspector.JpaDomainModelIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CoreConfiguration.class, TextToDataConfiguration.class, CommonCoreTestConfiguration.class, DataConfiguration.class})
@JmixModule
@MessageSourceBasenames("test_support/messages")
public class TextToDataTestConfiguration {

    @Bean("test_JpaDomainModelIntrospector")
    public JpaDomainModelIntrospector jpaDomainModelIntrospector() {
        return new JpaDomainModelIntrospector();
    }
}
