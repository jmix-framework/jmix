/*
 * Copyright 2019 Haulmont.
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

package io.jmix.samples.rest.entity.driver;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity(name = "ref$Doc")
@JmixEntity
@Table(name = "REF_DOC")
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

    @InstanceName
    public String getCaption() {
        return getDescription();
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

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }
}
