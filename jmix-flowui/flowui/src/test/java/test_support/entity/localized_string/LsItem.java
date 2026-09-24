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

package test_support.entity.localized_string;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.LocalizedString;
import io.jmix.core.metamodel.annotation.PropertyDatatype;
import test_support.localized_string.StrictCodeDatatype;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@JmixEntity(name = "test_LsItem")
public class LsItem {

    @JmixId
    @JmixGeneratedValue
    private UUID id;

    /**
     * The instance name, so that a reference to the item is sorted by a localized text.
     */
    @InstanceName
    @LocalizedString
    private String name;

    /**
     * A LOB property: the database side leaves it to the standard sort expression, so the in-memory sorter
     * must leave it to the standard comparator too.
     */
    @LocalizedString
    @Lob
    private String description;

    /**
     * Carries a length limit that only bean validation declares.
     */
    @LocalizedString
    @Size(max = 12)
    private String shortName;

    /**
     * Carries a column length stricter than its bean validation limit. The metamodel reads the column length of a
     * DTO property too, so the property needs no table.
     */
    @LocalizedString
    @Column(length = 20)
    @Size(max = 30)
    private String title;

    /**
     * Carries a bean validation limit, declared through {@code @Length}, stricter than its column length.
     */
    @LocalizedString
    @Column(length = 30)
    @Length(max = 15)
    private String label;

    private String code;

    /**
     * A string of another datatype derived from the standard string one, which filters must keep treating as its
     * own datatype.
     */
    @PropertyDatatype(StrictCodeDatatype.ID)
    private String strictCode;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStrictCode() {
        return strictCode;
    }

    public void setStrictCode(String strictCode) {
        this.strictCode = strictCode;
    }
}
