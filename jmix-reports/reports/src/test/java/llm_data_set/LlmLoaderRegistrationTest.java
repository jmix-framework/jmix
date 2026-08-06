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

package llm_data_set;

import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.libintegration.LlmDataLoader;
import io.jmix.reports.yarg.exception.UnsupportedLoaderException;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import llm_data_set.test_support.LlmDataSetTestConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The LLM loader reaches the loader factory only when the AI Tools add-on provides the beans it needs.
 */
class LlmLoaderRegistrationTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {ReportsTestConfiguration.class, LlmDataSetTestConfiguration.class})
    class WithAddOnBeans {

        @Autowired
        protected ReportLoaderFactory loaderFactory;

        @Autowired
        protected LlmDataLoader llmDataLoader;

        @Test
        void testLlmLoaderTypeIsServedByTheLlmLoader() {
            assertThat(loaderFactory.createDataLoader(DataSetType.LLM.getCode())).isSameAs(llmDataLoader);
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {ReportsTestConfiguration.class})
    class WithoutAddOnBeans {

        @Autowired
        protected ReportLoaderFactory loaderFactory;

        @Test
        void testLlmLoaderTypeIsUnsupported() {
            assertThatThrownBy(() -> loaderFactory.createDataLoader(DataSetType.LLM.getCode()))
                    .isInstanceOf(UnsupportedLoaderException.class);
        }

        @Test
        void testBuiltInLoaderTypesKeepWorking() {
            assertThat(loaderFactory.createDataLoader("jpql")).isNotNull();
            assertThat(loaderFactory.createDataLoader("sql")).isNotNull();
            assertThat(loaderFactory.createDataLoader("groovy")).isNotNull();
            assertThat(loaderFactory.createDataLoader("json")).isNotNull();
        }
    }
}
