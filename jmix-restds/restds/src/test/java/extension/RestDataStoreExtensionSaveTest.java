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
import io.jmix.core.FileRef;
import io.jmix.core.SaveContext;
import io.jmix.restds.extension.RestDataStoreExtension;
import io.jmix.restds.extension.RestDataStoreExtension.AfterLoadContext;
import io.jmix.restds.extension.RestDataStoreExtension.AfterSaveContext;
import io.jmix.restds.extension.RestDataStoreExtension.BeforeSaveContext;
import io.jmix.restds.impl.RestDataStoreExtensionSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import test_support.BaseRestDsIntegrationTest;
import test_support.TestHttpRequestRecorder;
import test_support.TestHttpRequestRecorder.RecordedRequest;
import test_support.TestRestDataStoreExtension;
import test_support.entity.Customer;
import test_support.entity.CustomerWithFewerAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = RestDataStoreExtensionSaveTest.TestConfig.class)
class RestDataStoreExtensionSaveTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    TestRestDataStoreExtension extension;
    @Autowired
    RestDataStoreExtensionSupport extensionSupport;
    @Autowired
    TestHttpRequestRecorder requestRecorder;

    LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        extension.reset();
        requestRecorder.clear();
    }

    @Test
    void beforeSave_newEntity_jsonChangesSentToServer() {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("beforeSave-new-" + now);
        extension.onBeforeSave(context -> context.getEntityJson().put("email", "before-save@example.com"));

        dataManager.saveWithoutReload(customer);

        List<BeforeSaveContext> contexts = extension.getBeforeSaveContexts();
        assertThat(contexts).hasSize(1);
        assertThat(contexts.get(0).isNew()).isTrue();
        assertThat(contexts.get(0).getEntity()).isSameAs(customer);
        assertThat(contexts.get(0).getDataStoreName()).isEqualTo("restService1");
        assertThat(reload(customer).getEmail()).isEqualTo("before-save@example.com");
    }

    @Test
    void beforeSave_existingEntity_jsonChangesSentToServer() {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("beforeSave-existing-" + now);
        Customer saved = dataManager.save(customer);
        extension.reset();
        extension.onBeforeSave(context -> context.getEntityJson().put("firstName", "set-by-extension"));

        saved.setEmail("existing@example.com");
        dataManager.saveWithoutReload(saved);

        List<BeforeSaveContext> contexts = extension.getBeforeSaveContexts();
        assertThat(contexts).hasSize(1);
        assertThat(contexts.get(0).isNew()).isFalse();
        Customer reloaded = reload(customer);
        assertThat(reloaded.getFirstName()).isEqualTo("set-by-extension");
        assertThat(reloaded.getEmail()).isEqualTo("existing@example.com");
    }

    @Test
    void afterSave_called_withEntitySavedEntityAndJson() {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("afterSave-" + now);
        SaveContext saveContext = new SaveContext().saving(customer);

        Customer saved = dataManager.save(saveContext).get(customer);

        List<AfterSaveContext> contexts = extension.getAfterSaveContexts();
        assertThat(contexts).hasSize(1);
        AfterSaveContext context = contexts.get(0);
        assertThat(context.isNew()).isTrue();
        assertThat(context.getEntity()).isSameAs(customer);
        assertThat(context.getSavedEntity()).isEqualTo(saved);
        // DataManager passes each data store its own SaveContext with the entities of that store.
        assertThat(context.getSaveContext().getEntitiesToSave()).contains(customer);
        assertThat(context.getSavedEntityJson().get("id").asString()).isEqualTo(customer.getId().toString());
        assertThat(context.getSavedEntityJson().get("lastName").asString()).isEqualTo(customer.getLastName());
    }

    @Test
    void save_withReload_afterLoadCalledForReturnedInstance() {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("save-reload-" + now);

        Customer returned = dataManager.save(customer);

        List<AfterSaveContext> saveContexts = extension.getAfterSaveContexts();
        List<AfterLoadContext> loadContexts = extension.getAfterLoadContexts();
        assertThat(saveContexts).hasSize(1);
        assertThat(extension.getBeforeLoadContexts()).hasSize(1);
        assertThat(loadContexts).hasSize(1);
        assertThat(loadContexts.get(0).getEntity()).isSameAs(returned);
        assertThat(returned).isNotSameAs(saveContexts.get(0).getSavedEntity());
    }

    @Test
    void extension_injectsDataManager_contextStarts(@Autowired List<RestDataStoreExtension> extensions) {
        assertThat(extensions).hasSize(2);
    }

    @Test
    void beforeSave_fileRef_localStorageNameInJsonAndConvertedOnSave() {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("beforeSave-fileRef-" + now);
        customer.setDocument(new FileRef("restService1-fs", "2026/09/30/extension-test.txt", "extension-test.txt"));
        AtomicReference<String> documentInJson = new AtomicReference<>();
        extension.onBeforeSave(context -> documentInJson.set(context.getEntityJson().get("document").asString()));

        dataManager.saveWithoutReload(customer);

        RecordedRequest request = requestRecorder.getLastEntityRequest();
        assertThat(documentInJson.get()).startsWith("restService1-fs://");
        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        assertThat(request.body())
                .contains("\"fs://")
                .doesNotContain("restService1-fs");
        assertThat(reload(customer).getDocument().getStorageName()).isEqualTo("restService1-fs");
    }

    @Test
    void beforeSave_decimalNumbers_keptUnchanged() {
        Customer customer = dataManager.create(Customer.class);
        String json = """
                {"big": 1234567890123456789.123456789, "small": 0.0000001, "scaled": 1000.10}""";

        String result = extensionSupport.beforeSave("restService1", new SaveContext(), customer, true, json);

        assertThat(extension.getBeforeSaveContexts()).hasSize(1);
        assertThat(result)
                .contains("1234567890123456789.123456789")
                .contains("0.0000001")
                .contains("1000.10");
    }

    @Test
    void saveHooks_notSupportedEntity_notCalled() {
        CustomerWithFewerAttributes customer = dataManager.create(CustomerWithFewerAttributes.class);
        customer.setLastName("saveHooks-notSupported-" + now);

        dataManager.saveWithoutReload(customer);

        assertThat(extension.getBeforeSaveContexts()).isEmpty();
        assertThat(extension.getAfterSaveContexts()).isEmpty();
    }

    @Test
    void remove_saveHooksNotCalled() {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("remove-" + now);
        Customer saved = dataManager.save(customer);
        extension.reset();

        dataManager.remove(saved);

        assertThat(extension.getBeforeSaveContexts()).isEmpty();
        assertThat(extension.getAfterSaveContexts()).isEmpty();
    }

    private Customer reload(Customer customer) {
        return dataManager.load(Customer.class).id(customer.getId()).one();
    }

    @Configuration
    static class TestConfig {

        @Bean
        static TestHttpRequestRecorder testHttpRequestRecorder() {
            return new TestHttpRequestRecorder();
        }

        @Bean
        TestRestDataStoreExtension extension() {
            return new TestRestDataStoreExtension(10);
        }

        // An extension may need DataManager, for example to load related data. It must not create a bean cycle.
        @Bean
        RestDataStoreExtension dataManagerUsingExtension(DataManager dataManager) {
            return (dataStoreName, metaClass) -> false;
        }
    }
}
