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

import io.jmix.core.JmixOrder;
import io.jmix.flowui.xml.facet.loader.*;
import io.jmix.flowui.xml.facet.loader.FacetLoader;
import org.dom4j.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for default {@link FacetLoader} implementations.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE)
@Component("flowui_DefaultFacetLoaderConfig")
public class DefaultFacetLoaderConfig implements FacetLoaderConfig {

    protected Map<String, Class<? extends FacetLoader<?>>> loaders = new ConcurrentHashMap<>();

    public DefaultFacetLoaderConfig() {
        initDefaultLoaders();
    }

    @Override
    public boolean supports(Element element) {
        return loaders.containsKey(element.getName());
    }

    @Override
    public Class<? extends FacetLoader<?>> getLoader(Element element) {
        return loaders.get(element.getName());
    }

    protected void initDefaultLoaders() {
        loaders.put("dataLoadCoordinator", ViewDataLoadCoordinatorFacetLoader.class);
        loaders.put("fragmentDataLoadCoordinator", FragmentDataLoadCoordinatorFacetLoader.class);

        loaders.put("urlQueryParameters", UrlQueryParametersFacetLoader.class);
        loaders.put("timer", TimerFacetLoader.class);

        loaders.put("settings", ViewSettingsFacetLoader.class);
        loaders.put("fragmentSettings", FragmentSettingsFacetLoader.class);
    }
}
