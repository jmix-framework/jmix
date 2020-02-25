/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.entity.multidb;

import io.jmix.core.entity.BaseDbGeneratedIdEntity;
import io.jmix.core.metamodel.annotations.MetaProperty;
import io.jmix.core.metamodel.annotations.Store;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity(name = "test_Db1Order")
@Table(name = "ORDER_")
@Store(name = "db1")
public class Db1Order extends BaseDbGeneratedIdEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE/*, generator = "ref$Db1Order"*/)
//    @SequenceGenerator(name = "ref$Db1Order", sequenceName = "order_sequence", allocationSize = 1)
    @Column(name = "ID")
    protected Long id;

    @Column(name = "ORDER_DATE")
    private Date orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Db1Customer customer;

    @Transient
    @MetaProperty(related = "mem1CustomerId")
    private Mem1Customer mem1Customer;

    @Column(name = "MEM_CUST_ID")
    private UUID mem1CustomerId;

//    @Transient
//    @MetaProperty(related = "ikCustomerId")
//    private IdentityCustomer ikCustomer;
//
//    @Column(name = "IK_CUST_ID")
//    private Long ikCustomerId;
//
//    @Transient
//    @MetaProperty(related = "ikOrderId")
//    private IdentityOrder ikOrder;
//
//    @Column(name = "IK_ORDER_ID")
//    private Long ikOrderId;
//
//    @Transient
//    @MetaProperty(related = "ckCustomerId")
//    private CompKeyCustomer ckCustomer;
//
//    @Embedded
//    @AttributeOverrides({
//            @AttributeOverride(name = "tenantId", column = @Column(name = "CK_CUSTOMER_TENANT_ID")),
//            @AttributeOverride(name = "entityId", column = @Column(name = "CK_CUSTOMER_ENTITY_ID"))
//    })
//    private CompKey ckCustomerId;

    @Override
    protected void setDbGeneratedId(Long dbId) {
        this.id = dbId;
    }

    @Override
    protected Long getDbGeneratedId() {
        return id;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Db1Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Db1Customer customer) {
        this.customer = customer;
    }

    public Mem1Customer getMem1Customer() {
        return mem1Customer;
    }

    public void setMem1Customer(Mem1Customer mem1Customer) {
        this.mem1Customer = mem1Customer;
    }

    public UUID getMem1CustomerId() {
        return mem1CustomerId;
    }

    public void setMem1CustomerId(UUID mem1CustomerId) {
        this.mem1CustomerId = mem1CustomerId;
    }

//    public IdentityCustomer getIkCustomer() {
//        return ikCustomer;
//    }
//
//    public void setIkCustomer(IdentityCustomer ikCustomer) {
//        this.ikCustomer = ikCustomer;
//    }
//
//    public Long getIkCustomerId() {
//        return ikCustomerId;
//    }
//
//    public void setIkCustomerId(Long ikCustomerId) {
//        this.ikCustomerId = ikCustomerId;
//    }
//
//    public IdentityOrder getIkOrder() {
//        return ikOrder;
//    }
//
//    public void setIkOrder(IdentityOrder ikOrder) {
//        this.ikOrder = ikOrder;
//    }
//
//    public Long getIkOrderId() {
//        return ikOrderId;
//    }
//
//    public void setIkOrderId(Long ikOrderId) {
//        this.ikOrderId = ikOrderId;
//    }
//
//    public CompKeyCustomer getCkCustomer() {
//        return ckCustomer;
//    }
//
//    public void setCkCustomer(CompKeyCustomer ckCustomer) {
//        this.ckCustomer = ckCustomer;
//    }
//
//    public CompKey getCkCustomerId() {
//        return ckCustomerId;
//    }
//
//    public void setCkCustomerId(CompKey ckCustomerId) {
//        this.ckCustomerId = ckCustomerId;
//    }
}
