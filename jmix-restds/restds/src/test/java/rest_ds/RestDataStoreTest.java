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
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.SampleServiceConnection;
import test_support.TestSupport;
import test_support.entity.Country;
import test_support.entity.Customer;
import test_support.entity.CustomerRegionDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RestDataStoreTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;
    
    LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void testLoad() {
        Customer customer = dataManager.load(Customer.class).id(TestSupport.UUID_1).one();

        assertThat(customer).isNotNull();

        List<Customer> customers = dataManager.load(Customer.class).all().list();

        assertThat(customers).isNotEmpty();
    }

    @Test
    void testCreateUpdateDelete() {
        Customer customer = dataManager.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");

        Customer createdCustomer = dataManager.save(customer);

        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getLastName()).isEqualTo(newName);
        assertThat(createdCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(createdCustomer.getCreatedBy()).isEqualTo(SampleServiceConnection.CLIENT_ID);
        assertThat(createdCustomer.getCreatedDate()).isNotNull();
        assertThat(createdCustomer.getLastModifiedBy()).isNull();
        assertThat(createdCustomer.getLastModifiedDate()).isNotNull();

        createdCustomer.setLastName("updated-cust-" + LocalDateTime.now());

        Customer updatedCustomer = dataManager.save(createdCustomer);

        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getLastName()).isEqualTo(createdCustomer.getLastName());
        assertThat(updatedCustomer.getEmail()).isEqualTo(createdCustomer.getEmail());
        assertThat(updatedCustomer.getLastModifiedBy()).isEqualTo(SampleServiceConnection.CLIENT_ID);

        dataManager.remove(updatedCustomer);

        Customer deletedCustomer = dataManager.load(Customer.class).id(updatedCustomer.getId()).optional().orElse(null);

        assertThat(deletedCustomer).isNull();
    }

    @Test
    void testCount() {
        Customer customer = createCustomer(null, "new-cust-1-" + now);

        LoadContext<Object> loadContext = new LoadContext<>(metadata.getClass(Customer.class)).setQuery(
                new LoadContext.Query("")
                        .setCondition(PropertyCondition.equal("id", customer.getId()))
                        .setParameter("id", customer.getId())
        );
        long customerCount = dataManager.getCount(loadContext);

        assertThat(customerCount).isEqualTo(1);
    }

    @Test
    void testCondition() {
        Customer customer1 = createCustomer(null, "testCondition-cust-1-" + now);
        Customer customer2 = createCustomer(null, "testCondition-cust-2-" + now);

        List<Customer> customers = dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("lastName", customer2.getLastName()))
                .list();

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer2);
    }

    @Test
    void testLoadOneWithCondition() {
        Customer customer1 = createCustomer(null, "testCondition-cust-1-" + now);
        Customer customer2 = createCustomer(null, "testCondition-cust-2-" + now);

        Customer customer = dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("lastName", customer2.getLastName()))
                .one();

        assertThat(customer).isEqualTo(customer2);

        Optional<Customer> optionalCustomer = dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("lastName", "non-existent name"))
                .optional();

        assertThat(optionalCustomer).isEqualTo(Optional.empty());
    }

    @Test
    void testQuery() {
        String firstName = "testQuery-firstName-" + now;
        Customer customer1 = createCustomer(firstName, "testQuery-cust-1-" + now);
        Customer customer2 = createCustomer(firstName, "testQuery-cust-2-" + now);

        String query = """
                {
                  "property": "firstName",
                  "operator": "=",
                  "value": "%s"
                }
                """.formatted(firstName);

        // test query only
        List<Customer> customers = dataManager.load(Customer.class).query(query).list();

        assertThat(customers).size().isEqualTo(2);

        // test query and condition
        customers = dataManager.load(Customer.class)
                .query(query)
                .condition(PropertyCondition.equal("lastName", customer1.getLastName()))
                .list();

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer1);
    }

    @Test
    void testQueryWithParameters() {
        String firstName = "testQuery-firstName-" + now;
        Customer customer1 = createCustomer(firstName, "testQuery-cust-1-" + now);
        Customer customer2 = createCustomer(firstName, "testQuery-cust-2-" + now);

        String query = """
                {
                  "property": "firstName",
                  "operator": "=",
                  "parameterName": "name"
                }
                """;

        List<Customer> customers = dataManager.load(Customer.class)
                .query(query)
                .parameter("name", firstName)
                .list();

        assertThat(customers).size().isEqualTo(2);
    }

    @Test
    void testQueryAndConditionWithParameters() {
        String firstName = "testQuery-firstName-" + now;
        Customer customer1 = createCustomer(firstName, "testQuery-cust-1-" + now);
        Customer customer2 = createCustomer(firstName, "testQuery-cust-2-" + now);

        PropertyCondition condition = PropertyCondition.createWithParameterName("firstName", PropertyCondition.Operation.EQUAL, "name");
        condition.setParameterValue(firstName);

        List<Customer> customers = dataManager.load(Customer.class)
                .condition(condition)
                .list();

        assertThat(customers).size().isEqualTo(2);

        String query = """
                {
                  "property": "lastName",
                  "operator": "startsWith",
                  "parameterName": "last_name"
                }
                """;

        customers = dataManager.load(Customer.class)
                .query(query)
                .parameter("last_name", customer1.getLastName())
                .condition(condition)
                .list();

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer1);
    }

    @Test
    void testLoadByIds() {
        Customer customer1 = createCustomer(null, "testLoadByIds-cust-1-" + now);
        Customer customer2 = createCustomer(null, "testLoadByIds-cust-2-" + now);

        List<Customer> customers = dataManager.load(Customer.class).ids(customer1.getId(), customer2.getId()).list();

        assertThat(customers).size().isEqualTo(2);
    }

    @Test
    void testLoadValues() {
        try {
            List<KeyValueEntity> keyValueEntities = dataManager.loadValues("select c.firstName from Customer c")
                    .store("restService1")
                    .property("firstName")
                    .list();
            fail("Should throw exception");
        } catch (Throwable e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Test
    void testDifferentEntityName() {
        // load one

        CustomerRegionDto region = dataManager.load(CustomerRegionDto.class).id(TestSupport.UUID_1).one();

        assertThat(region).isNotNull();

        // load many

        List<CustomerRegionDto> list = dataManager.load(CustomerRegionDto.class).all().list();

        assertThat(list).isNotEmpty();

        // create

        region = dataManager.create(CustomerRegionDto.class);
        region.setName("testDifferentEntityName-" + now);

        CustomerRegionDto savedRegion = dataManager.save(region);

        assertThat(savedRegion).isEqualTo(region);

        // update

        region.setName("testDifferentEntityName-updated-" + now);

        CustomerRegionDto updatedRegion = dataManager.save(region);

        assertThat(updatedRegion).isEqualTo(region);

        // delete

        dataManager.remove(region);

        CustomerRegionDto deletedRegion = dataManager.load(CustomerRegionDto.class).id(region.getId()).optional().orElse(null);

        assertThat(deletedRegion).isNull();
    }

    @Test
    void testStringId() {
        // create

        Country country = dataManager.create(Country.class);
        country.setCode("zz-" + now);
        country.setName("testStringId-" + now);

        Country savedCountry = dataManager.save(country);

        assertThat(savedCountry).isEqualTo(country);

        // update

        savedCountry.setName("testStringId-updated-" + now);

        savedCountry = dataManager.save(savedCountry);

        assertThat(savedCountry).isEqualTo(country);

        // load

        savedCountry = dataManager.load(Country.class).id(country.getCode()).one();

        // delete

        dataManager.remove(savedCountry);

        Country deletedCountry = dataManager.load(Country.class).id(country.getCode()).optional().orElse(null);

        assertThat(deletedCountry).isNull();
    }

    @Test
    void testUtf8() {
        Customer customer = metadata.create(Customer.class);
        customer.setFirstName("Hà nội");
        customer.setLastName("Việt nam");
        customer.setEmail("test@mail.com");
        Customer savedCustomer = dataManager.save(customer);

        assertThat(savedCustomer.getFirstName()).isEqualTo("Hà nội");

        Customer loadedCustomer = dataManager.load(Id.of(customer)).one();

        assertThat(loadedCustomer.getFirstName()).isEqualTo("Hà nội");
    }

    private Customer createCustomer(String firstName, String lastName) {
        Customer customer = metadata.create(Customer.class);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail("test@mail.com");
        dataManager.save(customer);
        return customer;
    }
}
