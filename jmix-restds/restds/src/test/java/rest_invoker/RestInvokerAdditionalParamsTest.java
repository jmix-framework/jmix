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

package rest_invoker;

import io.jmix.restds.impl.RestInvoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import test_support.BaseRestDsIntegrationTest;
import test_support.TestHttpRequestRecorder;
import test_support.TestHttpRequestRecorder.RecordedRequest;
import test_support.TestSupport;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ContextConfiguration(classes = RestInvokerAdditionalParamsTest.TestConfig.class)
class RestInvokerAdditionalParamsTest extends BaseRestDsIntegrationTest {

    static final String LAST_NAME_FILTER = """
            {"conditions": [{"property": "lastName", "operator": "=", "value": "no-such-customer"}]}
            """;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    TestHttpRequestRecorder requestRecorder;

    RestInvoker restInvoker;
    JsonMapper jsonMapper = new JsonMapper();

    @BeforeEach
    void setUp() {
        restInvoker = applicationContext.getBean(RestInvoker.class, "restService1");
        requestRecorder.clear();
    }

    @Test
    void load_additionalParams_sentInQueryString() {
        restInvoker.load(new RestInvoker.LoadParams("Customer", TestSupport.UUID_1, null,
                Map.of("testParam", "value1")));

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.GET);
        assertThat(request.queryParam("testParam")).isEqualTo("value1");
    }

    @Test
    void load_valueWithUrlCharacters_sentUnchanged() {
        String value = "a&b={c} d/e?f";

        restInvoker.load(new RestInvoker.LoadParams("Customer", TestSupport.UUID_1, null,
                Map.of("testParam", value)));

        assertThat(requestRecorder.getLastEntityRequest().queryParam("testParam")).isEqualTo(value);
    }

    @Test
    void loadList_withoutFilter_additionalParamsSentInQueryString() {
        restInvoker.loadList(new RestInvoker.LoadListParams("Customer", 1, 0, null, null, null,
                Map.of("testParam", "value1")));

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.GET);
        assertThat(request.queryParam("testParam")).isEqualTo("value1");
        assertThat(request.queryParam("limit")).isEqualTo("1");
    }

    @Test
    void loadList_withFilter_additionalParamsSentInSearchBody() {
        restInvoker.loadList(new RestInvoker.LoadListParams("Customer", 1, 0, null, LAST_NAME_FILTER, null,
                Map.of("testParam", "value1", "testFlag", true)));

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        JsonNode body = jsonMapper.readTree(request.body());
        assertThat(body.get("testParam").asString()).isEqualTo("value1");
        assertThat(body.get("testFlag").isBoolean()).isTrue();
        assertThat(body.get("testFlag").asBoolean()).isTrue();
        assertThat(body.has("filter")).isTrue();
    }

    @Test
    void count_withoutFilter_additionalParamsSentInQueryString() {
        restInvoker.count("Customer", null, Map.of("testParam", "value1"));

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.GET);
        assertThat(request.queryParam("returnCount")).isEqualTo("true");
        assertThat(request.queryParam("testParam")).isEqualTo("value1");
    }

    @Test
    void count_withFilter_additionalParamsSentInSearchBody() {
        restInvoker.count("Customer", LAST_NAME_FILTER, Map.of("testParam", "value1"));

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        JsonNode body = jsonMapper.readTree(request.body());
        assertThat(body.get("returnCount").asBoolean()).isTrue();
        assertThat(body.get("testParam").asString()).isEqualTo("value1");
    }

    @Test
    void loadListParams_nameUsedByStore_rejected() {
        assertThatThrownBy(() -> new RestInvoker.LoadListParams("Customer", 1, 0, null, null, null,
                Map.of("limit", 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void loadParams_nullValue_rejected() {
        Map<String, Object> params = new HashMap<>();
        params.put("testParam", null);

        assertThatThrownBy(() -> new RestInvoker.LoadParams("Customer", TestSupport.UUID_1, null, params))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("testParam");
    }

    @Configuration
    static class TestConfig {

        @Bean
        static TestHttpRequestRecorder testHttpRequestRecorder() {
            return new TestHttpRequestRecorder();
        }
    }
}
