import moment from 'moment';

export function viewToServerObject(calendarView, dateFormatter) {
    return {
        activeEnd: dateFormatter(calendarView.activeEnd),
        activeStart: dateFormatter(calendarView.activeStart),
        currentEnd: dateFormatter(calendarView.currentEnd),
        currentStart: dateFormatter(calendarView.currentStart),
        title: calendarView.title,
        type: calendarView.type
    }
}

export function mouseInfoToServerObject(mouseDetails) {
    return {
        button: mouseDetails.button,
        pageX: mouseDetails.pageX,
        pageY: mouseDetails.pageY,
        altKey: mouseDetails.altKey,
        ctrlKey: mouseDetails.ctrlKey,
        metaKey: mouseDetails.metaKey,
        shiftKey: mouseDetails.shiftKey,
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

export function createCalendarCellDetails(context) {
    const dayEvent = context.dayEvent;
    const dateFormatter = context.calendar.formatIso.bind(context.calendar);

    let isCellDisabled = false;
    const validRange = context.calendar.getOption('validRange');
    if (validRange) {
        const date = moment(dayEvent.date).startOf('day');
        const startDisabled = validRange.start ? date.isBefore(validRange.start) : false;
        const endDisabled = validRange.end ? date.isSameOrAfter(validRange.end) : false;
        isCellDisabled = startDisabled || endDisabled;
    }
    return {
        date: dateFormatter(dayEvent.date),
        isFuture: dayEvent.isFuture,
        isPast: dayEvent.isPast,
        isToday: dayEvent.isToday,
        isOther: dayEvent.isOther,
        isDisabled: isCellDisabled,
        isMonthStart: dayEvent.isMonthStart,
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

function isJavaScriptFunction(stringFunction) {
    return stringFunction.indexOf('(') !== -1
        && stringFunction.indexOf(')') !== -1
        && stringFunction.indexOf('{') !== -1
        && stringFunction.lastIndexOf('}') !== -1
}

// todo rp move to Option?
export function assignI18n(calendarI18n, jmixI18n) {
    if (isNotNullUndefined(jmixI18n.direction)) {
        calendarI18n['direction'] = jmixI18n.direction.toLowerCase();
    }
    if (isNotNullUndefined(jmixI18n.dayOfWeek)) {
        calendarI18n['week'].dow = jmixI18n.dayOfWeek;
    }
    if (isNotNullUndefined(jmixI18n.dayOfYear)) {
        calendarI18n['week'].doy = jmixI18n.dayOfYear;
    }
    if (isNotNullUndefined(jmixI18n.weekText)) {
        calendarI18n['weekText'] = jmixI18n.weekText;
    }
    if (isNotNullUndefined(jmixI18n.weekTextLong)) {
        calendarI18n['weekTextLong'] = jmixI18n.weekTextLong;
    }
    if (isNotNullUndefined(jmixI18n.allDayText)) {
        calendarI18n['allDayText'] = jmixI18n.allDayText;
    }
    if (isNotNullUndefined(jmixI18n.moreLinkText)) {
        calendarI18n['moreLinkText'] = isJavaScriptFunction(jmixI18n.moreLinkText)
            ? parseJavaScriptFunction(jmixI18n.moreLinkText)
            : new Function("count", "return `" + jmixI18n.moreLinkText + "`");
    }
    if (isNotNullUndefined(jmixI18n.noEventsText)) {
        calendarI18n['noEventsText'] = jmixI18n.noEventsText;
    }
    if (isNotNullUndefined(jmixI18n.closeHint)) {
        calendarI18n['closeHint'] = jmixI18n.closeHint;
    }
    if (isNotNullUndefined(jmixI18n.eventHint)) {
        calendarI18n['eventHint'] = jmixI18n.eventHint;
    }
    if (isNotNullUndefined(jmixI18n.timeHint)) {
        calendarI18n['timeHint'] = jmixI18n.timeHint;
    }
    if (isNotNullUndefined(jmixI18n.navLinkHint)) {
        calendarI18n['navLinkHint'] = isJavaScriptFunction(jmixI18n.navLinkHint)
            ? parseJavaScriptFunction(jmixI18n.navLinkHint)
            : new Function("date", "return `" + jmixI18n.navLinkHint + "`");
    }
    if (isNotNullUndefined(jmixI18n.moreLinkHint)) {
        calendarI18n['moreLinkHint'] = isJavaScriptFunction(jmixI18n.moreLinkHint)
            ? parseJavaScriptFunction(jmixI18n.moreLinkHint)
            : new Function("count", "return `" + jmixI18n.moreLinkHint + "`")
    }
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

function isNotNullUndefined(value) {
    return value !== null && value !== undefined;
}

export default viewToServerObject;