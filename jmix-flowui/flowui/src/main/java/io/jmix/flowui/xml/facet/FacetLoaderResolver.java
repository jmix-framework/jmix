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

package io.jmix.flowui.xml.facet;

import io.jmix.flowui.xml.facet.loader.FacetLoader;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Resolver class responsible for determining the appropriate {@link FacetLoader} implementation
 * for a given XML element.
 */
@Component("flowui_FacetLoaderResolver")
public class FacetLoaderResolver {

    protected List<FacetLoaderConfig> loaderConfigs = Collections.emptyList();

    @Autowired
    public void setLoaderConfigs(List<FacetLoaderConfig> loaderConfigs) {
        this.loaderConfigs = loaderConfigs;
    }

    /**
     * @param element element to load
     * @return {@link FacetLoader} instance for loading the given {@code element}
     */
    @Nullable
    public Class<? extends FacetLoader<?>> getLoader(Element element) {
        for (FacetLoaderConfig config : loaderConfigs) {
            if (config.supports(element)) {
                return config.getLoader(element);
            }
        }

        return null;
    }
}
