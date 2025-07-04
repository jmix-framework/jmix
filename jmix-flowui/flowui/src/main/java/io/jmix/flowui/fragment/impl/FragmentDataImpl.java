/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.fragment.impl;

import io.jmix.flowui.fragment.FragmentData;
import io.jmix.flowui.model.impl.AbstractDataComponentsHolder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link FragmentData} interface providing methods
 * to manage fragment-specific data API elements.
 */
@Component("flowui_FragmentData")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FragmentDataImpl extends AbstractDataComponentsHolder implements FragmentData {

    protected String fragmentId;

    @Nullable
    @Override
    public String getFragmentId() {
        return fragmentId;
    }

    @Override
    public void setFragmentId(@Nullable String fragmentId) {
        this.fragmentId = fragmentId;
    }

    @Nullable
    @Override
    protected String getOwnerId() {
        return getFragmentId();
    }
}
