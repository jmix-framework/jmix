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

package extension;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.restds.extension.RestDataStoreExtension.AfterLoadContext;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import test_support.BaseRestDsIntegrationTest;
import test_support.TestHttpRequestRecorder;
import test_support.TestHttpRequestRecorder.RecordedRequest;
import test_support.TestRestDataStoreExtension;
import test_support.entity.Customer;
import test_support.entity.CustomerWithExtraAttributes;
import test_support.entity.CustomerWithFewerAttributes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ContextConfiguration(classes = RestDataStoreExtensionLoadTest.TestConfig.class)
class RestDataStoreExtensionLoadTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    TestHttpRequestRecorder requestRecorder;
    @Autowired
    @Qualifier("firstExtension")
    TestRestDataStoreExtension firstExtension;
    @Autowired
    @Qualifier("secondExtension")
    TestRestDataStoreExtension secondExtension;

    JsonMapper jsonMapper = new JsonMapper();
    LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        resetRecorded();
    }

    @Test
    void beforeLoad_loadById_parametersSentInQueryString() {
        Customer customer = createCustomer("beforeLoad-byId-" + now, null);
        firstExtension.onBeforeLoad(context -> context.getRequestParameters().put("testParam", "value1"));

        dataManager.load(Customer.class).id(customer.getId()).one();

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.GET);
        assertThat(request.uri().getPath()).endsWith("/Customer/" + customer.getId());
        assertThat(request.queryParam("testParam")).isEqualTo("value1");
    }

    @Test
    void beforeLoad_listWithoutCondition_parametersSentInQueryString() {
        firstExtension.onBeforeLoad(context -> context.getRequestParameters().put("testParam", "value1"));

        dataManager.load(Customer.class).all().maxResults(5).list();

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.GET);
        assertThat(request.queryParam("testParam")).isEqualTo("value1");
    }

    @Test
    void beforeLoad_listWithCondition_parametersSentInSearchBody() {
        Customer customer = createCustomer("beforeLoad-condition-" + now, null);
        firstExtension.onBeforeLoad(context -> context.getRequestParameters().put("testParam", "value1"));

        dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("lastName", customer.getLastName()))
                .list();

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        JsonNode body = jsonMapper.readTree(request.body());
        assertThat(body.get("testParam").asString()).isEqualTo("value1");
    }

    @Test
    void beforeLoad_hintSet_serverReceivesParameter() {
        Customer customer = createCustomer("beforeLoad-hint-" + now, null);
        firstExtension.onBeforeLoad(context -> {
            if (context.getLoadContext().getHints().containsKey("test.returnNulls")) {
                context.getRequestParameters().put("returnNulls", true);
            }
        });

        dataManager.load(Customer.class).id(customer.getId()).one();
        dataManager.load(Customer.class).id(customer.getId()).hint("test.returnNulls", true).one();

        List<AfterLoadContext> contexts = firstExtension.getAfterLoadContexts();
        assertThat(contexts).hasSize(2);
        assertThat(contexts.get(0).getEntityJson().has("email")).isFalse();
        assertThat(contexts.get(1).getEntityJson().has("email")).isTrue();
        assertThat(contexts.get(1).getEntityJson().get("email").isNull()).isTrue();
    }

    @Test
    void beforeLoad_parameterNameUsedByStore_loadFails() {
        firstExtension.onBeforeLoad(context -> context.getRequestParameters().put("limit", 1000));

        assertThatThrownBy(() -> dataManager.load(Customer.class).all().maxResults(5).list())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void extensions_twoSupporting_calledInOrderWithSharedParameters() {
        AtomicBoolean secondSawFirstParameter = new AtomicBoolean();
        firstExtension.onBeforeLoad(context -> context.getRequestParameters().put("fromFirst", "1"));
        secondExtension.onBeforeLoad(context ->
                secondSawFirstParameter.set(context.getRequestParameters().containsKey("fromFirst")));

        dataManager.load(Customer.class).all().maxResults(1).list();

        assertThat(secondSawFirstParameter).isTrue();
    }

    @Test
    void afterLoad_list_eachEntityGetsItsOwnJson() {
        Customer customer1 = createCustomer("afterLoad-list-1-" + now, null);
        Customer customer2 = createCustomer("afterLoad-list-2-" + now, null);

        List<Customer> loaded = dataManager.load(Customer.class).ids(customer1.getId(), customer2.getId()).list();

        List<AfterLoadContext> contexts = firstExtension.getAfterLoadContexts();
        assertThat(loaded).hasSize(2);
        assertThat(contexts).hasSize(2);
        for (AfterLoadContext context : contexts) {
            Customer entity = (Customer) context.getEntity();
            assertThat(context.getDataStoreName()).isEqualTo("restService1");
            assertThat(context.getEntityJson().get("id").asString()).isEqualTo(entity.getId().toString());
            assertThat(context.getEntityJson().get("lastName").asString()).isEqualTo(entity.getLastName());
        }
        assertThat(contexts)
                .extracting(AfterLoadContext::getEntity)
                .containsExactlyInAnyOrderElementsOf(loaded);
    }

    @Test
    void afterLoad_loadOneWithCondition_called() {
        Customer customer = createCustomer("afterLoad-one-" + now, null);

        dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("lastName", customer.getLastName()))
                .one();

        List<AfterLoadContext> contexts = firstExtension.getAfterLoadContexts();
        assertThat(contexts).hasSize(1);
        assertThat(contexts.get(0).getEntityJson().get("id").asString()).isEqualTo(customer.getId().toString());
    }

    @Test
    void afterLoad_loadByIdNotFound_notCalled() {
        Optional<Customer> loaded = dataManager.load(Customer.class).id(UUID.randomUUID()).optional();

        assertThat(loaded).isEmpty();
        assertThat(firstExtension.getBeforeLoadContexts()).hasSize(1);
        assertThat(firstExtension.getAfterLoadContexts()).isEmpty();
    }

    @Test
    void afterLoad_propertyNotInMetaClass_availableInJson() {
        Customer customer = createCustomer("afterLoad-fewer-" + now, "fewer@example.com");
        firstExtension.setSupportedClass(CustomerWithFewerAttributes.class);

        dataManager.load(CustomerWithFewerAttributes.class).id(customer.getId()).one();

        List<AfterLoadContext> contexts = firstExtension.getAfterLoadContexts();
        assertThat(metadata.getClass(CustomerWithFewerAttributes.class).findProperty("email")).isNull();
        assertThat(contexts).hasSize(1);
        assertThat(contexts.get(0).getEntityJson().get("email").asString()).isEqualTo("fewer@example.com");
    }

    @Test
    void supports_otherEntity_notCalled() {
        dataManager.load(CustomerWithExtraAttributes.class).all().maxResults(1).list();

        assertThat(firstExtension.getBeforeLoadContexts()).isEmpty();
        assertThat(firstExtension.getAfterLoadContexts()).isEmpty();
    }

    @Test
    void beforeCount_withoutCondition_parametersSentInQueryString() {
        firstExtension.onBeforeCount(context -> context.getRequestParameters().put("testParam", "value1"));

        dataManager.getCount(new LoadContext<>(metadata.getClass(Customer.class)));

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(request.method()).isEqualTo(HttpMethod.GET);
        assertThat(request.queryParam("returnCount")).isEqualTo("true");
        assertThat(request.queryParam("testParam")).isEqualTo("value1");
        assertThat(firstExtension.getBeforeCountContexts()).hasSize(1);
        assertThat(firstExtension.getBeforeLoadContexts()).isEmpty();
    }

    @Test
    void beforeCount_withCondition_parametersSentInSearchBody() {
        Customer customer = createCustomer("beforeCount-condition-" + now, null);
        firstExtension.onBeforeCount(context -> context.getRequestParameters().put("testParam", "value1"));

        long count = dataManager.getCount(new LoadContext<>(metadata.getClass(Customer.class)).setQuery(
                new LoadContext.Query("")
                        .setCondition(PropertyCondition.equal("lastName", customer.getLastName()))));

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(count).isEqualTo(1);
        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        assertThat(jsonMapper.readTree(request.body()).get("testParam").asString()).isEqualTo("value1");
        assertThat(firstExtension.getBeforeLoadContexts()).isEmpty();
    }

    private Customer createCustomer(String lastName, @Nullable String email) {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName(lastName);
        customer.setEmail(email);
        Customer saved = dataManager.save(customer);
        resetRecorded();
        return saved;
    }

    private void resetRecorded() {
        firstExtension.reset();
        secondExtension.reset();
        requestRecorder.clear();
    }

    @Configuration
    static class TestConfig {

        @Bean
        static TestHttpRequestRecorder testHttpRequestRecorder() {
            return new TestHttpRequestRecorder();
        }

        // Declared before the first extension to check that the call order comes from getOrder().
        @Bean
        TestRestDataStoreExtension secondExtension() {
            return new TestRestDataStoreExtension(20);
        }

        @Bean
        TestRestDataStoreExtension firstExtension() {
            return new TestRestDataStoreExtension(10);
        }
    }
}
