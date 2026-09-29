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

package io.jmix.core.metamodel.datatype;

import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Interface to be implemented by beans in add-ons and applications that assign a per-property {@link Datatype}
 * instance when the metamodel is loaded, for example according to an annotation on the entity attribute.
 * <p>
 * The returned datatype becomes the datatype of the property range, so all components that format and parse
 * the attribute value through its range use it.
 * <p>
 * A datatype set explicitly by the {@link io.jmix.core.metamodel.annotation.PropertyDatatype} annotation takes
 * precedence over providers. Providers are invoked in the order defined by the
 * {@link org.springframework.core.annotation.Order} annotation with a {@link io.jmix.core.JmixOrder} value, and the
 * first non-null result is used. If all providers return null, the datatype registered for the property type in
 * {@link DatatypeRegistry} is used.
 * <p>
 * The {@link io.jmix.core.metamodel.annotation.NumberFormat} annotation is handled by the framework provider
 * {@link io.jmix.core.metamodel.datatype.impl.NumberFormatDatatypeProvider} with the
 * {@code JmixOrder.HIGHEST_PRECEDENCE + 100} order. A provider with a higher precedence can replace the datatype
 * created for this annotation.
 * <p>
 * Providers are invoked while the metadata is being built, so they must not depend on
 * {@link io.jmix.core.Metadata} or on beans that depend on it.
 */
@NullMarked
public interface AdaptiveDatatypeProvider {

    /**
     * Returns a datatype for the given property, or null if this provider does not handle the property.
     * <p>
     * The method is invoked for every property that has no explicitly assigned datatype, including reference
     * properties, so the implementation must check the property and its type before returning a datatype.
     *
     * @param metaProperty property being loaded; its annotated element and domain are available,
     *                     but its range is not assigned yet
     * @param type         Java type of the property value, or the element type for a collection property
     * @return datatype for the property, or null to let the next provider or the default datatype be used
     */
    @Nullable
    Datatype<?> getAdaptiveDatatype(MetaProperty metaProperty, Class<?> type);
}
