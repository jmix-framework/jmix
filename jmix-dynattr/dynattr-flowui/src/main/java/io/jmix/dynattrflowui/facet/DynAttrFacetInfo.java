/*
 * Copyright 2022 Haulmont.
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

package io.jmix.dynattrflowui.facet;

import io.jmix.flowui.Facets;
import io.jmix.flowui.Views;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("dynattr_DynAttrFacetInfo")
public class DynAttrFacetInfo {
    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected Views views;

    private final List<String> dynAttrViews = new ArrayList<>();
    private volatile boolean isInitialized = false;

    protected void scanForFacets() {
        viewRegistry.getViewInfos().forEach(viewInfo -> {
            // todo other method to get facet info
            try {
                if (ViewControllerUtils.getViewFacets(views.create(viewInfo.getId())).getFacets().anyMatch(DynAttrFacet.class::isInstance)) {
                    dynAttrViews.add(viewInfo.getId());
                }
            } catch (Exception e) {

            }
        });
    }

    public List<String> getDynAttrViews() {
        if (!isInitialized) {
            scanForFacets();
            isInitialized = true;
        }
        return dynAttrViews;
    }
}
