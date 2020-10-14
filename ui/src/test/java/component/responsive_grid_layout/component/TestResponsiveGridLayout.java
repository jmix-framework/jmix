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

package component.responsive_grid_layout.component;

import io.jmix.ui.component.impl.ResponsiveGridLayoutImpl;
import io.jmix.ui.widget.JmixResponsiveGridLayout;

public class TestResponsiveGridLayout extends ResponsiveGridLayoutImpl {

    @Override
    protected JmixResponsiveGridLayout createComponent() {
        return new TestJmixResponsiveGridLayout();
    }

    @Override
    public TestJmixResponsiveGridLayout getComponent() {
        return (TestJmixResponsiveGridLayout) super.getComponent();
    }

    public void setInitialized(boolean initialized) {
        getComponent().setInitialized(initialized);
    }

    public static class TestJmixResponsiveGridLayout extends JmixResponsiveGridLayout {

        public void setInitialized(boolean initialized) {
            this.initialized = initialized;
        }
    }
}
