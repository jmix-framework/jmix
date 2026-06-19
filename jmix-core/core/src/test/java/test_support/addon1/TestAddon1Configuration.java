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

package test_support.addon1;

import io.jmix.core.annotation.JmixModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import test_support.TestBean;
import test_support.base.TestBaseConfiguration;

@Configuration
@ComponentScan
@JmixModule(id = "addon-1", dependsOn = TestBaseConfiguration.class)
@PropertySource(name = "addon-1", value = "test_support/addon1/addon1-module.properties")
public class TestAddon1Configuration {

    @Bean
    TestBean testAddon1Bean() {
        return new TestAddon1Bean();
    }

    @Bean
    @Primary
    public CacheManager addonCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public TestAddonFooBean addonFooBean() {
        return new TestAddonFooBean();
    }

    @Bean
    @Primary
    public TestAddonBarBean addonBarBean() {
        return new TestAddonBarBean();
    }
}
