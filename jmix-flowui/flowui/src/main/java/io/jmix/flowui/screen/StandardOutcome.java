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

package io.jmix.flowui.screen;

import io.jmix.flowui.SimilarToUi;

/**
 * A possible outcome of screens extending {@link StandardEditor} and {@link StandardLookup}.
 * <p>
 * Constants of this enum can be used instead of {@link CloseAction} instances in {@link Screen#close(StandardOutcome)}
 * method to initiate closing and in {@link Screen.AfterCloseEvent#closedWith(StandardOutcome)},
 * {@link AfterScreenCloseEvent#closedWith(StandardOutcome)}.
 * method to determine how the screen was closed.
 *
 * @see #CLOSE
 * @see #COMMIT
 * @see #DISCARD
 * @see #SELECT
 */
@SimilarToUi
public enum StandardOutcome {

    // TODO: gg, extract action id constants
    /**
     * The screen is closed without an explicit commit. However, the screen notifies the user if there are unsaved changes.
     */
    CLOSE(new StandardCloseAction("close")),

    /**
     * The screen is closed after an explicit commit. If the screen still contains unsaved changes, the user is notified about it.
     */
    COMMIT(new StandardCloseAction("commit")),

    /**
     * The screen is closed without an explicit commit and it did not notify the user about unsaved changes.
     */
    DISCARD(new StandardCloseAction("close", false)),

    /**
     * The screen is closed after the user selected an item in the lookup component.
     */
    SELECT(new StandardCloseAction("select"));

    private final CloseAction closeAction;

    StandardOutcome(CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public CloseAction getCloseAction() {
        return closeAction;
    }
}
