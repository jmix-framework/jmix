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

package remote_service;

import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.ContactType;
import test_support.entity.Customer;
import test_support.entity.CustomerContact;
import test_support.service.CustomerService;
import test_support.service.SampleService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteServiceTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    CustomerService customerService;

    @Autowired
    SampleService sampleService;

    private Customer customer;
    private CustomerContact contact1;
    private CustomerContact contact2;

    @BeforeEach
    void setUp() {
        // create customer and 2 contacts and save

        customer = dataManager.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Smith");
        customer.setEmail("john@example.com");
        customer.setContacts(new HashSet<>());

        contact1 = dataManager.create(CustomerContact.class);
        contact1.setCustomer(customer);
        contact1.setContactType(ContactType.PHONE);
        contact1.setContactValue("333-333-3333");
        contact1.setPreferred(true);
        customer.getContacts().add(contact1);

        contact2 = dataManager.create(CustomerContact.class);
        contact2.setCustomer(customer);
        contact2.setContactType(ContactType.EMAIL);
        contact2.setContactValue("john@example.com");
        customer.getContacts().add(contact2);

        dataManager.save(customer);
    }

    @Test
    void testServiceWithDifferentRemoteName() {
        customer.setContacts(null);

        CustomerContact contact = customerService.getPreferredContact(customer);

        assertThat(contact).isEqualTo(contact1);
    }

    @AfterEach
    void tearDown() {
        dataManager.remove(contact1, contact2, customer);
    }

    @Test
    void testSimpleTypes() throws Exception {
        sampleService.voidMethod();

        String s = sampleService.stringMethod("hello");
        assertThat(s).isEqualTo("hello");

        String s1 = sampleService.stringMethod(null);
        assertThat(s1).isNull();

        boolean b = sampleService.booleanMethod(true);
        assertThat(b).isTrue();

        Boolean b1 = sampleService.booleanWrapperMethod(true);
        assertThat(b1).isTrue();

        int i = sampleService.intMethod(1);
        assertThat(i).isEqualTo(1);

        Integer i1 = sampleService.intWrapperMethod(1);
        assertThat(i1).isEqualTo(1);
    }

    @Test
    void testOtherTypes() throws Exception{
        FileRef fileRef = FileRef.create("fileStorage1", "/path1", "file1.txt");
        FileRef resultFileRef = sampleService.fileRefMethod(fileRef);
        assertThat(resultFileRef).isEqualTo(fileRef);

        byte[] resultBytes = sampleService.binaryMethod("hello".getBytes(StandardCharsets.UTF_8));
        assertThat(resultBytes).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));

        URI uri = new URI("https://jmix.io");
        URI resultUri = sampleService.uriMethod(uri);
        assertThat(resultUri).isEqualTo(uri);

        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID resultUuid = sampleService.uuidMethod(uuid);
        assertThat(resultUuid).isEqualTo(uuid);
    }

    @Test
    void testDates() throws Exception {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2025-04-26 18:44");
        Date resultDate = sampleService.dateMethod(date);
        assertThat(resultDate).isEqualTo(date);

        LocalDateTime localDateTime = LocalDateTime.parse("2025-04-26T18:44");
        LocalDateTime resultLocalDateTime = sampleService.localDateTimeMethod(localDateTime);
        assertThat(resultLocalDateTime).isEqualTo(localDateTime);

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2025-04-26T18:44+04:00");
        OffsetDateTime resultOffsetDateTime = sampleService.offsetDateTimeMethod(offsetDateTime);
        assertThat(resultOffsetDateTime).isEqualTo(offsetDateTime);

        // ZonedDateTime is not supported by entities and REST
    }

    @Test
    void testEntities() {
        Customer resultCustomer = sampleService.entityMethod(customer);
        assertThat(resultCustomer).isNotNull();
        assertThat(resultCustomer).isEqualTo(customer);

        Customer resultCustomer1 = sampleService.entityMethod(null);
        assertThat(resultCustomer1).isNull();

        List<Customer> customers = sampleService.entityCollectionMethod(List.of(customer));
        assertThat(customers).isNotEmpty();
        assertThat(customers.get(0)).isEqualTo(customer);
    }

    @Test
    void testEnums() {
        ContactType resultEnum = sampleService.enumMethod(ContactType.PHONE);
        assertThat(resultEnum).isEqualTo(ContactType.PHONE);
    }

    @Test
    void testPojos() {
        SampleService.SamplePojo pojo = new SampleService.SamplePojo();
        pojo.setName("John");
        pojo.setAge(25);
        FileRef fileRef = FileRef.create("fileStorage1", "/path1", "file1.txt");
        pojo.setFileRef(fileRef);

        SampleService.SamplePojo resultPojo = sampleService.pojoMethod(pojo);
        assertThat(resultPojo).isNotNull();
        assertThat(resultPojo.getName()).isEqualTo("John");
        assertThat(resultPojo.getAge()).isEqualTo(25);
        assertThat(resultPojo.getFileRef()).isEqualTo(fileRef);

        SampleService.SamplePojoWithEntity pojoWithEntity = new SampleService.SamplePojoWithEntity();
        pojoWithEntity.setName("John");
        pojoWithEntity.setCustomer(customer);

        SampleService.SamplePojoWithEntity resultPojoWithEntity = sampleService.pojoWithEntityMethod(pojoWithEntity);
        assertThat(resultPojoWithEntity).isNotNull();
        assertThat(resultPojoWithEntity.getName()).isEqualTo("John");
        assertThat(resultPojoWithEntity.getCustomer()).isEqualTo(customer);

        SampleService.SampleRecord record = new SampleService.SampleRecord("John", 25);
        SampleService.SampleRecord resultRecord = sampleService.recordMethod(record);
        assertThat(resultRecord).isEqualTo(record);

        SampleService.SampleRecordWithEntity recordWithEntity = new SampleService.SampleRecordWithEntity("John", customer);
        SampleService.SampleRecordWithEntity resultRecordWithEntity = sampleService.recordWithEntityMethod(recordWithEntity);
        assertThat(resultRecordWithEntity).isEqualTo(recordWithEntity);
    }

    @Test
    void testMultipleParams() {
        SampleService.SamplePojo pojo = new SampleService.SamplePojo();
        pojo.setName("John");
        pojo.setAge(25);

        SampleService.MultipleParamsPojo resultPojo = sampleService.multipleParamsMethod(10, "hello", customer, pojo);
        assertThat(resultPojo).isNotNull();
        assertThat(resultPojo.number()).isEqualTo(10);
        assertThat(resultPojo.str()).isEqualTo("hello");
        assertThat(resultPojo.entity()).isEqualTo(customer);
        assertThat(resultPojo.pojo()).isEqualTo(pojo);
    }
}
