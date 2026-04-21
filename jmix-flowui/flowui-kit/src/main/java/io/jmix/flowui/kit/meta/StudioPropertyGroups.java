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

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "id",
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL))
    public interface Id {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "id",
            type = StudioPropertyType.COMPONENT_ID))
    public interface IdWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "id",
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL, required = true))
    public interface RequiredId {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "visible",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "true"))
    public interface Visible {
    }

    @StudioPropertyGroup
    public interface IdAndVisible extends Id, Visible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "alignSelf",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
            defaultValue = "AUTO",
            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface AlignSelf {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "justifySelf",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "io.jmix.flowui.kit.component.Alignment",
            defaultValue = "AUTO",
            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface JustifySelf {
    }

    @StudioPropertyGroup
    public interface AlignSelfAndJustifySelf extends AlignSelf, JustifySelf {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "colspan",
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.POSITION))
    public interface Colspan {
    }

    @StudioPropertyGroup
    public interface AlignSelfAndJustifySelfAndColspan extends AlignSelfAndJustifySelf, Colspan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "classNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface ClassNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "css",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface Css {
    }

    @StudioPropertyGroup
    public interface ClassNamesAndCss extends ClassNames, Css {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "enabled",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL))
    public interface Enabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "enabled",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL, defaultValue = "true"))
    public interface EnabledWithTrueDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "height",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface HeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxHeight",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MaxHeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minHeight",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MinHeightWithoutOptions {

    }

    @StudioPropertyGroup
    public interface MinAndMaxHeightWithoutOptions extends MinHeightWithoutOptions, MaxHeightWithoutOptions {
    }

    @StudioPropertyGroup
    public interface HeightWithoutOptionsGroup extends HeightWithoutOptions, MinAndMaxHeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface WidthWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxWidth",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MaxWidthWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minWidth",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
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

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "height",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface Height {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxHeight",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface MaxHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minHeight",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface MinHeight {
    }

    @StudioPropertyGroup
    public interface MinAndMaxHeight extends MinHeight, MaxHeight {
    }

    @StudioPropertyGroup
    public interface HeightGroup extends Height, MinAndMaxHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface Width {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxWidth",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface MaxWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minWidth",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
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

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "size",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface SizeProperty {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "ariaLabel",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface AriaLabel {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "ariaLabelledBy",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface AriaLabelledBy {
    }

    @StudioPropertyGroup
    public interface AriaLabelAndLabelledBy extends AriaLabel, AriaLabelledBy {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "tabIndex",
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL))
    public interface TabIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "focusShortcut",
            type = StudioPropertyType.SHORTCUT_COMBINATION,
            category = StudioProperty.Category.GENERAL))
    public interface FocusShortcut {
    }

    @StudioPropertyGroup
    public interface AriaLabelAndTabIndexAndFocusShortcut extends AriaLabel, AriaLabelledBy, TabIndex, FocusShortcut {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "required",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.VALIDATION,
            defaultValue = "false"))
    public interface Required {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "requiredMessage",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface RequiredMessage {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "errorMessage",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface ErrorMessage {
    }

    @StudioPropertyGroup
    public interface RequiredAndRequiredMessageAndErrorMessage extends Required, RequiredMessage, ErrorMessage {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "acceptedFileTypes",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.VALIDATION))
    public interface AcceptedFileTypes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "action",
            type = StudioPropertyType.ACTION_REF,
            classFqn = "io.jmix.flowui.kit.action.Action"))
    public interface Action {
    }

    @StudioPropertyGroup(properties =
    @StudioProperty(xmlAttribute = "actionVariant",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            setMethod = "setVariant",
            classFqn = "io.jmix.flowui.kit.action.ActionVariant",
            defaultValue = "DEFAULT",
            options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}))
    public interface ActionVariantWithDefaultDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "actionVariant",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            setMethod = "setVariant",
            classFqn = "io.jmix.flowui.kit.action.ActionVariant",
            defaultValue = "DANGER",
            options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}))
    public interface ActionVariantWithDangerDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "actionVariant",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            setMethod = "setVariant",
            classFqn = "io.jmix.flowui.kit.action.ActionVariant",
            defaultValue = "PRIMARY",
            options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}))
    public interface ActionVariantWithPrimaryDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "aggregatable",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Aggregatable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "aggregationPosition",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.flowui.component.grid.EnhancedDataGrid$AggregationPosition",
            defaultValue = "BOTTOM",
            options = {"TOP", "BOTTOM"}))
    public interface AggregationPosition {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "alignItems",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
            defaultValue = "START",
            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface AlignItems {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "alignSelf",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface GridAlignSelf {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "allRowsVisible",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface AllRowsVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "allowCustomValue",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface AllowCustomValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "allowedCharPattern",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface AllowedCharPattern {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "allowedCharPattern",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface AllowedCharPatternWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "alternateText",
            type = StudioPropertyType.LOCALIZED_STRING,
            setMethod = "setAlt"))
    public interface AlternateText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "autoApply",
            type = StudioPropertyType.BOOLEAN))
    public interface AutoApply {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "auto",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false",
            initialValue = "true"))
    public interface Auto {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "autoOpen",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface AutoOpen {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "autoWidth",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.SIZE,
            defaultValue = "false"))
    public interface AutoWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "autocapitalize",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize",
            defaultValue = "NONE",
            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}))
    public interface Autocapitalize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "autocomplete",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.textfield.Autocomplete",
            defaultValue = "OFF",
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
                    "TEL_EXTENSION", "URL", "PHOTO"}))
    public interface Autocomplete {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "autocorrect",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface Autocorrect {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "autofocus",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface Autofocus {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "autoselect",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface Autoselect {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "bean",
            type = StudioPropertyType.STRING,
            required = true))
    public interface Bean {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "boxSizing",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.SIZE,
            setParameterFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
            classFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
            defaultValue = "UNDEFINED",
            options = {"UNDEFINED", "CONTENT_BOX", "BORDER_BOX"}))
    public interface BoxSizing {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "cellTitle",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface CellTitle {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "charCode",
            type = StudioPropertyType.STRING))
    public interface CharCode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "checkSeconds",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface CheckSeconds {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "checkable",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Checkable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "checked",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Checked {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "class",
            type = StudioPropertyType.ENTITY_CLASS,
            required = true))
    public interface RequiredEntityClass {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "class",
            type = StudioPropertyType.ENTITY_CLASS))
    public interface EntityClass {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "class",
            type = StudioPropertyType.FRAGMENT_CLASS,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredFragmentClass {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "clearButtonAriaLabel",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface ClearButtonAriaLabel {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "clearButtonVisible",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface ClearButtonVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "clickShortcut",
            type = StudioPropertyType.SHORTCUT_COMBINATION))
    public interface ClickShortcut {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "color",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface Color {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "columnRendering",
            type = StudioPropertyType.ENUMERATION,
            defaultValue = "EAGER",
            classFqn = "com.vaadin.flow.component.grid.ColumnRendering",
            options = {"EAGER", "LAZY"}))
    public interface ColumnRendering {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "columnReorderingAllowed",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface ColumnReorderingAllowed {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "columns",
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface Columns {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "componentPrefix",
            type = StudioPropertyType.STRING,
            defaultValue = "component_"))
    public interface ComponentPrefix {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "connectingStatusText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface ConnectingStatusText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "containerPrefix",
            type = StudioPropertyType.STRING,
            defaultValue = "container_"))
    public interface ContainerPrefix {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "content",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Content {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "dataContainer",
            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
            category = StudioProperty.Category.DATA_BINDING,
            required = true,
            typeParameter = "E"))
    public interface CollectionDataContainer {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "dataContainer",
            type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF,
            category = StudioProperty.Category.DATA_BINDING))
    public interface CollectionOrInstanceDataContainer {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "dataLoader",
            type = StudioPropertyType.DATA_LOADER_REF,
            category = StudioProperty.Category.DATA_BINDING,
            required = true))
    public interface DataLoader {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "datatype",
            type = StudioPropertyType.DATATYPE_ID,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface Datatype {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "defaultValue",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface DefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "defaultValue",
            type = StudioPropertyType.STRING))
    public interface DefaultValueWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "description",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Description {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "detailsVisibleOnClick",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface DetailsVisibleOnClick {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "dropAllowed",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface DropAllowed {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "dropMode",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.dnd.GridDropMode",
            options = {"BETWEEN", "ON_TOP",
                    "ON_TOP_OR_BETWEEN", "ON_GRID"}))
    public interface DropMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "editable",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Editable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "editorBuffered",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface EditorBuffered {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "emptyStateText",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface EmptyStateText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "escapeValueForLike",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface EscapeValueForLike {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "exclude",
            type = StudioPropertyType.STRING))
    public interface Exclude {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "expand",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.POSITION))
    public interface Expand {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "fetchPlan",
            type = StudioPropertyType.FETCH_PLAN))
    public interface FetchPlan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "fileNameVisible",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface FileNameVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "fileNotSelectedText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface FileNotSelectedText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "fileStorageName",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface FileStorageName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "fileStoragePutMode",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            options = {"MANUAL", "IMMEDIATE"},
            defaultValue = "IMMEDIATE"))
    public interface FileStoragePutMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "fileTooBigText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface FileTooBigText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "firstResult",
            type = StudioPropertyType.INTEGER,
            defaultValue = "0"))
    public interface FirstResult {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "flexGrow",
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.POSITION))
    public interface FlexGrow {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "fontFamily",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
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
            type = StudioPropertyType.LOCALIZED_STRING,
            required = true))
    public interface Format {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "format",
            type = StudioPropertyType.STRING))
    public interface StringFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "frozen",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.POSITION,
            defaultValue = "false"))
    public interface Frozen {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "gap",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
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
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface HelperText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "hideDelay",
            type = StudioPropertyType.INTEGER))
    public interface HideDelay {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "iconAfterText",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface IconAfterText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "iconClassNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface IconClassNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.GENERAL))
    public interface Icon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            setParameterFqn = "com.vaadin.flow.component.icon.Icon"))
    public interface LookAndFeelIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "includeAll",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
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
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface Indeterminate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "itemsContainer",
            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "T"))
    public interface ItemsContainerTypeParameterT {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "itemsContainer",
            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface ItemsContainerTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "itemsEnum",
            type = StudioPropertyType.ENUM_CLASS,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface ItemsEnum {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "justifyContent",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
            setMethod = "setJustifyContentMode", defaultValue = "START",
            options = {"START", "END", "CENTER", "BETWEEN", "AROUND", "EVENLY"}))
    public interface JustifyContent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "key",
            type = StudioPropertyType.STRING))
    public interface Key {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "label",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Label {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "labelPosition",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "io.jmix.flowui.component.SupportsLabelPosition$LabelPosition",
            options = {"ASIDE", "TOP"}, defaultValue = "ASIDE")

    )
    public interface LabelPosition {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "labelVisible",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "true"))
    public interface LabelVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "labelWidth",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface LabelWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "ligature",
            type = StudioPropertyType.STRING))
    public interface Ligature {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "loadMenuConfig",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "true"))
    public interface LoadMenuConfig {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "margin",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface Margin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "max",
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationDoubleMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxFileSize",
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.VALIDATION))
    public interface MaxFileSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "max",
            type = StudioPropertyType.INTEGER))
    public interface IntegerMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxLength",
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.VALIDATION))
    public interface MaxLength {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "maxResults",
            type = StudioPropertyType.INTEGER,
            defaultValue = "0"))
    public interface MaxResults {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "max",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationStringMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "value",
            type = StudioPropertyType.STRING,
            required = true))
    public interface RequiredStringValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "value",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface StringValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "value",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL))
    public interface BooleanValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "value",
            type = StudioPropertyType.BIG_DECIMAL,
            required = true))
    public interface RequiredBigDecimalValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "value",
            type = StudioPropertyType.DOUBLE,
            required = true))
    public interface RequiredDoubleValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "value",
            type = StudioPropertyType.INTEGER,
            required = true))
    public interface RequiredIntegerValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "integer",
            type = StudioPropertyType.INTEGER,
            required = true))
    public interface RequiredInteger {

    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "message",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Message {
    }

    @StudioPropertyGroup
    public interface MessageAndCheckSeconds extends Message, CheckSeconds {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "messagesGroup",
            type = StudioPropertyType.STRING))
    public interface MessagesGroup {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "metaClass",
            type = StudioPropertyType.ENTITY_NAME,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface MetaClassTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "metaClass",
            type = StudioPropertyType.ENTITY_NAME,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "E"))
    public interface MetaClassTypeParameterE {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "min",
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationDoubleMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "min",
            type = StudioPropertyType.INTEGER))
    public interface IntegerMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minLength",
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.VALIDATION))
    public interface MinLength {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "min",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationStringMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minWidth",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.GENERAL,
            options = {"AUTO", "100%"}, required = true))
    public interface RequiredMinWidthWithOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "multiSort",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface MultiSort {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "multiSortOnShiftClickOnly",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface MultiSortOnShiftClickOnly {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "multiSortPriority",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.Grid.MultiSortPriority",
            options = {"APPEND", "PREPEND"},
            defaultValue = "PREPEND"))
    public interface MultiSortPriority {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "name",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Name {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "nestedNullBehavior",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.Grid.NestedNullBehavior",
            options = {"THROW", "ALLOW_NULLS"},
            defaultValue = "THROW"))
    public interface NestedNullBehavior {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "nullRepresentation",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface NullRepresentation {
    }

    @StudioPropertyGroup
    public interface FormatAndNullRepresentation extends Format, NullRepresentation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "openOnHover",
            category = StudioProperty.Category.GENERAL,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface OpenOnHover {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "opened",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Opened {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "operationEditable",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface OperationEditable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "operationTextVisible",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "true"))
    public interface OperationTextVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "overlayWidth",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface OverlayWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "padding",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "true"))
    public interface Padding {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "pageSize",
            type = StudioPropertyType.INTEGER))
    public interface PageSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "pageSize",
            type = StudioPropertyType.INTEGER,
            defaultValue = "50"))
    public interface PageSizeWithDefaultValue50 {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "param",
            type = StudioPropertyType.STRING))
    public interface Param {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "parameterName",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface ParameterName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "parameterName",
            type = StudioPropertyType.STRING))
    public interface ParameterNameWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "parameterClass",
            type = StudioPropertyType.STRING,
            typeParameter = "V",
            required = true))
    public interface RequiredParameterClassTypeParameterVWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "parameterClass",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            typeParameter = "V",
            required = true))
    public interface RequiredParameterClassTypeParameterVWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "password",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Password {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "pattern",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface Pattern {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "placeholder",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Placeholder {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "processingStatusText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface ProcessingStatusText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "property",
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "T",
            required = true))
    public interface PropertyTypeParameterT {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "property",
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface PropertyTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "property",
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V",
            required = true))
    public interface RequiredPropertyTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "operation",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter$Operation",
            options = {"EQUAL", "NOT_EQUAL", "GREATER",
                    "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS", "NOT_CONTAINS",
                    "STARTS_WITH", "ENDS_WITH", "IS_SET", "IN_LIST", "NOT_IN_LIST", "IN_INTERVAL",
                    "DATE_EQUALS", "IS_COLLECTION_EMPTY", "MEMBER_OF_COLLECTION",
                    "NOT_MEMBER_OF_COLLECTION"},
            required = true))
    public interface RequiredPropertyFilterOperation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "operationsList",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.GENERAL,
            options = {"EQUAL", "NOT_EQUAL", "GREATER",
                    "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS", "NOT_CONTAINS",
                    "STARTS_WITH", "ENDS_WITH", "IS_SET", "IN_LIST", "NOT_IN_LIST", "IN_INTERVAL",
                    "DATE_EQUALS", "IS_COLLECTION_EMPTY", "MEMBER_OF_COLLECTION",
                    "NOT_MEMBER_OF_COLLECTION"}))
    public interface PropertyFilterOperationsList {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "property",
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.DATA_BINDING))
    public interface Property {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "query",
            type = StudioPropertyType.JPA_QUERY))
    public interface Query {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "readOnly",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface ReadOnly {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "readOnly",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface ReadOnlyWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "remainingTimeText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface RemainingTimeText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "remainingTimeUnknownText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface RemainingTimeUnknownText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "resource",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Resource {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "resource",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            setMethod = "setSrc"))
    public interface ImageResource {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "rotation",
            type = StudioPropertyType.DOUBLE))
    public interface Rotation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "rowsDraggable",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface RowsDraggable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "searchStringFormat",
            type = StudioPropertyType.STRING))
    public interface SearchStringFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "selectionMode",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            classFqn = "com.vaadin.flow.component.grid.Grid.SelectionMode",
            defaultValue = "SINGLE",
            options = {"SINGLE", "MULTI", "NONE"}))
    public interface SelectionMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "shortcutCombination",
            type = StudioPropertyType.SHORTCUT_COMBINATION,
            category = StudioProperty.Category.GENERAL))
    public interface ShortcutCombination {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "showInContextMenuEnabled",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface ShowInContextMenuEnabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "sortable",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValueRef = "parent:sortable"))
    public interface ColumnSortable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "sortable",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface Sortable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "spacing",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.POSITION,
            defaultValue = "true"))
    public interface Spacing {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "stepButtonsVisible",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface StepButtonsVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "step",
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.GENERAL))
    public interface Step {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "step",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            options = {"900s", "15m", "20m", "30m", "2h", "3h", "4h", "6h", "8h", "12h"}))
    public interface TimeStep {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "store",
            type = StudioPropertyType.STORE))
    public interface Store {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "strategyClass",
            type = StudioPropertyType.STRING))
    public interface StrategyClass {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "submit",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Submit {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "summaryText",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface DetailsSummaryText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "summaryText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface SummaryText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "symbol",
            type = StudioPropertyType.STRING))
    public interface Symbol {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "text",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Text {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "text",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"small", "align-start", "align-end", "align-left", "align-center", "align-right", "helper-above-field"}))
    public interface TextInputFieldThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"small", "align-start", "align-end", "align-center", "align-right", "helper-above-field"}))
    public interface TextAreaThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"filled", "reverse", "small"}))
    public interface DetailsThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "warning",
                    "error", "contrast", "icon", "contained", "outlined"}))
    public interface ButtonThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"contrast", "error", "success"}))
    public interface ProgressBarThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"non-checkable"}))
    public interface UserMenuItemThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"fill", "contain", "cover", "scale-down"}))
    public interface ImageThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"spacing-xs", "spacing-s", "spacing", "spacing-l", "spacing-xl"}))
    public interface LayoutThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"no-border", "no-row-borders", "column-borders", "row-stripes",
                    "compact", "wrap-cell-content", "column-dividers"}))
    public interface GridThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}))
    public interface HtmlComponentThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface ThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "themeNames",
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill"}))
    public interface NativeLabelThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "title",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Title {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "title",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface TitleWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "trimEnabled",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true",
            defaultValueRef = "application_property:jmix.ui.component.default-trim-enabled"))
    public interface TrimEnabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "uploadDialogCancelText",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface UploadDialogCancelText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "uploadDialogTitle",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface UploadDialogTitle {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "uploadIcon",
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface UploadIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "uploadText",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface UploadText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "username",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Username {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "valueChangeMode",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.data.value.ValueChangeMode",
            options = {"EAGER", "LAZY", "TIMEOUT", "ON_BLUR", "ON_CHANGE"}))
    public interface ValueChangeMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "valueChangeTimeout",
            type = StudioPropertyType.INTEGER))
    public interface ValueChangeTimeout {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "visible",
            type = StudioPropertyType.BOOLEAN, defaultValue = "true"))
    public interface VisibleWithDefaultValueTrue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "weekNumbersVisible",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface WeekNumbersVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "useUserTimezone",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface UseUserTimezone {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "type",
            type = StudioPropertyType.ENUMERATION,
            options = {"DATE", "DATETIME"}))
    public interface DateFormatterType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "whiteSpace",
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace",
            defaultValue = "NORMAL",
            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT", "INITIAL"}))
    public interface WhiteSpace {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width",
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.SIZE))
    public interface WidthWithIntegerType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"},
            defaultValue = "100%"))
    public interface WidthWithDefaultValue100 {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"},
            defaultValue = "AUTO"))
    public interface WidthWithDefaultValueAuto {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"},
            defaultValue = "UNDEFINED"))
    public interface WidthWithDefaultValueUndefined {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "width",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"},
            initialValue = "100%"))
    public interface WidthWithInitialValue100 {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "wrap",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface Wrap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "description",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface DescriptionWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "enabled",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface EnabledWithTrueDefaultValueWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            initialValue = "CHECK",
            setParameterFqn = "com.vaadin.flow.component.icon.Icon"))
    public interface IconWithCheckInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            initialValue = "BAN",
            setParameterFqn = "com.vaadin.flow.component.icon.Icon"))
    public interface IconWithBanInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            initialValue = "PENCIL",
            setParameterFqn = "com.vaadin.flow.component.icon.Icon"))
    public interface IconWithPencilInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            type = StudioPropertyType.ICON))
    public interface IconWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "key",
            type = StudioPropertyType.STRING,
            initialValue = "editorActionsColumn"))
    public interface KeyWithEditorActionsColumnInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "minWidth",
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"},
            initialValue = "100px"))
    public interface MinWidthWithInitialValue100px {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "name",
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface LocalizedNameWithoutCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "name",
            type = StudioPropertyType.STRING, required = true))
    public interface RequiredStringName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "type",
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface StringType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "type",
            type = StudioPropertyType.ENUMERATION,
            options = {"CONTAINER_REF", "LOADER_REF", "ICON"}))
    public interface TypeContainerRefLoaderRefIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "resizable",
            type = StudioPropertyType.BOOLEAN,
            defaultValueRef = "parent:resizable"))
    public interface ResizableWithoutCategoryWithParentDefaultValueRef {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "resizable",
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface ResizableWithFalseDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "resizable",
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.SIZE,
            defaultValueRef = "parent:resizable"))
    public interface ResizableWithParentDefaultValueRef {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "text",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL,
            initialValue = "msg:///actions.Cancel"))
    public interface TextWithCancelInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "text",
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL,
            initialValue = "msg:///actions.Edit"))
    public interface TextWithEditInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "type",
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.flowui.component.AggregationInfo$Type",
            options = {"SUM", "COUNT", "AVG", "MIN", "MAX"}))
    public interface AggregationType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "id",
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL,
            required = true,
            initialValue = "create"))
    public interface RequiredIdWithCreateInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(xmlAttribute = "icon",
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "PLUS",
            setParameterFqn = "com.vaadin.flow.component.icon.Icon"))
    public interface LookAndFeelIconWithPlusDefaultValue {
    }

    @StudioPropertyGroup
    public interface HtmlTextElementDefaultProperties extends ClassNames, ClickShortcut, Css,
            EnabledWithTrueDefaultValue, Size, Id, Text, NativeLabelThemeNames, Title, Visible, WhiteSpace {
    }

    @StudioPropertyGroup
    public interface EditorActionButtonDefaultProperties extends Title, ShortcutCombination, WhiteSpace,
            ButtonThemeNames, ClassNames, IconAfterText {
    }

    @StudioPropertyGroup
    public interface EditorActionTextButtonDefaultProperties extends EditorActionButtonDefaultProperties, Text {
    }

    @StudioPropertyGroup
    public interface LoginFormDefaultProperties extends ForgotPassword, Password, Submit, Title, Username {
    }

    @StudioPropertyGroup
    public interface UserMenuItemDefaultProperties extends RequiredId, Visible, Enabled, Checkable, Checked, UserMenuItemThemeNames {
    }

    @StudioPropertyGroup
    public interface TextUserMenuItemDefaultProperties extends UserMenuItemDefaultProperties, RequiredText, Icon {
    }

    @StudioPropertyGroup
    public interface IdAndParam extends Id, Param {
    }

    @StudioPropertyGroup
    public interface ResponsiveStepDefaultProperties extends RequiredMinWidthWithOptions, Columns {
    }

    @StudioPropertyGroup
    public interface BasicComponentDefaultProperties extends AlignSelfAndJustifySelfAndColspan, IdAndVisible, Css {
    }

    @StudioPropertyGroup
    public interface AddonComponentDefaultProperties extends ClassNamesAndCss, IdAndVisible, Size, AlignSelf, Colspan {
    }

    @StudioPropertyGroup
    public interface SizedComponentDefaultProperties extends AlignSelfAndJustifySelfAndColspan, ClassNamesAndCss,
            IdAndVisible, EnabledWithTrueDefaultValue, Size {
    }

    @StudioPropertyGroup
    public interface FieldThemeTitleAndValueChangeDefaultProperties extends TextInputFieldThemeNames, Title,
            ValueChangeMode, ValueChangeTimeout {
    }

    @StudioPropertyGroup
    public interface IconDefaultProperties extends AlignSelfAndJustifySelfAndColspan, ClassNamesAndCss,
            IdAndVisible, ClickShortcut, Color, RequiredIcon, SizeProperty {
    }

    @StudioPropertyGroup
    public interface SvgIconDefaultProperties extends AlignSelfAndJustifySelfAndColspan, ClassNamesAndCss,
            IdAndVisible, ClickShortcut, Color, Resource, SizeProperty, Symbol {
    }

    @StudioPropertyGroup
    public interface FontIconDefaultProperties extends AlignSelfAndJustifySelfAndColspan, ClassNamesAndCss,
            IdAndVisible, CharCode, ClickShortcut, Color, FontFamily, IconClassNames,
            Ligature, SizeProperty {
    }

    @StudioPropertyGroup
    public interface FieldDefaultProperties extends SizedComponentDefaultProperties,
            AriaLabelAndTabIndexAndFocusShortcut, RequiredAndRequiredMessageAndErrorMessage,
            CollectionOrInstanceDataContainer, HelperText, Label, ReadOnly {
    }

    @StudioPropertyGroup
    public interface TextInputFieldDefaultProperties extends FieldDefaultProperties, Autocapitalize, Autocomplete,
            Autocorrect, Autofocus, Autoselect, ClearButtonVisible, Placeholder {
    }

    @StudioPropertyGroup
    public interface MultiSelectComboBoxDefaultProperties extends AlignSelfAndJustifySelfAndColspan,
            ClassNamesAndCss, IdAndVisible, EnabledWithTrueDefaultValue, Size,
            AriaLabelAndTabIndexAndFocusShortcut, AllowCustomValue, AllowedCharPattern, Autofocus, AutoOpen,
            OverlayWidth, ClearButtonVisible, CollectionOrInstanceDataContainer, ErrorMessage, HelperText,
            ItemsContainerTypeParameterV, ItemsEnum, Label, MetaClassTypeParameterV, Opened, PageSize,
            Placeholder, PropertyTypeParameterV, ReadOnly, Required, TextInputFieldThemeNames, Title {
    }

    @StudioPropertyGroup
    public interface ListBoxDefaultProperties extends AlignSelfAndJustifySelfAndColspan, ClassNamesAndCss,
            IdAndVisible, EnabledWithTrueDefaultValue, Size, AriaLabel, AriaLabelledBy,
            ItemsContainerTypeParameterV, ItemsEnum, ReadOnly {
    }

    @StudioPropertyGroup
    public interface ValuePickerDefaultProperties extends AlignSelfAndJustifySelfAndColspan, ClassNamesAndCss,
            IdAndVisible, EnabledWithTrueDefaultValue, Size, AriaLabelAndTabIndexAndFocusShortcut,
            RequiredAndRequiredMessageAndErrorMessage, AllowCustomValue, Autofocus, ReadOnly, ThemeNames, Title,
            CollectionOrInstanceDataContainer, HelperText, Label, Placeholder, PropertyTypeParameterV {
    }

    @StudioPropertyGroup
    public interface AutoWidthLayoutDefaultProperties extends AlignSelfAndJustifySelfAndColspan,
            ClassNamesAndCss, IdAndVisible, Height, MaxHeight, MaxWidth, MinHeight, MinWidth, WidthWithDefaultValueAuto {
    }

    @StudioPropertyGroup
    public interface EnabledAutoWidthLayoutDefaultProperties extends AutoWidthLayoutDefaultProperties, EnabledWithTrueDefaultValue {
    }

    @StudioPropertyGroup
    public interface DataLoadCoordinatorDefaultProperties extends IdWithoutCategory, Auto, ComponentPrefix, ContainerPrefix {
    }

    @StudioPropertyGroup
    public interface SettingsDefaultProperties extends IdWithoutCategory, Auto {
    }

    @StudioPropertyGroup
    public interface RequiredStringNameAndValue extends RequiredStringName, RequiredStringValue {
    }

    @StudioPropertyGroup
    public interface RequiredStringNameAndValueAndType extends RequiredStringNameAndValue, TypeContainerRefLoaderRefIcon {
    }

    @StudioPropertyGroup
    public interface MessageAndInclusiveRequiredBigDecimalValue extends Message, Inclusive, RequiredBigDecimalValue {
    }

    @StudioPropertyGroup
    public interface MessageAndInclusiveRequiredDoubleValue extends Message, Inclusive, RequiredDoubleValue {
    }

    @StudioPropertyGroup
    public interface MessageAndRequiredIntegerValue extends Message, RequiredIntegerValue {
    }

    @StudioPropertyGroup
    public interface MessagesGroupAndTitle extends MessagesGroup, Title {
    }
}
