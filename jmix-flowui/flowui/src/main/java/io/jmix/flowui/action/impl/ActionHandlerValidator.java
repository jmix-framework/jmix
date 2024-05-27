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

package io.jmix.flowui.action.impl;

import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.view.OpenMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class that checks whether handlers set for an action are appropriate for the given open mode.
 */
public class ActionHandlerValidator {

    private static final Logger log = LoggerFactory.getLogger(ActionHandlerValidator.class);

    private static void printNavigationWarning(Action action, String handlerName) {
        log.warn(String.format("'%s' is set for '%s' action but it's not invoked when navigating",
                handlerName, action.getId()));
    }

    private static void printDialogWarning(Action action, String handlerName) {
        log.warn(String.format("'%s' is set for '%s' action but it's not invoked when opening view in dialog window",
                handlerName, action.getId()));
    }

    /**
     * Checks whether handlers set for the action are appropriate for the given open mode.
     * <p>
     * Prints warnings to the log.
     */
    public static void validate(ViewOpeningAction action, OpenMode openMode) {
        if (openMode == OpenMode.NAVIGATION) {
            if (action.getAfterCloseHandler() != null)
                printNavigationWarning(action, "afterCloseHandler");

            if (action.getViewConfigurer() != null)
                printNavigationWarning(action, "viewConfigurer");

        } else if (action.getOpenMode() == OpenMode.DIALOG) {
            if (action.getRouteParametersProvider() != null)
                printDialogWarning(action, "routeParametersProvider");

            if (action.getQueryParametersProvider() != null)
                printDialogWarning(action, "queryParametersProvider");
        }
    }

    /**
     * Checks whether handlers set for the action are appropriate for the given open mode.
     * <p>
     * Prints warnings to the log.
     */
    public static void validate(CreateAction<?> action, OpenMode openMode) {
        validate((ViewOpeningAction) action, openMode);

        if (openMode == OpenMode.NAVIGATION) {
            if (action.getNewEntitySupplier() != null)
                printNavigationWarning(action, "newEntitySupplier");

            if (action.getInitializer() != null)
                printNavigationWarning(action, "initializer");

            if (action.getTransformation() != null)
                printNavigationWarning(action, "transformation");

            if (action.getAfterSaveHandler() != null)
                printNavigationWarning(action, "afterSaveHandler");
        }
    }

    /**
     * Checks whether handlers set for the action are appropriate for the given open mode.
     * <p>
     * Prints warnings to the log.
     */
    public static void validate(EditAction<?> action, OpenMode openMode) {
        validate((ViewOpeningAction) action, openMode);

        if (openMode == OpenMode.NAVIGATION) {
            if (action.getTransformation() != null)
                printNavigationWarning(action, "transformation");

            if (action.getAfterSaveHandler() != null)
                printNavigationWarning(action, "afterSaveHandler");
        }
    }

    /**
     * Checks whether handlers set for the action are appropriate for the given open mode.
     * <p>
     * Prints warnings to the log.
     */
    public static void validate(ReadAction<?> action, OpenMode openMode) {
        validate((ViewOpeningAction) action, openMode);

        if (openMode == OpenMode.NAVIGATION) {
            if (action.getTransformation() != null)
                printNavigationWarning(action, "transformation");

            if (action.getAfterSaveHandler() != null)
                printNavigationWarning(action, "afterSaveHandler");
        }
    }
}
