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

package io.jmix.flowui.view.template;

import io.jmix.core.annotation.Experimental;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Provides helper methods for view templates.
 */
@Experimental
@NullMarked
public interface ViewTemplateHelper {

    /**
     * Returns properties that should be used by a view template for the given entity.
     * <p>
     * Implementations may apply default filtering rules and then process explicit include and exclude lists.
     *
     * @param metaClass entity metadata
     * @param includeProperties property names that must be included when supported by the implementation
     * @param excludeProperties property names that must be excluded from the result
     * @return filtered entity properties in metadata order
     */
    List<MetaProperty> getProperties(MetaClass metaClass, List<String> includeProperties, List<String> excludeProperties);

    /**
     * Returns composition collection properties that should be rendered as tabs by a detail view template.
     * <p>
     * Implementations return direct composition {@code *-to-many} properties, applying default filtering
     * rules and then removing properties listed in {@code excludeProperties}. Association collections,
     * datatype collections, and single-value properties are not returned.
     *
     * @param metaClass         entity metadata
     * @param excludeProperties property names that must be excluded from the result
     * @return filtered composition collection properties in metadata order
     */
    List<MetaProperty> getCollectionProperties(MetaClass metaClass, List<String> excludeProperties);
}
