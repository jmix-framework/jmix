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

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

/**
 * Prepares and shows option dialogs.
 */
@StudioFacet(
        xmlElement = "optionDialog",
        caption = "OptionDialog",
        description = "Prepares and shows option dialogs",
        defaultProperty = "message",
        category = "Facets",
        icon = "io/jmix/ui/icon/facet/dialog.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/facets/option-dialog-facet.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", type = PropertyType.COMPONENT_ID, required = true)
        }
)
public interface OptionDialogFacet extends Facet, ActionsAwareDialogFacet<OptionDialogFacet>, HasSubParts {

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
     * Sets dialog message.
     *
     * @param message message
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setMessage(@Nullable String message);

    /**
     * @return dialog message
     */
    @Nullable
    String getMessage();

    /**
     * Sets dialog message content mode.
     *
     * @param contentMode content mode
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "TEXT")
    void setContentMode(ContentMode contentMode);

    /**
     * @return dialog message content mode
     */
    ContentMode getContentMode();

    /**
     * Sets the mode of the dialog window
     *
     * @param windowMode the mode of the dialog window
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "NORMAL")
    void setWindowMode(WindowMode windowMode);

    /**
     * @return the mode of the dialog window
     */
    WindowMode getWindowMode();

    /**
     * Sets dialog style name.
     *
     * @param styleName style name
     */
    @StudioProperty(name = "stylename", type = PropertyType.CSS_CLASSNAME_LIST)
    void setStyleName(String styleName);

    /**
     * @return dialog style name
     */
    @Nullable
    String getStyleName();

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
     * @return id of action that triggers dialog
     */
    @Nullable
    String getActionTarget();

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
     * @return id of button that triggers dialog
     */
    @Nullable
    String getButtonTarget();

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
     * Sets whether html sanitizer is enabled or not for dialog content.
     *
     * @param htmlSanitizerEnabled specifies whether html sanitizer is enabled
     */
    @StudioProperty(type = PropertyType.BOOLEAN, defaultValue = "true")
    void setHtmlSanitizerEnabled(boolean htmlSanitizerEnabled);

    /**
     * @return html sanitizer is enabled for dialog content
     */
    boolean isHtmlSanitizerEnabled();

    /**
     * Shows dialog.
     */
    void show();
}
