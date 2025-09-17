import localesAll from '@fullcalendar/core/locales-all.js';

import * as utils from "./jmix-full-calendar-utils";
import {RAW_EN_LOCALE} from "./jmix-full-calendar-utils";
import moment from 'moment';

const NO_VIEW_MORE_LINK_CLICK = 'NO_VIEW';

export const MORE_LINK_CLICK = 'moreLinkClick';
export const MORE_LINK_CLASS_NAMES = 'moreLinkClassNames';
export const UNSELECT_CANCEL = 'unselectCancel';
export const DAY_HEADER_CLASS_NAMES = 'dayHeaderClassNames';
export const DAY_CELL_CLASS_NAMES = 'dayCellClassNames';
export const SLOT_LABEL_CLASS_NAMES = 'slotLabelClassNames';
export const NOW_INDICATOR_CLASS_NAMES = 'nowIndicatorClassNames';
export const NAV_LINK_DAY_CLICK = 'navLinkDayClick';
export const NAV_LINK_WEEK_CLICK = 'navLinkWeekClick';
export const DAY_CELL_BOTTOM_TEXT = 'dayCellBottomText';
export const CURRENT_DATE = 'currentDate';
export const CURRENT_SELECTION = 'currentSelection';
export const CURRENT_VIEW = 'currentView';
const EVENT_OVERLAP = 'eventOverlap';
const SELECT_OVERLAP = 'selectOverlap';
const EVENT_CONSTRAINT = 'eventConstraint';
const BUSINESS_HOURS = 'businessHours';
const SELECT_CONSTRAINT = 'selectConstraint';
const SELECT_ALLOW = 'selectAllow';
const VIEWS = 'views';
const DAY_MAX_EVENT_ROWS = 'dayMaxEventRows';
const DAY_MAX_EVENTS = 'dayMaxEvents';
const EVENT_ORDER = 'eventOrder';

/**
 * Options that should be skipped by default FullCalendar updating and should be updated manually.
 * Because they don't exist in FullCalendar or have complex content.
 */
const SKIP_OPTIONS = [MORE_LINK_CLICK, MORE_LINK_CLASS_NAMES, UNSELECT_CANCEL, DAY_HEADER_CLASS_NAMES,
    DAY_CELL_CLASS_NAMES, SLOT_LABEL_CLASS_NAMES, NOW_INDICATOR_CLASS_NAMES, NAV_LINK_DAY_CLICK, NAV_LINK_WEEK_CLICK,
    DAY_CELL_BOTTOM_TEXT, EVENT_OVERLAP, SELECT_OVERLAP, EVENT_CONSTRAINT, BUSINESS_HOURS, SELECT_CONSTRAINT,
    SELECT_ALLOW, VIEWS, DAY_MAX_EVENT_ROWS, DAY_MAX_EVENTS, EVENT_ORDER, CURRENT_SELECTION, CURRENT_DATE, CURRENT_VIEW];

class Options {

    constructor() {
        this.customOptions = {};
    }

    set calendar(calendar) {
        this._calendar = calendar;
    }

    addListener(optionName, listener) {
        if (!this.listeners) {
            this.listeners = {};
        }
        if (!this.listeners[optionName]) {
            this.listeners[optionName] = [];
        }
        this.listeners[optionName].push(listener);
    }

    updateOptions(options) {
        if (!options) {
            return;
        }
        this._calendar.batchRendering(() => {
            for (const key in options) {
                if (!this._skipOption(key)) {
                    this.updateOption(key, options[key]);
                }
            }

            this._updateMoreLinkClick(options);
            this._updateMoreLinkClassNames(options);
            this._updateEventOverlap(options);
            this._updateEventConstraint(options);
            this._updateBusinessHours(options);
            this._updateSelectOverlap(options);
            this._updateSelectConstraint(options);
            this._updateSelectAllow(options);

            this._updateDayMaxEventRows(options);
            this._updateDayMaxEvents(options);

            this._updateDayHeaderClassNames(options);
            this._updateDayCellClassNames(options);
            this._updateSlotLabelClassNames(options);
            this._updateNowIndicatorClassNames(options);

            this._updateEventOrder(options);

            this._updateNavLinkDayClick(options);
            this._updateNavLinkWeekClick(options);

            this._updateDyCellBottomText(options);
            this._updateCurrentDate(options);
            this._updateCurrentSelection(options);
            this._updateCurrentView(options);
        });
    }

    getOption(name) {
        if (this.customOptions[name]) {
            return this.customOptions[name];
        }
        return this._calendar.getOption(name);
    }

    updateOption(key, value) {
        let oldValue = this._calendar.getOption(key);
        if (oldValue !== value) {
            this._calendar.setOption(key, value);
        }
    }

    updateLocale(jmixI18n) {
        if (!jmixI18n) {
            return;
        }
        // Every instance of JmixFullCalendar should have unique locale
        // suffix for moment.js. When FullCalendar uses momentPlugin the
        // localisation of months, days, etc. is configured by moment.js locale.
        // However, moment.js instance is a global for a web page. And if we change
        // for English locale names of month, it can affect all usages of moment.js
        // in the web page. To avoid this, add suffix for locales. Thus, it creates
        // new locale, that won't affect usages of moment.js.
        const createMomentLocale = !this.localeSuffix
        if (createMomentLocale) {
            this.localeSuffix = this._generateLocaleSuffix();
        }

        // Create FullCalendar locale
        const calendarI18nArray = localesAll.filter((item) => item.code === jmixI18n.locale);
        let calendarI18n = calendarI18nArray.length > 0 ? calendarI18nArray[0] : RAW_EN_LOCALE;
        const newLocaleName = calendarI18n.code + '-' + this.localeSuffix;
        calendarI18n = this._combineI18n(calendarI18n, jmixI18n);
        calendarI18n.code = newLocaleName;

        // Create Moment locale
        const momentLocale = jmixI18n.momentLocale;
        momentLocale.localeName = newLocaleName;
        momentLocale.week = {};
        momentLocale.week.dow = calendarI18n.week.dow;
        momentLocale.week.doy = calendarI18n.week.doy;

        this._updateMomentJsLocale(momentLocale, createMomentLocale);

        this.updateOption("locale", calendarI18n);

        const formatOptions = utils.convertToLocaleDependedOptions(jmixI18n);

        this.updateOptions(formatOptions);
    }

    processInitialOptions(serverOptions) {
        const options = serverOptions;
        if (!serverOptions) {
            return {};
        }

        const views = serverOptions[VIEWS];
        if (views) {
            options[VIEWS] = this._processViews(views);
        }

        const unselectCancel = serverOptions[UNSELECT_CANCEL];
        if (!unselectCancel) {
            delete options[UNSELECT_CANCEL];
        }

        for (const property in options) {
            const value = options[property];
            if (value === null || value === undefined) {
                delete options[property];
            }
        }

        if (options[MORE_LINK_CLICK]) {
            options[MORE_LINK_CLICK] = this._getMoreLinkClick(options);
        }
        if (options[MORE_LINK_CLASS_NAMES]) {
            options[MORE_LINK_CLASS_NAMES] = this._getMoreLinkClassNames(options);
        }
        if (options[EVENT_OVERLAP]) {
            options[EVENT_OVERLAP] = this._getEventOverlap(options);
        }
        if (options[EVENT_CONSTRAINT]) {
            options[EVENT_CONSTRAINT] = this._getEventConstraint(options);
        }
        if (options[BUSINESS_HOURS]) {
            options[BUSINESS_HOURS] = this._getBusinessHours(options);
        }
        if (options[SELECT_OVERLAP]) {
            options[SELECT_OVERLAP] = this._getSelectOverlap(options);
        }
        if (options[SELECT_CONSTRAINT]) {
            options[SELECT_CONSTRAINT] = this._getSelectConstraint(options);
        }

        const selectAllow = this._getSelectAllow(options)
        if (selectAllow) {
            options[SELECT_ALLOW] = selectAllow;
        }

        if (options[DAY_MAX_EVENT_ROWS]) {
            options[DAY_MAX_EVENT_ROWS] = this._getDayMaxEventRows(options);
        }
        if (options[DAY_MAX_EVENTS]) {
            options[DAY_MAX_EVENTS] = this._getDayMaxEvents(options);
        }
        if (options[DAY_HEADER_CLASS_NAMES]) {
            options[DAY_HEADER_CLASS_NAMES] = this._getDayHeaderClassNames(options);
        }
        if (options[DAY_CELL_CLASS_NAMES]) {
            options[DAY_CELL_CLASS_NAMES] = this._getDayCellClassNames(options);
        }
        if (options[SLOT_LABEL_CLASS_NAMES]) {
            options[SLOT_LABEL_CLASS_NAMES] = this._getSlotLabelClassNames(options);
        }
        if (options[NOW_INDICATOR_CLASS_NAMES]) {
            options[NOW_INDICATOR_CLASS_NAMES] = this._getNowIndicatorClassNames(options);
        }
        if (options[EVENT_ORDER]) {
            options[EVENT_ORDER] = this._getEventOrder(options);
        }
        if (options[NAV_LINK_DAY_CLICK]) {
            options[NAV_LINK_DAY_CLICK] = this._getNavLinkDayClick(options);
        }
        if (options[NAV_LINK_WEEK_CLICK]) {
            options[NAV_LINK_WEEK_CLICK] = this._getNavLinkWeekClick(options);
        }

        this._updateDyCellBottomText(options);
        delete options.dayCellBottomText;

        this._updateCurrentDate(options);
        delete options.currentDate;

        this._updateCurrentSelection(options);
        delete options.currentSelection;

        this._updateCurrentView(options);
        delete options.currentView;

        return options;
    }

    _processViews(viewsObject) {
        for (let view in viewsObject) {
            if (view === 'customCalendarDisplayModes') {
                continue;
            }
            if (view === 'listDay' || view === 'listWeek' || view === 'listMonth' || view === 'listYear') {
                viewsObject[view] = this._processListView(viewsObject[view]);
                continue;
            }
            if (view === 'timeGridDay' || view === 'timeGridWeek') {
                viewsObject[view] = this._processTimeGrid(viewsObject[view]);
                continue;
            }
            if (view === 'dayGridDay' || view === 'dayGridWeek' || view === 'dayGridMonth' || view === 'dayGridYear') {
                viewsObject[view] = {...viewsObject[view], ...(viewsObject[view].properties) && {...viewsObject[view].properties}};
                viewsObject[view] = utils.deleteNullProperties(viewsObject[view]);
                delete viewsObject[view].properties;
            }
        }

        if (viewsObject.customCalendarDisplayModes) {
            for (const view of viewsObject.customCalendarDisplayModes) {
                viewsObject[view.displayMode] = {
                    type: view.baseDisplayMode,
                    ...(view.dayCount) && {dayCount: view.dayCount},
                    ...(view.duration) && {duration: view.duration},
                    ...view.properties && {...view.properties}
                };
            }
        }
        delete viewsObject.customCalendarDisplayModes;

        return viewsObject;
    }

    _processListView(listView) {
        let newListView = {...listView, ...listView.properties && {...listView.properties}};

        // Delete null properties and 'properties' property
        newListView = utils.deleteNullProperties(newListView);
        delete newListView.properties;

        // Handle listDayFormat
        if (newListView.listDayVisible === false) {
            newListView.listDayFormat = false;
            delete newListView.listDayVisible;
        }
        // Handle listDaySideFormat
        if (newListView.listDaySideVisible === false) {
            newListView.listDaySideFormat = false;
            delete newListView.listDaySideVisible;
        }
        return newListView;
    }

    _processTimeGrid(timeGridView) {
        let newTimeGridView = {...timeGridView, ...timeGridView.properties && {...timeGridView.properties}};

        // Delete null properties and 'properties' property
        newTimeGridView = utils.deleteNullProperties(newTimeGridView);
        delete newTimeGridView.properties;

        // Handle allDaySlot
        if (utils.isNotNullUndefined(newTimeGridView.allDaySlotVisible)) {
            newTimeGridView.allDaySlot = newTimeGridView.allDaySlotVisible;
            delete newTimeGridView.allDaySlotVisible;
        }
        return newTimeGridView;
    }

    _skipOption(key) {
        return SKIP_OPTIONS.includes(key);
    }

    _getMoreLinkClick(options) {
        const moreLinkClick = options[MORE_LINK_CLICK]

        if (moreLinkClick) {
            if (moreLinkClick.functionEnabled) {
                return this._onMoreLinkClick.bind(this);
            }
            return moreLinkClick.calendarView;
        }
    }

    _updateMoreLinkClick(options) {
        const moreLinkClick = options[MORE_LINK_CLICK]

        if (moreLinkClick) {
            this.updateOption('moreLinkClick', moreLinkClick.calendarView);

            if (moreLinkClick.functionEnabled) {
                this.updateOption('moreLinkClick', this._onMoreLinkClick.bind(this));
            }
        }
    }

    _getMoreLinkClassNames(options) {
        const moreLinkClassNames = options[MORE_LINK_CLASS_NAMES];

        if (moreLinkClassNames) {
            if (moreLinkClassNames.functionEnabled) {
                return this._onMoreLinkClassNames.bind(this);
            }
            return moreLinkClassNames.classNames;
        }
    }

    _updateMoreLinkClassNames(options) {
        const moreLinkClassNames = options[MORE_LINK_CLASS_NAMES];

        if (moreLinkClassNames) {
            this.updateOption(MORE_LINK_CLASS_NAMES, moreLinkClassNames.classNames);

            if (moreLinkClassNames.functionEnabled) {
                this.updateOption(MORE_LINK_CLASS_NAMES, this._onMoreLinkClassNames.bind(this));
            }
        }
    }

    _getEventOverlap(options) {
        const eventOverlap = options[EVENT_OVERLAP];

        if (eventOverlap) {
            if (eventOverlap.jsFunction) {
                const jsFunction = utils.parseJavaScriptFunction(eventOverlap['jsFunction']);
                if (jsFunction) {
                    return jsFunction;
                }
            }
            return eventOverlap.enabled;
        }
    }

    _updateEventOverlap(options) {
        const eventOverlap = options[EVENT_OVERLAP];

        if (eventOverlap) {
            this.updateOption(EVENT_OVERLAP, eventOverlap.enabled);

            if (eventOverlap.jsFunction) {
                const jsFunction = utils.parseJavaScriptFunction(eventOverlap['jsFunction']);
                if (jsFunction) {
                    this.updateOption(EVENT_OVERLAP, jsFunction);
                }
            }
        }
    }

    _getEventConstraint(options) {
        const eventConstraint = options[EVENT_CONSTRAINT];

        if (eventConstraint) {
            const bHours = eventConstraint.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                return bHours;
            }
            if (eventConstraint.groupId) {
                return eventConstraint.groupId;
            }

            return eventConstraint.businessHoursEnabled ? "businessHours" : undefined;
        }
    }

    _updateEventConstraint(options) {
        const eventConstraint = options[EVENT_CONSTRAINT];

        if (eventConstraint) {
            const bHours = eventConstraint.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                this.updateOption(EVENT_CONSTRAINT, bHours);
                return;
            }
            if (eventConstraint.groupId) {
                this.updateOption(EVENT_CONSTRAINT, eventConstraint.groupId);
                return;
            }

            this.updateOption(EVENT_CONSTRAINT, eventConstraint.businessHoursEnabled ? "businessHours" : undefined);
        }
    }

    _getBusinessHours(options) {
        const businessHours = options[BUSINESS_HOURS];

        if (businessHours) {
            const bHours = businessHours.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                return bHours;
            }
            return businessHours.enabled;
        }
    }

    _updateBusinessHours(options) {
        const businessHours = options[BUSINESS_HOURS];

        if (businessHours) {
            this.updateOption(BUSINESS_HOURS, businessHours.enabled);

            const bHours = businessHours.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                this.updateOption(BUSINESS_HOURS, bHours);
            }
        }
    }

    _getSelectOverlap(options) {
        const selectOverlap = options[SELECT_OVERLAP];

        if (selectOverlap) {
            if (selectOverlap.jsFunction) {
                const jsFunction = utils.parseJavaScriptFunction(selectOverlap.jsFunction);
                if (jsFunction) {
                    return jsFunction;
                }
            }
            return selectOverlap.enabled;
        }
    }

    _updateSelectOverlap(options) {
        const selectOverlap = options[SELECT_OVERLAP];

        if (selectOverlap) {
            this.updateOption(SELECT_OVERLAP, selectOverlap.enabled);

            if (selectOverlap.jsFunction) {
                const jsFunction = utils.parseJavaScriptFunction(selectOverlap.jsFunction);
                if (jsFunction) {
                    this.updateOption(SELECT_OVERLAP, jsFunction);
                }
            }
        }
    }

    _getSelectConstraint(options) {
        const selectConstraint = options[SELECT_CONSTRAINT];

        if (selectConstraint) {
            const bHours = selectConstraint.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                return bHours;
            }
            if (selectConstraint.groupId) {
                return selectConstraint.groupId;
            }
            return selectConstraint.businessHoursEnabled ? "businessHours" : undefined;
        }
    }

    _updateSelectConstraint(options) {
        const selectConstraint = options[SELECT_CONSTRAINT];

        if (selectConstraint) {
            const bHours = selectConstraint.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                this.updateOption(SELECT_CONSTRAINT, bHours);
                return;
            }
            if (selectConstraint.groupId) {
                this.updateOption(SELECT_CONSTRAINT, selectConstraint.groupId);
                return;
            }

            this.updateOption(SELECT_CONSTRAINT, selectConstraint.businessHoursEnabled ? "businessHours" : undefined);
        }
    }

    _getSelectAllow(options) {
        const selectAllow = options[SELECT_ALLOW];
        if (selectAllow === undefined) {
            return;
        }
        if (selectAllow === null) {
            return selectAllow;
        } else {
            const jsFunction = utils.parseJavaScriptFunction(selectAllow);
            if (jsFunction) {
                return jsFunction;
            }
        }
    }

    _updateSelectAllow(options) {
        const selectAllow = options[SELECT_ALLOW];
        if (selectAllow === undefined) {
            return;
        }
        if (selectAllow === null) {
            this.updateOption(SELECT_ALLOW, selectAllow);
        } else {
            const jsFunction = utils.parseJavaScriptFunction(selectAllow);
            if (jsFunction) {
                this.updateOption(SELECT_ALLOW, jsFunction);
            }
        }
    }

    _getDayMaxEventRows(options) {
        const dayMaxEventRows = options[DAY_MAX_EVENT_ROWS];

        if (dayMaxEventRows) {
            if (dayMaxEventRows.max) {
                return dayMaxEventRows.max;
            }
            return dayMaxEventRows.defaultEnabled;
        }
    }

    _updateDayMaxEventRows(options) {
        const dayMaxEventRows = options[DAY_MAX_EVENT_ROWS];

        if (dayMaxEventRows) {
            this.updateOption(DAY_MAX_EVENT_ROWS, dayMaxEventRows.defaultEnabled);

            if (dayMaxEventRows.max) {
                this.updateOption(DAY_MAX_EVENT_ROWS, dayMaxEventRows.max);
            }
        }
    }

    _getDayMaxEvents(options) {
        const dayMaxEvents = options[DAY_MAX_EVENTS];

        if (dayMaxEvents) {
            if (dayMaxEvents.max) {
                return dayMaxEvents.max;
            }
            return dayMaxEvents.defaultEnabled;
        }
    }

    _updateDayMaxEvents(options) {
        const dayMaxEvents = options[DAY_MAX_EVENTS];

        if (dayMaxEvents) {
            this.updateOption(DAY_MAX_EVENTS, dayMaxEvents.defaultEnabled);

            if (dayMaxEvents.max) {
                this.updateOption(DAY_MAX_EVENTS, dayMaxEvents.max);
            }
        }
    }

    _getDayHeaderClassNames(options) {
        const dayHeaderClassNames = options[DAY_HEADER_CLASS_NAMES];

        return dayHeaderClassNames ? this._onDayHeaderClassNames.bind(this) : null;
    }

    _updateDayHeaderClassNames(options) {
        const dayHeaderClassNames = options[DAY_HEADER_CLASS_NAMES];

        if (utils.isNotNullUndefined(dayHeaderClassNames)) {
            this.updateOption(DAY_HEADER_CLASS_NAMES, dayHeaderClassNames ? this._onDayHeaderClassNames.bind(this) : null);
        }
    }

    _getDayCellClassNames(options) {
        const dyCellClassNames = options[DAY_CELL_CLASS_NAMES];

        return dyCellClassNames ? this._onDayCellClassNames.bind(this) : null;
    }

    _updateDayCellClassNames(options) {
        const dyCellClassNames = options[DAY_CELL_CLASS_NAMES];

        if (utils.isNotNullUndefined(dyCellClassNames)) {
            this.updateOption(DAY_CELL_CLASS_NAMES, dyCellClassNames ? this._onDayCellClassNames.bind(this) : null);
        }
    }

    _getSlotLabelClassNames(options) {
        const slotLabelClassNames = options[SLOT_LABEL_CLASS_NAMES];

        return slotLabelClassNames ? this._onSlotLabelClassNames.bind(this) : null;
    }

    _updateSlotLabelClassNames(options) {
        const slotLabelClassNames = options[SLOT_LABEL_CLASS_NAMES];

        if (utils.isNotNullUndefined(slotLabelClassNames)) {
            this.updateOption(SLOT_LABEL_CLASS_NAMES, slotLabelClassNames ? this._onSlotLabelClassNames.bind(this) : null);
        }
    }

    _getNowIndicatorClassNames(options) {
        const nowIndicatorClassNames = options[NOW_INDICATOR_CLASS_NAMES];

        return nowIndicatorClassNames ? this._onNowIndicatorClassNames.bind(this) : null;
    }

    _updateNowIndicatorClassNames(options) {
        const nowIndicatorClassNames = options[NOW_INDICATOR_CLASS_NAMES];

        if (utils.isNotNullUndefined(nowIndicatorClassNames)) {
            this.updateOption(NOW_INDICATOR_CLASS_NAMES, nowIndicatorClassNames ? this._onNowIndicatorClassNames.bind(this) : null);
        }
    }

    _getEventOrder(options) {
        const eventOrder = options[EVENT_ORDER];

        if (eventOrder) {
            if (eventOrder.jsFunction) {
                const jsFunction = utils.parseJavaScriptFunction(eventOrder.jsFunction);
                if (jsFunction) {
                    return jsFunction;
                }
            }
            return eventOrder.serializedEventOrder;
        }
    }

    _updateEventOrder(options) {
        const eventOrder = options[EVENT_ORDER];

        if (eventOrder) {
            if (eventOrder.jsFunction) {
                const jsFunction = utils.parseJavaScriptFunction(eventOrder.jsFunction);
                if (jsFunction) {
                    this.updateOption(EVENT_ORDER, jsFunction);
                    return;
                }
            }
            this.updateOption(EVENT_ORDER, eventOrder.serializedEventOrder);
        }
    }

    _getNavLinkDayClick(options) {
        const navLinkDayClick = options[NAV_LINK_DAY_CLICK];

        return navLinkDayClick ? this._onNavLinkDayClick.bind(this) : null;
    }

    _updateNavLinkDayClick(options) {
        const navLinkDayClick = options[NAV_LINK_DAY_CLICK];

        if (utils.isNotNullUndefined(navLinkDayClick)) {
            this.updateOption(NAV_LINK_DAY_CLICK, navLinkDayClick ? this._onNavLinkDayClick.bind(this) : null);
        }
    }

    _getNavLinkWeekClick(options) {
        const navLinkWeekClick = options[NAV_LINK_WEEK_CLICK];

        return navLinkWeekClick ? this._onNavLinkWeekClick.bind(this) : null;
    }

    _updateNavLinkWeekClick(options) {
        const navLinkWeekClick = options[NAV_LINK_WEEK_CLICK];

        if (utils.isNotNullUndefined(navLinkWeekClick)) {
            this.updateOption(NAV_LINK_WEEK_CLICK, navLinkWeekClick ? this._onNavLinkWeekClick.bind(this) : null);
        }
    }

    _updateDyCellBottomText(options) {
        const dayCellBottomText = options[DAY_CELL_BOTTOM_TEXT];

        if (!dayCellBottomText) {
            return;
        }

        this.customOptions[DAY_CELL_BOTTOM_TEXT] = {
            textGeneratorEnabled: dayCellBottomText.textGeneratorEnabled,
            classNamesGeneratorEnabled: dayCellBottomText.classNamesGeneratorEnabled
        }
    }

    _updateCurrentDate(options) {
        const currentDate = options[CURRENT_DATE];

        if (!currentDate) {
            return;
        }

        this.customOptions[CURRENT_DATE] = currentDate;
    }

    _updateCurrentSelection(options) {
        const currentSelection = options[CURRENT_SELECTION];

        if (!currentSelection) {
            return;
        }

        this.customOptions[CURRENT_SELECTION] = {
            allDay: currentSelection.allDay,
            startDateTime: currentSelection.startDateTime,
            endDateTime: currentSelection.endDateTime
        };
    }

    _updateCurrentView(options) {
        const currentView = options[CURRENT_VIEW];

        if (!currentView) {
            return;
        }

        this.customOptions[CURRENT_VIEW] = currentView;
    }

    _onMoreLinkClick(e) {
        if (this.listeners[MORE_LINK_CLICK]) {
            this.listeners[MORE_LINK_CLICK].forEach((listener) => listener(e));
        }
        return NO_VIEW_MORE_LINK_CLICK;
    }

    _onMoreLinkClassNames(e) {
        if (this.listeners[MORE_LINK_CLASS_NAMES]) {
            this.listeners[MORE_LINK_CLASS_NAMES].forEach((listener) => listener(e));
        }
        return [];
    }

    _onDayHeaderClassNames(e) {
        if (this.listeners[DAY_HEADER_CLASS_NAMES]) {
            this.listeners[DAY_HEADER_CLASS_NAMES].forEach((listener) => listener(e));
        }
        return [];
    }

    _onDayCellClassNames(e) {
        if (this.listeners[DAY_CELL_CLASS_NAMES]) {
            this.listeners[DAY_CELL_CLASS_NAMES].forEach((listener) => listener(e));
        }
        return [];
    }

    _onSlotLabelClassNames(e) {
        if (this.listeners[SLOT_LABEL_CLASS_NAMES]) {
            this.listeners[SLOT_LABEL_CLASS_NAMES].forEach((listener) => listener(e));
        }
        return [];
    }

    _onNowIndicatorClassNames(e) {
        if (this.listeners[NOW_INDICATOR_CLASS_NAMES]) {
            this.listeners[NOW_INDICATOR_CLASS_NAMES].forEach((listener) => listener(e));
        }
        return [];
    }

    _onNavLinkDayClick(e) {
        if (this.listeners[NAV_LINK_DAY_CLICK]) {
            this.listeners[NAV_LINK_DAY_CLICK].forEach((listener) => listener(e));
        }
    }

    _onNavLinkWeekClick(e) {
        if (this.listeners[NAV_LINK_WEEK_CLICK]) {
            this.listeners[NAV_LINK_WEEK_CLICK].forEach((listener) => listener(e));
        }
    }

    /**
     * @param calendarI18n object to assign to
     * @param jmixI18n object to assign from
     * @private
     */
    _combineI18n(calendarI18n, jmixI18n) {
        const resultI18n = {
            code: calendarI18n.code,
            buttonHints: {
                ...calendarI18n.buttonHints && calendarI18n.buttonHints.prev && {prev: calendarI18n.buttonHints.prev},
                ...calendarI18n.buttonHints && calendarI18n.buttonHints.next && {next: calendarI18n.buttonHints.next},
                ...calendarI18n.buttonHints && calendarI18n.buttonHints.today && {today: calendarI18n.buttonHints.today},
            },
            buttonText: {
                ...calendarI18n.buttonText && calendarI18n.buttonText.day && {day: calendarI18n.buttonText.day},
                ...calendarI18n.buttonText && calendarI18n.buttonText.list && {list: calendarI18n.buttonText.list},
                ...calendarI18n.buttonText && calendarI18n.buttonText.month && {month: calendarI18n.buttonText.month},
                ...calendarI18n.buttonText && calendarI18n.buttonText.next && {next: calendarI18n.buttonText.next},
                ...calendarI18n.buttonText && calendarI18n.buttonText.nextYear && {nextYear: calendarI18n.buttonText.nextYear},
                ...calendarI18n.buttonText && calendarI18n.buttonText.prev && {prev: calendarI18n.buttonText.prev},
                ...calendarI18n.buttonText && calendarI18n.buttonText.prevYear && {prevYear: calendarI18n.buttonText.prevYear},
                ...calendarI18n.buttonText && calendarI18n.buttonText.today && {today: calendarI18n.buttonText.today},
                ...calendarI18n.buttonText && calendarI18n.buttonText.week && {week: calendarI18n.buttonText.week},
                ...calendarI18n.buttonText && calendarI18n.buttonText.year && {year: calendarI18n.buttonText.year},
            },
            week: {}
        };

        resultI18n.direction = utils.isNotNullUndefined(jmixI18n.direction)
            ? jmixI18n.direction.toLowerCase()
            : calendarI18n.direction;

        resultI18n['week'].dow = utils.isNotNullUndefined(jmixI18n.firstDayOfWeek)
            ? jmixI18n.firstDayOfWeek
            : calendarI18n.week.dow;

        resultI18n['week'].doy = utils.isNotNullUndefined(jmixI18n.dayOfYear)
            ? jmixI18n.dayOfYear
            : calendarI18n.week.doy;

        resultI18n.weekText = utils.isNotNullUndefined(jmixI18n.weekText)
            ? jmixI18n.weekText
            : calendarI18n.weekText;

        resultI18n.weekTextLong = utils.isNotNullUndefined(jmixI18n.weekTextLong)
            ? jmixI18n.weekTextLong
            : calendarI18n.weekTextLong;

        resultI18n.allDayText = utils.isNotNullUndefined(jmixI18n.allDayText)
            ? jmixI18n.allDayText
            : calendarI18n.allDayText;

        if (utils.isNotNullUndefined(jmixI18n.moreLinkText)) {
            resultI18n.moreLinkText = utils.isJavaScriptFunction(jmixI18n.moreLinkText)
                ? utils.parseJavaScriptFunction(jmixI18n.moreLinkText)
                : new Function("count", "return `" + jmixI18n.moreLinkText + "`");
        } else {
            resultI18n.moreLinkText = calendarI18n.moreLinkText;
        }

        resultI18n.noEventsText = utils.isNotNullUndefined(jmixI18n.noEventsText)
            ? jmixI18n.noEventsText
            : calendarI18n.noEventsText;

        resultI18n.closeHint = utils.isNotNullUndefined(jmixI18n.closeHint)
            ? jmixI18n.closeHint
            : calendarI18n.closeHint;

        resultI18n.eventHint = utils.isNotNullUndefined(jmixI18n.eventHint)
            ? jmixI18n.eventHint
            : calendarI18n.eventHint;

        resultI18n.timeHint = utils.isNotNullUndefined(jmixI18n.timeHint)
            ? jmixI18n.timeHint
            : calendarI18n.timeHint;

        if (utils.isNotNullUndefined(jmixI18n.navLinkHint)) {
            resultI18n.navLinkHint = utils.isJavaScriptFunction(jmixI18n.navLinkHint)
                ? utils.parseJavaScriptFunction(jmixI18n.navLinkHint)
                : new Function("date", "return `" + jmixI18n.navLinkHint + "`");
        } else {
            resultI18n.navLinkHint = calendarI18n.navLinkHint;
        }

        if (utils.isNotNullUndefined(jmixI18n.moreLinkHint)) {
            resultI18n.moreLinkHint = utils.isJavaScriptFunction(jmixI18n.moreLinkHint)
                ? utils.parseJavaScriptFunction(jmixI18n.moreLinkHint)
                : new Function("count", "return `" + jmixI18n.moreLinkHint + "`")
        } else {
            resultI18n.moreLinkHint = calendarI18n.moreLinkHint;
        }

        return resultI18n;
    }

    _generateLocaleSuffix() {
        const charactersLength = 6;
        const result = 'u-';
        if (window.crypto.randomUUID) {
            return result + window.crypto.randomUUID().split('-').join('').substring(0, charactersLength);
        }
        let suffix = '';
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        for (let i = 0; i < charactersLength; i++) {
            const randomIndex = Math.floor(Math.random() * characters.length);
            suffix += characters.charAt(randomIndex);
        }
        return result + suffix;
    }

    /**
     * If <code>momentPlugin</code> is used, FullCalendar starts to get all date units localization from
     * <code>moment.js</code>. This function register localized date units that are defined in message bundle.
     * @param config JSON with localized days, months, etc.
     * @see https://momentjs.com/docs/#/customization/
     * @private
     */
    _updateMomentJsLocale(config, create) {
        if (create) {
            // Should create new locale with the original parent locale
            moment.defineLocale(config.localeName, config);
        } else {
            moment.updateLocale(config.localeName, config);
        }
    }
}

export default Options;