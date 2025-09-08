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

package io.jmix.samples.restservice.app;

import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.rest.annotation.RestMethod;
import io.jmix.rest.annotation.RestService;
import io.jmix.samples.restservice.entity.ContactType;
import io.jmix.samples.restservice.entity.Customer;
import io.jmix.samples.restservice.entity.CustomerContact;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@RestService("SampleService")
public class SampleService {

    private final DataManager dataManager;

    public SampleService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @RestMethod
    public void voidMethod() {
    }

    @RestMethod
    public String stringMethod(String param) {
        return param;
    }

    @RestMethod
    public boolean booleanMethod(boolean param) {
        return param;
    }

    @RestMethod
    public Boolean booleanWrapperMethod(Boolean param) {
        return true;
    }

    @RestMethod
    public int intMethod(int param) {
        return param;
    }

    @RestMethod
    public Integer intWrapperMethod(Integer param) {
        return param;
    }

    @RestMethod
    public byte[] binaryMethod(byte[] param) {
        return param;
    }

    @RestMethod
    public String[] stringArrayMethod(String[] param) {
        return param;
    }

    @RestMethod
    public FileRef fileRefMethod(FileRef param) {
        return param;
    }

    @RestMethod
    public Date dateMethod(Date param) {
        return param;
    }

    @RestMethod
    public LocalDateTime localDateTimeMethod(LocalDateTime param) {
        return param;
    }

    @RestMethod
    public OffsetDateTime offsetDateTimeMethod(OffsetDateTime param) {
        return param;
    }

    @RestMethod
    public UUID uuidMethod(UUID param) {
        return param;
    }

    @RestMethod
    public URI uriMethod(URI param) {
        return param;
    }

    @RestMethod
    public Customer entityMethod(Customer param) {
        return param;
    }

    @RestMethod
    public List<Customer> entityListMethod(List<Customer> param) {
        for (Customer customer : param) {
            assert customer.getId() != null;
        }
        return param;
    }

    @RestMethod
    public Set<Customer> entitySetMethod(Set<Customer> param) {
        for (Customer customer : param) {
            assert customer.getId() != null;
        }
        return param;
    }

    @RestMethod
    public Map<String, CustomerContact> entityMapMethod(Map<String, CustomerContact> param) {
        for (Map.Entry<String, CustomerContact> entry : param.entrySet()) {
            UUID id = entry.getValue().getId();
            assert id != null;
        }
        return param;
    }

    @RestMethod
    public ContactType enumMethod(ContactType param) {
        return param;
    }

    @RestMethod
    public SamplePojo pojoMethod(SamplePojo param) {
        return param;
    }

    @RestMethod
    public SamplePojoWithEntity pojoWithEntityMethod(SamplePojoWithEntity param) {
        return param;
    }

    @RestMethod
    public SampleRecord recordMethod(SampleRecord param) {
        return param;
    }

    @RestMethod
    public SampleRecordWithEntity recordWithEntityMethod(SampleRecordWithEntity param) {
        return param;
    }

    @RestMethod
    public void booleanParamMethod(boolean param) {
    }

    @RestMethod
    public void booleanWrapperParamMethod(Boolean param) {
    }

    @RestMethod
    public void intParamMethod(int param) {
    }

    @RestMethod
    public void intWrapperParamMethod(Integer param) {
    }

    @RestMethod
    public void stringParamMethod(String param) {
    }

    @RestMethod
    public MultipleParamsPojo multipleParamsMethod(int number, String str, Customer entity, SamplePojo pojo) {
        return new MultipleParamsPojo(number, str, entity, pojo);
    }

    @RestMethod
    public List<SamplePojoWithEntity> pojoWithEntityListMethod(List<SamplePojoWithEntity> param) {
        for (SamplePojoWithEntity pojo : param) {
            assert pojo.getName() != null;
            if (pojo.getCustomer() != null) {
                assert pojo.getCustomer().getId() != null;
            }
        }
        return param;
    }

    public record MultipleParamsPojo(int number, String str, Customer entity, SamplePojo pojo) {}

    public static class SamplePojo {
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

    public static class SamplePojoWithEntity {
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

    public record SampleRecord(String name, int age) {}

    public record SampleRecordWithEntity(String name, Customer customer) {}
}