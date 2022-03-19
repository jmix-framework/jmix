/*
 * Copyright 2020 Haulmont.
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

package io.jmix.samples.rest.service;

import java.io.Serializable;
import java.util.Date;

public class TestPojo implements Serializable {
    private String field1;

    private TestNestedPojo nestedPojo;

    private Date dateField;

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public TestNestedPojo getNestedPojo() {
        return nestedPojo;
    }

    public void setNestedPojo(TestNestedPojo nestedPojo) {
        this.nestedPojo = nestedPojo;
    }

    public Date getDateField() {
        return dateField;
    }

    public void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    public static class TestNestedPojo implements Serializable {
        private int nestedField;

        public int getNestedField() {
            return nestedField;
        }

        public void setNestedField(int nestedField) {
            this.nestedField = nestedField;
        }
    }
}
