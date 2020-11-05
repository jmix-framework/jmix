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

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.Screens;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface AppWorkArea extends Component.BelongToFrame {

    String NAME = "workArea";

    /**
     * @return a mode
     */
    Mode getMode();

    /**
     * Sets a mode. The default value is {@link Mode#TABBED}.
     * <p>
     * Unable to change a mode in {@link State#WINDOW_CONTAINER} state.
     *
     * @param mode a mode
     */
    void setMode(Mode mode);

    /**
     * @return a state
     */
    State getState();

    /**
     * INTERNAL. Managed by the screen control mechanism {@link Screens}.
     * <p>
     * Sets a new state. The default value is {@link State#INITIAL_LAYOUT}.
     *
     * @param state new state
     */
    @Internal
    void switchTo(State state);

    /**
     * @return an initial layout
     */
    @Nullable
    VBoxLayout getInitialLayout();

    /**
     * Sets the initial layout.
     * <p>
     * Unable to change initial layout in {@link State#WINDOW_CONTAINER} state.
     *
     * @param initialLayout an initial layout
     * @see VBoxLayout
     */
    void setInitialLayout(VBoxLayout initialLayout);

    /**
     * Adds a listener that will be notified when a work area state is changed.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addStateChangeListener(Consumer<StateChangeEvent> listener);

    /**
     * Returns all opened screens that are inside the work area.
     *
     * @return opened screens stream
     **/
    Stream<Screen> getOpenedWorkAreaScreensStream();

    /**
     * Returns all active screens that are inside the work area.
     *
     * @return active screens stream
     **/
    Stream<Screen> getActiveWorkAreaScreensStream();

    /**
     * Returns all screens that are inside the current breadcrumbs.
     *
     * @return screens that are inside the current breadcrumbs
     **/
    Collection<Screen> getCurrentBreadcrumbs();

    /**
     * Event that is fired when work area changed its state.
     */
    class StateChangeEvent extends EventObject {

        protected final State state;

        public StateChangeEvent(AppWorkArea source, State state) {
            super(source);
            this.state = state;
        }

        @Override
        public AppWorkArea getSource() {
            return (AppWorkArea) super.getSource();
        }

        public State getState() {
            return state;
        }
    }

    /**
     * Work area mode
     */
    enum Mode {
        /**
         * If the main screen is in TABBED mode, it creates the {@link TabSheet} inside
         * and opens screens with {@link OpenMode#NEW_TAB} as tabs.
         */
        TABBED,

        /**
         * In SINGLE mode each new screen opened with {@link OpenMode#NEW_TAB}
         * opening type will replace the current screen.
         */
        SINGLE
    }

    /**
     * Work area state
     */
    enum State {
        /**
         * If the work area is in the INITIAL_LAYOUT state, the work area does not contain other screens.
         */
        INITIAL_LAYOUT,

        /**
         * If the work area is in the WINDOW_CONTAINER state, the work area contains at least one screen.
         */
        WINDOW_CONTAINER
    }
}