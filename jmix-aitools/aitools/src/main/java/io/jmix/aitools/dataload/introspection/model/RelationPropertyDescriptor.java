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

package io.jmix.aitools.dataload.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Descriptor of an association or composition (relation) entity property.
 */
public class RelationPropertyDescriptor extends AbstractEntityPropertyDescriptor {

    @Nullable
    protected String mappedBy;

    protected String targetEntityName;

    protected List<String> targetEntityLocalizedNames;

    @Nullable
    protected Boolean optionalRelation;

    protected String cardinality;

    public RelationPropertyDescriptor(String name,
                                      List<String> localizedNames,
                                      String javaType,
                                      String propertyType,
                                      @Nullable Boolean identifier,
                                      Boolean persistent,
                                      Boolean mandatory,
                                      @Nullable String comment,
                                      @Nullable String mappedBy,
                                      String targetEntityName,
                                      List<String> targetEntityLocalizedNames,
                                      @Nullable Boolean optionalRelation,
                                      String cardinality) {
        super(name, localizedNames, javaType, propertyType, identifier, persistent, mandatory, comment);
        this.mappedBy = mappedBy;
        this.targetEntityName = targetEntityName;
        this.targetEntityLocalizedNames = targetEntityLocalizedNames;
        this.optionalRelation = optionalRelation;
        this.cardinality = cardinality;
    }

    /**
     * Returns the name of the inverse property that owns this relation.
     *
     * @return {@code mappedBy} property name, or {@code null} if this side owns the relation
     */
    @Nullable
    public String getMappedBy() {
        return mappedBy;
    }

    /**
     * Returns the name of the entity this relation points to.
     *
     * @return target entity name
     */
    public String getTargetEntityName() {
        return targetEntityName;
    }

    /**
     * Returns the captions of the target entity for the configured locales.
     *
     * @return localized target entity names
     */
    public List<String> getTargetEntityLocalizedNames() {
        return targetEntityLocalizedNames;
    }

    /**
     * Returns whether the relation is optional.
     *
     * @return {@code false} if the relation is required, or {@code null} if it is optional
     */
    @Nullable
    public Boolean getOptionalRelation() {
        return optionalRelation;
    }

    /**
     * Returns the relation cardinality (for example {@code "ONE_TO_MANY"}).
     *
     * @return cardinality name
     */
    public String getCardinality() {
        return cardinality;
    }

    @Override
    protected String fieldsToString() {
        return super.fieldsToString() +
                ", mappedBy='" + mappedBy + "'" +
                ", targetEntityName='" + targetEntityName + "'" +
                ", targetEntityLocalizedNames=" + targetEntityLocalizedNames +
                ", optionalRelation=" + optionalRelation +
                ", cardinality='" + cardinality + "'";
    }
}
