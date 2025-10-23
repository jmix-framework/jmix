/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.searching.impl;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.search.searching.SearchSecurityDecorator;
import io.jmix.search.utils.Constants;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Class that encapsulates logic that substitutes an initial field name with the specific subfields.
 * Calculating such subfields depends on the field type and user permissions for the field.
 */
@Component("search_FullFieldNamesProvider")
public class FullFieldNamesProvider {

    protected final SearchSecurityDecorator securityDecorator;

    public FullFieldNamesProvider(SearchSecurityDecorator securityDecorator) {
        this.securityDecorator = securityDecorator;
    }

    /**
     * Returns the fields to be substituted for a given property path and field name. The substitution is based
     * on the field's type and the user's permissions.
     *
     * @param path {@link MetaPropertyPath} representing the relative path to the property
     * @param fieldName base name of the field to be evaluated
     * @return set of field names to be substituted based on the field type and user permissions
     */
    public Set<String> getFieldNamesForBaseField(MetaPropertyPath path, String fieldName) {
        Range range = path.getRange();
        if (isFileRefProperty(range)) {
            return Set.of(fieldName + "._file_name", fieldName + "._content");
        } else if (isReferenceProperty(range)) {
            if (securityDecorator.isEntityReadPermitted(range.asClass())) {
                return Set.of(fieldName + "." + Constants.INSTANCE_NAME_FIELD);
            } else {
                return emptySet();
            }
        }
        return Set.of(fieldName);
    }

    protected boolean isFileRefProperty(Range range) {
        if (range.isDatatype()) {
            Datatype<?> datatype = range.asDatatype();
            return datatype instanceof FileRefDatatype;
        } else {
            return false;
        }
    }

    protected boolean isReferenceProperty(Range range) {
        return range.isClass();
    }
}
