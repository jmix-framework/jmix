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

package io.jmix.flowui.kit.meta;

/**
 * Standard reusable {@link StudioPropertyGroup} definitions.
 */
@StudioAPI
public final class StudioPropertyGroups {

    private StudioPropertyGroups() {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL)
            }
    )
    public interface Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL, defaultValue = "true")
            }
    )
    public interface Visible {
    }

    @StudioPropertyGroup
    public interface IdAndVisible extends Id, Visible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.POSITION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"})
            }
    )
    public interface AlignSelf {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "justifySelf", type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.POSITION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"})
            }
    )
    public interface JustifySelf {
    }

    @StudioPropertyGroup
    public interface AlignSelfAndJustifySelf extends AlignSelf, JustifySelf {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.POSITION)
            }
    )
    public interface Colspan {
    }

    @StudioPropertyGroup
    public interface AlignSelfAndJustifySelfAndColspan extends AlignSelfAndJustifySelf, Colspan {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL)
            }
    )
    public interface ClassNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL)
            }
    )
    public interface Css {
    }

    @StudioPropertyGroup
    public interface ClassNamesAndCss extends ClassNames, Css {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL, defaultValue = "true")
            }
    )
    public interface Enabled {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    public interface HeightWithoutOptions {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    public interface MaxHeightWithoutOptions {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    public interface MinHeightWithoutOptions {

    }

    @StudioPropertyGroup
    public interface MinAndMaxHeightWithoutOptions extends MinHeightWithoutOptions, MaxHeightWithoutOptions {
    }

    @StudioPropertyGroup
    public interface HeightWithoutOptionsGroup extends HeightWithoutOptions, MinAndMaxHeightWithoutOptions {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    public interface WidthWithoutOptions {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    public interface MaxWidthWithoutOptions {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    public interface MinWidthWithoutOptions {
    }

    @StudioPropertyGroup
    public interface MinAndMaxWidthWithoutOptions extends MinWidthWithoutOptions, MaxWidthWithoutOptions {
    }

    @StudioPropertyGroup
    public interface WidthWithoutOptionsGroup extends WidthWithoutOptions, MinAndMaxWidthWithoutOptions {

    }

    @StudioPropertyGroup
    public interface SizeWithoutOptions extends WidthWithoutOptionsGroup, HeightWithoutOptionsGroup {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"})
            }
    )
    public interface Height {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"})
            }
    )
    public interface MaxHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"})
            }
    )
    public interface MinHeight {
    }

    @StudioPropertyGroup
    public interface MinAndMaxHeight extends MinHeight, MaxHeight {
    }

    @StudioPropertyGroup
    public interface HeightGroup extends Height, MinAndMaxHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"})
            }
    )
    public interface Width {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"})
            }
    )
    public interface MaxWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"})
            }
    )
    public interface MinWidth {
    }

    @StudioPropertyGroup
    public interface MinAndMaxWidth extends MinWidth, MaxWidth {
    }

    @StudioPropertyGroup
    public interface WidthGroup extends Width, MinAndMaxWidth {
    }

    @StudioPropertyGroup
    public interface Size extends WidthGroup, HeightGroup {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface AriaLabel {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface AriaLabelledBy {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface TabIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION)
            }
    )
    public interface FocusShortcut {
    }

    @StudioPropertyGroup
    public interface AriaLabelAndTabIndexAndFocusShortcut extends AriaLabel, AriaLabelledBy, TabIndex, FocusShortcut {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.VALIDATION, defaultValue = "false")
            }
    )
    public interface Required {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.VALIDATION)
            }
    )
    public interface RequiredMessage {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.VALIDATION)
            }
    )
    public interface ErrorMessage {
    }

    @StudioPropertyGroup
    public interface RequiredAndRequiredMessageAndErrorMessage extends Required, RequiredMessage, ErrorMessage {
    }
}
