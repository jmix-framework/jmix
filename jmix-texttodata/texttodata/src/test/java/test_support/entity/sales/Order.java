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

package test_support.entity.sales;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Comment;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import test_support.entity.TestPlainStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity(name = "textdt_Order")
@Table(name = "TEXTDT_ORDER")
@JmixEntity
public class Order {

    @Id
    @JmixGeneratedValue
    @Column(name = "ID")
    private UUID id;

    @Version
    @Column(name = "VERSION")
    private Integer version;

    @Column(name = "NUMBER_", nullable = false)
    @Comment("Business order number")
    private String number;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "ORDER_DATE")
    private LocalDate orderDate;

    @Column(name = "ACTIVE")
    private Boolean active;

    @Column(name = "STATUS")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "PLAIN_STATUS")
    private TestPlainStatus plainStatus;

    @Embedded
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "APPROVAL_ID", nullable = false)
    private OrderApproval approval;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order")
    private OrderShipment shipment;

    @Composition
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order")
    private OrderDetail detail;

    @Composition
    @OneToMany(mappedBy = "order")
    private List<OrderLine> lines;

    @ManyToMany
    @JoinTable(name = "TEXTDT_ORDER_TAG",
            joinColumns = @JoinColumn(name = "ORDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID"))
    private List<Tag> tags;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TestPlainStatus getPlainStatus() {
        return plainStatus;
    }

    public void setPlainStatus(TestPlainStatus plainStatus) {
        this.plainStatus = plainStatus;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public OrderApproval getApproval() {
        return approval;
    }

    public void setApproval(OrderApproval approval) {
        this.approval = approval;
    }

    public OrderShipment getShipment() {
        return shipment;
    }

    public void setShipment(OrderShipment shipment) {
        this.shipment = shipment;
    }

    public OrderDetail getDetail() {
        return detail;
    }

    public void setDetail(OrderDetail detail) {
        this.detail = detail;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
