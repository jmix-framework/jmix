import localesAll from '@fullcalendar/core/locales-all.js';

import * as utils from "./jmix-full-calendar-utils";
import {RAW_EN_LOCALE} from "./jmix-full-calendar-utils";

const NO_VIEW_MORE_LINK_CLICK = 'NO_VIEW';

export const MORE_LINK_CLICK = 'moreLinkClick';
export const MORE_LINK_CLASS_NAMES = 'moreLinkClassNames';
export const UNSELECT_CANCEL = 'unselectCancel';
export const DAY_HEADER_CLASS_NAMES = 'dayHeaderClassNames';
export const DAY_CELL_CLASS_NAMES = 'dayCellClassNames';
export const SLOT_LABEL_CLASS_NAMES = 'slotLabelClassNames';
export const NOW_INDICATOR_CLASS_NAMES = 'nowIndicatorClassNames';
const EVENT_OVERLAP = 'eventOverlap';
const SELECT_OVERLAP = 'selectOverlap';
const EVENT_CONSTRAINT = 'eventConstraint';
const BUSINESS_HOURS = 'businessHours';
const SELECT_CONSTRAINT = 'selectConstraint';
const SELECT_ALLOW = 'selectAllow';
const VIEWS = 'views';
const DAY_MAX_EVENT_ROWS = 'dayMaxEventRows';
const DAY_MAX_EVENTS = 'dayMaxEvents';
const FIRST_DAY = 'firstDay';
const EVENT_ORDER = 'eventOrder';

export function processInitialOptions(serverOptions) {
    const options = serverOptions;
    if (!serverOptions) {
        return {};
    }

    const views = serverOptions[VIEWS];
    if (views) {
        options[VIEWS] = processViews(views);
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

    return options;
}

function processViews(viewsObject) {
    for (let view in viewsObject) {
        if (view === 'customCalendarViews') {
            continue;
        }
        if (view === 'listDay'
            || view === 'listWeek'
            || view === 'listMonth'
            || view === 'listYear') {
            viewsObject[view] = processListView(viewsObject[view]);
            continue;
        }
        viewsObject[view] = {...viewsObject[view], ...viewsObject[view].properties && {...viewsObject[view].properties}};
        viewsObject[view] = utils.deleteNullProperties(viewsObject[view]);
        delete viewsObject[view].properties;
    }

    if (viewsObject.customCalendarViews) {
        for (const view of viewsObject.customCalendarViews) {
            viewsObject[view.calendarView] = {
                type: view.type,
                ...(view.dayCount) && {dayCount: view.dayCount},
                ...(view.duration) && {duration: view.duration},
                ...view.properties && {...view.properties}
            };
        }
    }
    delete viewsObject.customCalendarViews;

    return viewsObject;
}

function processListView(listView) {
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

class Options {

    constructor(calendar, context) {
        this.calendar = calendar;
        this.context = context;
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
        this.calendar.batchRendering(() => {
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

            this._updateFirstDay(options);
            this._updateEventOrder(options);
        });
    }

    updateOption(key, value) {
        let oldValue = this.calendar.getOption(key);
        if (oldValue !== value) {
            this.calendar.setOption(key, value);
        }
    }

    updateLocale(jmixI18n) {
        const calendarI18nArray = localesAll.filter((item) => item.code === jmixI18n.locale);
        const calendarI18n = calendarI18nArray.length > 0 ? calendarI18nArray[0] : RAW_EN_LOCALE;

        this._assignI18n(calendarI18n, jmixI18n);

        this.updateOption("locale", calendarI18n);

        const formatOptions = utils.convertToLocaleDependedOptions(jmixI18n);

        this.updateOptions(formatOptions);
    }

    _skipOption(key) {
        return MORE_LINK_CLICK === key
            || MORE_LINK_CLASS_NAMES === key
            || DAY_HEADER_CLASS_NAMES === key
            || DAY_CELL_CLASS_NAMES === key
            || SLOT_LABEL_CLASS_NAMES === key
            || NOW_INDICATOR_CLASS_NAMES === key
            || EVENT_OVERLAP === key
            || EVENT_CONSTRAINT === key
            || BUSINESS_HOURS === key
            || SELECT_OVERLAP === key
            || SELECT_CONSTRAINT === key
            || SELECT_ALLOW === key
            || DAY_MAX_EVENT_ROWS === key
            || DAY_MAX_EVENTS === key
            || FIRST_DAY === key
            || EVENT_ORDER === key
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

    _updateMoreLinkClassNames(options) {
        const moreLinkClassNames = options[MORE_LINK_CLASS_NAMES];

        if (moreLinkClassNames) {
            this.updateOption(MORE_LINK_CLASS_NAMES, moreLinkClassNames.classNames);

            if (moreLinkClassNames.functionEnabled) {
                this.updateOption(MORE_LINK_CLASS_NAMES, this._onMoreLinkClassNames.bind(this));
            }
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

    _updateDayMaxEventRows(options) {
        const dayMaxEventRows = options[DAY_MAX_EVENT_ROWS];

        if (dayMaxEventRows) {
            this.updateOption(DAY_MAX_EVENT_ROWS, dayMaxEventRows.defaultEnabled);

            if (dayMaxEventRows.max) {
                this.updateOption(DAY_MAX_EVENT_ROWS, dayMaxEventRows.max);
            }
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

    _updateDayHeaderClassNames(options) {
        const dayHeaderClassNames = options[DAY_HEADER_CLASS_NAMES];

        if (utils.isNotNullUndefined(dayHeaderClassNames)) {
            this.updateOption(DAY_HEADER_CLASS_NAMES, dayHeaderClassNames ? this._onDayHeaderClassNames.bind(this) : null);
        }
    }

    _updateDayCellClassNames(options) {
        const dyCellClassNames = options[DAY_CELL_CLASS_NAMES];

        if (utils.isNotNullUndefined(dyCellClassNames)) {
            this.updateOption(DAY_CELL_CLASS_NAMES, dyCellClassNames ? this._onDayCellClassNames.bind(this) : null);
        }
    }

    _updateSlotLabelClassNames(options) {
        const slotLabelClassNames = options[SLOT_LABEL_CLASS_NAMES];

        if (utils.isNotNullUndefined(slotLabelClassNames)) {
            this.updateOption(SLOT_LABEL_CLASS_NAMES, slotLabelClassNames ? this._onSlotLabelClassNames.bind(this) : null);
        }
    }

    _updateNowIndicatorClassNames(options) {
        const nowIndicatorClassNames = options[NOW_INDICATOR_CLASS_NAMES];

        if (utils.isNotNullUndefined(nowIndicatorClassNames)) {
            this.updateOption(NOW_INDICATOR_CLASS_NAMES, nowIndicatorClassNames ? this._onNowIndicatorClassNames.bind(this) : null);
        }
    }

    _updateFirstDay(options) {
        if (!options.hasOwnProperty(FIRST_DAY)) {
            return;
        }

        let value = options[FIRST_DAY];

        if (!this.context.initialized && value == null) {
            return;
        }

        if (value == null) {
            value = this.context.i18n.dayOfWeek;
        }

        this.updateOption(FIRST_DAY, value);
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

    _assignI18n(calendarI18n, jmixI18n) {
        if (utils.isNotNullUndefined(jmixI18n.direction)) {
            calendarI18n['direction'] = jmixI18n.direction.toLowerCase();
        }
        if (utils.isNotNullUndefined(jmixI18n.dayOfWeek)) {
            calendarI18n['week'].dow = jmixI18n.dayOfWeek;
        }
        if (utils.isNotNullUndefined(jmixI18n.dayOfYear)) {
            calendarI18n['week'].doy = jmixI18n.dayOfYear;
        }
        if (utils.isNotNullUndefined(jmixI18n.weekText)) {
            calendarI18n['weekText'] = jmixI18n.weekText;
        }
        if (utils.isNotNullUndefined(jmixI18n.weekTextLong)) {
            calendarI18n['weekTextLong'] = jmixI18n.weekTextLong;
        }
        if (utils.isNotNullUndefined(jmixI18n.allDayText)) {
            calendarI18n['allDayText'] = jmixI18n.allDayText;
        }
        if (utils.isNotNullUndefined(jmixI18n.moreLinkText)) {
            calendarI18n['moreLinkText'] = utils.isJavaScriptFunction(jmixI18n.moreLinkText)
                ? utils.parseJavaScriptFunction(jmixI18n.moreLinkText)
                : new Function("count", "return `" + jmixI18n.moreLinkText + "`");
        }
        if (utils.isNotNullUndefined(jmixI18n.noEventsText)) {
            calendarI18n['noEventsText'] = jmixI18n.noEventsText;
        }
        if (utils.isNotNullUndefined(jmixI18n.closeHint)) {
            calendarI18n['closeHint'] = jmixI18n.closeHint;
        }
        if (utils.isNotNullUndefined(jmixI18n.eventHint)) {
            calendarI18n['eventHint'] = jmixI18n.eventHint;
        }
        if (utils.isNotNullUndefined(jmixI18n.timeHint)) {
            calendarI18n['timeHint'] = jmixI18n.timeHint;
        }
        if (utils.isNotNullUndefined(jmixI18n.navLinkHint)) {
            calendarI18n['navLinkHint'] = utils.isJavaScriptFunction(jmixI18n.navLinkHint)
                ? utils.parseJavaScriptFunction(jmixI18n.navLinkHint)
                : new Function("date", "return `" + jmixI18n.navLinkHint + "`");
        }
        if (utils.isNotNullUndefined(jmixI18n.moreLinkHint)) {
            calendarI18n['moreLinkHint'] = utils.isJavaScriptFunction(jmixI18n.moreLinkHint)
                ? utils.parseJavaScriptFunction(jmixI18n.moreLinkHint)
                : new Function("count", "return `" + jmixI18n.moreLinkHint + "`")
        }
    }
}

export default Options;