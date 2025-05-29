import {html, PolymerElement} from '@polymer/polymer/polymer-element';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin';

import {Calendar} from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import multiMonthPlugin from '@fullcalendar/multimonth';
import interactionPlugin from '@fullcalendar/interaction';
import momentPlugin from '@fullcalendar/moment';
import {toMoment} from '@fullcalendar/moment';
import momentTimezonePlugin from '@fullcalendar/moment-timezone';

import moment from 'moment';

import * as utils from './jmix-full-calendar-utils.js';
import DataHolder from './DataHolder.js';
import Options, {
    MORE_LINK_CLASS_NAMES,
    MORE_LINK_CLICK,
    DAY_HEADER_CLASS_NAMES,
    DAY_CELL_CLASS_NAMES,
    SLOT_LABEL_CLASS_NAMES, NOW_INDICATOR_CLASS_NAMES, NAV_LINK_DAY_CLICK, NAV_LINK_WEEK_CLICK, DAY_CELL_BOTTOM_TEXT
} from './Options.js';

const FC_NON_BUSINESS_CLASS_NAME = 'fc-non-business';
const FC_DAYGRID_DAY = 'fc-daygrid-day';
const FC_DAY = 'fc-day';
const FC_LINK_CLASS_NAME = 'fc-more-link';
const FC_COL_HEADER_CELL = 'fc-col-header-cell';
const FC_TIMEGRID_SLOT_LABEL = 'fc-timegrid-slot-label';
const FC_TIMEGRID_NOW_INDICATOR_ARROW = 'fc-timegrid-now-indicator-arrow';
const FC_TIMEGRID_NOW_INDICATOR_LINE = 'fc-timegrid-now-indicator-line';
const FC_POPOVER = 'fc-popover';

const JMIX_DAY_CELL_BOTTOM_TEXT = 'jmix-day-cell-bottom-text';
const JMIX_HAS_BOTTOM_TEXT = 'jmix-has-bottom-text';

const DAY_GRID_DAY = "dayGridDay";
const DAY_GRID_WEEK = "dayGridWeek";
const DAY_GRID_MONTH = "dayGridMonth";
const DAY_GRID_YEAR = "dayGridYear";
const MULTI_MONTH_YEAR = "multiMonthYear";

class JmixFullCalendar extends ElementMixin(ThemableMixin(PolymerElement)) {
    static get template() {
        return html`
            <slot name="calendarSlot"/>
        `;
    }

    static get is() {
        return 'jmix-full-calendar';
    }

    static get properties() {
        return {
            initialOptions: {
                type: Object,
                value: null,
            },
            i18n: {
                type: Object,
                value: null,
                observer: '_onI18nChange',
            },
            /**
             * @Override
             */
            dir: {
                observer: '_onDirChane',
            },
        }
    }

    ready() {
        super.ready();

        // The Light DOM element is used for generating FullCalendar because
        // ShadowDOM hides all generated elements and the styling inner parts
        // becomes complicated.
        this.calendarElement = document.createElement("div");
        this.calendarElement.id = 'fullcalendar';
        this.calendarElement.slot = 'calendarSlot';
        this.appendChild(this.calendarElement);

        this.dataHolder = new DataHolder();

        this.jmixOptions = new Options();
        this.jmixOptions.addListener(MORE_LINK_CLICK, this._onMoreLinkClick.bind(this));
        this.jmixOptions.addListener(MORE_LINK_CLASS_NAMES, this._onMoreLinkClassNames.bind(this));
        this.jmixOptions.addListener(DAY_HEADER_CLASS_NAMES, this._onDayHeaderClassNames.bind(this));
        this.jmixOptions.addListener(DAY_CELL_CLASS_NAMES, this._onDayCellClassNames.bind(this));
        this.jmixOptions.addListener(SLOT_LABEL_CLASS_NAMES, this._onSlotLabelClassNames.bind(this));
        this.jmixOptions.addListener(NOW_INDICATOR_CLASS_NAMES, this._onNowIndicatorClassNames.bind(this));
        this.jmixOptions.addListener(NAV_LINK_DAY_CLICK, this._onNavLinkDayClick.bind(this));
        this.jmixOptions.addListener(NAV_LINK_WEEK_CLICK, this._onNavLinkWeekClick.bind(this));

        this.calendar = new Calendar(this.calendarElement, this.getInitialOptions());

        this.jmixOptions.calendar = this.calendar;

        // First call of `_onI18nChange` was ignored since jmixOptions was undefined.
        // So call it again to update locale.
        this._onI18nChange(this.i18n);

        this.render();

        this._setupResizeListener();
    }

    _setupResizeListener() {
        let timeoutId;
        function onResize() {
            if (timeoutId) {
                return;
            }
            timeoutId = setTimeout(() => {
                this.calendar.updateSize();
                timeoutId = null;
            }, 250);
        }

        new ResizeObserver(onResize.bind(this)).observe(this.calendarElement);
    }

    /**
     * Server callable function
     * <p>
     * It is invoked from <code>Component#onAttach()</code> to indicate that full initialization is complete
     * @private
     */
    _onCompleteInit() {
        this.initialized = true;
    }

    getInitialOptions() {
        const initialOptions = this.jmixOptions.processInitialOptions(this.initialOptions);
        return {
            headerToolbar: false,
            height: "100%",
            plugins: [dayGridPlugin, timeGridPlugin, listPlugin, multiMonthPlugin, interactionPlugin,
                momentTimezonePlugin, momentPlugin],
            eventClick: (e) => this._onEventClick(e),
            eventMouseEnter: (e) => this._onEventMouseEnter(e),
            eventMouseLeave: (e) => this._onEventMouseLeave(e),
            eventDrop: (e) => this._onEventDrop(e),
            eventResize: (e) => this._onEventResize(e),
            datesSet: (e) => this._onDatesSet(e),
            dateClick: (e) => this._onDateClick(e),
            select: (e) => this._onSelect(e),
            unselect: (e) => this._onUnselect(e),
            dayCellDidMount: (e) => this._onDayCellDidMount(e),
            eventDidMount: (e) => this._onEventDidMount(e),
            // If eventStartEditable is enabled, add fixedMirrorParent with the outer element. This helps
            // avoid distortion of the event draggable preview if the component is placed in the Split.
            ...(initialOptions && initialOptions.eventStartEditable) && {fixedMirrorParent: document.body},
            ...initialOptions,
        };
    }

    /**
     * Server callable function.
     * @param options the object that contains options as properties
     * @private
     */
    updateOptions(options) {
        this.jmixOptions.updateOptions(options);
    }

    /**
     * Server callable function.
     * @param name option name
     * @param value value
     */
    updateOption(name, value) {
        this.jmixOptions.updateOption(name, value);
    }

    /**
     * Server callable function.
     * <p>
     * Updates simple events source, calling <code>#refetch()</code> function.
     * @param context the context contains sourceId and data items
     * @private
     */
    _updateSyncSourcesData(context) {
        this.dataHolder.set(context.sourceId, context.data);

        this.calendar.getEventSourceById(context.sourceId).refetch();
    }

    /**
     * Server callable function.
     * <p>
     * Invokes <code>#refetch()</code> function of event source to reload data.
     * @param context an object contains sourceId
     */
    _updateAsyncSourcesData(context) {
        this.calendar.getEventSourceById(context.sourceId).refetch();
    }

    /**
     * Server callable function.
     * <p>
     * Is used for incremental data updates.
     * @param sourcesData the sources data contains a list of data "records". Each record has operation name and
     * items for performing operation
     * @private
     */
    _updateSourcesWithIncrementalData(sourcesData) {
        for (const sourceData of sourcesData) {
            if (!sourceData.items) {
                continue;
            }
            const items = this.dataHolder.get(sourceData.sourceId);
            switch (sourceData.operation) {
                case 'add':
                    items.push(...sourceData.items);
                    this.calendar.getEventSourceById(sourceData.sourceId).refetch();
                    break;
                case 'update':
                    for (const updItem of sourceData.items) {
                        const idx = items.findIndex(item => item.id === updItem.id);
                        if (idx >= 0) {
                            items[idx] = updItem;
                        }
                    }
                    this.calendar.getEventSourceById(sourceData.sourceId).refetch();
                    break;
                case 'remove':
                    for (const rmItem of sourceData.items) {
                        const idx = items.findIndex(item => item.id === rmItem.id);
                        if (idx >= 0) {
                            items.splice(idx, 1);
                        }
                    }
                    this.calendar.getEventSourceById(sourceData.sourceId).refetch();
                    break;
            }
        }
    }

    /**
     * Server callable function.
     * @param sourceId
     * @private
     */
    _addItemEventSource(sourceId) {
        this._addEventSource(sourceId, false);
    }

    /**
     * Server callable function.
     * @param sourceId
     * @private
     */
    _addLazyEventSource(sourceId) {
        this._addEventSource(sourceId, true);
    }

    _addEventSource(sourceId, lazySource) {
        if (this.calendar.getEventSourceById(sourceId)) {
            return;
        }

        this.dataHolder.set(sourceId, lazySource ? {compContext: this} : []);

        this.calendar.addEventSource(this._createEventSource(sourceId, lazySource));
    }

    /**
     * Server callable function.
     * @param sourceId
     * @private
     */
    _removeEventSource(sourceId) {
        const eventSource = this.calendar.getEventSourceById(sourceId);
        if (eventSource) {
            this.dataHolder.delete(sourceId);
            eventSource.remove();
        }
    }

    _createEventSource(sourceId, lazySource) {
        return {
            events: (a, b, c) => lazySource
                ? this._lazyFetchFunction(a, b, c, this.dataHolder, sourceId)
                : this._fetchFunction(a, b, c, this.dataHolder.get(sourceId)),
            id: sourceId
        }
    }

    _fetchFunction(fetchInfo, successCallback, failureCallback, data) {
        if (!data) {
            return;
        }
        successCallback(data);
    }

    async _lazyFetchFunction(fetchInfo, successCallback, failureCallback, dataHolder, sourceId) {
        const context = dataHolder.get(sourceId);
        const fetchCalendarItems = async () => {
            const startDate = this.formatDate(fetchInfo.start, true);
            const endDate = this.formatDate(fetchInfo.end, true);
            return await context.compContext.$server.fetchCalendarItems(sourceId, startDate, endDate);
        }
        const data = await fetchCalendarItems();
        dataHolder.set(sourceId, {compContext: context.compContext, data: data, lastFetchInfo: fetchInfo});
        successCallback(data);
    }

    _onDayHeaderClassNames(e) {
        let date = null;
        const viewType = e.view.type;
        // If current view is not DAY_GRID_MONTH or DAY_GRID_YEAR we
        // should provide a date value. Otherwise, null value will be sent,
        // because dates for headers in month and year are equal to 1970 year.
        if (viewType !== DAY_GRID_MONTH
            && viewType !== DAY_GRID_YEAR) {
            date = this.formatDate(e.date, true) // omit time
        }
        const context = {
            date: date,
            dow: e.dow,
            isDisabled: e.isDisabled,
            isFuture: e.isFuture,
            isOther: e.isOther,
            isPast: e.isPast,
            isToday: e.isToday,
            view: utils.createViewInfo(e.view, this.formatDate.bind(this))
        }

        const classNamesPromise = this.$server.getDayHeaderClassNames(context);
        classNamesPromise.then((classNames) => {
            // Find generated element and assign classNames
            for (const element of this.getElementsByClassName(FC_COL_HEADER_CELL)) {
                const target = utils.findElementRecursivelyByInnerText(element, e.text);
                if (target) {
                    element.classList.remove(...classNames);
                    element.classList.add(...classNames);
                }
            }
        });
    }

    _onDayCellClassNames(e) {
        const dateStr = this.formatDate(e.date, true); // omit time
        const context = {
            date: dateStr,
            dow: e.dow,
            isDisabled: e.isDisabled,
            isFuture: e.isFuture,
            isOther: e.isOther,
            isPast: e.isPast,
            isToday: e.isToday,
            view: utils.createViewInfo(e.view, this.formatDate.bind(this))
        }

        const classNamesPromise = this.$server.getDayCellClassNames(context);
        classNamesPromise.then((classNames) => {
            if (!classNames || classNames.length === 0) {
                return
            }
            // Find generated element and assign classNames
            for (const element of this.querySelectorAll("[data-date='" + dateStr + "']")) {
                if (element.nodeName === 'TD') {
                    element.classList.remove(...classNames);
                    element.classList.add(...classNames);
                }
            }
        });
    }

    _onSlotLabelClassNames(e) {
        const timeStr = moment(e.date).format('HH:mm:ss');
        const context = {
            time: timeStr,
            view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
        }

        const classNamesPromise = this.$server.getSlotLabelClassNames(context);
        classNamesPromise.then((classNames) => {
            if (!classNames || classNames.length === 0) {
                return;
            }
            // Find generated element and assign classNames
            for (const element of this.getElementsByClassName(FC_TIMEGRID_SLOT_LABEL)) {
                if (element.dataset.time === timeStr) {
                    element.classList.remove(...classNames);
                    element.classList.add(...classNames);
                    return;
                }
            }
        });
    }

    _onNowIndicatorClassNames(e) {
        const context = {
            isAxis: e.isAxis,
            dateTime: this.formatDate(e.date),
            view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
        }

        const classNamesPromise = this.$server.getNowIndicatorClassNames(context);
        classNamesPromise.then((classNames) => {
            if (!classNames || classNames.length === 0) {
                return;
            }
            const className = e.isAxis ? FC_TIMEGRID_NOW_INDICATOR_ARROW : FC_TIMEGRID_NOW_INDICATOR_LINE
            for (const element of this.getElementsByClassName(className)) {
                element.classList.remove(...classNames);
                element.classList.add(...classNames);
                return;
            }
        });
    }

    _onMoreLinkClassNames(e) {
        const context = {
            eventsCount: e.num,
            view: utils.createViewInfo(e.view, this.calendar.formatIso.bind(this.calendar))
        }
        const classNamesPromise = this.$server.getMoreLinkClassNames(context);
        classNamesPromise.then((classNames) => {
            if (!classNames || classNames.length === 0) {
                return
            }
            // Find generated element and assign classNames
            for (const linkElement of this.getElementsByClassName(FC_LINK_CLASS_NAME)) {
                if (linkElement.innerText === e.text) {
                    linkElement.classList.remove(...classNames);
                    linkElement.classList.add(...classNames);
                }
            }
        });
    }

    _onMoreLinkClick(e) {
        // Workaround with date for https://github.com/fullcalendar/fullcalendar/issues/7314
        let dateStr = e.date.toISOString();
        dateStr = dateStr.substring(0, dateStr.length - 1);

        this.dispatchEvent(new CustomEvent("jmix-more-link-click", {
            detail: {
                context: {
                    allDay: e.allDay,
                    dateTime: dateStr, // WA, do not format string, as it will be correctly parsed in the server part
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                    mouseDetails: utils.createMouseDetails(e.jsEvent),
                    allEvents: utils.segmentsToServerData(e.allSegs, this.formatDate.bind(this)),
                    hiddenEvents: utils.segmentsToServerData(e.hiddenSegs, this.formatDate.bind(this)),
                }
            }
        }))
    }

    _onEventClick(e) {
        this.dispatchEvent(new CustomEvent("jmix-event-click", {
            detail: {
                context: {
                    event: utils.eventToServerData(e.event),
                    mouseDetails: utils.createMouseDetails(e.jsEvent),
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                }
            }
        }))
    }

    _onEventMouseEnter(e) {
        this.dispatchEvent(new CustomEvent("jmix-event-mouse-enter", {
            detail: {
                context: {
                    event: utils.eventToServerData(e.event),
                    mouseDetails: utils.createMouseDetails(e.jsEvent),
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                }
            }
        }))
    }

    _onEventMouseLeave(e) {
        this.dispatchEvent(new CustomEvent("jmix-event-mouse-leave", {
            detail: {
                context: {
                    event: utils.eventToServerData(e.event),
                    mouseDetails: utils.createMouseDetails(e.jsEvent),
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                }
            }
        }))
    }

    _onEventDrop(e) {
        this.dispatchEvent(new CustomEvent('jmix-event-drop', {
            detail: {
                context: {
                    delta: e.delta,
                    event: utils.eventToServerData(e.event),
                    oldEvent: utils.eventToServerData(e.oldEvent),
                    mouseDetails: utils.createMouseDetails(e.jsEvent),
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                    relatedEvents: utils.eventsToServerData(e.relatedEvents),
                }
            }
        }));
    }

    _onEventResize(e) {
        this.dispatchEvent(new CustomEvent('jmix-event-resize', {
            detail: {
                context: {
                    ...(utils.isNotZeroDuration(e.startDelta)) && {startDelta: e.startDelta},
                    ...(utils.isNotZeroDuration(e.endDelta)) && {endDelta: e.endDelta},
                    event: utils.eventToServerData(e.event),
                    oldEvent: utils.eventToServerData(e.oldEvent),
                    mouseDetails: utils.createMouseDetails(e.jsEvent),
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                    relatedEvents: utils.eventsToServerData(e.relatedEvents),
                }
            }
        }));
    }

    _onDatesSet(e) {
        this.dispatchEvent(new CustomEvent('jmix-dates-set', {
            detail: {
                context: {
                    currentDate: this.formatDate(this.calendar.getDate(), true),
                    startDate: this.formatDate(e.start, true), // omit time as it always 00:00
                    endDate: this.formatDate(e.end, true),     // omit time as it always 00:00
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                    viewType: e.view.type,
                }
            }
        }));

        // The `_generateDayCellBottomText()` function sometime does not set 'JMIX_HAS_BOTTOM_TEXT' class (why?).
        // We should reassign class name if day cells do not have it.
        this._reassignHasBottomText();
    }

    _onDateClick(e) {
        this.dispatchEvent(new CustomEvent('jmix-date-click', {
            detail: {
                context: {
                    dateTime: this.formatDate(e.date),
                    allDay: e.allDay,
                    mouseDetails: utils.createMouseDetails(e.jsEvent),
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                }
            }
        }));
    }

    _onSelect(e) {
        this.dispatchEvent(new CustomEvent('jmix-select', {
            detail: {
                context: {
                    startDateTime: this.formatDate(e.start),
                    endDateTime: this.formatDate(e.end),
                    allDay: e.allDay,
                    view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
                    ...(e.jsEvent) && {mouseDetails: utils.createMouseDetails(e.jsEvent)},
                }
            }
        }));
    }

    _onUnselect(e) {
        const context = {
            view: utils.createViewInfo(e.view, this.formatDate.bind(this)),
            ...(e.jsEvent) && {mouseDetails: utils.createMouseDetails(e.jsEvent)},
        }

        this.dispatchEvent(new CustomEvent('jmix-unselect', {detail: {context: context}}));
    }

    _onDayCellDidMount(e) {
        this._generateDayCellBottomText(e);
        this._initContextMenuListener(e);
    }

    _generateDayCellBottomText(e) {
        const viewType = this.calendar.view.type;
        if (viewType !== DAY_GRID_MONTH
            && viewType !== DAY_GRID_YEAR) {
            return;
        }
        const dayCellBottomTextOption = this.jmixOptions.getOption(DAY_CELL_BOTTOM_TEXT);
        if (!dayCellBottomTextOption) {
            return;
        }
        if (e.el.classList.contains(FC_POPOVER)) {
            return;
        }
        if (dayCellBottomTextOption.textGeneratorEnabled) {
            const context = {
                date: this.formatDate(e.date, true),
                dow: e.dow,
                isDisabled: e.isDisabled,
                isFuture: e.isFuture,
                isOther: e.isOther,
                isPast: e.isPast,
                isToday: e.isToday,
                view: utils.createViewInfo(e.view, this.formatDate.bind(this))
            };

            this.$server.getDayCellBottomTextInfo(context)
                .then((textInfo) => {
                    if (!textInfo || !textInfo.text) {
                        return;
                    }
                    e.el.appendChild(this._createDayCellBottomTextDiv(textInfo));
                    e.el.classList.add(JMIX_HAS_BOTTOM_TEXT);
                });
        }
    }

    _reassignHasBottomText() {
        const viewType = this.calendar.view.type;
        if (viewType !== DAY_GRID_MONTH
            && viewType !== DAY_GRID_YEAR) {
            return;
        }
        const dayCellBottomTextOption = this.jmixOptions.getOption(DAY_CELL_BOTTOM_TEXT);
        if (!dayCellBottomTextOption || !dayCellBottomTextOption.textGeneratorEnabled) {
            return;
        }
        const days = this.calendarElement.getElementsByClassName(FC_DAYGRID_DAY);
        for (const dayElement of days) {
            if (dayElement.nodeName !== 'TD') {
                return;
            }
            if (dayElement.getElementsByClassName(JMIX_DAY_CELL_BOTTOM_TEXT).length > 0) {
                if (!dayElement.classList.contains(JMIX_HAS_BOTTOM_TEXT)) {
                    dayElement.classList.add(JMIX_HAS_BOTTOM_TEXT)
                }
            }
        }
    }

    _createDayCellBottomTextDiv(textInfo) {
        const div = document.createElement('DIV');
        div.className = JMIX_DAY_CELL_BOTTOM_TEXT;
        div.innerText = textInfo.text;
        if (textInfo.classNames) {
            div.classList.add(textInfo.classNames);
        }
        return div;
    }

    _initContextMenuListener(e) {
        const viewType = this.calendar.view.type;
        if (viewType !== DAY_GRID_DAY
            && viewType !== DAY_GRID_WEEK
            && viewType !== DAY_GRID_MONTH
            && viewType !== DAY_GRID_YEAR
            && viewType !== MULTI_MONTH_YEAR) {
            return;
        }
        e.el.addEventListener("contextmenu", (jsEvent) => {
            // If business-hours enabled there is a possibility to generate
            // event with wrong data due to FullCalendar layout.
            // See 'div' with 'fc-non-business' class name.
            if (jsEvent.target.className.includes(FC_NON_BUSINESS_CLASS_NAME)) {
                // Try to find correct element by point.
                const elements = document.elementsFromPoint(jsEvent.pageX, jsEvent.pageY);
                let dayElement;
                for (const element of elements) {
                    if (element.nodeName === 'TD'
                        && element.classList.contains(FC_DAYGRID_DAY)
                        && element.classList.contains(FC_DAY)) {
                        dayElement = element;
                        break;
                    }
                }
                if (!dayElement) {
                    // Do not generate event at all
                    return;
                }
                if (!this.contextMenuDetails) {
                    this.contextMenuDetails = {};
                }
                // Just add new properties, since there can be properties from an event
                this.contextMenuDetails['dayCell'] = utils.createCalendarCellDetailsFromElement(dayElement);
                this.contextMenuDetails['mouseDetails'] = utils.createMouseDetails(jsEvent);
            } else {
                if (!this.contextMenuDetails) {
                    this.contextMenuDetails = {};
                }
                // Just add new properties, since there can be properties from an event
                this.contextMenuDetails['dayCell'] = utils.createCalendarCellDetails(e, this.calendar);
                this.contextMenuDetails['mouseDetails'] = utils.createMouseDetails(jsEvent);
            }
        });
    }

    _onEventDidMount(e) {
        if (e.event.extendedProps.description) {
            e.el.title = e.event.extendedProps.description;
        }
        e.el.addEventListener("contextmenu", (jsEvent) => {
            if (!this.contextMenuDetails) {
                this.contextMenuDetails = {};
            }
            this.contextMenuDetails['mouseDetails'] = utils.createMouseDetails(jsEvent);
            this.contextMenuDetails['event'] = utils.eventToServerData(e.event);
        });
    }

    _onNavLinkDayClick(date) {
        this.dispatchEvent(new CustomEvent('jmix-day-link-click', {
            detail: {
                context: {
                    date: this.formatDate(date, true) // omit time
                }
            }
        }));
    }

    _onNavLinkWeekClick(date) {
        this.dispatchEvent(new CustomEvent('jmix-week-link-click', {
            detail: {
                context: {
                    date: this.formatDate(date, true) // omit time
                }
            }
        }));
    }

    _onI18nChange(i18n) {
        if (!this.jmixOptions) {
            return;
        }

        this.jmixOptions.updateLocale(i18n);
    }

    /**
     * Observers <code>dir</code> property changes. The <code>DirMixin</code> that is available from
     * <code>ElementMixin</code> listen <code>document.dir</code> property changes and triggers this function.
     * @param dir direction
     * @private
     */
    _onDirChane(dir) {
        this.jmixOptions.updateOption('direction', dir);
    }

    /**
     * Is required by Vaadin contextMenuTargetConnector.js. It returns
     * details for 'vaadin-context-menu-before-open' event that will be
     * sent to FullCalendarContextMenu#onBeforeOpenMenu().
     * @param e
     * @returns
     */
    getContextMenuBeforeOpenDetail(e) {
        const details = this.contextMenuDetails;
        this.contextMenuDetails = null;

        return details;
    };

    /**
     * Is required by Vaadin contextMenuTargetConnector.js. It returns
     * whether the calendar's context menu should be shown or not.
     * @param e
     * @returns {boolean}
     */
    preventContextMenu(e) {
        return !this.contextMenuDetails;
    };

    /**
     * Server callable function.
     */
    navigateToNext() {
        this.calendar.next();
    }

    /**
     * Server callable function.
     */
    incrementDate(duration) {
        this.calendar.incrementDate(duration);
    }

    /**
     * Server callable function.
     */
    navigateToPrevious() {
        this.calendar.prev();
    }

    /**
     * Server callable function.
     */
    navigateToToday() {
        this.calendar.today();
    }

    /**
     * Server callable function.
     */
    navigateToDate(date) {
        this.calendar.gotoDate(date);
    }

    /**
     * Server callable function.
     */
    scrollToTime(time) {
        this.calendar.scrollToTime(time);
    }

    /**
     * Server callable function.
     */
    scrollToTimeMs(timeInMs) {
        this.calendar.scrollToTime({milliseconds: Number(timeInMs)});
    }

    /**
     * Server callable function.
     */
    select(allDay, start, end) {
        this.calendar.select({start: start, end: end, allDay: allDay});
    }

    render() {
        setTimeout((e) => this.calendar.render());
    }

    formatDate(dateTime, omitTime = false) {
        if (!(dateTime instanceof Date)) {
            dateTime = new Date(dateTime);
        }

        let moment = toMoment(dateTime, this.calendar);
        if (omitTime) {
            return moment.startOf('day').format().substring(0, 10);
        }

        return moment.format();
    }
}

customElements.define(JmixFullCalendar.is, JmixFullCalendar);