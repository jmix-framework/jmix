/*
 * Copyright 2024 Haulmont.
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

package rest_ds;

import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AuthenticatedAsSystem;
import test_support.TestRestDsConfiguration;
import test_support.entity.CustomerPreference;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
public class RestDsIdentityIdTest {

    @Autowired
    DataManager dataManager;

    @Test
    void test() {
        CustomerPreference preference = dataManager.create(CustomerPreference.class);
        preference.setPreferenceType("test");
        preference.setPreferenceValue("test");

        assertThat(preference.getId()).isNull();

        CustomerPreference savedPreference = dataManager.save(preference);

        assertThat(savedPreference).isNotNull();
        assertThat(savedPreference.getId()).isNotNull();

        CustomerPreference loadedPreference = dataManager.load(CustomerPreference.class).id(savedPreference.getId()).one();

        assertThat(loadedPreference).isEqualTo(preference);
    }
}
