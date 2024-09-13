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

package rest_invoker;

import io.jmix.restds.impl.RestPasswordAuthenticator;
import io.jmix.restds.impl.RestInvoker;
import io.jmix.restds.impl.RestSerialization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.Customer;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RestInvokerPasswordGrantTest extends BaseRestDsIntegrationTest {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    RestSerialization restSerialization;

    ObjectMapper objectMapper = new ObjectMapper();

    RestInvoker restInvoker;

    @BeforeEach
    void setUp() {
        restInvoker = applicationContext.getBean(RestInvoker.class, "restService2");
        ((RestPasswordAuthenticator) restInvoker.getAuthenticator()).authenticate("admin", "admin");
    }

    @Test
    void testLoad() {
        var loadListParams = new RestInvoker.LoadListParams("Customer", 0, 0, null, null, null);
        List<Customer> customers = restSerialization.fromJsonCollection(
                restInvoker.loadList(loadListParams),
                Customer.class);

        assertThat(customers).isNotEmpty();

        var loadParams = new RestInvoker.LoadParams("Customer", customers.get(0).getId());
        Customer customer = restSerialization.fromJson(
                restInvoker.load(loadParams),
                Customer.class);

        assertThat(customer).isEqualTo(customers.get(0));
    }

    @Test
    void testUserInfo() throws IOException {
        String json = restInvoker.userInfo();

        JsonNode rootNode = objectMapper.readTree(json);
        assertThat(rootNode.get("username").asText()).isEqualTo("admin");

        JsonNode authoritiesNode = rootNode.get("attributes");
        assertThat(authoritiesNode).isNotNull();
        assertThat(authoritiesNode.get("id").asText()).isEqualTo("60885987-1b61-4247-94c7-dff348347f93");
        assertThat(authoritiesNode.get("active").asBoolean()).isEqualTo(true);
    }

    @Test
    void testPermissions() throws IOException {
        String json = restInvoker.permissions();

        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode authoritiesNode = rootNode.get("authorities");
        assertThat(authoritiesNode).isNotNull();
        assertThat(authoritiesNode.isArray()).isTrue();
        assertThat(authoritiesNode.size()).isEqualTo(1);
        assertThat(authoritiesNode.get(0).asText()).isEqualTo("ROLE_system-full-access");
    }
}
