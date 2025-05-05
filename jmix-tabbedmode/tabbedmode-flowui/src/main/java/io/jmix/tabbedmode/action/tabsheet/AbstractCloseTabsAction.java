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

package io.jmix.tabbedmode.action.tabsheet;

import com.vaadin.flow.component.Component;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.component.tabsheet.MainTabSheetUtils;

import java.util.List;

public abstract class AbstractCloseTabsAction<A extends AbstractCloseTabsAction<A>>
        extends TabbedViewsContainerAction<A> {

    public AbstractCloseTabsAction(String id) {
        super(id);
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && hasCloseableTabs();
    }

    protected abstract boolean hasCloseableTabs();

    protected void closeViewStacks(List<Views.ViewStack> viewStacks) {
        for (Views.ViewStack viewStack : viewStacks) {
            if (!viewStack.close()) {
                return;
            }
        }
    }

    protected Views.ViewStack asViewStack(Component component) {
        return new Views.ViewStack(target, MainTabSheetUtils.asViewContainer(component));
    }
}
