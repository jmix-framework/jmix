/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.observation;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentOwner;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;

/**
 * POJO class for information about {@link Fragment} that will be used for observation cardinalities.
 *
 * @param fragmentId    id of the fragment
 * @param fragmentClass FQN of the target fragment class
 * @param viewId        id of the enclosing {@link View}, or {@code null} if the fragment is not attached
 *                      to any view (e.g. created standalone)
 */
public record FragmentObservationInfo(@Nullable String fragmentId,
                                               String fragmentClass,
                                               @Nullable String viewId) {

    public FragmentObservationInfo(Fragment<?> fragment) {
        this(fragment.getId().orElse(null), fragment.getClass().getName(), resolveViewId(fragment));
    }

    public FragmentObservationInfo(FragmentOwner parent,
                                            @Nullable String fragmentId,
                                            String fragmentClass) {
        this(fragmentId, fragmentClass,
                parent instanceof Component component ? resolveViewId(component) : null);
    }

    @Nullable
    protected static String resolveViewId(Component component) {
        View<?> view = UiComponentUtils.findView(component);
        return view != null ? view.getId().orElse(null) : null;
    }
}
