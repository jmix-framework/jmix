/*
 * Copyright 2025 Haulmont.
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

package test_support.service;

import io.jmix.core.FileRef;
import io.jmix.restds.annotation.RemoteService;
import test_support.entity.ContactType;
import test_support.entity.Customer;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RemoteService(store = "restService1")
public interface SampleService {

    void voidMethod();

    String stringMethod(String param);

    boolean booleanMethod(boolean param);

    Boolean booleanWrapperMethod(Boolean param);

    int intMethod(int param);

    Integer intWrapperMethod(Integer param);

    byte[] binaryMethod(byte[] param);

    FileRef fileRefMethod(FileRef param);

    Date dateMethod(Date param);

    LocalDateTime localDateTimeMethod(LocalDateTime param);

    OffsetDateTime offsetDateTimeMethod(OffsetDateTime param);

    UUID uuidMethod(UUID param);

    URI uriMethod(URI param);

    Customer entityMethod(Customer param);

    List<Customer> entityCollectionMethod(List<Customer> param);

    ContactType enumMethod(ContactType param);

    SamplePojo pojoMethod(SamplePojo param);

    SamplePojoWithEntity pojoWithEntityMethod(SamplePojoWithEntity param);

    SampleRecord recordMethod(SampleRecord param);

    SampleRecordWithEntity recordWithEntityMethod(SampleRecordWithEntity param);

    MultipleParamsPojo multipleParamsMethod(int number, String str, Customer entity, SamplePojo pojo);

    record MultipleParamsPojo(int number, String str, Customer entity, SamplePojo pojo) {}

    class SamplePojo {
        private String name;
        private int age;
        private FileRef fileRef;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public FileRef getFileRef() {
            return fileRef;
        }

        public void setFileRef(FileRef fileRef) {
            this.fileRef = fileRef;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SamplePojo that)) return false;
            return getAge() == that.getAge() && Objects.equals(getName(), that.getName()) && Objects.equals(getFileRef(), that.getFileRef());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getAge(), getFileRef());
        }
    }

    class SamplePojoWithEntity {
        private String name;
        private Customer customer;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Customer getCustomer() {
            return customer;
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SamplePojoWithEntity that)) return false;
            return Objects.equals(getName(), that.getName()) && Objects.equals(getCustomer(), that.getCustomer());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getCustomer());
        }
    }

    record SampleRecord(String name, int age) {}

    record SampleRecordWithEntity(String name, Customer customer) {}
}