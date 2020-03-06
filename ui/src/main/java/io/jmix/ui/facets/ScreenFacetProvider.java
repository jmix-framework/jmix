/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.facets;

import io.jmix.core.BeanLocator;
import io.jmix.ui.components.ScreenFacet;
import io.jmix.ui.components.impl.WebScreenFacet;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component(ScreenFacetProvider.NAME)
public class ScreenFacetProvider extends AbstractScreenFacetProvider<ScreenFacet> {

    public static final String NAME = "jmix_ScreenFacetProvider";

    @Inject
    protected BeanLocator beanLocator;

    @Override
    public Class<ScreenFacet> getFacetClass() {
        return ScreenFacet.class;
    }

    @Override
    public ScreenFacet create() {
        WebScreenFacet screenFacet = new WebScreenFacet();
        screenFacet.setBeanLocator(beanLocator);
        return screenFacet;
    }

    @Override
    public String getFacetTag() {
        return "screen";
    }
}
