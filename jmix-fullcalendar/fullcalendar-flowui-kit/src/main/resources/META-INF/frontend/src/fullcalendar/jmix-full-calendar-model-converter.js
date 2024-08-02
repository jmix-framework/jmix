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

export default viewToServerObject;