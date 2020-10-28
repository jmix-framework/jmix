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

package io.jmix.ui.facet;

import org.springframework.context.ApplicationContext;
import io.jmix.ui.component.ScreenFacet;
import io.jmix.ui.component.impl.ScreenFacetImpl;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component("ui_ScreenFacetProvider")
public class ScreenFacetProvider extends AbstractScreenFacetProvider<ScreenFacet> {

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public Class<ScreenFacet> getFacetClass() {
        return ScreenFacet.class;
    }

    @Override
    public ScreenFacet create() {
        ScreenFacetImpl screenFacet = new ScreenFacetImpl();
        screenFacet.setApplicationContext(applicationContext);
        return screenFacet;
    }

    @Override
    public String getFacetTag() {
        return "screen";
    }
}
