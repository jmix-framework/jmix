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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Prepares and shows input dialogs.
 */
@StudioFacet(
        xmlElement = "inputDialog",
        caption = "InputDialog",
        description = "Prepares and shows input dialogs",
        defaultProperty = "caption",
        category = "Facets",
        icon = "io/jmix/ui/icon/facet/dialog.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/facets/input-dialog-facet.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", type = PropertyType.COMPONENT_ID, required = true)
        }
)
public interface InputDialogFacet extends Facet, ActionsAwareDialogFacet<InputDialogFacet>, HasSubParts {

    /**
     * Sets dialog caption.
     *
     * @param caption caption
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setCaption(@Nullable String caption);

    /**
     * @return dialog caption
     */
    @Nullable
    String getCaption();

    /**
     * Sets dialog width.
     *
     * @param width width
     */
    @StudioProperty(type = PropertyType.SIZE)
    void setWidth(@Nullable String width);

    /**
     * @return dialog width
     */
    float getWidth();

    /**
     * @return dialog width size unit
     */
    SizeUnit getWidthSizeUnit();

    /**
     * Sets dialog height.
     *
     * @param height height
     */
    @StudioProperty(type = PropertyType.SIZE)
    void setHeight(@Nullable String height);

    /**
     * @return dialog height
     */
    float getHeight();

    /**
     * @return dialog height size unit
     */
    SizeUnit getHeightSizeUnit();

    /**
     * Sets that dialog should be shown when action with id {@code actionId}
     * is performed.
     *
     * @param actionId action id
     */
    @StudioProperty(name = "onAction", type = PropertyType.COMPONENT_REF,
            options = "io.jmix.ui.action.Action")
    void setActionTarget(@Nullable String actionId);

    /**
     * @return id of action that triggers dialog
     */
    @Nullable
    String getActionTarget();

    /**
     * Sets that dialog should be shown when button with id {@code actionId}
     * is clicked.
     *
     * @param buttonId button id
     */
    @StudioProperty(name = "onButton", type = PropertyType.COMPONENT_REF,
            options = "io.jmix.ui.component.Button")
    void setButtonTarget(@Nullable String buttonId);

    /**
     * @return id of button that triggers dialog
     */
    @Nullable
    String getButtonTarget();

    /**
     * Defines a set of predefined actions to use in dialog.
     *
     * @param dialogActions one of {@link DialogActions} values
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    void setDialogActions(@Nullable DialogActions dialogActions);

    /**
     * @return set of predefined actions used in dialog
     */
    @Nullable
    DialogActions getDialogActions();

    /**
     * Adds the given {@code Consumer} as dialog {@link InputDialog.InputDialogCloseEvent} listener.
     *
     * @param closeListener close listener
     * @return close event subscription
     */
    Subscription addCloseListener(Consumer<InputDialog.InputDialogCloseEvent> closeListener);

    /**
     * Sets input dialog result handler.
     *
     * @param dialogResultHandler result handler
     */
    void setDialogResultHandler(Consumer<InputDialog.InputDialogResult> dialogResultHandler);

    /**
     * Sets additional handler for field validation. It receives input dialog context and must return {@link ValidationErrors}
     * instance. Returned validation errors will be shown with another errors from fields.
     *
     * @param validator validator
     */
    void setValidator(Function<InputDialog.ValidationContext, ValidationErrors> validator);

    /**
     * Sets input dialog parameters.
     *
     * @param parameters set of {@link InputParameter}
     */
    @StudioElementsGroup(xmlElement = "parameters",
            caption = "Parameters",
            icon = "io/jmix/ui/icon/element/parameters.svg")
    void setParameters(InputParameter... parameters);

    /**
     * Creates InputDialog.
     *
     * @return input dialog instance
     */
    InputDialog create();

    /**
     * Shows InputDialog.
     */
    InputDialog show();
}
