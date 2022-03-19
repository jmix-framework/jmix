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

package com.haulmont.cuba.web.gui.facets;

import com.haulmont.cuba.gui.components.ScreenFacet;
import com.haulmont.cuba.web.gui.components.WebScreenFacet;
import io.jmix.ui.facet.ScreenFacetProvider;
import org.springframework.stereotype.Component;

@Component("cuba_ScreenFacetProvider")
public class CubaScreenFacetProvider extends ScreenFacetProvider {

    @Override
    public Class getFacetClass() {
        return ScreenFacet.class;
    }

    @Override
    public ScreenFacet create() {
        WebScreenFacet screenFacet = new WebScreenFacet();
        screenFacet.setApplicationContext(applicationContext);
        return screenFacet;
    }
}
