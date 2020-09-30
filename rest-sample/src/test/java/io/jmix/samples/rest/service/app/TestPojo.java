/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.service.app;

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
