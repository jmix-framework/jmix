/*
 * Copyright 2022 Haulmont.
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

package test_support.entity.is_get_conflict;


import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import test_support.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;

@JmixEntity
@Entity(name = "test_GetterConflictEntity")
@Table(name = "TEST_GETTER_CONFLICT_ENTITY")
public class GetterConflictEntity extends BaseEntity {
    //test support:
    @Transient
    public boolean getCustomInvoked = false;
    @Transient
    public boolean isCustomInvoked = false;
    @Transient
    public boolean getDebitInvoked = false;
    @Transient
    public boolean isDebitInvoked = false;
    @Transient
    public boolean getOverpaymentInvoked = false;
    @Transient
    public boolean isOverpaymentInvoked = false;
    @Transient
    public boolean getCounterInvoked = false;
    @Transient
    public boolean isCounterInvoked = false;
    @Transient
    public boolean getPositiveInvoked = false;
    @Transient
    public boolean isPositiveInvoked = false;
    @Transient
    public boolean getCommissionInvoked = false;
    @Transient
    public boolean getIsCommissionInvoked = false;
    @Transient
    public boolean setIsCommissionInvoked = false;
    @Transient
    public boolean setCommissionInvoked = false;


    //Boolean property
    //property getter:"get";
    // "is" - just for convenience/interface implementation (e.g. io.jmix.reports.entity.ReportTemplate)
    @Column(name = "FIRST")
    private Boolean custom;

    public Boolean getCustom() {
        getCustomInvoked = true;
        return custom;
    }

    public boolean isCustom() {
        isCustomInvoked = true;
        return Boolean.TRUE.equals(custom);
    }

    public void setCustom(Boolean custom) {
        this.custom = custom;
    }


    //Not Boolean property
    //property getter:"get" - as usual
    //"is" - just calculated value not included to metadata
    @Column(name = "DEBIT")
    private BigDecimal debit;

    public BigDecimal getDebit() {
        getDebitInvoked = true;
        return debit;
    }

    public boolean isDebit() {
        isDebitInvoked = true;
        return debit.doubleValue() > 0;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }


    //Boolean property
    //property getter:"is"
    //"get" - calculated value
    @Column(name = "OVERPAYMENT")
    private Boolean overpayment;

    private BigDecimal amount;

    public Boolean isOverpayment() {
        isOverpaymentInvoked = true;
        return overpayment;
    }

    public BigDecimal getOverpayment() {
        getOverpaymentInvoked = true;
        return Boolean.TRUE.equals(overpayment) ? amount : BigDecimal.ZERO;
    }

    public BigDecimal getUnderpayment() {
        return Boolean.FALSE.equals(overpayment) ? amount : BigDecimal.ZERO;
    }

    public void setOverpayment(Boolean overpayment) {
        this.overpayment = overpayment;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    //Method-based attribute and just field with getter and setter
    //Not Boolean property
    //property getter:"is"
    //"get" - just getter for transient field. There is no BigDecimal attribute "counter" in this entity
    @Transient
    public BigDecimal counter;

    public BigDecimal getCounter() {
        getCounterInvoked = true;
        return counter;
    }

    public void setCounter(BigDecimal counter) {
        this.counter = counter;
    }

    @JmixProperty
    public Boolean isCounter() {
        isCounterInvoked = true;
        return counter != null;
    }


    //Method-based attribute and just boolean field with getter and setter
    //Boolean property
    //property getter:"get"
    //"is" - just getter for field
    private Boolean positive;

    @Transient
    protected int value = 1;

    public Boolean isPositive() {
        isPositiveInvoked = true;
        return positive;
    }

    public void setPositive(Boolean positive) {
        this.positive = positive;
    }

    @JmixProperty
    public Integer getPositive() {
        getPositiveInvoked = true;
        return Boolean.TRUE.equals(positive) ? value : 0;
    }

    @Column(name = "COMMISSION")
    private String commission;

    @Column(name = "IS_COMMISSION")
    private String isCommission;

    public String getCommission() {
        getCommissionInvoked = true;
        return commission;
    }

    public void setCommission(String commission) {
        setCommissionInvoked = true;
        this.commission = commission;
    }

    public String getIsCommission() {
        getIsCommissionInvoked = true;
        return isCommission;
    }

    public void setIsCommission(String isCommission) {
        setIsCommissionInvoked = true;
        this.isCommission = isCommission;
    }

}
