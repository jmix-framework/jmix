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

package io.jmix.flowui.view.impl;

import com.vaadin.flow.component.Composite;
import io.jmix.flowui.facet.FacetOwner;
import io.jmix.flowui.facet.impl.AbstractFacetHolder;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewFacets;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link ViewFacets} interface. This class manages a collection of facets
 * associated with a specific {@link View}.
 */
@Component("flowui_ViewFacets")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ViewFacetsImpl extends AbstractFacetHolder implements ViewFacets {

    protected final View<?> view;

    public ViewFacetsImpl(View<?> view) {
        this.view = view;
    }

    @Override
    protected <T extends Composite<?> & FacetOwner> T getOwner() {
        //noinspection unchecked
        return (T) view;
    }
}
