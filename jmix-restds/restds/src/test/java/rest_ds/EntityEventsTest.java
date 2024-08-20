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
import io.jmix.core.SaveContext;
import io.jmix.core.event.EntityLoadingEvent;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.restds.event.RestEntityRemovedEvent;
import io.jmix.restds.event.RestEntitySavedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AuthenticatedAsSystem;
import test_support.TestRestDsConfiguration;
import test_support.entity.Customer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
public class EntityEventsTest {

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    DataManager dataManager;

    TestRestEntitySavedEventListener restEntitySavedEventListener;
    TestRestEntityRemovedEventListener restEntityRemovedEventListener;
    TestEntitySavingEventListener savingEventListener;
    TestEntityLoadingEventListener loadingEventListener;

    @BeforeEach
    void setUp() {
        restEntitySavedEventListener = new TestRestEntitySavedEventListener();
        applicationContext.addApplicationListener(restEntitySavedEventListener);

        restEntityRemovedEventListener = new TestRestEntityRemovedEventListener();
        applicationContext.addApplicationListener(restEntityRemovedEventListener);

        savingEventListener = new TestEntitySavingEventListener();
        applicationContext.addApplicationListener(savingEventListener);

        loadingEventListener = new TestEntityLoadingEventListener();
        applicationContext.addApplicationListener(loadingEventListener);
    }

    @AfterEach
    void tearDown() {
        if (loadingEventListener != null) {
            applicationContext.removeApplicationListener(loadingEventListener);
        }
        if (restEntityRemovedEventListener != null) {
            applicationContext.removeApplicationListener(restEntityRemovedEventListener);
        }
        if (savingEventListener != null) {
            applicationContext.removeApplicationListener(savingEventListener);
        }
        if (restEntitySavedEventListener != null) {
            applicationContext.removeApplicationListener(restEntitySavedEventListener);
        }
    }

    @Test
    void test() {
        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Doe");

        // creating

        dataManager.save(customer);

        assertThat(restEntitySavedEventListener.events).hasSize(1);

        RestEntitySavedEvent<Customer> savedEvent = restEntitySavedEventListener.events.get(0);
        assertThat(savedEvent.getSavedEntity()).isEqualTo(customer);
        assertThat(savedEvent.isNewEntity()).isTrue();

        assertThat(savingEventListener.events).hasSize(1);
        assertThat(loadingEventListener.events).hasSize(1);

        // loading

        Customer savedCustomer = dataManager.load(Customer.class).id(customer.getId()).one();

        assertThat(loadingEventListener.events).hasSize(2);

        // updating

        savedCustomer.setFirstName("Johny");
        savedCustomer = dataManager.save(savedCustomer);

        assertThat(restEntitySavedEventListener.events).hasSize(2);
        savedEvent = restEntitySavedEventListener.events.get(1);
        assertThat(savedEvent.isNewEntity()).isFalse();
        assertThat(savedEvent.getSavedEntity()).isEqualTo(savedCustomer);
        assertThat(savedEvent.getReturnedEntity()).isEqualTo(savedCustomer);

        assertThat(savingEventListener.events).hasSize(2);
        assertThat(loadingEventListener.events).hasSize(3);

        // updating again with discardSaved

        dataManager.save(new SaveContext().saving(savedCustomer).setDiscardSaved(true));

        assertThat(restEntitySavedEventListener.events).hasSize(3);
        savedEvent = restEntitySavedEventListener.events.get(2);
        assertThat(savedEvent.getSavedEntity()).isEqualTo(savedCustomer);
        assertThat(savedEvent.getReturnedEntity()).isEqualTo(savedCustomer);

        assertThat(savingEventListener.events).hasSize(3);
        assertThat(loadingEventListener.events).hasSize(3); // not reloaded

        // removing

        dataManager.remove(savedCustomer);

        assertThat(restEntitySavedEventListener.events).hasSize(3);
        assertThat(restEntityRemovedEventListener.events).hasSize(1);
        assertThat(restEntityRemovedEventListener.events.get(0).getEntity()).isEqualTo(savedCustomer);
    }

    private static class TestRestEntitySavedEventListener implements ApplicationListener<RestEntitySavedEvent<Customer>> {

        List<RestEntitySavedEvent<Customer>> events = new ArrayList<>();

        @Override
        public void onApplicationEvent(RestEntitySavedEvent<Customer> event) {
            events.add(event);
        }
    }

    private static class TestRestEntityRemovedEventListener implements ApplicationListener<RestEntityRemovedEvent<Customer>> {

        List<RestEntityRemovedEvent<Customer>> events = new ArrayList<>();

        @Override
        public void onApplicationEvent(RestEntityRemovedEvent<Customer> event) {
            events.add(event);
        }
    }

    private static class TestEntitySavingEventListener implements ApplicationListener<EntitySavingEvent<Customer>> {

        List<EntitySavingEvent<Customer>> events = new ArrayList<>();

        @Override
        public void onApplicationEvent(EntitySavingEvent<Customer> event) {
            events.add(event);
        }
    }

    private static class TestEntityLoadingEventListener implements ApplicationListener<EntityLoadingEvent<Customer>> {

        List<EntityLoadingEvent<Customer>> events = new ArrayList<>();

        @Override
        public void onApplicationEvent(EntityLoadingEvent<Customer> event) {
            events.add(event);
        }
    }
}
