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

package test_support.entity.sales;

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Used by {@code data_components.DataContextInvariantFuzzTest} (embedded in {@link Customer} and
 * part of each scenario's persisted baseline), whose deterministic regression baseline depends on
 * this entity's persistent attribute set. Adding, removing, or renaming attributes here shifts the
 * fuzz violation counts and can break the gate. If such a change is unavoidable, re-run the fuzz
 * test with {@code -PincludeSlowTests=true} and update the baseline in
 * {@code docs/features/data-context/tests/coverage.md}.
 */
@Embeddable
@JmixEntity(name = "test_Address")
public class Address {

    private static final long serialVersionUID = 2116285103670251834L;

    @Column(name = "CITY")
    protected String city;

    @Column(name = "ZIP", length = 10)
    protected String zip;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
