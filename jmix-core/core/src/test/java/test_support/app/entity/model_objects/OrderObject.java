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

package test_support.app.entity.model_objects;

import io.jmix.core.metamodel.annotation.JmixEntity;

import java.time.LocalDate;
import java.util.List;

@JmixEntity(name = "test_OrderObject")
public class OrderObject {

    private LocalDate date;

    private String number;

    private String orderState;

    private CustomerObject customer;

    private List<OrderLineObject> lines;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CustomerObject getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerObject customer) {
        this.customer = customer;
    }

    public List<OrderLineObject> getLines() {
        return lines;
    }

    public void setLines(List<OrderLineObject> lines) {
        this.lines = lines;
    }

    public OrderState getOrderState() {
        return orderState == null ? null : OrderState.fromId(orderState);
    }

    public void setOrderState(OrderState orderState) {
        this.orderState = orderState == null ? null : orderState.getId();
    }
}
