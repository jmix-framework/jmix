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

package io.jmix.ui.builder;


import io.jmix.ui.screen.CloseAction;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardOutcome;

import java.util.EventObject;

/**
 * Event sent to listeners added to the screen using {@code withAfterCloseListener()} method of screen builders.
 *
 * @param <S> type of the screen
 */
public class AfterScreenCloseEvent<S extends Screen> extends EventObject {

    protected final CloseAction closeAction;

    public AfterScreenCloseEvent(S source, CloseAction closeAction) {
        super(source);
        this.closeAction = closeAction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public S getSource() {
        return (S) super.getSource();
    }

    @SuppressWarnings("unchecked")
    public S getScreen() {
        return (S) super.getSource();
    }

    /**
     * @return action passed to the {@link Screen#close(CloseAction)} method of the screen.
     */
    public CloseAction getCloseAction() {
        return closeAction;
    }

    /**
     * Checks that screen was closed with the given {@code outcome}.
     */
    public boolean closedWith(StandardOutcome outcome) {
        return outcome.getCloseAction().equals(closeAction);
    }
}
