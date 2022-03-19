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

import com.haulmont.cuba.gui.components.LookupScreenFacet;
import com.haulmont.cuba.web.gui.components.WebLookupScreenFacet;
import io.jmix.ui.facet.LookupScreenFacetProvider;
import org.springframework.stereotype.Component;

@Component("cuba_LookupScreenFacetProvider")
public class CubaLookupScreenFacetProvider extends LookupScreenFacetProvider {

    @Override
    public Class getFacetClass() {
        return LookupScreenFacet.class;
    }

    @Override
    public LookupScreenFacet create() {
        WebLookupScreenFacet lookupScreenFacet = new WebLookupScreenFacet();
        lookupScreenFacet.setApplicationContext(applicationContext);
        return lookupScreenFacet;
    }
}
