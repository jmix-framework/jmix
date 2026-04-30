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

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL))
    public interface Id {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredId {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "true"))
    public interface Visible {
    }

    @StudioPropertyGroup
    public interface IdAndVisible extends Id, Visible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALIGN_SELF,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
            defaultValue = "AUTO",
            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface AlignSelf {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "io.jmix.flowui.kit.component.Alignment",
            defaultValue = "AUTO",
            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface JustifySelf {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLSPAN,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.POSITION))
    public interface Colspan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLASS_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface ClassNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CSS,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface Css {
    }

    @StudioPropertyGroup
    public interface ClassNamesAndCss extends ClassNames, Css {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ENABLED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL))
    public interface EnabledWithoutDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ENABLED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "true"))
    public interface Enabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HEIGHT,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface HeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HIERARCHY_COLUMN,
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.GENERAL,
            typeParameter = "E"))
    public interface HierarchyColumnTypeParameterE {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HIERARCHY_PROPERTY,
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.GENERAL,
            required = true,
            typeParameter = "E"))
    public interface RequiredHierarchyPropertyTypeParameterE {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_HEIGHT,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MaxHeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_HEIGHT,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MinHeightWithoutOptions {
    }

    @StudioPropertyGroup
    public interface MinAndMaxHeightWithoutOptions extends MinHeightWithoutOptions, MaxHeightWithoutOptions {
    }

    @StudioPropertyGroup
    public interface HasHeightWithoutOptions extends HeightWithoutOptions, MinAndMaxHeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface WidthWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MaxWidthWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface MinWidthWithoutOptions {
    }

    @StudioPropertyGroup
    public interface MinAndMaxWidthWithoutOptions extends MinWidthWithoutOptions, MaxWidthWithoutOptions {
    }

    @StudioPropertyGroup
    public interface HasWidthWithoutOptions extends WidthWithoutOptions, MinAndMaxWidthWithoutOptions {

    }

    @StudioPropertyGroup
    public interface HasSizeWithoutOptions extends HasWidthWithoutOptions, HasHeightWithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HEIGHT,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface Height {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_HEIGHT,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface MaxHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_HEIGHT,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface MinHeight {
    }

    @StudioPropertyGroup
    public interface MinAndMaxHeight extends MinHeight, MaxHeight {
    }

    @StudioPropertyGroup
    public interface HasHeightAttributes extends Height, MinAndMaxHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface Width {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface MaxWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            options = {"AUTO", "100%"}))
    public interface MinWidth {
    }

    @StudioPropertyGroup
    public interface MinAndMaxWidth extends MinWidth, MaxWidth {
    }

    @StudioPropertyGroup
    public interface HasWidthAttributes extends Width, MinAndMaxWidth {
    }

    @StudioPropertyGroup
    public interface HasSizeAttributes extends HasWidthAttributes, HasHeightAttributes {
    }

    @StudioPropertyGroup
    public interface BaseComponent extends IdAndVisible, AlignSelf, JustifySelf, Colspan, Css {
    }

    @StudioPropertyGroup
    public interface BaseSizedComponent extends BaseComponent, HasSizeAttributes {
    }

    @StudioPropertyGroup
    public interface BaseComponentWithClassNames extends BaseComponent, ClassNames {
    }

    @StudioPropertyGroup
    public interface BaseSizedComponentWithClassNames extends BaseSizedComponent, ClassNames {
    }

    @StudioPropertyGroup
    public interface BaseSizedEnabledComponentWithClassName extends BaseSizedComponentWithClassNames, Enabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SIZE,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface Size {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ARIA_LABEL,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface AriaLabel {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface AriaLabelledBy {
    }

    @StudioPropertyGroup
    public interface HasAriaLabel extends AriaLabel, AriaLabelledBy {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TAB_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface TabIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT,
            type = StudioPropertyType.SHORTCUT_COMBINATION))
    public interface FocusShortcut {
    }

    @StudioPropertyGroup
    public interface HasFocusableAttributes extends TabIndex, FocusShortcut {
    }

    @StudioPropertyGroup
    public interface HasAriaLabelAndFocusableAttributes extends HasAriaLabel, HasFocusableAttributes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REQUIRED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.VALIDATION,
            defaultValue = "false"))
    public interface Required {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REQUIRED_MESSAGE,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface RequiredMessage {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ERROR_MESSAGE,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface ErrorMessage {
    }

    @StudioPropertyGroup
    public interface RequiredAttributes extends Required, RequiredMessage {
    }

    @StudioPropertyGroup
    public interface ValidationAttributes extends ErrorMessage {
    }

    @StudioPropertyGroup
    public interface HasRequiredAndValidationAttributes extends RequiredAttributes, ValidationAttributes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ACCEPTED_FILE_TYPES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.VALIDATION))
    public interface AcceptedFileTypes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ACCEPTED_FILE_TYPES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.GENERAL))
    public interface AcceptedFileTypesWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AGGREGATABLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Aggregatable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AGGREGATION_POSITION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.flowui.component.grid.EnhancedDataGrid$AggregationPosition",
            defaultValue = "BOTTOM",
            options = {"TOP", "BOTTOM"}))
    public interface AggregationPosition {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALIGN_ITEMS,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
            defaultValue = "START",
            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"},
            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment"))
    public interface AlignItems {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALIGN_SELF,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}))
    public interface GridAlignSelf {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALL_ROWS_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface AllRowsVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALLOW_CUSTOM_VALUE,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface AllowCustomValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALLOWED_CHAR_PATTERN,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface AllowedCharPattern {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALTERNATE_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            setMethod = "setAlt"))
    public interface AlternateText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTO_APPLY,
            type = StudioPropertyType.BOOLEAN))
    public interface AutoApply {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTO,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false",
            initialValue = "true"))
    public interface Auto {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTO_OPEN,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface AutoOpen {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTO_WIDTH,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.SIZE,
            defaultValue = "false"))
    public interface AutoWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTOCAPITALIZE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize",
            defaultValue = "NONE",
            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}))
    public interface Autocapitalize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTOCOMPLETE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.textfield.Autocomplete",
            defaultValue = "OFF",
            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME", "FAMILY_NAME",
                    "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD", "CURRENT_PASSWORD",
                    "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS", "ADDRESS_LINE1", "ADDRESS_LINE2",
                    "ADDRESS_LINE3", "ADDRESS_LEVEL1", "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY",
                    "COUNTRY_NAME", "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE", "TRANSACTION_CURRENCY",
                    "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY", "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL",
                    "TEL_COUNTRY_CODE", "TEL_NATIONAL", "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX",
                    "TEL_LOCAL_SUFFIX", "TEL_EXTENSION", "URL", "PHOTO"}))
    public interface Autocomplete {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTOCORRECT,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface Autocorrect {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTOFOCUS,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface Autofocus {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTOSELECT,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface Autoselect {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BEAN,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface Bean {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BOX_SIZING,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.SIZE,
            classFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
            defaultValue = "UNDEFINED",
            options = {"UNDEFINED", "CONTENT_BOX", "BORDER_BOX"},
            setParameterFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing"))
    public interface BoxSizing {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CELL_TITLE,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface CellTitle {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CHAR_CODE,
            type = StudioPropertyType.STRING))
    public interface CharCode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CHECK_SECONDS,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface CheckSeconds {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CHECKABLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Checkable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CHECKED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Checked {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLASS,
            type = StudioPropertyType.ENTITY_CLASS,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredEntityClass {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLASS,
            type = StudioPropertyType.ENTITY_CLASS,
            category = StudioProperty.Category.GENERAL))
    public interface EntityClass {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLASS,
            type = StudioPropertyType.FRAGMENT_CLASS,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredFragmentClass {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLEAR_BUTTON_ARIA_LABEL,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface ClearButtonAriaLabel {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLEAR_BUTTON_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface ClearButtonVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT,
            type = StudioPropertyType.SHORTCUT_COMBINATION))
    public interface ClickShortcut {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT,
            type = StudioPropertyType.SHORTCUT_COMBINATION,
            category = StudioProperty.Category.GENERAL))
    public interface ClickShortcutWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLOR,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface Color {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLUMN_RENDERING,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.ColumnRendering",
            defaultValue = "EAGER",
            options = {"EAGER", "LAZY"}))
    public interface ColumnRendering {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLUMN_REORDERING_ALLOWED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface ColumnReorderingAllowed {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLUMNS,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface Columns {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COMPONENT_PREFIX,
            type = StudioPropertyType.STRING,
            defaultValue = "component_"))
    public interface ComponentPrefix {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CONNECTING_STATUS_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface ConnectingStatusText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CONTAINER_PREFIX,
            type = StudioPropertyType.STRING,
            defaultValue = "container_"))
    public interface ContainerPrefix {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CONTENT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Content {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATA_CONTAINER,
            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
            category = StudioProperty.Category.DATA_BINDING,
            required = true,
            typeParameter = "E"))
    public interface CollectionDataContainer {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATA_CONTAINER,
            type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF,
            category = StudioProperty.Category.DATA_BINDING))
    public interface CollectionOrInstanceDataContainer {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATA_LOADER,
            type = StudioPropertyType.DATA_LOADER_REF,
            category = StudioProperty.Category.DATA_BINDING,
            required = true))
    public interface DataLoader {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATATYPE,
            type = StudioPropertyType.DATATYPE_ID,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface Datatype {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DEFAULT_VALUE,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface DefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DESCRIPTION,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Description {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DETAILS_VISIBLE_ON_CLICK,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface DetailsVisibleOnClick {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DETAILS_VISIBLE_ON_CLICK,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface DetailsVisibleOnClickWithDefaultValueTrue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DROP_ALLOWED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface DropAllowed {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DROP_ALLOWED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "true"))
    public interface DropAllowedWithTrueDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DROP_MODE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.dnd.GridDropMode",
            options = {"BETWEEN", "ON_TOP", "ON_TOP_OR_BETWEEN", "ON_GRID"}))
    public interface DropMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EDITABLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Editable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EDITOR_BUFFERED,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface EditorBuffered {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EMPTY_STATE_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface EmptyStateText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ESCAPE_VALUE_FOR_LIKE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface EscapeValueForLike {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EXCLUDE,
            type = StudioPropertyType.STRING))
    public interface Exclude {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EXPAND,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.POSITION))
    public interface Expand {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FETCH_PLAN,
            type = StudioPropertyType.FETCH_PLAN,
            category = StudioProperty.Category.GENERAL))
    public interface FetchPlan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FILE_NAME_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface FileNameVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FILE_NOT_SELECTED_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface FileNotSelectedText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FILE_STORAGE_NAME,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface FileStorageName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FILE_STORAGE_PUT_MODE,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "IMMEDIATE",
            options = {"MANUAL", "IMMEDIATE"}))
    public interface FileStoragePutMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FILE_TOO_BIG_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface FileTooBigText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FIRST_RESULT,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "0"))
    public interface FirstResult {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FLEX_GROW,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.POSITION))
    public interface FlexGrow {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FONT_FAMILY,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface FontFamily {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FOOTER,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Footer {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FORGOT_PASSWORD,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface ForgotPassword {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING,
            required = true))
    public interface Format {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FORMAT,
            type = StudioPropertyType.STRING))
    public interface StringFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FROZEN,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.POSITION,
            defaultValue = "false"))
    public interface Frozen {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.GAP,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface Gap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HAS_IN_EXPRESSION,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface HasInExpression {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HEADER,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Header {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HELPER_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface HelperText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HIDE_DELAY,
            type = StudioPropertyType.INTEGER))
    public interface HideDelay {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON_AFTER_TEXT,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface IconAfterText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON_CLASS_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface IconClassNames {

    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            setParameterFqn = "com.vaadin.flow.component.Component"))
    public interface Icon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface IconString {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INCLUDE_ALL,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface IncludeAll {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INCLUSIVE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL))
    public interface Inclusive {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INCORRECT_FILE_TYPE_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface IncorrectFileTypeText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INDETERMINATE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface Indeterminate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ITEMS_CONTAINER,
            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "T"))
    public interface ItemsContainerTypeParameterT {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ITEMS_CONTAINER,
            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface ItemsContainerTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ITEMS_ENUM,
            type = StudioPropertyType.ENUM_CLASS,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface ItemsEnum {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.JUSTIFY_CONTENT,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
            defaultValue = "START",
            options = {"START", "END", "CENTER", "BETWEEN", "AROUND", "EVENLY"},
            setMethod = "setJustifyContentMode",
            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode"))
    public interface JustifyContent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.KEY,
            type = StudioPropertyType.STRING))
    public interface Key {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LABEL,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Label {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LABEL_POSITION,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.POSITION,
            classFqn = "io.jmix.flowui.component.SupportsLabelPosition$LabelPosition",
            defaultValue = "ASIDE",
            options = {"ASIDE", "TOP"}))
    public interface LabelPosition {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LABEL_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "true"))
    public interface LabelVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LABEL_WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE))
    public interface LabelWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LIGATURE,
            type = StudioPropertyType.STRING))
    public interface Ligature {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LOAD_MENU_CONFIG,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "true"))
    public interface LoadMenuConfig {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MARGIN,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface Margin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationDoubleMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.GENERAL))
    public interface DoubleMaxWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_FILE_SIZE,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.VALIDATION))
    public interface MaxFileSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL))
    public interface IntegerMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationIntegerMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_LENGTH,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.VALIDATION))
    public interface MaxLength {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_RESULTS,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "0"))
    public interface MaxResults {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationStringMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredStringValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface StringValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL))
    public interface BooleanValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.BIG_DECIMAL,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredBigDecimalValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredDoubleValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredIntegerValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INTEGER,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredInteger {

    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MESSAGE,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Message {
    }

    @StudioPropertyGroup
    public interface MessageAndCheckSeconds extends Message, CheckSeconds {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MESSAGES_GROUP,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface MessagesGroup {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.META_CLASS,
            type = StudioPropertyType.ENTITY_NAME,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface MetaClassTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.META_CLASS,
            type = StudioPropertyType.ENTITY_NAME,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "E"))
    public interface MetaClassTypeParameterE {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationDoubleMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.GENERAL))
    public interface DoubleMinWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL))
    public interface IntegerMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationIntegerMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_LENGTH,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.VALIDATION))
    public interface MinLength {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface ValidationStringMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.GENERAL,
            required = true,
            options = {"AUTO", "100%"}))
    public interface RequiredMinWidthWithOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MULTI_SORT,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface MultiSort {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MULTI_SORT_ON_SHIFT_CLICK_ONLY,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface MultiSortOnShiftClickOnly {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MULTI_SORT_PRIORITY,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.Grid.MultiSortPriority",
            defaultValue = "PREPEND",
            options = {"APPEND", "PREPEND"}))
    public interface MultiSortPriority {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Name {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NESTED_NULL_BEHAVIOR,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.grid.Grid.NestedNullBehavior",
            defaultValue = "THROW",
            options = {"THROW", "ALLOW_NULLS"}))
    public interface NestedNullBehavior {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NULL_REPRESENTATION,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface NullRepresentation {
    }

    @StudioPropertyGroup
    public interface FormatAndNullRepresentation extends Format, NullRepresentation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPEN_ON_HOVER,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface OpenOnHover {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPENED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface Opened {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPERATION_EDITABLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface OperationEditable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPERATION_TEXT_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "true"))
    public interface OperationTextVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OVERLAY_WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface OverlayWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PADDING,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "true"))
    public interface PaddingWithTrueDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PADDING,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface PaddingWithFalseDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PAGE_SIZE,
            type = StudioPropertyType.INTEGER))
    public interface PageSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PAGE_SIZE,
            type = StudioPropertyType.INTEGER,
            defaultValue = "50"))
    public interface PageSizeWithDefaultValue50 {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PARAM,
            type = StudioPropertyType.STRING))
    public interface Param {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PARAMETER_NAME,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface ParameterName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PARAMETER_CLASS,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            required = true,
            typeParameter = "V"))
    public interface RequiredParameterClassTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PASSWORD,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Password {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PATTERN,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.VALIDATION))
    public interface Pattern {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PLACEHOLDER,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Placeholder {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROCESSING_STATUS_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface ProcessingStatusText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROPERTY,
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.DATA_BINDING,
            required = true,
            typeParameter = "T"))
    public interface PropertyTypeParameterT {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROPERTY,
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.DATA_BINDING,
            typeParameter = "V"))
    public interface PropertyTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROPERTY,
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.DATA_BINDING,
            required = true,
            typeParameter = "V"))
    public interface RequiredPropertyTypeParameterV {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPERATION,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter$Operation",
            required = true,
            options = {"EQUAL", "NOT_EQUAL", "GREATER", "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS",
                    "NOT_CONTAINS", "STARTS_WITH", "ENDS_WITH", "IS_SET", "IN_LIST", "NOT_IN_LIST", "IN_INTERVAL",
                    "DATE_EQUALS", "IS_COLLECTION_EMPTY", "MEMBER_OF_COLLECTION", "NOT_MEMBER_OF_COLLECTION"}))
    public interface RequiredPropertyFilterOperation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPERATIONS_LIST,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.GENERAL,
            options = {"EQUAL", "NOT_EQUAL", "GREATER", "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS",
                    "NOT_CONTAINS", "STARTS_WITH", "ENDS_WITH", "IS_SET", "IN_LIST", "NOT_IN_LIST", "IN_INTERVAL",
                    "DATE_EQUALS", "IS_COLLECTION_EMPTY", "MEMBER_OF_COLLECTION", "NOT_MEMBER_OF_COLLECTION"}))
    public interface PropertyFilterOperationsList {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROPERTY,
            type = StudioPropertyType.PROPERTY_REF,
            category = StudioProperty.Category.DATA_BINDING))
    public interface Property {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.QUERY,
            type = StudioPropertyType.JPA_QUERY,
            category = StudioProperty.Category.GENERAL))
    public interface Query {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.READ_ONLY,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface ReadOnly {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REMAINING_TIME_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface RemainingTimeText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REMAINING_TIME_UNKNOWN_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface RemainingTimeUnknownText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RESOURCE,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Resource {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RESOURCE,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            setMethod = "setSrc"))
    public interface ImageResource {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ROTATION,
            type = StudioPropertyType.DOUBLE))
    public interface Rotation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ROWS_DRAGGABLE,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface RowsDraggable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SEARCH_STRING_FORMAT,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface SearchStringFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SELECTION_MODE,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            classFqn = "com.vaadin.flow.component.grid.Grid.SelectionMode",
            defaultValue = "SINGLE",
            options = {"SINGLE", "MULTI", "NONE"}))
    public interface SelectionMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION,
            type = StudioPropertyType.SHORTCUT_COMBINATION,
            category = StudioProperty.Category.GENERAL))
    public interface ShortcutCombination {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHOW_IN_CONTEXT_MENU_ENABLED,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface ShowInContextMenuEnabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHOW_ORPHANS,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface ShowOrphans {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SORTABLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValueRef = "parent:sortable"))
    public interface ColumnSortable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SORTABLE,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface Sortable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SPACING,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.POSITION,
            defaultValue = "true"))
    public interface Spacing {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STEP_BUTTONS_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface StepButtonsVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STEP,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.GENERAL))
    public interface Step {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STEP,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            options = {"900s", "15m", "20m", "30m", "2h", "3h", "4h", "6h", "8h", "12h"}))
    public interface TimeStep {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STORE,
            type = StudioPropertyType.STORE,
            category = StudioProperty.Category.GENERAL))
    public interface Store {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STRATEGY_CLASS,
            type = StudioPropertyType.STRING))
    public interface StrategyClass {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SUBMIT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Submit {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SUMMARY_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface DetailsSummaryText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SUMMARY_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface SummaryText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOL,
            type = StudioPropertyType.STRING))
    public interface Symbol {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Text {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"small", "align-start", "align-end", "align-left", "align-center", "align-right",
                    "helper-above-field"}))
    public interface TextInputFieldThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"small", "align-start", "align-end", "align-center", "align-right", "helper-above-field"}))
    public interface TextAreaThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"filled", "reverse", "small"}))
    public interface DetailsThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "warning", "error",
                    "contrast", "icon", "contained", "outlined"}))
    public interface ButtonThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"contrast", "error", "success"}))
    public interface ProgressBarThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"non-checkable"}))
    public interface UserMenuItemThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"fill", "contain", "cover", "scale-down"}))
    public interface ImageThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"spacing-xs", "spacing-s", "spacing", "spacing-l", "spacing-xl"}))
    public interface LayoutThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"no-border", "no-row-borders", "column-borders", "row-stripes", "compact", "wrap-cell-content",
                    "column-dividers"}))
    public interface GridThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}))
    public interface HtmlComponentThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface ThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill"}))
    public interface NativeLabelThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TITLE,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Title {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TRIM_ENABLED,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true",
            defaultValueRef = "application_property:jmix.ui.component.default-trim-enabled"))
    public interface TrimEnabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.UPLOAD_DIALOG_CANCEL_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface UploadDialogCancelText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.UPLOAD_DIALOG_TITLE,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface UploadDialogTitle {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.UPLOAD_ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface UploadIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.UPLOAD_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL))
    public interface UploadText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.USERNAME,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Username {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_MODE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.data.value.ValueChangeMode",
            options = {"EAGER", "LAZY", "TIMEOUT", "ON_BLUR", "ON_CHANGE"}))
    public interface ValueChangeMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_MODE,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            classFqn = "com.vaadin.flow.data.value.ValueChangeMode",
            options = {"EAGER", "LAZY", "TIMEOUT", "ON_BLUR", "ON_CHANGE"}))
    public interface ValueChangeModeWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_TIMEOUT,
            type = StudioPropertyType.INTEGER))
    public interface ValueChangeTimeout {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_TIMEOUT,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL))
    public interface ValueChangeTimeoutWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WEEK_NUMBERS_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface WeekNumbersVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.USE_USER_TIMEZONE,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface UseUserTimezone {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.ENUMERATION,
            options = {"DATE", "DATETIME"}))
    public interface DateFormatterType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WHITE_SPACE,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace",
            defaultValue = "NORMAL",
            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT", "INITIAL"}))
    public interface WhiteSpace {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.SIZE))
    public interface WidthWithIntegerType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            defaultValue = "100%",
            options = {"AUTO", "100%"}))
    public interface WidthWithDefaultValue100 {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            defaultValue = "100%"))
    public interface WidthWithDefaultValue100WithoutOptions {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            defaultValue = "AUTO",
            options = {"AUTO", "100%"}))
    public interface WidthWithDefaultValueAuto {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            defaultValue = "UNDEFINED",
            options = {"AUTO", "100%"}))
    public interface WidthWithDefaultValueUndefined {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            initialValue = "100%",
            options = {"AUTO", "100%"}))
    public interface WidthWithInitialValue100 {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WRAP,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "false"))
    public interface Wrap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ENABLED,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "true"))
    public interface EnabledWithTrueDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            initialValue = "CHECK",
            setParameterFqn = "com.vaadin.flow.component.Component"))
    public interface IconWithCheckInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            initialValue = "BAN",
            setParameterFqn = "com.vaadin.flow.component.Component"))
    public interface IconWithBanInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            initialValue = "PENCIL",
            setParameterFqn = "com.vaadin.flow.component.Component"))
    public interface IconWithPencilInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.KEY,
            type = StudioPropertyType.STRING,
            initialValue = "editorActionsColumn"))
    public interface KeyWithEditorActionsColumnInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_WIDTH,
            type = StudioPropertyType.SIZE,
            category = StudioProperty.Category.SIZE,
            initialValue = "100px",
            options = {"AUTO", "100%"}))
    public interface MinWidthWithInitialValue100px {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface LocalizedName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredStringName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface StringType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            options = {"CONTAINER_REF", "LOADER_REF", "ICON"}))
    public interface TypeContainerRefLoaderRefIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RESIZABLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.SIZE,
            defaultValue = "false"))
    public interface ResizableWithFalseDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RESIZABLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.SIZE,
            defaultValueRef = "parent:resizable"))
    public interface ResizableWithParentDefaultValueRef {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL,
            initialValue = "msg:///actions.Cancel"))
    public interface TextWithCancelInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL,
            initialValue = "msg:///actions.Edit"))
    public interface TextWithEditInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.flowui.component.AggregationInfo$Type",
            options = {"SUM", "COUNT", "AVG", "MIN", "MAX"}))
    public interface AggregationType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL,
            required = true,
            initialValue = "create"))
    public interface RequiredIdWithCreateInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.COMPONENT_ID,
            category = StudioProperty.Category.GENERAL,
            required = true,
            initialValue = "close"))
    public interface RequiredIdWithCloseInitialValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "PLUS",
            setParameterFqn = "com.vaadin.flow.component.Component"))
    public interface IconWithPlusDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "CLOSE",
            setParameterFqn = "com.vaadin.flow.component.Component"))
    public interface IconWithCloseDefaultValue {
    }

    @StudioPropertyGroup
    public interface HtmlTextElementDefaultProperties extends ClassNames, ClickShortcut, Css,
            Enabled, HasSizeAttributes, Id, Text, NativeLabelThemeNames, Title, Visible, WhiteSpace {
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
    public interface IdAndParam extends Id, Param {
    }

    @StudioPropertyGroup
    public interface ResponsiveStepDefaultProperties extends RequiredMinWidthWithOptions, Columns {
    }

    @StudioPropertyGroup
    public interface AddonComponentDefaultProperties extends ClassNamesAndCss, IdAndVisible, HasSizeAttributes,
            AlignSelf, Colspan {
    }

    @StudioPropertyGroup
    public interface SizedComponentDefaultProperties extends BaseSizedEnabledComponentWithClassName {
    }

    @StudioPropertyGroup
    public interface FieldThemeTitleAndValueChangeDefaultProperties extends TextInputFieldThemeNames, Title,
            ValueChangeModeAttributes {
    }

    @StudioPropertyGroup
    public interface DataBindingAttributes extends CollectionOrInstanceDataContainer, Property {
    }

    @StudioPropertyGroup
    public interface TextAttributes extends Text, WhiteSpace {
    }

    @StudioPropertyGroup
    public interface ValueChangeModeAttributes extends ValueChangeMode, ValueChangeTimeout {
    }

    @StudioPropertyGroup
    public interface ValueChangeModeAttributesWithGeneralCategory extends
            ValueChangeModeWithGeneralCategory, ValueChangeTimeoutWithGeneralCategory {
    }

    @StudioPropertyGroup
    public interface BaseIconComponent extends BaseComponentWithClassNames, ClickShortcutWithGeneralCategory, Color,
            Size {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ICON,
            type = StudioPropertyType.ICON,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            required = true))
    public interface IconDefaultProperties extends BaseIconComponent {
    }

    @StudioPropertyGroup
    public interface SvgIconDefaultProperties extends BaseIconComponent, Resource, Symbol {
    }

    @StudioPropertyGroup
    public interface FontIconDefaultProperties extends BaseIconComponent, CharCode, FontFamily, IconClassNames,
            Ligature {
    }

    @StudioPropertyGroup
    public interface BaseFieldComponent extends BaseSizedEnabledComponentWithClassName,
            CollectionOrInstanceDataContainer, HelperText, Label, ReadOnly {
    }

    @StudioPropertyGroup
    public interface ValidatableBaseFieldComponent extends BaseFieldComponent, ValidationAttributes {
    }

    @StudioPropertyGroup
    public interface FieldDefaultProperties extends ValidatableBaseFieldComponent, HasAriaLabelAndFocusableAttributes,
            RequiredAttributes {
    }

    @StudioPropertyGroup
    public interface SelectionFieldComponent extends ValidatableBaseFieldComponent, HasAriaLabel, ItemsEnum,
            PropertyTypeParameterV {
    }

    @StudioPropertyGroup
    public interface BaseTextFieldComponent extends ValidatableBaseFieldComponent,
            HasAriaLabelAndFocusableAttributes, Autocapitalize, Autocomplete, Autocorrect, Autofocus,
            Autoselect, ClearButtonVisible, FieldThemeTitleAndValueChangeDefaultProperties, Placeholder, StringValue {
    }

    @StudioPropertyGroup
    public interface TextInputFieldDefaultProperties extends BaseTextFieldComponent, RequiredAttributes {
    }

    @StudioPropertyGroup
    public interface MultiSelectComboBoxDefaultProperties extends BaseSizedEnabledComponentWithClassName,
            HasAriaLabelAndFocusableAttributes, AllowCustomValue, AllowedCharPattern, Autofocus, AutoOpen,
            OverlayWidth, ClearButtonVisible, CollectionOrInstanceDataContainer, ErrorMessage, HelperText,
            ItemsContainerTypeParameterV, ItemsEnum, Label, MetaClassTypeParameterV, Opened, PageSize,
            Placeholder, PropertyTypeParameterV, ReadOnly, Required, TextInputFieldThemeNames, Title {
    }

    @StudioPropertyGroup
    public interface ListBoxDefaultProperties extends BaseSizedEnabledComponentWithClassName, HasAriaLabel,
            ItemsContainerTypeParameterV, ItemsEnum, ReadOnly {
    }

    @StudioPropertyGroup
    public interface ListBoxComponent extends ListBoxDefaultProperties {
    }

    @StudioPropertyGroup
    public interface ValuePickerDefaultProperties extends BaseSizedEnabledComponentWithClassName,
            HasAriaLabelAndFocusableAttributes, HasRequiredAndValidationAttributes, AllowCustomValue, Autofocus,
            ReadOnly, ThemeNames, Title, CollectionOrInstanceDataContainer, HelperText, Label, Placeholder,
            PropertyTypeParameterV {
    }

    @StudioPropertyGroup
    public interface ValuePickerComponent extends ValuePickerDefaultProperties {
    }

    @StudioPropertyGroup
    public interface BooleanFieldComponent extends BaseSizedEnabledComponentWithClassName,
            HasAriaLabelAndFocusableAttributes, HasRequiredAndValidationAttributes, Autofocus, ClickShortcut,
            BooleanValue, CollectionOrInstanceDataContainer, Label, PropertyTypeParameterV, ReadOnly {
    }

    @StudioPropertyGroup
    public interface CheckboxComponent extends BooleanFieldComponent, Indeterminate {
    }

    @StudioPropertyGroup
    public interface SwitchComponent extends BooleanFieldComponent {
    }

    @StudioPropertyGroup
    public interface TextFieldComponent extends TextInputFieldDefaultProperties, AllowedCharPattern, Datatype,
            MaxLength, MinLength, Pattern, PropertyTypeParameterV, TrimEnabled {
    }

    @StudioPropertyGroup
    public interface EmailFieldComponent extends TextInputFieldDefaultProperties, AllowedCharPattern,
            MaxLength, MinLength, Pattern, Property {
    }

    @StudioPropertyGroup
    public interface NumberFieldComponent extends TextInputFieldDefaultProperties, AllowedCharPattern,
            ValidationDoubleMax, ValidationDoubleMin, Property, Step, StepButtonsVisible {
    }

    @StudioPropertyGroup
    public interface EntityComboBoxComponent extends FieldDefaultProperties, AllowedCharPattern, AllowCustomValue,
            Autofocus, AutoOpen, OverlayWidth, ItemsContainerTypeParameterV, MetaClassTypeParameterV, Opened,
            PageSize, Pattern, Placeholder, PropertyTypeParameterV, TextInputFieldThemeNames, Title {
    }

    @StudioPropertyGroup
    public interface EntityPickerComponent extends ValuePickerComponent, MetaClassTypeParameterV {
    }

    @StudioPropertyGroup
    public interface ProgressBarComponent extends BaseSizedComponentWithClassNames, Indeterminate,
            ValidationDoubleMax, ValidationDoubleMin, ProgressBarThemeNames, StringValue {
    }

    @StudioPropertyGroup
    public interface VirtualListComponent extends BaseSizedEnabledComponentWithClassName,
            ItemsContainerTypeParameterV, ItemsEnum, HasFocusableAttributes {
    }

    @StudioPropertyGroup
    public interface BaseDropdownButtonComponent extends BaseSizedEnabledComponentWithClassName, OpenOnHover,
            Icon, HasFocusableAttributes, TextAttributes, Title {
    }

    @StudioPropertyGroup
    public interface UserMenuDefaultProperties extends BaseComponentWithClassNames, Enabled, OpenOnHover,
            HasFocusableAttributes, Title {
    }

    @StudioPropertyGroup
    public interface GridColumnDefaultProperties extends AutoWidth, FlexGrow, Footer, Frozen, Header, Key,
            ResizableWithParentDefaultValueRef, ColumnSortable, Visible, WidthWithDefaultValueUndefined {
    }

    @StudioPropertyGroup
    public interface BoundGridColumnDefaultProperties extends GridColumnDefaultProperties,
            PropertyTypeParameterT, Editable {
    }

    @StudioPropertyGroup
    public interface EditorActionsColumnDefaultProperties extends AutoWidth, FlexGrow, Footer, Header,
            KeyWithEditorActionsColumnInitialValue, ResizableWithParentDefaultValueRef,
            Visible, WidthWithDefaultValueUndefined {
    }

    @StudioPropertyGroup
    public interface BaseGridComponent extends ClassNamesAndCss, IdAndVisible, Enabled,
            GridAlignSelf, JustifySelf, AriaLabel, AggregationPosition, AllRowsVisible, Colspan,
            ColumnReorderingAllowed, CollectionDataContainer, DropMode, EmptyStateText, HasHeightAttributes, MaxWidth,
            MetaClassTypeParameterE, MultiSort, MultiSortOnShiftClickOnly, MultiSortPriority,
            NestedNullBehavior, PageSizeWithDefaultValue50, RowsDraggable, SelectionMode, HasFocusableAttributes,
            GridThemeNames, WidthWithInitialValue100, EditorBuffered, ColumnRendering {
    }

    @StudioPropertyGroup
    public interface DataGridDefaultProperties extends BaseGridComponent, Aggregatable, DetailsVisibleOnClick {
    }

    @StudioPropertyGroup
    public interface TreeDataGridDefaultProperties extends BaseGridComponent, Aggregatable,
            DetailsVisibleOnClickWithDefaultValueTrue, RequiredHierarchyPropertyTypeParameterE,
            HierarchyColumnTypeParameterE, ShowOrphans {
    }

    @StudioPropertyGroup
    public interface FileUploadFieldDefaultProperties extends BaseSizedEnabledComponentWithClassName,
            HasRequiredAndValidationAttributes, AcceptedFileTypes, ClearButtonAriaLabel, ClearButtonVisible,
            ConnectingStatusText, CollectionOrInstanceDataContainer, DropAllowed, FileNameVisible,
            FileNotSelectedText, FileTooBigText, HelperText, IncorrectFileTypeText, Label, MaxFileSize,
            ProcessingStatusText, Property, ReadOnly, RemainingTimeText, RemainingTimeUnknownText,
            UploadDialogCancelText, UploadDialogTitle, UploadIcon, UploadText {
    }

    @StudioPropertyGroup
    public interface FileStorageUploadFieldDefaultProperties extends FileUploadFieldDefaultProperties, FileStorageName,
            FileStoragePutMode {
    }

    @StudioPropertyGroup
    public interface FileStorageUploadFieldComponent extends FileStorageUploadFieldDefaultProperties {
    }

    @StudioPropertyGroup
    public interface AccordionPanelDefaultProperties extends ClassNamesAndCss, Colspan,
            EnabledWithoutDefaultValue, Height, IdAndVisible, MaxHeight, MaxWidth, MinHeight,
            MinWidth, SummaryText, Opened, DetailsThemeNames, WidthWithDefaultValue100 {
    }

    @StudioPropertyGroup
    public interface BaseLayoutDefaultProperties extends AlignItems, BoxSizing, ClassNames, Enabled,
            Expand, Height, JustifyContent, Margin, MaxHeight, MaxWidth, MinHeight, MinWidth, Spacing {
    }

    @StudioPropertyGroup
    public interface BaseLayout extends BaseLayoutDefaultProperties {
    }

    @StudioPropertyGroup
    public interface ComponentLayoutDefaultProperties extends BaseComponent, BaseLayoutDefaultProperties, ClickShortcut,
            LayoutThemeNames, Wrap {
    }

    @StudioPropertyGroup
    public interface ComponentLayout extends ComponentLayoutDefaultProperties {
    }

    @StudioPropertyGroup
    public interface AutoWidthLayoutDefaultProperties extends BaseComponentWithClassNames, Height, MaxHeight,
            MaxWidth, MinHeight, MinWidth, WidthWithDefaultValueAuto {
    }

    @StudioPropertyGroup
    public interface EnabledAutoWidthLayoutDefaultProperties extends AutoWidthLayoutDefaultProperties, Enabled {
    }

    @StudioPropertyGroup
    public interface ClickableAutoWidthLayoutDefaultProperties extends AutoWidthLayoutDefaultProperties, ClickShortcut {
    }

    @StudioPropertyGroup
    public interface DetailsDefaultProperties extends EnabledAutoWidthLayoutDefaultProperties, Opened,
            DetailsSummaryText, DetailsThemeNames {
    }

    @StudioPropertyGroup
    public interface FlexLayoutDefaultProperties extends ClickableAutoWidthLayoutDefaultProperties,
            Enabled, Expand, AlignItems, JustifyContent {
    }

    @StudioPropertyGroup
    public interface DataLoadCoordinatorDefaultProperties extends Id, Auto, ComponentPrefix, ContainerPrefix {
    }

    @StudioPropertyGroup
    public interface SettingsDefaultProperties extends Id, Auto {
    }

    @StudioPropertyGroup
    public interface InitialLayoutDefaultProperties extends Id, BaseLayout, Css, PaddingWithTrueDefaultValue,
            LayoutThemeNames, WidthWithDefaultValue100 {
    }

    @StudioPropertyGroup
    public interface LoginErrorMessageDefaultProperties extends Title, Message, Username, Password {
    }

    @StudioPropertyGroup
    public interface FormItemDefaultProperties extends ClassNames, ClickShortcut, Colspan, Label, Enabled,
            IdAndVisible {
    }

    @StudioPropertyGroup
    public interface UserIndicatorDefaultProperties extends HasSizeAttributes, ClassNamesAndCss, IdAndVisible, Enabled,
            Title {
    }

    @StudioPropertyGroup
    public interface KeyValueCollectionLoaderDefaultProperties extends Id, FirstResult, MaxResults, Store, Query {
    }

    @StudioPropertyGroup
    public interface NoOptionSizedAddonComponentDefaultProperties extends ClassNamesAndCss, IdAndVisible,
            HasSizeWithoutOptions, AlignSelf, Colspan {
    }

    @StudioPropertyGroup
    public interface BaseFilterDefaultProperties extends ClassNamesAndCss, Colspan,
            DefaultValue, Enabled, HasRequiredAndValidationAttributes, HelperText,
            IdAndVisible, Label, LabelVisible, ParameterName, ReadOnly,
            HasFocusableAttributes, ThemeNames {
    }

    @StudioPropertyGroup
    public interface PropertyFilterDefaultProperties extends BaseFilterDefaultProperties,
            RequiredPropertyFilterOperation, PropertyFilterOperationsList, OperationEditable,
            OperationTextVisible, RequiredPropertyTypeParameterV {
    }

    @StudioPropertyGroup
    public interface JpqlFilterDefaultProperties extends BaseFilterDefaultProperties, HasInExpression,
            RequiredParameterClassTypeParameterV, Width {
    }

    @StudioPropertyGroup
    public interface FilterComponentDefaultProperties extends BaseSizedEnabledComponentWithClassName,
            HasAriaLabelAndFocusableAttributes, HasRequiredAndValidationAttributes, AutoApply, DataLoader,
            DefaultValue, HelperText, Label, LabelPosition, LabelVisible, LabelWidth, ParameterName, ReadOnly,
            ThemeNames {
    }

    @StudioPropertyGroup
    public interface PropertyFilterComponent extends FilterComponentDefaultProperties,
            RequiredPropertyFilterOperation, PropertyFilterOperationsList, OperationEditable,
            OperationTextVisible, RequiredPropertyTypeParameterV {
    }

    @StudioPropertyGroup
    public interface JpqlFilterComponent extends FilterComponentDefaultProperties, HasInExpression,
            RequiredParameterClassTypeParameterV {
    }

    @StudioPropertyGroup
    public interface BaseComboBoxItemsQuery extends SearchStringFormat, EscapeValueForLike, Query {
    }

    @StudioPropertyGroup
    public interface RequiredStringNameAndValue extends RequiredStringName, RequiredStringValue {
    }

    @StudioPropertyGroup
    public interface RequiredStringNameAndValueAndType extends RequiredStringNameAndValue,
            TypeContainerRefLoaderRefIcon {
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

    @StudioPropertyGroup
    public interface DigitsComponent extends Message, RequiredInteger, RequiredFraction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FRACTION,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface RequiredFraction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REGEXP,
            type = StudioPropertyType.STRING,
            required = true))
    public interface RegexpComponent extends Message {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.KEY_COMBINATION,
                            type = StudioPropertyType.STRING,
                            required = true),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RESET_FOCUS_ON_ACTIVE_ELEMENT,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface ShortcutCombinationComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REF,
            type = StudioPropertyType.ACTION_REF))
    public interface ActionItemComponent extends RequiredId {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FILTERABLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValueRef = "parent:filterable"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_ALIGN,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.ColumnTextAlign",
                            defaultValue = "START",
                            options = {"CENTER", "END", "START"})
            }
    )
    public interface ColumnComponent extends BoundGridColumnDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FILTERABLE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HEADER_FILTER_APPLY_SHORTCUT,
                            type = StudioPropertyType.SHORTCUT_COMBINATION)
            }
    )
    public interface ColumnsElementGroupComponent extends Exclude, Sortable, IncludeAll, ResizableWithFalseDefaultValue {
    }

    @StudioPropertyGroup
    public interface EditButtonComponent extends EditorActionButtonDefaultProperties, TextWithEditInitialValue,
            IconWithPencilInitialValue {
    }

    @StudioPropertyGroup
    public interface SaveButtonComponent extends EditorActionTextButtonDefaultProperties, IconWithCheckInitialValue {
    }

    @StudioPropertyGroup
    public interface CloseButtonComponent extends EditorActionTextButtonDefaultProperties, IconWithBanInitialValue {
    }

    @StudioPropertyGroup
    public interface CancelButtonComponent extends EditorActionButtonDefaultProperties, IconWithBanInitialValue,
            TextWithCancelInitialValue {
    }

    @StudioPropertyGroup
    public interface AggregationInfoComponent extends CellTitle, StrategyClass, AggregationType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REMEMBER_ME,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface JmixLoginFormComponent extends LoginFormDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COMPONENT,
                            type = StudioPropertyType.COMPONENT_REF,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            componentRefTags = "genericFilter"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONFIGURATION_PARAM,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONDITION_PARAM,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface GenericFilterElementComponent extends Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FLEX_GROW,
                            type = StudioPropertyType.DOUBLE,
                            category = StudioProperty.Category.POSITION),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LAZY,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"icon-on-top"})
            }
    )
    public interface TabComponent extends HasAriaLabel, ClassNamesAndCss, Label, Visible, RequiredId,
            EnabledWithoutDefaultValue {
    }

    @StudioPropertyGroup
    public interface TextUserItemUserMenuItemComponent extends Icon, Visible, Checked, Checkable, RequiredId,
            RequiredText, UserMenuItemThemeNames, EnabledWithoutDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REF,
            type = StudioPropertyType.ACTION_REF,
            category = StudioProperty.Category.GENERAL))
    public interface ActionUserMenuItemComponent extends Visible, Checked, Checkable, RequiredId,
            UserMenuItemThemeNames, EnabledWithoutDefaultValue {
    }

    @StudioPropertyGroup
    public interface ComponentUserMenuItemComponent extends Visible, Checked, Checkable, RequiredId,
            UserMenuItemThemeNames, EnabledWithoutDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VIEW_ID,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VIEW_CLASS,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OPEN_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.GENERAL,
                            classFqn = "io.jmix.flowui.view.OpenMode",
                            defaultValue = "NAVIGATION",
                            options = {"NAVIGATION", "DIALOG"},
                            setMethod = "setOpenMode")
            }
    )
    public interface ViewUserItemUserMenuItemComponent extends Icon, Visible, Checked, Checkable,
            RequiredId, RequiredText, UserMenuItemThemeNames, EnabledWithoutDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FOCUS_DELAY,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HOVER_DELAY,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MANUAL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.POSITION,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.shared.Tooltip$TooltipPosition",
                            options = {"TOP_START", "TOP", "TOP_END", "BOTTOM_START", "BOTTOM", "BOTTOM_END",
                                    "START_TOP", "START", "START_BOTTOM", "END_TOP", "END", "END_BOTTOM"},
                            setParameterFqn = "com.vaadin.flow.component.shared.Tooltip$TooltipPosition")
            }
    )
    public interface TooltipComponent extends HideDelay, RequiredText, Opened {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COMPONENT,
                            type = StudioPropertyType.COMPONENT_REF,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            componentRefTags = {"simplePagination"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FIRST_RESULT_PARAM,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_RESULTS_PARAM,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface PaginationComponent extends Id {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COMPONENT,
            type = StudioPropertyType.COMPONENT_REF,
            category = StudioProperty.Category.GENERAL,
            required = true,
            componentRefTags = "propertyFilter"))
    public interface FacetPropertyFilterComponent extends IdAndParam {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COMPONENT,
            type = StudioPropertyType.COMPONENT_REF,
            category = StudioProperty.Category.GENERAL,
            required = true,
            componentRefTags = {"dataGrid", "treeDataGrid", "groupDataGrid"}))
    public interface DataGridFilterComponent extends IdAndParam {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LABELS_POSITION,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout$ResponsiveStep$LabelsPosition",
            defaultValue = "TOP",
            options = {"ASIDE", "TOP"}))
    public interface FormLayoutResponsiveStepComponent extends ResponsiveStepDefaultProperties {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LABELS_POSITION,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            classFqn = "io.jmix.flowui.component.SupportsResponsiveSteps$ResponsiveStep$LabelsPosition",
            defaultValue = "TOP",
            options = {"ASIDE", "TOP"}))
    public interface ResponsiveStepComponent extends ResponsiveStepDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.JOIN,
                            type = StudioPropertyType.JPQL_FILTER_JOIN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.WHERE,
                            type = StudioPropertyType.JPQL_FILTER_WHERE)
            }
    )
    public interface JpqlFilterConditionJpqlComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPERATION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
            required = true,
            options = {"AND", "OR"}))
    public interface GroupFilterElementComponent extends IdAndVisible, ClassNamesAndCss, Enabled, Colspan, SummaryText,
            OperationTextVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OPERATION,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
                            defaultValue = "AND",
                            options = {"AND", "OR"})
            }
    )
    public interface ConfigurationComponent extends Name, RequiredId {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INCLUDE,
                            type = StudioPropertyType.STRING,
                            required = true),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXCLUDE_PROPERTIES,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXCLUDE_RECURSIVELY,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface PropertiesComponent extends Exclude {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COMPONENT_ID,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface SettingsFacetComponentComponent extends EnabledWithoutDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REF_COLUMN,
            type = StudioPropertyType.COMPONENT_REF,
            required = true,
            componentRefTags = {"column"}))
    public interface GridColumnVisibilityMenuItemComponent extends Text {
    }

    @StudioPropertyGroup
    public interface GridContextMenuItemComponent extends IdAndVisible, TextAttributes, ClassNamesAndCss, Icon, Action,
            Enabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TOUCH_OPTIMIZED,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface NavigationBarComponent extends Css {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DRAWER_OPENED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PRIMARY_SECTION,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.applayout.AppLayout$Section",
                            options = {"DRAWER", "NAVBAR"},
                            setParameterFqn = "com.vaadin.flow.component.applayout.AppLayout$Section")
            }
    )
    public interface AppLayoutComponent extends ClassNamesAndCss {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLASS,
            type = StudioPropertyType.COMPONENT_CLASS,
            category = StudioProperty.Category.GENERAL,
            required = true,
            useAsInjectionType = true))
    public interface GenericComponentComponent extends BaseComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLASS,
            type = StudioPropertyType.FRAGMENT_CLASS,
            category = StudioProperty.Category.GENERAL,
            required = true,
            useAsInjectionType = true))
    public interface FragmentComponent extends BaseComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ABBREVIATION,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COLOR_INDEX,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.IMAGE,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"xlarge", "large", "small", "xsmall"})
            }
    )
    public interface AvatarComponent extends BaseSizedComponentWithClassNames, Name {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DISABLE_ON_CLICK,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "warning",
                                    "error", "contrast", "icon"})
            }
    )
    public interface ButtonComponent extends SizedComponentDefaultProperties, HasAriaLabelAndFocusableAttributes,
            TextAttributes, Title, Action, Autofocus, Icon, IconAfterText, ShortcutCombination {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_CONTAINER,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
                            category = StudioProperty.Category.DATA_BINDING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"helper-above-field", "vertical", "horizontal"})
            }
    )
    public interface CheckboxGroupComponent extends SelectionFieldComponent, RequiredAttributes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTO_OPEN,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface ComboBoxComponent extends FieldDefaultProperties, Title, Pattern, PageSize, Datatype, ItemsEnum,
            Autofocus, Placeholder, OverlayWidth, AllowCustomValue, ClearButtonVisible, PropertyTypeParameterV,
            TextInputFieldThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTO_EXPAND,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.combobox.MultiSelectComboBox$AutoExpandMode",
                            defaultValue = "NONE",
                            options = {"VERTICAL", "HORIZONTAL", "BOTH", "NONE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECTED_ITEMS_ON_TOP,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface MultiSelectComboBoxComponent extends MultiSelectComboBoxDefaultProperties {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"tertiary", "non-checkable"}))
    public interface UserMenuComponent extends UserMenuDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DROPDOWN_INDICATOR_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"small", "large", "tertiary-inline", "primary", "success", "warning", "error",
                                    "contrast", "icon", "dropdown-indicators", "no-dropdown-indicators"})
            }
    )
    public interface DropdownButtonComponent extends BaseDropdownButtonComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DROPDOWN_ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"small", "large", "tertiary-inline", "primary", "success", "warning", "error",
                                    "contrast", "icon"})
            }
    )
    public interface ComboButtonComponent extends BaseDropdownButtonComponent, Action, ShortcutCombination {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATATYPE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.DATA_BINDING,
                            classFqn = "io.jmix.core.metamodel.datatype.Datatype",
                            options = {"date", "dateTime", "localDateTime", "offsetDateTime", "localDate"},
                            typeParameter = "V"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATE_FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface DatePickerComponent extends FieldDefaultProperties, Name, Opened, AutoOpen, Placeholder,
            WeekNumbersVisible, ClearButtonVisible, AllowedCharPattern, ValidationStringMin, ValidationStringMax,
            PropertyTypeParameterV, TextInputFieldThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATATYPE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.DATA_BINDING,
                            classFqn = "io.jmix.core.metamodel.datatype.Datatype",
                            options = {"date", "dateTime", "localDateTime", "offsetTime", "localTime", "offsetDateTime",
                                    "time", "localDate"},
                            typeParameter = "V"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATE_FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.VALIDATION),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATE_PLACEHOLDER,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TIME_PLACEHOLDER,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL)
            }
    )
    public interface DateTimePickerComponent extends FieldDefaultProperties, TimeStep, AutoOpen, WeekNumbersVisible,
            ValidationStringMin, ValidationStringMax, PropertyTypeParameterV, TextInputFieldThemeNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTO_FOCUS,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface DrawerToggleComponent extends BaseSizedComponentWithClassNames, HasAriaLabelAndFocusableAttributes,
            ThemeNames, ClickShortcut {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_LAYOUT_ID,
            type = StudioPropertyType.COMPONENT_REF))
    public interface SidePanelLayoutCloserComponent extends BaseSizedComponentWithClassNames,
            HasAriaLabelAndFocusableAttributes, ThemeNames {
    }

    @StudioPropertyGroup
    public interface DataGridComponent extends DataGridDefaultProperties, MinWidthWithInitialValue100px {
    }

    @StudioPropertyGroup
    public interface TreeDataGridComponent extends TreeDataGridDefaultProperties, MinWidthWithInitialValue100px {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STEP,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL))
    public interface IntegerFieldComponent extends TextInputFieldDefaultProperties, Property,
            ValidationIntegerMin, ValidationIntegerMax, StepButtonsVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REVEAL_BUTTON_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            defaultValue = "true"))
    public interface PasswordFieldComponent extends TextInputFieldDefaultProperties, Pattern, Property,
            AllowedCharPattern {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"vertical", "horizontal", "helper-above-field"}))
    public interface RadioButtonGroupComponent extends SelectionFieldComponent, Required, Datatype,
            ItemsContainerTypeParameterV {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.NO_VERTICAL_OVERLAP,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EMPTY_SELECTION_ALLOWED,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.VALIDATION),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EMPTY_SELECTION_CAPTION,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_CONTAINER,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
                            category = StudioProperty.Category.DATA_BINDING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_ENUM,
                            type = StudioPropertyType.ENUM_CLASS,
                            category = StudioProperty.Category.DATA_BINDING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"small", "align-left", "align-start", "align-center", "align-end", "align-right",
                                    "helper-above-field"})
            }
    )
    public interface SelectComponent extends FieldDefaultProperties, Datatype, Autofocus, Placeholder, OverlayWidth,
            PropertyTypeParameterV {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ORIENTATION,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            classFqn = "com.vaadin.flow.component.tabs.Tabs$Orientation",
                            defaultValue = "HORIZONTAL",
                            options = {"VERTICAL", "HORIZONTAL"},
                            setMethod = "setOrientation"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"centered", "small", "minimal", "hide-scroll-buttons", "show-scroll-buttons",
                                    "filled", "equal-width-tabs"})
            }
    )
    public interface TabsComponent extends BaseSizedComponentWithClassNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_ROWS,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MIN_ROWS,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "2")
            }
    )
    public interface TextAreaComponent extends ValidatableBaseFieldComponent, HasAriaLabelAndFocusableAttributes,
            ValueChangeModeAttributes, Pattern, Required, Property, MinLength, MaxLength, Autofocus, Autoselect,
            TrimEnabled, StringValue, Placeholder, Autocorrect, Autocomplete, Autocapitalize, TextAreaThemeNames,
            ClearButtonVisible, AllowedCharPattern {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATATYPE,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.DATA_BINDING,
            classFqn = "io.jmix.core.metamodel.datatype.Datatype",
            options = {"offsetTime", "localTime", "time"},
            typeParameter = "V"))
    public interface TimePickerComponent extends FieldDefaultProperties, TimeStep, AutoOpen, Placeholder,
            ClearButtonVisible, AllowedCharPattern, ValidationStringMin, ValidationStringMax, PropertyTypeParameterV {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FORGOT_PASSWORD_BUTTON_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.REMEMBER_ME_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LOCALES_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true")
            }
    )
    public interface LoginFormComponent extends BaseComponentWithClassNames, Enabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FORGOT_PASSWORD_BUTTON_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL))
    public interface LoginOverlayComponent extends BaseComponentWithClassNames, Opened, Enabled {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATA_LOADER,
                            type = StudioPropertyType.DATA_LOADER_REF,
                            category = StudioProperty.Category.DATA_BINDING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_PER_PAGE_DEFAULT_VALUE,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_PER_PAGE_ITEMS,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "20, 50, 100, 500, 1000, 5000",
                            defaultValueRef = "application_property:jmix.ui.component.pagination-items-per-page-items"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_PER_PAGE_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_PER_PAGE_UNLIMITED_ITEM_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTO_LOAD,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "false")
            }
    )
    public interface SimplePaginationComponent extends BaseComponentWithClassNames {
    }

    @StudioPropertyGroup
    public interface ImageHtmlComponent extends BaseSizedComponentWithClassNames, Title, Enabled, DataBindingAttributes,
            TextAttributes, ClickShortcut, HasAriaLabel, AlternateText, ImageResource, ImageThemeNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTO_UPLOAD,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DROP_LABEL,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DROP_LABEL_ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_FILES,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.VALIDATION),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RECEIVER_FQN,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RECEIVER_TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            options = {"MEMORY_BUFFER", "MULTI_FILE_MEMORY_BUFFER", "FILE_TEMPORARY_STORAGE_BUFFER",
                                    "MULTI_FILE_TEMPORARY_STORAGE_BUFFER"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.UPLOAD_HANDLER_FQN,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.UPLOAD_HANDLER_TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            defaultValue = "IN_MEMORY",
                            options = {"IN_MEMORY", "FILE_TEMPORARY_STORAGE"})
            }
    )
    public interface UploadComponent extends SizedComponentDefaultProperties, UploadText, UploadIcon,
            MaxFileSize, AcceptedFileTypesWithGeneralCategory, DropAllowedWithTrueDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FILE_NAME,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface FileUploadFieldComponent extends FileUploadFieldDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTO_APPLY,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true",
                            defaultValueRef = "application_property:jmix.ui.component.filter-auto-apply"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.APPLY_SHORTCUT,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OPENED,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PROPERTY_HIERARCHY_DEPTH,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SUMMARY_TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "Filter"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"filled", "reverse", "small", "no-padding"})
            }
    )
    public interface GenericFilterComponentComponent extends SizedComponentDefaultProperties, DataLoader {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPERATION,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
            required = true,
            options = {"AND", "OR"}))
    public interface GroupFilterComponentComponent extends BaseComponentWithClassNames, Enabled, AutoApply,
            DataLoader, SummaryText, OperationTextVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HEIGHT,
                            type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE,
                            defaultValue = "100%",
                            options = {"AUTO", "100%"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MARGIN,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXPAND,
                            type = StudioPropertyType.COMPONENT_REF,
                            category = StudioProperty.Category.POSITION),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BOX_SIZING,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.SIZE,
                            classFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            defaultValue = "UNDEFINED",
                            options = {"CONTENT_BOX", "BORDER_BOX", "UNDEFINED"},
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing")
            }
    )
    public interface LayoutComponent extends MinAndMaxWidth, MinAndMaxHeight, ClassNamesAndCss, Spacing, Enabled,
            AlignItems, JustifyContent, WidthWithDefaultValue100, PaddingWithTrueDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FOCUS_COMPONENT,
                            type = StudioPropertyType.COMPONENT_REF,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FOCUS_MODE,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "AUTO",
                            options = {"NO_FOCUS", "AUTO"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXTENDS,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL)
            }
    )
    public interface ViewComponent extends MessagesGroupAndTitle {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_SUGGESTIONS_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FONT_SIZE,
                            type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "1rem"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HIGHLIGHT_ACTIVE_LINE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HIGHLIGHT_GUTTER_LINE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LIVE_SUGGESTIONS_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MODE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.GENERAL,
                            classFqn = "io.jmix.flowui.kit.component.codeeditor.CodeEditorMode",
                            options = {"ABAP", "ABC", "ACTIONSCRIPT", "ADA", "ALDA", "APACHE_CONF", "APEX",
                                    "APPLESCRIPT", "AQL", "ASCIIDOC", "ASL", "ASSEMBLY_X_86", "AUTOHOTKEY", "BATCHFILE",
                                    "BIBTEX", "C_9_SEARCH", "C_CPP", "CIRRU", "CLOJURE", "COBOL", "COFFEE",
                                    "COLDFUSION", "CRYSTAL", "CSHARP", "CSOUND_DOCUMENT", "CSOUND_ORCHESTRA",
                                    "CSOUND_SCORE", "CSP", "CSS", "CURLY", "D", "DART", "DIFF", "DJANGO", "DOCKERFILE",
                                    "DOT", "DROOLS", "EDIFACT", "EIFFEL", "EJS", "ELIXIR", "ELM", "ERLANG", "FORTH",
                                    "FORTRAN", "FSHARP", "FSL", "FTL", "GCODE", "GHERKIN", "GITIGNORE", "GLSL",
                                    "GOBSTONES", "GOLANG", "GRAPHQLSCHEMA", "GROOVY", "HAML", "HANDLEBARS", "HASKELL",
                                    "HASKELL_CABAL", "HAXE", "HJSON", "HTML", "HTML_ELIXIR", "HTML_RUBY", "INI", "IO",
                                    "ION", "JACK", "JADE", "JAVA", "JAVASCRIPT", "JEXL", "JSON", "JSON_5", "JSONIQ",
                                    "JSP", "JSSM", "JSX", "JULIA", "KOTLIN", "LATEX", "LATTE", "LESS", "LIQUID", "LISP",
                                    "LIVESCRIPT", "LOGIQL", "LOGTALK", "LSL", "LUA", "LUAPAGE", "LUCENE", "MAKEFILE",
                                    "MARKDOWN", "MASK", "MATLAB", "MAZE", "MEDIAWIKI", "MEL", "MIPS", "MIXAL",
                                    "MUSHCODE", "MYSQL", "NGINX", "NIM", "NIX", "NSIS", "NUNJUCKS", "OBJECTIVEC",
                                    "OCAML", "PARTIQL", "PASCAL", "PERL", "PGSQL", "PHP", "PHP_LARAVEL_BLADE", "PIG",
                                    "PLAIN_TEXT", "PLSQL", "POWERSHELL", "PRAAT", "PRISMA", "PROLOG", "PROPERTIES",
                                    "PROTOBUF", "PUPPET", "PYTHON", "QML", "R", "RAKU", "RAZOR", "RDOC", "RED",
                                    "REDSHIFT", "RHTML", "ROBOT", "RST", "RUBY", "RUST", "SAC", "SASS", "SCAD", "SCALA",
                                    "SCHEME", "SCRYPT", "SCSS", "SH", "SJS", "SLIM", "SMARTY", "SMITHY", "SNIPPETS",
                                    "SOY_TEMPLATE", "SPACE", "SPARQL", "SQL", "SQLSERVER", "STYLUS", "SVG", "SWIFT",
                                    "TCL", "TERRAFORM", "TEX", "TEXT", "TEXTILE", "TOML", "TSX", "TURTLE", "TWIG",
                                    "TYPESCRIPT", "VALA", "VBSCRIPT", "VELOCITY", "VERILOG", "VHDL", "VISUALFORCE",
                                    "WOLLOK", "XML", "XQUERY", "YAML", "ZEEK"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PRINT_MARGIN_COLUMN,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "80"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_GUTTER,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_LINE_NUMBERS,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_PRINT_MARGIN,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SUGGEST_ON,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_WRAP,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TAB_SIZE,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "4"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.USE_SOFT_TABS,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            classFqn = "io.jmix.flowui.kit.component.codeeditor.CodeEditorTheme",
                            options = {"AMBIANCE", "CHAOS", "CHROME", "CLOUD_9_DAY", "CLOUD_9_NIGHT",
                                    "CLOUD_9_NIGHT_LOW_COLOR", "CLOUDS", "CLOUDS_MIDNIGHT", "COBALT", "CRIMSON_EDITOR",
                                    "DAWN", "DRACULA", "DREAMWEAVER", "ECLIPSE", "GITHUB", "GOB", "GRUVBOX",
                                    "GRUVBOX_DARK_HARD", "GRUVBOX_LIGHT_HARD", "IDLE_FINGERS", "IPLASTIC",
                                    "KATZENMILCH", "KR_THEME", "KUROIR", "MERBIVORE", "MERBIVORE_SOFT",
                                    "MONO_INDUSTRIAL", "MONOKAI", "NORD_DARK", "ONE_DARK", "PASTEL_ON_DARK",
                                    "SOLARIZED_DARK", "SOLARIZED_LIGHT", "SQLSERVER", "TERMINAL", "TEXTMATE",
                                    "TOMORROW", "TOMORROW_NIGHT", "TOMORROW_NIGHT_BLUE", "TOMORROW_NIGHT_BRIGHT",
                                    "TOMORROW_NIGHT_EIGHTIES", "TWILIGHT", "VIBRANT_INK", "XCODE"})
            }
    )
    public interface CodeEditorComponent extends ValidatableBaseFieldComponent, RequiredAttributes,
            HasFocusableAttributes, Title, Property, Placeholder {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_COLUMN_LABEL,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.REORDERABLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECT_ALL_BUTTONS_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECTED_ITEMS_COLUMN_LABEL,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"no-border", "no-row-border", "checkmarks", "no-space-between-actions"})
            }
    )
    public interface TwinColumnComponent extends ListBoxComponent, HasRequiredAndValidationAttributes,
            DataBindingAttributes, Label, HelperText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATA_GRID,
                            type = StudioPropertyType.COMPONENT_REF,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            componentRefTags = {"dataGrid", "treeDataGrid"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXCLUDE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HIDE_ALL_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            initialValue = "COG"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INCLUDE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_ALL_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "warning",
                                    "error", "contrast", "icon"})
            }
    )
    public interface GridColumnVisibilityComponent extends SizedComponentDefaultProperties, TextAttributes,
            HasFocusableAttributes {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MENU,
                            type = StudioPropertyType.COMPONENT_REF,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            componentRefTags = {"listMenu", "horizontalMenu"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FILTER_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.GENERAL,
                            classFqn = "io.jmix.flowui.component.menufilterfield.MenuFilterField$FilterMode",
                            defaultValue = "CASE_INSENSITIVE",
                            options = {"CASE_INSENSITIVE", "CASE_SENSITIVE"})
            }
    )
    public interface MenuFilterFieldComponent extends SizedComponentDefaultProperties,
            HasAriaLabelAndFocusableAttributes, FieldThemeTitleAndValueChangeDefaultProperties, Label, ReadOnly,
            Autofocus, HelperText, Autoselect, StringValue, Placeholder, ClearButtonVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"no-border", "compact"}))
    public interface RichTextEditorComponent extends BaseFieldComponent, HasAriaLabel, Property, ValueChangeMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TAB_NAVIGATION,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL))
    public interface HorizontalMenuComponent extends BaseSizedComponentWithClassNames, LoadMenuConfig {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.META_CLASS,
                            type = StudioPropertyType.ENTITY_NAME,
                            category = StudioProperty.Category.DATA_BINDING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"toggle-reverse"})
            }
    )
    public interface ListMenuComponent extends HasSizeAttributes, IdAndVisible, ClassNamesAndCss, LoadMenuConfig {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FILE,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface HtmlComponent extends BaseComponentWithClassNames, Content {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONTENT_ALIGNMENT,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout$ContentAlignment",
                            defaultValue = "START",
                            options = {"START", "END", "CENTER", "STRETCH", "SPACE_BETWEEN", "SPACE_AROUND"},
                            setMethod = "setAlignContent",
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout$ContentAlignment"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FLEX_DIRECTION,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout$FlexDirection",
                            defaultValue = "ROW",
                            options = {"ROW", "ROW_REVERSE", "COLUMN", "COLUMN_REVERSE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FLEX_WRAP,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout$FlexWrap",
                            defaultValue = "NOWRAP",
                            options = {"NOWRAP", "WRAP", "WRAP_REVERSE"})
            }
    )
    public interface FlexLayoutComponent extends FlexLayoutDefaultProperties {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SCROLL_BARS_DIRECTION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "com.vaadin.flow.component.orderedlayout.Scroller$ScrollDirection",
            defaultValue = "VERTICAL",
            options = {"VERTICAL", "HORIZONTAL", "BOTH", "NONE"},
            setMethod = "setScrollDirection"))
    public interface ScrollerComponent extends EnabledAutoWidthLayoutDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ORIENTATION,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.splitlayout.SplitLayout$Orientation",
                            defaultValue = "HORIZONTAL",
                            options = {"VERTICAL", "HORIZONTAL"},
                            setMethod = "setOrientation"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SPLITTER_POSITION,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"small", "minimal", "splitter-spacing"})
            }
    )
    public interface SplitLayoutComponent extends ClickableAutoWidthLayoutDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CLOSE_ON_OUTSIDE_CLICK,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DISPLAY_AS_OVERLAY_ON_SMALL_DEVICES,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_HORIZONTAL_MAX_SIZE,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_HORIZONTAL_MIN_SIZE,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_HORIZONTAL_SIZE,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_OVERLAY,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_POSITION,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            classFqn = "io.jmix.flowui.kit.component.sidepanellayout.SidePanelPosition",
                            defaultValue = "RIGHT",
                            options = {"LEFT", "RIGHT", "INLINE_START", "INLINE_END", "TOP", "BOTTOM"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_VERTICAL_MAX_SIZE,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_VERTICAL_MIN_SIZE,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SIDE_PANEL_VERTICAL_SIZE,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HEIGHT,
                            type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE,
                            initialValue = "100%",
                            options = {"AUTO", "100%"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MODAL,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OVERLAY_ARIA_LABEL,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface SidePanelLayoutComponent extends BaseComponent, MinAndMaxWidth, MinAndMaxHeight,
            WidthWithInitialValue100 {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTO_RESPONSIVE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTO_ROWS,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COLUMN_SPACING,
                            type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COLUMN_WIDTH,
                            type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATA_CONTAINER,
                            type = StudioPropertyType.DATA_CONTAINER_REF,
                            category = StudioProperty.Category.DATA_BINDING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXPAND_COLUMNS,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXPAND_FIELDS,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LABELS_ASIDE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LABEL_SPACING,
                            type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LABELS_POSITION,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            classFqn = "com.vaadin.flow.component.formlayout.FormLayout$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_COLUMNS,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MIN_COLUMNS,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ROW_SPACING,
                            type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.LOOK_AND_FEEL)
            }
    )
    public interface FormLayoutComponent extends EnabledAutoWidthLayoutDefaultProperties, LabelWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"centered", "small", "minimal", "hide-scroll-buttons", "show-scroll-buttons", "filled",
                    "equal-width-tabs", "no-border", "bordered", "no-padding"}))
    public interface TabSheetComponent extends AutoWidthLayoutDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TITLE_HEADING_LEVEL,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SUBTITLE,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"elevated", "outlined", "horizontal", "stretch-media", "cover-media"})
            }
    )
    public interface CardComponent extends SizedComponentDefaultProperties, HasAriaLabel, Title {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COLUMN_MIN_WIDTH,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEMS_ENUM,
                            type = StudioPropertyType.ENUM_CLASS,
                            category = StudioProperty.Category.DATA_BINDING,
                            typeParameter = "T")
            }
    )
    public interface GridLayoutComponent extends SizedComponentDefaultProperties, Gap, ItemsContainerTypeParameterT {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DELAY,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL,
                            required = true),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.REPEATING,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTOSTART,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "false")
            }
    )
    public interface TimerComponent extends RequiredId {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ACTION,
            type = StudioPropertyType.ACTION_REF,
            category = StudioProperty.Category.GENERAL,
            classFqn = "io.jmix.flowui.kit.action.Action"))
    public interface Action {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ACTION_VARIANT,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            classFqn = "io.jmix.flowui.kit.action.ActionVariant",
            defaultValue = "DEFAULT",
            options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"},
            setMethod = "setVariant"))
    public interface ActionVariantWithDefaultDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ACTION_VARIANT,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            classFqn = "io.jmix.flowui.kit.action.ActionVariant",
            defaultValue = "DANGER",
            options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"},
            setMethod = "setVariant"))
    public interface ActionVariantWithDangerDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ACTION_VARIANT,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            classFqn = "io.jmix.flowui.kit.action.ActionVariant",
            defaultValue = "PRIMARY",
            options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"},
            setMethod = "setVariant"))
    public interface ActionVariantWithPrimaryDefaultValue {
    }

    @StudioPropertyGroup
    public interface DropdownActionItem extends ActionVariantWithDefaultDefaultValue, Description, Enabled, Visible,
            Icon, RequiredId, Text {
    }

    @StudioPropertyGroup
    public interface BaseActionDefaultProperties extends ActionVariantWithDefaultDefaultValue,
            Description, EnabledWithTrueDefaultValue, ShortcutCombination, Visible {
    }

    @StudioPropertyGroup
    public interface PrimaryCloseActionDefaultProperties extends ActionVariantWithPrimaryDefaultValue, Description,
            Enabled, ShortcutCombination, Visible, RequiredIdWithCloseInitialValue, IconWithCloseDefaultValue {
    }

    @StudioPropertyGroup
    public interface ActionDefaultPropertiesWithPlusIcon extends BaseActionDefaultProperties, RequiredId, Text,
            IconWithPlusDefaultValue {
    }
}
