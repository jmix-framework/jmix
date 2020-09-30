/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.service.app;

import io.jmix.samples.rest.entity.driver.Car;

import java.io.Serializable;

/**
 *
 */
public class PojoWithNestedEntity implements Serializable {

    public PojoWithNestedEntity(Car car, int intField) {
        this.car = car;
        this.intField = intField;
    }

    private Car car;

    private int intField;

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }
}
