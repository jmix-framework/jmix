/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dataimport.impl;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.dataimport.configuration.DuplicateEntityPolicy;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.configuration.mapping.PropertyMapping;
import io.jmix.dataimport.configuration.mapping.ReferenceImportPolicy;
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping;
import io.jmix.dataimport.exception.ImportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("datimp_ImportConfigurationValidator")
public class ImportConfigurationValidator {
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Metadata metadata;

    public void validate(ImportConfiguration importConfiguration) throws ImportException {
        MetaClass entityMetaClass = metadata.getClass(importConfiguration.getEntityClass());
        boolean configWithUpdatePolicyExists = importConfiguration.getUniqueEntityConfigurations().stream()
                .anyMatch(uniqueEntityConfiguration -> uniqueEntityConfiguration.getDuplicateEntityPolicy() == DuplicateEntityPolicy.UPDATE);
        validatePropertyMappings(entityMetaClass, importConfiguration.getPropertyMappings(), configWithUpdatePolicyExists);
    }

    protected void validatePropertyMappings(MetaClass ownerEntity, List<PropertyMapping> propertyMappings, boolean uniqueConfigWithUpdatePolicyExists) {
        propertyMappings.forEach(propertyMapping -> {
            if (propertyMapping instanceof ReferenceMultiFieldPropertyMapping) {
                validateMultiFieldMapping(ownerEntity, (ReferenceMultiFieldPropertyMapping) propertyMapping, uniqueConfigWithUpdatePolicyExists);
            }
        });
    }

    protected void validateMultiFieldMapping(MetaClass ownerEntity, ReferenceMultiFieldPropertyMapping propertyMapping, boolean uniqueConfigWithUpdatePolicyExists) {
        MetaProperty referenceProperty = ownerEntity.getProperty(propertyMapping.getEntityPropertyName());
        if (metadataTools.isEmbedded(referenceProperty) && propertyMapping.getReferenceImportPolicy() != ReferenceImportPolicy.CREATE) {
            throw new ImportException(String.format("Incorrect policy [%s] for embedded reference [%s]. Only CREATE policy supported.", propertyMapping.getReferenceImportPolicy(),
                    propertyMapping.getEntityPropertyName()));
        }

        boolean isOneToMany = referenceProperty.getRange().getCardinality() == Range.Cardinality.ONE_TO_MANY;
        if (isOneToMany && uniqueConfigWithUpdatePolicyExists) {
            throw new ImportException(String.format("UPDATE policy for duplicates is not supported if there is a mapping for one-to-many property. Reference property: [%s]",
                    propertyMapping.getEntityPropertyName()));
        }
        if (isOneToMany && propertyMapping.getReferenceImportPolicy() != ReferenceImportPolicy.CREATE) {
            throw new ImportException(String.format("Incorrect policy [%s] for one-to-many reference [%s]. Only CREATE policy supported.", propertyMapping.getReferenceImportPolicy(),
                    propertyMapping.getEntityPropertyName()));
        }

        validatePropertyMappings(referenceProperty.getRange().asClass(), propertyMapping.getReferencePropertyMappings(), uniqueConfigWithUpdatePolicyExists);
    }
}
