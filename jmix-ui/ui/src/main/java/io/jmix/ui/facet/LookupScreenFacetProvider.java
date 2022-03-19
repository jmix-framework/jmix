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

import io.jmix.ui.component.impl.LookupScreenFacetImpl;
import org.springframework.context.ApplicationContext;
import io.jmix.core.Metadata;
import io.jmix.ui.component.LookupScreenFacet;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component("ui_LookupScreenFacetProvider")
public class LookupScreenFacetProvider
        extends AbstractEntityAwareScreenFacetProvider<LookupScreenFacet> {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public Class<LookupScreenFacet> getFacetClass() {
        return LookupScreenFacet.class;
    }

    @Override
    public LookupScreenFacet create() {
        LookupScreenFacetImpl lookupScreenFacet = new LookupScreenFacetImpl();
        lookupScreenFacet.setApplicationContext(applicationContext);
        return lookupScreenFacet;
    }

    @Override
    public String getFacetTag() {
        return "lookupScreen";
    }

    @Override
    protected Metadata getMetadata() {
        return metadata;
    }
}
