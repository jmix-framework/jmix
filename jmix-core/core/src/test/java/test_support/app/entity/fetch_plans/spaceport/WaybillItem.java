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

package test_support.app.entity.fetch_plans.spaceport;

import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.apache.commons.lang3.LocaleUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Table(name = "ST_WAYBILL_ITEM")
@javax.persistence.Entity(name = "st_WaybillItem")
@JmixEntity
public class WaybillItem {
    private static final long serialVersionUID = 5178883424732340774L;

    @JmixProperty(mandatory = true)
    @JmixGeneratedValue
    @Id
    private UUID id;

    @Column(name = "NUMBER_")
    private Integer number;

    @Column(name = "NAME", unique = true)
    private String name;

    @Column(name = "WEIGHT")
    private Double weight;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    @AttributeOverrides({
            @AttributeOverride(name = "length", column = @Column(name = "DIM_LENGTH")),
            @AttributeOverride(name = "width", column = @Column(name = "DIM_WIDTH")),
            @AttributeOverride(name = "height", column = @Column(name = "DIM_HEIGHT"))
    })
    private Dimensions dim;

    @Column(name = "CHARGE")
    private BigDecimal charge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WAYBILL_ID")
    private Waybill waybill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WAYBILL_CATEGORY_ID")
    private WaybillCategory waybillCategory;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Waybill getWaybill() {
        return waybill;
    }

    public void setWaybill(Waybill waybill) {
        this.waybill = waybill;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public Dimensions getDim() {
        return dim;
    }

    public void setDim(Dimensions dim) {
        this.dim = dim;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public WaybillCategory getWaybillCategory() {
        return waybillCategory;
    }

    public void setWaybillCategory(WaybillCategory waybillCategory) {
        this.waybillCategory = waybillCategory;
    }

    @InstanceName
    @DependsOnProperties({"waybillCategory", "number"})
    public String getCaption() {
        return "Item: " + number + ", category: " + (waybillCategory == null ? "" : waybillCategory.getName());
    }
}