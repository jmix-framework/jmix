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
package test_support.entity.entity_extension;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@JmixEntity
@Entity(name = "exttest_Doc")
@Table(name = "EXTTEST_DOC")
@DiscriminatorValue("100")
@PrimaryKeyJoinColumn(name = "CARD_ID", referencedColumnName = "ID")
public class Doc extends Card {

    private static final long serialVersionUID = -3158618141974583321L;

    @Column(name = "DOC_NUMBER", length = 50)
    protected String number = "";

    @Column(name = "AMOUNT")
    protected BigDecimal amount;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "doc")
    @Composition
    protected List<Plant> plants;

    @OneToOne(mappedBy = "doc")
    protected Waybill waybill;

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

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public Waybill getWaybill() {
        return waybill;
    }

    public void setWaybill(Waybill waybill) {
        this.waybill = waybill;
    }
}
