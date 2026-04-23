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

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Common XML element names reused by Studio meta descriptions and generator tooling.
 */
public final class StudioXmlElements {
    public static final String ACCORDION = "accordion";
    public static final String ACCORDION_PANEL = "accordionPanel";
    public static final String ACTION = "action";
    public static final String ACTION_ITEM = "actionItem";
    public static final String ADDITIONAL_INFORMATION = "additionalInformation";
    public static final String AGGREGATION = "aggregation";
    public static final String AGGREGATIONS = "aggregations";
    public static final String AGGREGATION_PROPERTIES = "aggregationProperties";
    public static final String ANCHOR = "anchor";
    public static final String ANGLE_AXIS = "angleAxis";
    public static final String ANIMATION_DELAY_FUNCTION = "animationDelayFunction";
    public static final String ANIMATION_DELAY_UPDATE_FUNCTION = "animationDelayUpdateFunction";
    public static final String ANIMATION_DURATION_FUNCTION = "animationDurationFunction";
    public static final String ANIMATION_DURATION_UPDATE_FUNCTION = "animationDurationUpdateFunction";
    public static final String APP_LAYOUT = "appLayout";
    public static final String AREA_STYLE = "areaStyle";
    public static final String ARIA = "aria";
    public static final String ARTICLE = "article";
    public static final String ASIDE = "aside";
    public static final String ATTRIBUTES = "attributes";
    public static final String AVATAR = "avatar";
    public static final String AXIS_LABEL = "axisLabel";
    public static final String AXIS_LINE = "axisLine";
    public static final String AXIS_NAME = "axisName";
    public static final String AXIS_POINTER = "axisPointer";
    public static final String AXIS_TICK = "axisTick";
    public static final String BACKGROUND_STYLE = "backgroundStyle";
    public static final String BAR = "bar";
    public static final String BIG_DECIMAL_FIELD = "bigDecimalField";
    public static final String BLOCK = "block";
    public static final String BLOCKS = "blocks";
    public static final String BLUR = "blur";
    public static final String BOXPLOT = "boxplot";
    public static final String BRUSH = "brush";
    public static final String BRUSH_STYLE = "brushStyle";
    public static final String BUSINESS_HOURS = "businessHours";
    public static final String BUTTON = "button";
    public static final String C3 = "c3";
    public static final String CALENDAR = "calendar";
    public static final String CALENDAR_PARAMETERS = "calendarParameters";
    public static final String CALLBACK_DATA_PROVIDER = "callbackDataProvider";
    public static final String CANCEL_BUTTON = "cancelButton";
    public static final String CANDLESTICK = "candlestick";
    public static final String CARD = "card";
    public static final String CHART = "chart";
    public static final String CHECKBOX = "checkbox";
    public static final String CHECKBOX_GROUP = "checkboxGroup";
    public static final String CLEAR_BUTTON_ICON = "clearButtonIcon";
    public static final String CLOSE_BUTTON = "closeButton";
    public static final String CLUSTER = "cluster";
    public static final String CODE = "code";
    public static final String CODE_EDITOR = "codeEditor";
    public static final String COLLECTION = "collection";
    public static final String COLOR_FUNCTION = "colorFunction";
    public static final String COLOR_ITEM = "colorItem";
    public static final String COLOR_PALETTE = "colorPalette";
    public static final String COLOR_SCALE_GENERATOR_FUNCTION = "colorScaleGeneratorFunction";
    public static final String COLUMN = "column";
    public static final String COLUMNS = "columns";
    public static final String COLUMN_FOOTER_RENDERER_FUNCTION = "columnFooterRendererFunction";
    public static final String COLUMN_HEADER_RENDERER_FUNCTION = "columnHeaderRendererFunction";
    public static final String COLUMN_REF = "columnRef";
    public static final String COMBO_BOX = "comboBox";
    public static final String COMBO_BUTTON = "comboButton";
    public static final String COMPONENT = "component";
    public static final String COMPONENT_ITEM = "componentItem";
    public static final String CONDITION = "condition";
    public static final String CONDITIONS = "conditions";
    public static final String CONFIGURATION = "configuration";
    public static final String CONFIGURATIONS = "configurations";
    public static final String CONTAINER_DATA_PROVIDER = "containerDataProvider";
    public static final String CONTENT = "content";
    public static final String CONTEXT_MENU = "contextMenu";
    public static final String CONTINUOUS_VISUAL_MAP = "continuousVisualMap";
    public static final String CONTROLLER = "controller";
    public static final String CROSS_STYLE = "crossStyle";
    public static final String CUSTOM = "custom";
    public static final String CUSTOM_DISPLAY_MODES = "customDisplayModes";
    public static final String CUSTOM_FORM_AREA = "customFormArea";
    public static final String DASHBOARD = "dashboard";
    public static final String DASHBOARD_SECTION = "dashboardSection";
    public static final String DASHBOARD_WIDGET = "dashboardWidget";
    public static final String DATA = "data";
    public static final String DATASET_CONSTRAINT = "datasetConstraint";
    public static final String DATASET_CONSTRAINTS = "datasetConstraints";
    public static final String DATA_BACKGROUND = "dataBackground";
    public static final String DATA_GRID = "dataGrid";
    public static final String DATA_GRID_FILTER = "dataGridFilter";
    public static final String DATA_ITEM = "dataItem";
    public static final String DATA_LOAD_COORDINATOR = "dataLoadCoordinator";
    public static final String DATA_PROVIDERS = "dataProviders";
    public static final String DATA_SET = "dataSet";
    public static final String DATA_VECTOR_SOURCE = "dataVectorSource";
    public static final String DATA_ZOOM = "dataZoom";
    public static final String DATE = "date";
    public static final String DATE_PICKER = "datePicker";
    public static final String DATE_TIME_PICKER = "dateTimePicker";
    public static final String DAY = "day";
    public static final String DAY_GRID_DAY = "dayGridDay";
    public static final String DAY_GRID_MONTH = "dayGridMonth";
    public static final String DAY_GRID_WEEK = "dayGridWeek";
    public static final String DAY_GRID_YEAR = "dayGridYear";
    public static final String DECAL = "decal";
    public static final String DECALS = "decals";
    public static final String DECIMAL_MAX = "decimalMax";
    public static final String DECIMAL_MIN = "decimalMin";
    public static final String DERIVED_PROPERTIES = "derivedProperties";
    public static final String DERIVED_PROPERTY = "derivedProperty";
    public static final String DESCRIPTION = "description";
    public static final String DESCRIPTION_LIST = "descriptionList";
    public static final String DETAIL = "detail";
    public static final String DETAILS = "details";
    public static final String DIGITS = "digits";
    public static final String DISPLAY_MODE = "displayMode";
    public static final String DISPLAY_MODE_PROPERTIES = "displayModeProperties";
    public static final String DIV = "div";
    public static final String DOUBLE_MAX = "doubleMax";
    public static final String DOUBLE_MIN = "doubleMin";
    public static final String DOWNLOAD_BUTTON_ICON = "downloadButtonIcon";
    public static final String DRAWER_LAYOUT = "drawerLayout";
    public static final String DRAWER_TOGGLE = "drawerToggle";
    public static final String DROPDOWN_BUTTON = "dropdownButton";
    public static final String DROPDOWN_ICON = "dropdownIcon";
    public static final String DROP_LABEL_ICON = "dropLabelIcon";
    public static final String DURATION = "duration";
    public static final String DYNAMIC_ATTRIBUTES = "dynamicAttributes";
    public static final String DYNAMIC_ATTRIBUTES_PANEL = "dynamicAttributesPanel";
    public static final String EDITOR_ACTIONS_COLUMN = "editorActionsColumn";
    public static final String EDIT_BUTTON = "editButton";
    public static final String EFFECT_SCATTER = "effectScatter";
    public static final String EMAIL = "email";
    public static final String EMAIL_FIELD = "emailField";
    public static final String EMPHASIS = "emphasis";
    public static final String EMPTY_CIRCLE_STYLE = "emptyCircleStyle";
    public static final String EMPTY_STATE_COMPONENT = "emptyStateComponent";
    public static final String ENCODE = "encode";
    public static final String END_LABEL = "endLabel";
    public static final String END_POINT = "endPoint";
    public static final String END_SLOT = "endSlot";
    public static final String ENTITY_COMBO_BOX = "entityComboBox";
    public static final String ENTITY_PICKER = "entityPicker";
    public static final String ENTRY = "entry";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String EXCLUSIONS = "exclusions";
    public static final String EXTENT = "extent";
    public static final String FEATURES = "features";
    public static final String FIELD_SET = "fieldSet";
    public static final String FILE_STORAGE_UPLOAD_FIELD = "fileStorageUploadField";
    public static final String FILE_UPLOAD_FIELD = "fileUploadField";
    public static final String FILTER_FUNCTION = "filterFunction";
    public static final String FLEX_LAYOUT = "flexLayout";
    public static final String FONT_ICON = "fontIcon";
    public static final String FOOTER = "footer";
    public static final String FORM = "form";
    public static final String FORMATTER = "formatter";
    public static final String FORMATTER_FUNCTION = "formatterFunction";
    public static final String FORM_ITEM = "formItem";
    public static final String FORM_LAYOUT = "formLayout";
    public static final String FORM_ROW = "formRow";
    public static final String FRAGMENT = "fragment";
    public static final String FRAGMENT_DATA_LOAD_COORDINATOR = "fragmentDataLoadCoordinator";
    public static final String FRAGMENT_RENDERER = "fragmentRenderer";
    public static final String FRAGMENT_SETTINGS = "fragmentSettings";
    public static final String FULL_TEXT_FILTER = "fullTextFilter";
    public static final String FUNCTION = "function";
    public static final String FUNNEL = "funnel";
    public static final String FUTURE = "future";
    public static final String FUTURE_OR_PRESENT = "futureOrPresent";
    public static final String GAUGE = "gauge";
    public static final String GENERAL = "general";
    public static final String GENERIC_FILTER = "genericFilter";
    public static final String GEO_MAP = "geoMap";
    public static final String GRAPES_JS = "grapesJs";
    public static final String GRID = "grid";
    public static final String GRID_COLUMN_VISIBILITY = "gridColumnVisibility";
    public static final String GRID_ITEM = "gridItem";
    public static final String GRID_LAYOUT = "gridLayout";
    public static final String GROUP_BY = "groupBy";
    public static final String GROUP_COLUMN = "groupColumn";
    public static final String GROUP_DATA_GRID = "groupDataGrid";
    public static final String GROUP_FILTER = "groupFilter";
    public static final String H1 = "h1";
    public static final String H2 = "h2";
    public static final String H3 = "h3";
    public static final String H4 = "h4";
    public static final String H5 = "h5";
    public static final String H6 = "h6";
    public static final String HANDLE = "handle";
    public static final String HANDLE_STYLE = "handleStyle";
    public static final String HBOX = "hbox";
    public static final String HEADER = "header";
    public static final String HEADER_CONTENT = "headerContent";
    public static final String HEADER_PREFIX = "headerPrefix";
    public static final String HEADER_SUFFIX = "headerSuffix";
    public static final String HEATMAP = "heatmap";
    public static final String HEATMAP_DATA_VECTOR_SOURCE = "heatmapDataVectorSource";
    public static final String HIDDEN_DAYS = "hiddenDays";
    public static final String HIDDEN_FROM_AGGREGATIONS = "hiddenFromAggregations";
    public static final String HIDDEN_FROM_DRAG_DROP = "hiddenFromDragDrop";
    public static final String HIDDEN_PROPERTIES = "hiddenProperties";
    public static final String HORIZONTAL = "horizontal";
    public static final String HORIZONTAL_MENU = "horizontalMenu";
    public static final String HR = "hr";
    public static final String HTML = "html";
    public static final String HTML_OBJECT = "htmlObject";
    public static final String ICON = "icon";
    public static final String ICON_STYLE = "iconStyle";
    public static final String IFRAME = "iframe";
    public static final String IMAGE = "image";
    public static final String IMAGE_EXTENT = "imageExtent";
    public static final String IMAGE_STATIC_SOURCE = "imageStaticSource";
    public static final String IMAGE_WMS_SOURCE = "imageWmsSource";
    public static final String INCLUSIONS = "inclusions";
    public static final String INDICATOR = "indicator";
    public static final String INDICATORS = "indicators";
    public static final String INDICATOR_STYLE = "indicatorStyle";
    public static final String INITIAL_LAYOUT = "initialLayout";
    public static final String INPUT = "input";
    public static final String INSIDE_DATA_ZOOM = "insideDataZoom";
    public static final String INSTANCE = "instance";
    public static final String INTEGER_FIELD = "integerField";
    public static final String INTERVAL_FUNCTION = "intervalFunction";
    public static final String IN_BRUSH = "inBrush";
    public static final String IN_RANGE = "inRange";
    public static final String ITEM = "item";
    public static final String ITEMS = "items";
    public static final String ITEMS_QUERY = "itemsQuery";
    public static final String ITEM_STYLE = "itemStyle";
    public static final String JPQL = "jpql";
    public static final String JPQL_FILTER = "jpqlFilter";
    public static final String KANBAN = "kanban";
    public static final String KEY_VALUE_COLLECTION = "keyValueCollection";
    public static final String KEY_VALUE_INSTANCE = "keyValueInstance";
    public static final String LABEL = "label";
    public static final String LABEL_FORMATTER_FUNCTION = "labelFormatterFunction";
    public static final String LABEL_LAYOUT = "labelLayout";
    public static final String LABEL_LINE = "labelLine";
    public static final String LAYERS = "layers";
    public static final String LAYOUT = "layout";
    public static final String LEFT_TOP_POINT = "leftTopPoint";
    public static final String LEGEND = "legend";
    public static final String LINE = "line";
    public static final String LINE_STYLE = "lineStyle";
    public static final String LIST_BOX = "listBox";
    public static final String LIST_DAY = "listDay";
    public static final String LIST_ITEM = "listItem";
    public static final String LIST_MENU = "listMenu";
    public static final String LIST_MONTH = "listMonth";
    public static final String LIST_WEEK = "listWeek";
    public static final String LIST_YEAR = "listYear";
    public static final String LOADER = "loader";
    public static final String LOCAL_DATE_RENDERER = "localDateRenderer";
    public static final String LOCAL_DATE_TIME_RENDERER = "localDateTimeRenderer";
    public static final String LOGIN_FORM = "loginForm";
    public static final String LOGIN_OVERLAY = "loginOverlay";
    public static final String MAGIC_TYPE = "magicType";
    public static final String MAIN = "main";
    public static final String MAIN_TAB_SHEET = "mainTabSheet";
    public static final String MAIN_VIEW = "mainView";
    public static final String MAP_VIEW = "mapView";
    public static final String MARKDOWN = "markdown";
    public static final String MARK_AREA = "markArea";
    public static final String MARK_LINE = "markLine";
    public static final String MARK_POINT = "markPoint";
    public static final String MAX = "max";
    public static final String MAX_FUNCTION = "maxFunction";
    public static final String MEDIA = "media";
    public static final String MENU_FILTER_FIELD = "menuFilterField";
    public static final String MENU_ITEM = "menuItem";
    public static final String MIDDLE_SLOT = "middleSlot";
    public static final String MIN = "min";
    public static final String MINOR_SPLIT_LINE = "minorSplitLine";
    public static final String MINOR_TICK = "minorTick";
    public static final String MIN_FUNCTION = "minFunction";
    public static final String MOVE_HANDLE_STYLE = "moveHandleStyle";
    public static final String MULTIPLE = "multiple";
    public static final String MULTI_MONTH_YEAR = "multiMonthYear";
    public static final String MULTI_SELECT_COMBO_BOX = "multiSelectComboBox";
    public static final String MULTI_SELECT_COMBO_BOX_PICKER = "multiSelectComboBoxPicker";
    public static final String MULTI_SELECT_LIST_BOX = "multiSelectListBox";
    public static final String MULTI_VALUE_PICKER = "multiValuePicker";
    public static final String NAME_TEXT_STYLE = "nameTextStyle";
    public static final String NATIVE_BUTTON = "nativeButton";
    public static final String NATIVE_DETAILS = "nativeDetails";
    public static final String NATIVE_JSON = "nativeJson";
    public static final String NATIVE_LABEL = "nativeLabel";
    public static final String NAV = "nav";
    public static final String NAVIGATION_BAR = "navigationBar";
    public static final String NEGATIVE = "negative";
    public static final String NEGATIVE_OR_ZERO = "negativeOrZero";
    public static final String NOTIFICATIONS_INDICATOR = "notificationsIndicator";
    public static final String NOT_BLANK = "notBlank";
    public static final String NOT_EMPTY = "notEmpty";
    public static final String NOT_NULL = "notNull";
    public static final String NUMBER = "number";
    public static final String NUMBER_FIELD = "numberField";
    public static final String NUMBER_RENDERER = "numberRenderer";
    public static final String ON_COMPONENT_VALUE_CHANGED = "onComponentValueChanged";
    public static final String ON_CONTAINER_ITEM_CHANGED = "onContainerItemChanged";
    public static final String ON_FRAGMENT_EVENT = "onFragmentEvent";
    public static final String ON_VIEW_EVENT = "onViewEvent";
    public static final String ORDERED_LIST = "orderedList";
    public static final String OSM_SOURCE = "osmSource";
    public static final String OUT_OF_BRUSH = "outOfBrush";
    public static final String OUT_RANGE = "outRange";
    public static final String P = "p";
    public static final String PAGE_FORMATTER_FUNCTION = "pageFormatterFunction";
    public static final String PAGE_ICONS = "pageIcons";
    public static final String PAGE_TEXT_STYLE = "pageTextStyle";
    public static final String PAGINATION = "pagination";
    public static final String PAIR_POINT_LINE = "pairPointLine";
    public static final String PARAM = "param";
    public static final String PARAMETER = "parameter";
    public static final String PARAMETERS = "parameters";
    public static final String PASSWORD_FIELD = "passwordField";
    public static final String PAST = "past";
    public static final String PAST_OR_PRESENT = "pastOrPresent";
    public static final String PIE = "pie";
    public static final String PIECE = "piece";
    public static final String PIECES = "pieces";
    public static final String PIECEWISE_VISUAL_MAP = "piecewiseVisualMap";
    public static final String PIVOT_TABLE = "pivotTable";
    public static final String PLUGIN = "plugin";
    public static final String PLUGINS = "plugins";
    public static final String POINT = "point";
    public static final String POINTER = "pointer";
    public static final String POINT_PAIR = "pointPair";
    public static final String POLAR = "polar";
    public static final String POSITIVE = "positive";
    public static final String POSITIVE_OR_ZERO = "positiveOrZero";
    public static final String PRE = "pre";
    public static final String PREFIX = "prefix";
    public static final String PROGRESS = "progress";
    public static final String PROGRESS_BAR = "progressBar";
    public static final String PROJECTION = "projection";
    public static final String PROPERTIES = "properties";
    public static final String PROPERTIES_MAPPING = "propertiesMapping";
    public static final String PROPERTY = "property";
    public static final String PROPERTY_FILTER = "propertyFilter";
    public static final String RADAR = "radar";
    public static final String RADIO_BUTTON_GROUP = "radioButtonGroup";
    public static final String RADIUS_AXIS = "radiusAxis";
    public static final String RANGE_INPUT = "rangeInput";
    public static final String REFRESH = "refresh";
    public static final String REGEXP = "regexp";
    public static final String RENDERER = "renderer";
    public static final String RENDERERS = "renderers";
    public static final String RENDERER_OPTIONS = "rendererOptions";
    public static final String RESPONSIVE_STEP = "responsiveStep";
    public static final String RESPONSIVE_STEPS = "responsiveSteps";
    public static final String RESTORE = "restore";
    public static final String RICH = "rich";
    public static final String RICH_STYLE = "richStyle";
    public static final String RICH_TEXT_EDITOR = "richTextEditor";
    public static final String RIGHT_BOTTOM_POINT = "rightBottomPoint";
    public static final String RIPPLE_EFFECT = "rippleEffect";
    public static final String ROW = "row";
    public static final String ROWS = "rows";
    public static final String SAVE_AS_IMAGE = "saveAsImage";
    public static final String SAVE_BUTTON = "saveButton";
    public static final String SCATTER = "scatter";
    public static final String SCROLLABLE_LEGEND = "scrollableLegend";
    public static final String SCROLLER = "scroller";
    public static final String SEARCH_FIELD = "searchField";
    public static final String SECTION = "section";
    public static final String SELECT = "select";
    public static final String SELECTED = "selected";
    public static final String SELECTED_DATA_BACKGROUND = "selectedDataBackground";
    public static final String SELECTOR_LABEL = "selectorLabel";
    public static final String SEPARATOR = "separator";
    public static final String SERIES = "series";
    public static final String SETTINGS = "settings";
    public static final String SHADOW_STYLE = "shadowStyle";
    public static final String SHORTCUT_COMBINATION = "shortcutCombination";
    public static final String SIDE_PANEL_LAYOUT = "sidePanelLayout";
    public static final String SIDE_PANEL_LAYOUT_CLOSER = "sidePanelLayoutCloser";
    public static final String SIMPLE_PAGINATION = "simplePagination";
    public static final String SINGLE = "single";
    public static final String SINGLE_POINT_LINE = "singlePointLine";
    public static final String SIZE = "size";
    public static final String SLIDER_DATA_ZOOM = "sliderDataZoom";
    public static final String SORTERS_FUNCTION = "sortersFunction";
    public static final String SORT_FUNCTION = "sortFunction";
    public static final String SOURCE = "source";
    public static final String SPAN = "span";
    public static final String SPLIT = "split";
    public static final String SPLIT_AREA = "splitArea";
    public static final String SPLIT_LINE = "splitLine";
    public static final String SPREADSHEET = "spreadsheet";
    public static final String START_POINT = "startPoint";
    public static final String START_SLOT = "startSlot";
    public static final String STATE_ANIMATION = "stateAnimation";
    public static final String SUBTEXT_STYLE = "subtextStyle";
    public static final String SUBTITLE = "subtitle";
    public static final String SUFFIX = "suffix";
    public static final String SVG_ICON = "svgIcon";
    public static final String SWIMLANE = "swimlane";
    public static final String SWIMLANES = "swimlanes";
    public static final String SWITCH = "switch";
    public static final String SYMBOL_SIZE_FUNCTION = "symbolSizeFunction";
    public static final String TAB = "tab";
    public static final String TABS = "tabs";
    public static final String TAB_SHEET = "tabSheet";
    public static final String TASK_RENDERER_FUNCTION = "taskRendererFunction";
    public static final String TERM = "term";
    public static final String TEST_COMPONENT = "testComponent";
    public static final String TEXT_AREA = "textArea";
    public static final String TEXT_FIELD = "textField";
    public static final String TEXT_ITEM = "textItem";
    public static final String TEXT_STYLE = "textStyle";
    public static final String TILE = "tile";
    public static final String TILE_SIZE = "tileSize";
    public static final String TILE_WMS_SOURCE = "tileWmsSource";
    public static final String TIMER = "timer";
    public static final String TIME_GRID_DAY = "timeGridDay";
    public static final String TIME_GRID_WEEK = "timeGridWeek";
    public static final String TIME_PICKER = "timePicker";
    public static final String TITLE = "title";
    public static final String TOOLBOX = "toolbox";
    public static final String TOOLTIP = "tooltip";
    public static final String TREE_DATA_GRID = "treeDataGrid";
    public static final String TWIN_COLUMN = "twinColumn";
    public static final String UNORDERED_LIST = "unorderedList";
    public static final String UPLOAD = "upload";
    public static final String UPLOAD_ICON = "uploadIcon";
    public static final String URL_QUERY_PARAMETERS = "urlQueryParameters";
    public static final String USER_INDICATOR = "userIndicator";
    public static final String USER_MENU = "userMenu";
    public static final String VALIDATORS = "validators";
    public static final String VALUE = "value";
    public static final String VALUE_FORMATTER_FUNCTION = "valueFormatterFunction";
    public static final String VALUE_PICKER = "valuePicker";
    public static final String VBOX = "vbox";
    public static final String VECTOR = "vector";
    public static final String VECTOR_SOURCE = "vectorSource";
    public static final String VERTICAL = "vertical";
    public static final String VIEW = "view";
    public static final String VIEW_ITEM = "viewItem";
    public static final String VIRTUAL_LIST = "virtualList";
    public static final String VISUAL_MAP = "visualMap";
    public static final String WEBDAV_DOCUMENT_LINK = "webdavDocumentLink";
    public static final String WEBDAV_DOCUMENT_UPLOAD = "webdavDocumentUpload";
    public static final String WEBDAV_DOCUMENT_VERSION_LINK = "webdavDocumentVersionLink";
    public static final String WORK_AREA = "workArea";
    public static final String WORLD_EXTENT = "worldExtent";
    public static final String XYZ_SOURCE = "xyzSource";
    public static final String X_AXES = "xAxes";
    public static final String X_AXIS = "xAxis";
    public static final String Y_AXES = "yAxes";
    public static final String Y_AXIS = "yAxis";

    private static final Map<String, String> CONSTANT_REFERENCES = createConstantReferences();
    private static final Map<String, String> CONSTANT_VALUES = createConstantValues();

    private StudioXmlElements() {
    }

    public static @Nullable String resolveConstantReference(String xmlElement) {
        return CONSTANT_REFERENCES.get(xmlElement);
    }

    public static @Nullable String resolveConstantValue(@Nullable String constantReference) {
        if (constantReference == null) {
            return null;
        }

        return CONSTANT_VALUES.get(constantReference);
    }

    private static Map<String, String> createConstantReferences() {
        Map<String, String> references = new LinkedHashMap<>();
        for (Field field : StudioXmlElements.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || field.getType() != String.class) {
                continue;
            }

            try {
                references.put((String) field.get(null), constantReference(field));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to access StudioXmlElements constant " + field.getName(), e);
            }
        }
        return Map.copyOf(references);
    }

    private static Map<String, String> createConstantValues() {
        Map<String, String> values = new LinkedHashMap<>();
        for (Field field : StudioXmlElements.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || field.getType() != String.class) {
                continue;
            }

            try {
                String constantValue = (String) field.get(null);
                values.put(shortConstantReference(field), constantValue);
                values.put(constantReference(field), constantValue);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to access StudioXmlElements constant " + field.getName(), e);
            }
        }
        return Map.copyOf(values);
    }

    private static String shortConstantReference(Field field) {
        return StudioXmlElements.class.getSimpleName() + "." + field.getName();
    }

    private static String constantReference(Field field) {
        return StudioXmlElements.class.getName() + "." + field.getName();
    }
}
