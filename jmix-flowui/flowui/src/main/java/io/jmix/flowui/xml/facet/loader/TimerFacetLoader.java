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

package io.jmix.flowui.xml.facet.loader;

import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.impl.FacetsImpl;
import io.jmix.flowui.xml.facet.FacetProvider;
import org.dom4j.Element;

import java.util.Objects;

public class TimerFacetLoader extends AbstractFacetLoader<Timer> {

    @Override
    protected Timer createFacet() {
        return facets.create(Timer.class);
    }

    @Override
    public void loadFacet() {
        // for backward compatibility, should be removed in future releases
        if (facets instanceof FacetsImpl facetsImpl) {
            FacetProvider<Timer> provider = facetsImpl.getProvider(Timer.class);

            if (provider != null) {
                provider.loadFromXml(resultFacet, element, context);
                return;
            }
        }

        loadId(element);
        loadDelay(element);

        loaderSupport.loadBoolean(element, "repeating", resultFacet::setRepeating);
        loaderSupport.loadBoolean(element, "autostart", resultFacet::setAutostart);
    }

    protected void loadId(Element element) {
        String id = loaderSupport.loadString(element, "id")
                .orElseThrow(() -> new IllegalStateException("Timer id must be defined"));
        resultFacet.setId(id);
    }

    protected void loadDelay(Element element) {
        int delay = loaderSupport.loadInteger(element, "delay")
                .orElse(-1);
        if (delay <= 0) {
            throw new GuiDevelopmentException("Timer 'delay' must be greater than 0", context, "Timer ID",
                    Objects.requireNonNull(resultFacet.getId()));
        }

        resultFacet.setDelay(delay);
    }
}
