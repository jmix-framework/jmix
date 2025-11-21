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

package io.jmix.search.index.impl.dynattr;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Service providing support for operations related to dynamic attributes.
 */
@Component("search_DynamicAttributesSupport")
public class DynamicAttributesSupport {

    private static final Logger log = LoggerFactory.getLogger(DynamicAttributesSupport.class);
    protected final DynamicAttributesSupportDelegate proxy;

    public DynamicAttributesSupport(ObjectProvider<DynamicAttributesSupportDelegate> proxy) {
        this.proxy = proxy.getIfAvailable();
        if (this.proxy == null) {
            log.warn("Dynamic attributes support proxy is not available.");
        } else {
            log.debug("Dynamic attributes support proxy is available");
        }
    }

    /**
     * Determines if a given entity property name represents a dynamic attribute.
     *
     * @param entityPropertyFullName the full name of the entity property to check
     * @return true if the property name contains a plus sign ("+"), indicating it is a dynamic attribute; false otherwise
     */
    public boolean isDynamicAttributeName(String entityPropertyFullName) {
        return proxy != null && proxy.isDynamicAttributeName(entityPropertyFullName);
    }

    /**
     * Determines if the specified {@link MetaPropertyPath} represents a dynamic attribute.
     *
     * @param propertyPath the {@link MetaPropertyPath} to evaluate
     * @return {@code true} if the {@link MetaPropertyPath} corresponds to a dynamic attribute; {@code false} otherwise
     */
    public boolean isDynamicAttribute(MetaPropertyPath propertyPath) {
        return proxy != null && proxy.isDynamicAttributeName(propertyPath.getFirstPropertyName());
    }
}
