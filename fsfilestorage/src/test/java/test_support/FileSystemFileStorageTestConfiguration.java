/*
 * Copyright 2020 Haulmont.
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

import io.jmix.core.FileStorage;
import io.jmix.core.annotation.JmixModule;
import io.jmix.fsfilestorage.FileSystemFileStorageConfiguration;
import io.jmix.fsfilestorage.FileSystemFileStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import java.net.URI;

@Configuration
@PropertySource("classpath:/test_support/test-app.properties")
@JmixModule(dependsOn = FileSystemFileStorageConfiguration.class)
public class FileSystemFileStorageTestConfiguration {
    @Bean
    @Primary
    FileStorage<URI, String> fileStorage() {
        return new FileSystemFileStorage();
    }

}
