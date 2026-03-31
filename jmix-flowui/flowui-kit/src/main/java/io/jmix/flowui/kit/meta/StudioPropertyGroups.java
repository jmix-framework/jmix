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

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID))
    public interface Id {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID))
    public interface IdWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true))
    public interface RequiredId {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, category = StudioProperty.Category.GENERAL, defaultValue = "true"))
    public interface Visible {
    }

    @StudioPropertyGroup
    public interface IdAndVisible extends Id, Visible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "alignSelf", type = StudioPropertyType.ENUMERATION, category = StudioProperty.Category.POSITION, classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment", defaultValue = "AUTO", options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface AlignSelf {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "justifySelf", type = StudioPropertyType.ENUMERATION, category = StudioProperty.Category.POSITION, classFqn = "io.jmix.flowui.kit.component.Alignment", defaultValue = "AUTO", options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface JustifySelf {
    }

    @StudioPropertyGroup
    public interface AlignSelfAndJustifySelf extends AlignSelf, JustifySelf {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER, category = StudioProperty.Category.POSITION))
    public interface Colspan {
    }

    @StudioPropertyGroup
    public interface AlignSelfAndJustifySelfAndColspan extends AlignSelfAndJustifySelf, Colspan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST, category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface ClassNames {
    }

    @StudioPropertyGroup
    public interface Css {
    }

    @StudioPropertyGroup
    public interface ClassNamesAndCss extends ClassNames, Css {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN))
    public interface Enabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL, defaultValue = "true"))
    public interface EnabledWithTrueDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface HeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MaxHeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MinHeightWithoutOptions {

    }

    @StudioPropertyGroup
    public interface MinAndMaxHeightWithoutOptions extends MinHeightWithoutOptions, MaxHeightWithoutOptions {
    }

    @StudioPropertyGroup
    public interface HeightWithoutOptionsGroup extends HeightWithoutOptions, MinAndMaxHeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, category = StudioProperty.Category.SIZE))
    public interface WidthWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE, category = StudioProperty.Category.SIZE))
    public interface MaxWidthWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE, category = StudioProperty.Category.SIZE))
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

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}))
    public interface Height {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}))
    public interface MaxHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}))
    public interface MinHeight {
    }

    @StudioPropertyGroup
    public interface MinAndMaxHeight extends MinHeight, MaxHeight {
    }

    @StudioPropertyGroup
    public interface HeightGroup extends Height, MinAndMaxHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}))
    public interface Width {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}))
    public interface MaxWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE, options = {"AUTO", "100%"}))
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

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "acceptedFileTypes", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface AcceptedFileTypes {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "action", type = StudioPropertyType.ACTION_REF,
                            classFqn = "io.jmix.flowui.kit.action.Action")
            }
    )
    public interface Action {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"})
            }
    )
    public interface ActionVariantWithDefaultDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DANGER", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"})
            }
    )
    public interface ActionVariantWithDangerDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "PRIMARY", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"})
            }
    )
    public interface ActionVariantWithPrimaryDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "aggregatable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface Aggregatable {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "aggregationPosition", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.grid.EnhancedDataGrid$AggregationPosition",
                            defaultValue = "BOTTOM", options = {"TOP", "BOTTOM"})
            }
    )
    public interface AggregationPosition {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "alignItems", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "START",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"})
            }
    )
    public interface AlignItems {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"})
            }
    )
    public interface GridAlignSelf {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "allRowsVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface AllRowsVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface AllowCustomValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "allowedCharPattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface AllowedCharPattern {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "alternateText", type = StudioPropertyType.LOCALIZED_STRING,
                            setMethod = "setAlt")
            }
    )
    public interface AlternateText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "autoApply", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface AutoApply {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "auto", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false", initialValue = "true")
            }
    )
    public interface Auto {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface AutoOpen {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "autoWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface AutoWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "autocapitalize", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize", defaultValue = "NONE",
                            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"})
            }
    )
    public interface Autocapitalize {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "autocomplete", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocomplete", defaultValue = "OFF",
                            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME",
                                    "FAMILY_NAME", "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD",
                                    "CURRENT_PASSWORD", "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS",
                                    "ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "ADDRESS_LEVEL1",
                                    "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY", "COUNTRY_NAME",
                                    "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE",
                                    "TRANSACTION_CURRENCY", "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY",
                                    "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL", "TEL_COUNTRY_CODE", "TEL_NATIONAL",
                                    "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX", "TEL_LOCAL_SUFFIX",
                                    "TEL_EXTENSION", "URL", "PHOTO",})
            }
    )
    public interface Autocomplete {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "autocorrect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface Autocorrect {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface Autofocus {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface Autoselect {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = "bean",
                            type = StudioPropertyType.STRING,
                            required = true
                    )
            }
    )
    public interface Bean {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "boxSizing", category = StudioProperty.Category.SIZE, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            classFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            defaultValue = "UNDEFINED",
                            options = {"UNDEFINED", "CONTENT_BOX", "BORDER_BOX"})
            }
    )
    public interface BoxSizing {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "cellTitle", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface CellTitle {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "charCode", type = StudioPropertyType.STRING)
            }
    )
    public interface CharCode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = "checkSeconds",
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    public interface CheckSeconds {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "checkable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface Checkable {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "checked", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface Checked {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "class", type = StudioPropertyType.ENTITY_CLASS, required = true)
            }
    )
    public interface RequiredEntityClass {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "class", type = StudioPropertyType.ENTITY_CLASS)
            }
    )
    public interface EntityClass {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "class", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.FRAGMENT_CLASS, required = true)
            }
    )
    public interface RequiredFragmentClass {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "clearButtonAriaLabel", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface ClearButtonAriaLabel {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface ClearButtonVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "clickShortcut", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION)
            }
    )
    public interface ClickShortcut {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "color", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Color {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "columnRendering", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.ColumnRendering", defaultValue = "EAGER",
                            options = {"EAGER", "LAZY"})
            }
    )
    public interface ColumnRendering {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "columnReorderingAllowed", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface ColumnReorderingAllowed {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "columns", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER, required = true)
            }
    )
    public interface Columns {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "componentPrefix", type = StudioPropertyType.STRING,
                            defaultValue = "component_")
            }
    )
    public interface ComponentPrefix {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "connectingStatusText", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface ConnectingStatusText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "containerPrefix", type = StudioPropertyType.STRING,
                            defaultValue = "container_")
            }
    )
    public interface ContainerPrefix {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "content", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Content {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, required = true,
                            typeParameter = "E")
            }
    )
    public interface CollectionDataContainer {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF)
            }
    )
    public interface CollectionOrInstanceDataContainer {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dataLoader", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_LOADER_REF,
                            required = true)
            }
    )
    public interface DataLoader {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "datatype", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATATYPE_ID,
                            typeParameter = "V")
            }
    )
    public interface Datatype {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "defaultValue", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING)
            }
    )
    public interface DefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Description {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "detailsVisibleOnClick", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface DetailsVisibleOnClick {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dropAllowed", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface DropAllowed {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dropMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.dnd.GridDropMode", options = {"BETWEEN", "ON_TOP",
                            "ON_TOP_OR_BETWEEN", "ON_GRID"})
            }
    )
    public interface DropMode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "editable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface Editable {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "editorBuffered", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface EditorBuffered {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "emptyStateText", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface EmptyStateText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "escapeValueForLike", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface EscapeValueForLike {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "exclude", type = StudioPropertyType.STRING)
            }
    )
    public interface Exclude {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "expand", category = StudioProperty.Category.POSITION, type = StudioPropertyType.STRING)
            }
    )
    public interface Expand {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fetchPlan", type = StudioPropertyType.FETCH_PLAN)
            }
    )
    public interface FetchPlan {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fileNameVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface FileNameVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fileNotSelectedText", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface FileNotSelectedText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fileStorageName", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING)
            }
    )
    public interface FileStorageName {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fileStoragePutMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            options = {"MANUAL", "IMMEDIATE"}, defaultValue = "IMMEDIATE")
            }
    )
    public interface FileStoragePutMode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fileTooBigText", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface FileTooBigText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "firstResult",
            type = StudioPropertyType.INTEGER, defaultValue = "0"))
    public interface FirstResult {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "flexGrow",
            category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER))
    public interface FlexGrow {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "fontFamily",
            category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING))
    public interface FontFamily {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "footer",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Footer {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "forgotPassword",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface ForgotPassword {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "format",
            type = StudioPropertyType.LOCALIZED_STRING, required = true))
    public interface Format {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "frozen",
            category = StudioProperty.Category.POSITION, type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface Frozen {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "gap",
            category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING))
    public interface Gap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "hasInExpression",
            type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface HasInExpression {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "header",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Header {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "helperText",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING))
    public interface HelperText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "hideDelay",
            type = StudioPropertyType.INTEGER))
    public interface HideDelay {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "iconAfterText",
            type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface IconAfterText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "iconClassNames",
            category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST))
    public interface IconClassNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ICON))
    public interface Icon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ICON, required = true))
    public interface RequiredIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
            setParameterFqn = "com.vaadin.flow.component.icon.Icon"))
    public interface LookAndFeelIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "includeAll",
            type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface IncludeAll {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "inclusive",
            type = StudioPropertyType.BOOLEAN))
    public interface Inclusive {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "incorrectFileTypeText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface IncorrectFileTypeText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "indeterminate",
            category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface Indeterminate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "itemsContainer",
            category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "T"))
    public interface ItemsContainerTypeParameterT {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "itemsContainer",
            category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"))
    public interface ItemsContainerTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "itemsEnum",
            category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS, typeParameter = "V"))
    public interface ItemsEnum {
    }

    @StudioPropertyGroup(
            properties = @StudioProperty(xmlAttribute = "justifyContent",
                    category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                    setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                    classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                    setMethod = "setJustifyContentMode", defaultValue = "START",
                    options = {"START", "END", "CENTER", "BETWEEN", "AROUND", "EVENLY"})
    )
    public interface JustifyContent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "key",
            type = StudioPropertyType.STRING))
    public interface Key {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "label",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING))
    public interface Label {
    }

    @StudioPropertyGroup(
            properties = @StudioProperty(xmlAttribute = "labelPosition",
                    category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                    classFqn = "io.jmix.flowui.component.SupportsLabelPosition$LabelPosition",
                    options = {"ASIDE", "TOP"}, defaultValue = "ASIDE")

    )
    public interface LabelPosition {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "labelVisible",
            category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"))
    public interface LabelVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "labelWidth",
            category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE))
    public interface LabelWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "ligature", type = StudioPropertyType.STRING))
    public interface Ligature {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "loadMenuConfig",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"))
    public interface LoadMenuConfig {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "margin",
            category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface Margin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "max",
            category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.DOUBLE))
    public interface ValidationDoubleMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxFileSize",
            category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER))
    public interface MaxFileSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "max",
            type = StudioPropertyType.INTEGER))
    public interface IntegerMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxLength",
            category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER))
    public interface MaxLength {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxResults",
            type = StudioPropertyType.INTEGER, defaultValue = "0"))
    public interface MaxResults {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "max",
            category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING))
    public interface ValidationStringMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "value",
            type = StudioPropertyType.STRING, required = true))
    public interface RequiredStringValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "integer",
            type = StudioPropertyType.INTEGER, required = true))
    public interface RequiredInteger {

    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "message",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Message {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "messagesGroup",
            type = StudioPropertyType.STRING))
    public interface MessagesGroup {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "metaClass",
            category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME, typeParameter = "V"))
    public interface MetaClassTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "metaClass",
            category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME, typeParameter = "E"))
    public interface MetaClassTypeParameterE {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "min",
            category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.DOUBLE))
    public interface ValidationDoubleMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "min",
            type = StudioPropertyType.INTEGER))
    public interface IntegerMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minLength",
            category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER))
    public interface MinLength {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "min",
            category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING))
    public interface ValidationStringMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minWidth",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, required = true))
    public interface RequiredMinWidthWithOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "multiSort",
            type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface MultiSort {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "multiSortOnShiftClickOnly",
            type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface MultiSortOnShiftClickOnly {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "multiSortPriority",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.Grid.MultiSortPriority",
            options = {"APPEND", "PREPEND"}, defaultValue = "PREPEND"))
    public interface MultiSortPriority {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "name",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING))
    public interface Name {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "nestedNullBehavior",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.Grid.NestedNullBehavior",
            options = {"THROW", "ALLOW_NULLS"}, defaultValue = "THROW"))
    public interface NestedNullBehavior {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "nullRepresentation",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface NullRepresentation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "openOnHover",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface OpenOnHover {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "opened",
            category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "false"))
    public interface Opened {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "operationTextVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    public interface OperationTextVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "overlayWidth", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.SIZE)
            }
    )
    public interface OverlayWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "padding", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    public interface Padding {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER)
            }
    )
    public interface PageSize {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER, defaultValue = "50")
            }
    )
    public interface PageSizeWithDefaultValue50 {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING)
            }
    )
    public interface Param {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "parameterName", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING)
            }
    )
    public interface ParameterName {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "password", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Password {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "pattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING)
            }
    )
    public interface Pattern {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Placeholder {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "processingStatusText", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface ProcessingStatusText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "T", required = true)
            }
    )
    public interface PropertyTypeParameterT {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V")
            }
    )
    public interface PropertyTypeParameterV {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface Property {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY)
            }
    )
    public interface Query {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "false")
            }
    )
    public interface ReadOnly {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface ReadOnlyWithoutCategory {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "remainingTimeText", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface RemainingTimeText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "remainingTimeUnknownText", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface RemainingTimeUnknownText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "resource", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING)
            }
    )
    public interface Resource {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "resource", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING,
                            setMethod = "setSrc")
            }
    )
    public interface ImageResource {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "rotation", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface Rotation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "rowsDraggable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface RowsDraggable {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "searchStringFormat", type = StudioPropertyType.STRING)
            }
    )
    public interface SearchStringFormat {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "selectionMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.SelectionMode", defaultValue = "SINGLE",
                            options = {"SINGLE", "MULTI", "NONE"})
            }
    )
    public interface SelectionMode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION)
            }
    )
    public interface ShortcutCombination {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = "showInContextMenuEnabled",
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"
                    )
            }
    )
    public interface ShowInContextMenuEnabled {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "sortable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:sortable")
            }
    )
    public interface ColumnSortable {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "sortable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    public interface Sortable {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "spacing", category = StudioProperty.Category.POSITION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    public interface Spacing {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "stepButtonsVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface StepButtonsVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "step", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.DOUBLE)
            }
    )
    public interface Step {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "step", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING,
                            options = {"900s", "15m", "20m", "30m", "2h", "3h", "4h", "6h", "8h", "12h"})
            }
    )
    public interface TimeStep {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "store", type = StudioPropertyType.STORE)
            }
    )
    public interface Store {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "strategyClass", type = StudioPropertyType.STRING)
            }
    )
    public interface StrategyClass {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "submit", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Submit {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "summaryText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface DetailsSummaryText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "summaryText", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface SummaryText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbol", type = StudioPropertyType.STRING)
            }
    )
    public interface Symbol {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Text {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING, required = true)
            }
    )
    public interface RequiredText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"})
            }
    )
    public interface FieldThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"filled", "reverse", "small"})
            }
    )
    public interface DetailsThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"})
            }
    )
    public interface ButtonThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"non-checkable"})
            }
    )
    public interface UserMenuItemThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"fill", "contain", "cover", "scale-down"})
            }
    )
    public interface ImageThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"spacing-xs", "spacing-s", "spacing", "spacing-l", "spacing-xl"})
            }
    )
    public interface LayoutThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"})
            }
    )
    public interface ComboBoxThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"no-border", "no-row-borders", "column-borders", "row-stripes",
                                    "compact", "wrap-cell-content", "column-dividers"})
            }
    )
    public interface GridThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"})
            }
    )
    public interface HtmlComponentThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface ThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill"})
            }
    )
    public interface NativeLabelThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "title", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Title {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "trimEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true",
                            defaultValueRef = "application_property:jmix.ui.component.default-trim-enabled")
            }
    )
    public interface TrimEnabled {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "uploadDialogCancelText", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface UploadDialogCancelText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "uploadDialogTitle", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface UploadDialogTitle {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "uploadIcon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON)
            }
    )
    public interface UploadIcon {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "uploadText", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface UploadText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "username", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Username {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "valueChangeMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode",
                            options = {"EAGER", "LAZY", "TIMEOUT", "ON_BLUR", "ON_CHANGE"})
            }
    )
    public interface ValueChangeMode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER)
            }
    )
    public interface ValueChangeTimeout {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    public interface VisibleWithDefaultValueTrue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "weekNumbersVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface WeekNumbersVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"})
            }
    )
    public interface WhiteSpace {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.INTEGER)
            }
    )
    public interface WidthWithIntegerType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%")
            }
    )
    public interface WidthWithDefaultValue100 {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"},
                            defaultValue = "AUTO")
            }
    )
    public interface WidthWithDefaultValueAuto {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "UNDEFINED")
            }
    )
    public interface WidthWithDefaultValueUndefined {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, initialValue = "100%")
            }
    )
    public interface WidthWithInitialValue100 {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "wrap", category = StudioProperty.Category.LOOK_AND_FEEL,
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false")
            }
    )
    public interface Wrap {
    }

    @StudioPropertyGroup
    public interface MessageAndRequiredStringValue extends Message, RequiredStringValue {
    }

    @StudioPropertyGroup
    public interface MessageAndInclusiveRequiredStringValue extends Message, Inclusive, RequiredStringValue {
    }
}
