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

package test_support.app.entity.adaptive_datatype;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.NumberFormat;

import java.math.BigDecimal;
import java.util.UUID;

@JmixEntity(name = "app_AdaptiveDatatypeEntity")
public class AdaptiveDatatypeEntity {

    @JmixId
    @JmixGeneratedValue
    private UUID id;

    @TestDisplayPrefix("code:")
    private String code;

    private String plain;

    @NumberFormat(pattern = "#,##0.00", decimalSeparator = ".", groupingSeparator = ",")
    @TestDisplayPrefix("amount:")
    private BigDecimal amount;

    @NumberFormat(pattern = "#,##0.00", decimalSeparator = ".", groupingSeparator = ",")
    private BigDecimal overriddenAmount;

    @TestDisplayPrefix("low:")
    private String contested;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPlain() {
        return plain;
    }

    public void setPlain(String plain) {
        this.plain = plain;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getOverriddenAmount() {
        return overriddenAmount;
    }

    public void setOverriddenAmount(BigDecimal overriddenAmount) {
        this.overriddenAmount = overriddenAmount;
    }

    public String getContested() {
        return contested;
    }

    public void setContested(String contested) {
        this.contested = contested;
    }

    @JmixProperty
    @TestDisplayPrefix("summary:")
    public String getSummary() {
        return code + " " + plain;
    }
}
