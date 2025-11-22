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

package io.jmix.flowui.sys.registration;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.xml.facet.loader.FacetLoader;
import org.springframework.lang.Nullable;

/**
 * Default implementation of {@link FacetRegistration}.
 */
@Internal
public class FacetRegistrationImpl implements FacetRegistration {

    protected final Class<? extends Facet> facet;
    protected final Class<? extends Facet> replacedFacet;
    protected final String tag;
    protected final Class<? extends FacetLoader<?>> facetLoader;

    public FacetRegistrationImpl(Class<? extends Facet> facet,
                                 Class<? extends Facet> replacedFacet,
                                 String tag,
                                 Class<? extends FacetLoader<?>> facetLoader) {
        this.facet = facet;
        this.replacedFacet = replacedFacet;
        this.tag = tag;
        this.facetLoader = facetLoader;
    }

    @Override
    public Class<? extends Facet> getFacet() {
        return facet;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends Facet> Class<T> getReplacedFacet() {
        return (Class<T>) replacedFacet;
    }

    @Nullable
    @Override
    public String getTag() {
        return tag;
    }

    @Nullable
    @Override
    public Class<? extends FacetLoader<?>> getFacetLoader() {
        return facetLoader;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        return facet.equals(((FacetRegistrationImpl) obj).facet);
    }

    @Override
    public int hashCode() {
        return facet.hashCode();
    }

    @Override
    public String toString() {
        String replacedFacet = getReplacedFacet() == null ? "null" : getReplacedFacet().getName();
        String facetLoaderClass = getFacetLoader() == null ? "null" : getFacetLoader().getName();

        return "{\"facet\": \"" + facet + "\", "
                + "\"tag\": \"" + tag + "\", "
                + "\"replacedFacet\": \"" + replacedFacet + "\", "
                + "\"facetLoader\": \"" + facetLoaderClass + "\"}";
    }
}
