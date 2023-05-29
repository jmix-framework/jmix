/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.core.model.fetchjoin;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

@NamePattern("%s|party")
@Table(name = "JOINTEST_SALES_PERSON")
@Entity(name = "jointest$SalesPerson")
@JmixEntity
public class SalesPerson extends StandardEntity {
    private static final long serialVersionUID = -3909350001169083443L;

    @Column(name = "SALESPERSON_NUMBER")
    protected Integer salespersonNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTY_ID")
    protected Party party;

    public void setSalespersonNumber(Integer salespersonNumber) {
        this.salespersonNumber = salespersonNumber;
    }

    public Integer getSalespersonNumber() {
        return salespersonNumber;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Party getParty() {
        return party;
    }
}
