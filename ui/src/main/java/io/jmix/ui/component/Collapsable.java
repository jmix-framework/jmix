/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.StudioProperty;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Is able to collapse (folding).
 */
public interface Collapsable extends Component {
    boolean isExpanded();

    @StudioProperty(name = "collapsed", defaultValue = "false")
    void setExpanded(boolean expanded);

    boolean isCollapsable();

    @StudioProperty(defaultValue = "false")
    void setCollapsable(boolean collapsable);

    Subscription addExpandedStateChangeListener(Consumer<ExpandedStateChangeEvent> listener);

    class ExpandedStateChangeEvent extends EventObject implements HasUserOriginated {
        private final boolean expanded;
        private final boolean userOriginated;

        public ExpandedStateChangeEvent(Collapsable component, boolean expanded, boolean userOriginated) {
            super(component);
            this.expanded = expanded;
            this.userOriginated = userOriginated;
        }

        @Override
        public Collapsable getSource() {
            return (Collapsable) super.getSource();
        }

        /**
         * @return true if Component has been expanded.
         */
        public boolean isExpanded() {
            return expanded;
        }

        @Override
        public boolean isUserOriginated() {
            return userOriginated;
        }
    }
}