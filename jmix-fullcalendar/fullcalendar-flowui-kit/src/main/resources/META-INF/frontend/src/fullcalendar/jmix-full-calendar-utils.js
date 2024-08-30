import moment from 'moment';

const FC_DAY_PAST = 'fc-day-past';
const FC_DAY_FUTURE = 'fc-day-future';
const FC_DAY_TODAY = 'fc-day-today';
const FC_DAY_OTHER = 'fc-day-other';
const FC_DAY_DISABLED = 'fc-day-disabled';

/**
 * CAUTION! Copied from @fullcalendar/core/index.js
 * @type {{}}
 */
const MINIMAL_RAW_EN_LOCALE = {
    code: 'en',
    week: {
        dow: 0,
        doy: 4, // 4 days need to be within the year to be considered the first week
    },
    direction: 'ltr',
    buttonText: {
        prev: 'prev',
        next: 'next',
        prevYear: 'prev year',
        nextYear: 'next year',
        year: 'year',
        today: 'today',
        month: 'month',
        week: 'week',
        day: 'day',
        list: 'list',
    },
    weekText: 'W',
    weekTextLong: 'Week',
    closeHint: 'Close',
    timeHint: 'Time',
    eventHint: 'Event',
    allDayText: 'all-day',
    moreLinkText: 'more',
    noEventsText: 'No events to display',
}

/**
 * CAUTION! Copied from @fullcalendar/core/index.js
 * @type {{}}
 */
export const RAW_EN_LOCALE = Object.assign(Object.assign({}, MINIMAL_RAW_EN_LOCALE), {
    // Includes things we don't want other locales to inherit,
    // things that derive from other translatable strings.
    buttonHints: {
        prev: 'Previous $0',
        next: 'Next $0',
        today(buttonText, unit) {
            return (unit === 'day')
                ? 'Today'
                : `This ${buttonText}`;
        },
    }, viewHint: '$0 view', navLinkHint: 'Go to $0', moreLinkHint(eventCnt) {
        return `Show ${eventCnt} more event${eventCnt === 1 ? '' : 's'}`;
    }
});

export function createViewInfo(view, dateFormatter) {
    return {
        activeEnd: dateFormatter(view.activeEnd, true), // omit time as it always 00:00
        activeStart: dateFormatter(view.activeStart, true), // omit time as it always 00:00
        currentEnd: dateFormatter(view.currentEnd, true), // omit time as it always 00:00
        currentStart: dateFormatter(view.currentStart, true), // omit time as it always 00:00
        type: view.type
    }
}

export function createMouseDetails(jsEvent) {
    return {
        button: jsEvent.button,
        pageX: jsEvent.pageX,
        pageY: jsEvent.pageY,
        altKey: jsEvent.altKey,
        ctrlKey: jsEvent.ctrlKey,
        metaKey: jsEvent.metaKey,
        shiftKey: jsEvent.shiftKey,
    }
}

export function segmentsToServerData(segments, dateFormatter) {
    const serverData = [];

    segments.forEach((segment) =>
        serverData.push(segmentToServerData(segment, dateFormatter)));

    return serverData;
}

export function eventsToServerData(events) {
    if (!events) {
        return [];
    }
    const serverEvents = []

    events.forEach((e) => serverEvents.push(eventToServerData(e)))

    return serverEvents;
}

export function eventToServerData(event) {
    return event.toJSON();
}

export function segmentToServerData(segment, dateFormatter) {
    return {
        endDate: dateFormatter(segment.end),
        startDate: dateFormatter(segment.start),
        isEnd: segment.isEnd,
        isStart: segment.isStart,
        eventId: segment.event.id,
        eventSourceId: segment.event.extendedProps.jmixSourceId,
    }
}

export function createCalendarCellDetailsFromElement(dayCellElement) {
    return {
        date: dayCellElement.dataset.date,
        isFuture: dayCellElement.classList.contains(FC_DAY_FUTURE),
        isPast: dayCellElement.classList.contains(FC_DAY_PAST),
        isToday: dayCellElement.classList.contains(FC_DAY_TODAY),
        isOther: dayCellElement.classList.contains(FC_DAY_OTHER),
        isDisabled: dayCellElement.classList.contains(FC_DAY_DISABLED),
        // Ignore locale, since FullCalendar always consider Sunday = 0
        dow: moment(dayCellElement.dataset.date, "YYYY-MM-DD").day()
    }
}

export function createCalendarCellDetails(dayEvent, calendar) {
    const dateFormatter = calendar.formatIso.bind(calendar);

    let isCellDisabled = false;
    const validRange = calendar.getOption('validRange');
    if (validRange) {
        const date = moment(dayEvent.date).startOf('day');
        const startDisabled = validRange.start ? date.isBefore(validRange.start) : false;
        const endDisabled = validRange.end ? date.isSameOrAfter(validRange.end) : false;
        isCellDisabled = startDisabled || endDisabled;
    }
    return {
        date: dateFormatter(dayEvent.date, true), // omit time
        isFuture: dayEvent.isFuture,
        isPast: dayEvent.isPast,
        isToday: dayEvent.isToday,
        isOther: dayEvent.isOther,
        isDisabled: isCellDisabled || dayEvent.el.className.includes('disabled'),
        dow: dayEvent.dow,
    };
}

export function parseJavaScriptFunction(stringFunction) {
    let startArgsIndex = stringFunction.indexOf('(');
    let endArgsIndex = stringFunction.indexOf(')');

    let startBodyIndex = stringFunction.indexOf('{');
    let endBodyIndex = stringFunction.lastIndexOf('}');

    if (startArgsIndex === -1 || endArgsIndex === -1 || startBodyIndex === -1 || endBodyIndex === -1) {
        console.warn('Unparsable native JavaScript function: ' + stringFunction);
        return;
    }

    let args = stringFunction.slice(startArgsIndex + 1, endArgsIndex)
        .split(',')
        .map(element => element.trim());
    let body = stringFunction.slice(startBodyIndex + 1, endBodyIndex).trim();

    return new Function(args, body);
}

export function isJavaScriptFunction(stringFunction) {
    return stringFunction.indexOf('(') !== -1
        && stringFunction.indexOf(')') !== -1
        && stringFunction.indexOf('{') !== -1
        && stringFunction.lastIndexOf('}') !== -1
}

export function convertToLocaleDependedOptions(jmixI18n) {
    const options = {};
    if (isNotNullUndefined(jmixI18n.dayPopoverFormat)) {
        options['dayPopoverFormat'] = isJavaScriptFunction(jmixI18n.dayPopoverFormat)
            ? parseJavaScriptFunction(jmixI18n.dayPopoverFormat)
            : jmixI18n.dayPopoverFormat;
    }
    if (isNotNullUndefined(jmixI18n.dayHeaderFormat)) {
        options['dayHeaderFormat'] = isJavaScriptFunction(jmixI18n.dayHeaderFormat)
            ? parseJavaScriptFunction(jmixI18n.dayHeaderFormat)
            : jmixI18n.dayHeaderFormat;
    }
    if (isNotNullUndefined(jmixI18n.weekNumberFormat)) {
        options['weekNumberFormat'] = isJavaScriptFunction(jmixI18n.weekNumberFormat)
            ? parseJavaScriptFunction(jmixI18n.weekNumberFormat)
            : jmixI18n.weekNumberFormat;
    }
    if (isNotNullUndefined(jmixI18n.slotLabelFormat)) {
        options['slotLabelFormat'] = isJavaScriptFunction(jmixI18n.slotLabelFormat)
            ? parseJavaScriptFunction(jmixI18n.slotLabelFormat)
            : jmixI18n.slotLabelFormat;
    }
    if (isNotNullUndefined(jmixI18n.eventTimeFormat)) {
        options['eventTimeFormat'] = isJavaScriptFunction(jmixI18n.eventTimeFormat)
            ? parseJavaScriptFunction(jmixI18n.eventTimeFormat)
            : jmixI18n.eventTimeFormat;
    }
    if (isNotNullUndefined(jmixI18n.monthStartFormat)) {
        options['monthStartFormat'] = isJavaScriptFunction(jmixI18n.monthStartFormat)
            ? parseJavaScriptFunction(jmixI18n.monthStartFormat)
            : jmixI18n.monthStartFormat;
    }
    return options;
}

/**
 * Deletes null properties of first level.
 * @param object the object to delete from
 * @returns {{[p: string]: unknown}}
 */
export function deleteNullProperties(object) {
    return Object.fromEntries(Object.entries(object).filter(([_, v]) => v != null));
}

export function isNotNullUndefined(value) {
    return value !== null && value !== undefined;
}

export function findElementRecursivelyByInnerText(element, innerText) {
    if (element.innerText === innerText) {
        return element;
    }
    if (element.children) {
        for (const child of element.children) {
            const result = findElementRecursivelyByInnerText(child, innerText);
            if (result) {
                return result;
            }
        }
    }
}

export default createViewInfo;