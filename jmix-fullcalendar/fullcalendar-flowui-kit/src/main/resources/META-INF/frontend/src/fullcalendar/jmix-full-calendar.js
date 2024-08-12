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
import momentTimezonePlugin from '@fullcalendar/moment-timezone';
import localesAll from '@fullcalendar/core/locales-all.js';

import moment from 'moment';

import * as calendarUtils from './jmix-full-calendar-utils.js';
import {RAW_EN_LOCALE} from "./jmix-full-calendar-utils.js";
import {dataHolder} from './DataHolder.js';
import Options, {
    processInitialOptions,
    MORE_LINK_CLASS_NAMES,
    MORE_LINK_CLICK_FUNCTION,
} from './Options.js';

const FC_LINK_CLASS_NAME = 'fc-more-link';

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
            /**
             * @private
             */
            _eventDescriptionPosition: {
                type: String,
                value: 'bottom-end'
            },
            i18n: {
                type: Object,
                value: null,
                observer: '_onI18nChange',
            }
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

        this.calendar = new Calendar(this.calendarElement, this.getInitialOptions());
        this.calendar.render();

        this.jmixOptions = new Options(this.calendar, this);
        this.jmixOptions.addListener(MORE_LINK_CLICK_FUNCTION, this._onMoreLinkClick.bind(this));
        this.jmixOptions.addListener(MORE_LINK_CLASS_NAMES, this._onMoreLinkClassNames.bind(this));

        this._onI18nChange(this.i18n);

        // Rerender calendar since after page refresh component layout is broken
        this.render();
    }

    /**
     * Server callable function
     * @private
     */
    _onCompleteInit() {
        this.initialized = true;
    }

    getInitialOptions() {
        const initialOptions = processInitialOptions(this.initialOptions);
        return {
            headerToolbar: false,
            height: "100%",
            plugins: [dayGridPlugin, timeGridPlugin, listPlugin, multiMonthPlugin, interactionPlugin,
                momentTimezonePlugin, momentPlugin],
            timeZone: 'UTC',
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
            ...initialOptions,
        };
    }

    /**
     * Server callable function
     * @param options
     * @private
     */
    updateOptions(options) {
        this.jmixOptions.updateOptions(options);
    }

    /**
     * Server callable function
     * @param name
     * @param value
     */
    updateOption(name, value) {
        this.jmixOptions.updateOption(name, value);
    }

    /**
     * Server callable function
     * @param context
     * @private
     */
    _updateSyncSourcesData(context) {
        dataHolder.set(context.sourceId, context.data);

        this.calendar.getEventSourceById(context.sourceId).refetch();
    }

    /**
     * Server callable function
     * @param sourcesData
     * @private
     */
    _updateSourcesWithIncrementalData(sourcesData) {
        for (const sourceData of sourcesData) {
            if (!sourceData.items) {
                continue;
            }
            const items = dataHolder.get(sourceData.sourceId);
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
        if (lazySource) {
            dataHolder.set(sourceId, {compContext: this});
        }
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
            dataHolder.delete(sourceId);
            eventSource.remove();
        }
    }

    _createEventSource(sourceId, lazySource) {
        const fetchFunction = lazySource ? this._createAsyncFetchFunction(sourceId) : this._createFetchFunction(sourceId);
        return {
            events: fetchFunction,
            id: sourceId
        }
    }

    _createFetchFunction(sourceId) {
        const fetchFunction = eval(
            "var fetchFunction = function (fetchInfo, successCallback, failureCallback) {\n" +
            "        const data = dataHolder.get('" + sourceId + "');\n" +
            "        if (!data) {\n" +
            "            return;\n" +
            "        }\n" +
            "        successCallback(data);\n" +
            "  };\n" +
            "fetchFunction;");
        return fetchFunction;
    }

    _createAsyncFetchFunction(sourceId) {
        const fetchFunction = eval(
            "var lazyFetchFunction = async function (fetchInfo, successCallback, failureCallback) {\n" +
            "      const context = dataHolder.get('" + sourceId + "');\n" +
            "      const fetchCalendarItems = async () => {\n" +
            "            return await context.compContext.$server.fetchCalendarItems('" + sourceId + "', fetchInfo.startStr, fetchInfo.endStr);\n" +
            "      }\n" +
            "      const data = await fetchCalendarItems();\n" +
            "      dataHolder.set('" + sourceId + "', {compContext: context.compContext, data: data, lastFetchInfo: fetchInfo});\n" +
            "      successCallback(data);" +
            "   }\n" +
            "lazyFetchFunction;");
        return fetchFunction;
    }

    _onMoreLinkClassNames(e) {
        const context = {
            eventsCount: e.num,
            shortText: e.shortText,
            text: e.text,
            view: calendarUtils.viewToServerObject(e.view, this.calendar.formatIso.bind(this.calendar))
        }
        const classNamesPromise = this.$server.getMoreLinkClassNames(context);
        classNamesPromise.then((classNames) => {
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
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent("jmix-more-link-click", {
            detail: {
                context: {
                    allDay: e.allDay,
                    date: this.calendar.formatIso(e.date),
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                    mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent),
                    allData: calendarUtils.segmentsToServerData(e.allSegs, dateFormatter),
                    hiddenData: calendarUtils.segmentsToServerData(e.hiddenSegs, dateFormatter),
                }
            }
        }))
    }

    _onEventClick(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent("jmix-event-click", {
            detail: {
                context: {
                    event: calendarUtils.eventToServerData(e.event),
                    mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent),
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                }
            }
        }))
    }

    _onEventMouseEnter(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent("jmix-event-mouse-enter", {
            detail: {
                context: {
                    event: calendarUtils.eventToServerData(e.event),
                    mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent),
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                }
            }
        }))
    }

    _onEventMouseLeave(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent("jmix-event-mouse-leave", {
            detail: {
                context: {
                    event: calendarUtils.eventToServerData(e.event),
                    mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent),
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                }
            }
        }))
    }

    _onEventDrop(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent('jmix-event-drop', {
            detail: {
                context: {
                    delta: e.delta,
                    event: calendarUtils.eventToServerData(e.event),
                    oldEvent: calendarUtils.eventToServerData(e.oldEvent),
                    mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent),
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                    relatedEvents: calendarUtils.eventsToServerData(e.relatedEvents),
                }
            }
        }));
    }

    _onEventResize(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent('jmix-event-resize', {
            detail: {
                context: {
                    startDelta: e.startDelta,
                    endDelta: e.endDelta,
                    event: calendarUtils.eventToServerData(e.event),
                    oldEvent: calendarUtils.eventToServerData(e.oldEvent),
                    mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent),
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                    relatedEvents: calendarUtils.eventsToServerData(e.relatedEvents),
                }
            }
        }));
    }

    _onDatesSet(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent('jmix-dates-set', {
            detail: {
                context: {
                    startDateTime: e.startStr,
                    endDateTime: e.endStr,
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                    viewType: e.view.type,
                }
            }
        }));
    }

    _onDateClick(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent('jmix-date-click', {
            detail: {
                context: {
                    date: e.dateStr,
                    allDay: e.allDay,
                    mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent),
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                }
            }
        }));
    }

    _onSelect(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);
        this.dispatchEvent(new CustomEvent('jmix-select', {
            detail: {
                context: {
                    start: e.startStr,
                    end: e.endStr,
                    allDay: e.allDay,
                    view: calendarUtils.viewToServerObject(e.view, dateFormatter),
                    ...(e.jsEvent) && {mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent)},
                }
            }
        }));
    }

    _onUnselect(e) {
        const dateFormatter = this.calendar.formatIso.bind(this.calendar);

        const context = {
            view: calendarUtils.viewToServerObject(e.view, dateFormatter),
            ...(e.jsEvent) && {mouseDetails: calendarUtils.mouseInfoToServerObject(e.jsEvent)},
        }

        this.dispatchEvent(new CustomEvent('jmix-unselect', {detail: {context: context}}));
    }

    _onDayCellDidMount(e) {
        const cellElement = e.el;
        cellElement.addEventListener("contextmenu", (jsEvent) => {
            // Get current view from calendar, since view from event can be obsolete
            const viewType = this.calendar.view.type;
            if (viewType === 'dayGridDay'
                || viewType === 'dayGridWeek'
                || viewType === 'dayGridMonth'
                || viewType === 'dayGridYear') {
                if (!this.contextMenuDetails) {
                    this.contextMenuDetails = {};
                }
                this.contextMenuDetails['dayCell'] = calendarUtils.createCalendarCellDetails(
                    {dayEvent: e, jsEvent: jsEvent, calendar: this.calendar});
                this.contextMenuDetails['mouseDetails'] = calendarUtils.mouseInfoToServerObject(jsEvent);
            }
        });
    }

    _onEventDidMount(e) {
        const eventElement = e.el;

        if (e.event.extendedProps.description) {
            if (!eventElement.id) {
                eventElement.id = window.crypto.randomUUID();
            }

            const tooltip = document.createElement('vaadin-tooltip');
            tooltip.setAttribute("for", eventElement.id);
            tooltip.setAttribute("text", e.event.extendedProps.description);
            tooltip.setAttribute("position", this._eventDescriptionPosition)
            eventElement.appendChild(tooltip);
        }

        eventElement.addEventListener("contextmenu", (jsEvent) => {
            if (!this.contextMenuDetails) {
                this.contextMenuDetails = {};
            }
            this.contextMenuDetails['mouseDetails'] = calendarUtils.mouseInfoToServerObject(jsEvent);
            this.contextMenuDetails['eventCell'] = {
                isFuture: e.isFuture,
                isMirror: e.isMirror,
                isPast: e.isPast,
                isToday: e.isToday,
                event: calendarUtils.eventToServerData(e.event),
            };
        });
    }

    _onI18nChange(i18n) {
        if (!this.jmixOptions) {
            return;
        }

        // todo move to Options?
        const calendarI18nArray = localesAll.filter((item) => item.code === i18n.locale);

        const calendarI18n = calendarI18nArray.length > 0 ? calendarI18nArray[0] : RAW_EN_LOCALE;

        calendarUtils.assignI18n(calendarI18n, i18n);

        this.jmixOptions.updateOption("locale", calendarI18n);

        const formatOptions = calendarUtils.convertToLocaleDependedOptions(i18n);

        this.jmixOptions.updateOptions(formatOptions);
    }

    /**
     * Server callable function.
     * @param localizedNames
     * @private
     */
    _defineMomentJsLocale(localizedNames) {
        moment.defineLocale(localizedNames.locale, localizedNames);
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
}

customElements.define(JmixFullCalendar.is, JmixFullCalendar);