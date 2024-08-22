package io.jmix.fullcalendarflowui.component;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendarflowui.component.data.LazyEventProviderManager;
import io.jmix.fullcalendarflowui.component.event.*;
import io.jmix.fullcalendarflowui.component.event.MoreLinkClickEvent.EventProviderContext;
import io.jmix.fullcalendarflowui.component.data.AbstractEventProviderManager;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.LazyCalendarEventProvider;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import io.jmix.fullcalendarflowui.kit.component.serialization.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

@Internal
@Component("fcaldr_FullCalendarHelper")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FullCalendarDelegate {
    private static final Logger log = LoggerFactory.getLogger(FullCalendarDelegate.class);

    private static final String PACKAGE = "io.jmix.fullcalendarflowui.component";

    protected FullCalendar fullCalendar;
    protected JsonFactory jsonFactory;
    protected Messages messages;
    protected CurrentAuthentication currentAuthentication;

    protected FullCalendarI18n defaultI18n;
    protected FullCalendarI18n explicitI18n;

    public FullCalendarDelegate(FullCalendar fullCalendar,
                                Messages messages,
                                CurrentAuthentication currentAuthentication) {
        this.fullCalendar = fullCalendar;
        this.messages = messages;
        this.currentAuthentication = currentAuthentication;

        jsonFactory = createJsonFactory();
    }

    @Nullable
    public FullCalendarI18n getI18n() {
        return explicitI18n;
    }

    public void setI18n(@Nullable FullCalendarI18n i18n) {
        this.explicitI18n = i18n;

        setI18nInternal(defaultI18n.combine(explicitI18n));
    }

    public JsonArray getMoreLinkClassNames(DomMoreLinkClassNames clientContext) {
        if (fullCalendar.getMoreLinkClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        List<String> classNames = fullCalendar.getMoreLinkClassNamesGenerator()
                .apply(new MoreLinkClassNamesContext(
                        clientContext.getEventsCount(),
                        clientContext.getShortText(),
                        clientContext.getText(),
                        createViewInfo(clientContext.getView())));

        JsonArray result = classNames == null
                ? jsonFactory.createArray()
                : fullCalendar.getSerializer().toJsonArrayFromString(classNames);

        log.debug("Serialized 'MoreLinkClassNames': {}", result.toJson());

        return result;
    }

    public JsonArray getDayHeaderClassNames(DomDayHeaderClassNames clientContext) {
        if (fullCalendar.getDayHeaderClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        List<String> classNames = fullCalendar.getDayHeaderClassNamesGenerator()
                .apply(new DayHeaderClassNamesContext(
                        LocalDate.parse(clientContext.getDate()),
                        Objects.requireNonNull(DayOfWeek.fromId(clientContext.getDow())),
                        clientContext.isDisabled(),
                        clientContext.isFuture(),
                        clientContext.isOther(),
                        clientContext.isPast(),
                        clientContext.isToday(),
                        createViewInfo(clientContext.getView())));

        JsonArray result = classNames == null
                ? jsonFactory.createArray()
                : fullCalendar.getSerializer().toJsonArrayFromString(classNames);

        log.debug("Serialized 'DayHeaderClassNames': {}", result.toJson());

        return result;
    }

    public JsonArray getDayCellClassNames(DomDayCellClassNames clientContext) {
        if (fullCalendar.getDayCellClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        List<String> classNames = fullCalendar.getDayCellClassNamesGenerator()
                .apply(new DayCellClassNamesContext(
                        LocalDate.parse(clientContext.getDate()),
                        Objects.requireNonNull(DayOfWeek.fromId(clientContext.getDow())),
                        clientContext.isDisabled(),
                        clientContext.isFuture(),
                        clientContext.isOther(),
                        clientContext.isPast(),
                        clientContext.isToday(),
                        createViewInfo(clientContext.getView())));

        JsonArray result = classNames == null
                ? jsonFactory.createArray()
                : fullCalendar.getSerializer().toJsonArrayFromString(classNames);

        log.debug("Serialized 'DayCellClassNames': {}", result.toJson());

        return result;
    }

    public JsonArray getSlotLabelClassNames(DomSlotLabelClassNames clientContext) {
        if (fullCalendar.getSlotLabelClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        List<String> classNames = fullCalendar.getSlotLabelClassNamesGenerator()
                .apply(new SlotLabelClassNamesContext(
                        LocalTime.parse(clientContext.getTime()),
                        createViewInfo(clientContext.getView())));

        JsonArray result = classNames == null
                ? jsonFactory.createArray()
                : fullCalendar.getSerializer().toJsonArrayFromString(classNames);

        log.debug("Serialized 'SlotLabelClassNames': {}", result.toJson());

        return result;
    }

    public JsonArray fetchCalendarItems(String sourceId, String start, String end) {
        LazyEventProviderManager eventProviderWrapper = (LazyEventProviderManager) getEventProvidersMap().values().stream()
                .filter(ep -> ep.getSourceId().equals(sourceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot fetch items for lazy event provider," +
                        " since there is no event provider with client ID:" + sourceId));

        return eventProviderWrapper.fetchAndSerialize(
                new LazyCalendarEventProvider.ItemsFetchContext(
                        eventProviderWrapper.getEventProvider(),
                        parseAndTransform(start, getComponentZoneId()),
                        parseAndTransform(end, getComponentZoneId()),
                        getComponentTimeZone()));
    }

    public DatesSetEvent createDatesSetEvent(DomDatesSet domDatesSet, boolean fromClient) {
        return new DatesSetEvent(
                fullCalendar,
                fromClient,
                parseAndTransform(domDatesSet.getStartDateTime(), getComponentZoneId()),
                parseAndTransform(domDatesSet.getEndDateTime(), getComponentZoneId()),
                createViewInfo(domDatesSet.getView()));
    }

    public MoreLinkClickEvent createMoreLinkClickEvent(DomMoreLinkClick clientContext, boolean fromClient) {
        List<EventProviderContext> eventProviderContexts = new ArrayList<>();
        for (AbstractEventProviderManager epWrapper : getEventProvidersMap().values()) {
            EventProviderContext eventProviderContext = createEventProviderContext(epWrapper,
                    clientContext.getAllData(), clientContext.getHiddenData());
            if (eventProviderContext != null) {
                eventProviderContexts.add(eventProviderContext);
            }
        }
        return new MoreLinkClickEvent(
                fullCalendar,
                fromClient,
                clientContext.isAllDay(),
                parseAndTransform(clientContext.getDate(), getComponentZoneId()),
                createViewInfo(clientContext.getView()),
                eventProviderContexts,
                new MouseEventDetails(clientContext.getMouseDetails())
        );
    }

    public EventClickEvent createEventClickEvent(DomEventMouse clientContext, boolean fromClient) {
        AbstractEventProviderManager eventProviderManager = getEventProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), eventProviderManager);

        return new EventClickEvent(
                fullCalendar,
                fromClient,
                new MouseEventDetails(clientContext.getMouseDetails()),
                calendarEvent,
                eventProviderManager.getEventProvider(),
                createViewInfo(clientContext.getView())
        );
    }

    public EventMouseEnterEvent createEventMouseEnterEvent(DomEventMouse clientContext, boolean fromClient) {
        AbstractEventProviderManager eventProviderManager = getEventProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), eventProviderManager);

        return new EventMouseEnterEvent(
                fullCalendar,
                fromClient,
                new MouseEventDetails(clientContext.getMouseDetails()),
                calendarEvent,
                eventProviderManager.getEventProvider(),
                createViewInfo(clientContext.getView()));
    }

    public EventMouseLeaveEvent createEventMouseLeaveEvent(DomEventMouse clientContext, boolean fromClient) {
        AbstractEventProviderManager eventProviderManager = getEventProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), eventProviderManager);

        return new EventMouseLeaveEvent(
                fullCalendar,
                fromClient,
                new MouseEventDetails(clientContext.getMouseDetails()),
                calendarEvent,
                eventProviderManager.getEventProvider(),
                createViewInfo(clientContext.getView()));
    }

    public EventDropEvent createEventDropEvent(DomEventDrop clientEvent, boolean fromClient) {
        AbstractEventProviderManager epManager = getEventProviderManager(clientEvent.getEvent().getSourceId());

        CalendarEvent droppedEvent = getCalendarEvent(clientEvent.getEvent(), epManager);
        applyChangesToCalendarEvent(droppedEvent, clientEvent.getEvent());

        List<CalendarEvent> relatedEvents = getRelatedEvents(clientEvent.getRelatedEvents(), epManager);

        return new EventDropEvent(fullCalendar, fromClient,
                new MouseEventDetails(clientEvent.getMouseDetails()),
                createOldValues(clientEvent.getOldEvent()),
                droppedEvent,
                relatedEvents,
                createViewInfo(clientEvent.getView()),
                createDelta(clientEvent.getDelta()));
    }

    public EventResizeEvent createEventResizeEvent(DomEventResize clientEvent, boolean fromClient) {
        AbstractEventProviderManager epManager = getEventProviderManager(clientEvent.getEvent().getSourceId());

        CalendarEvent resizedEvent = getCalendarEvent(clientEvent.getEvent(), epManager);
        applyChangesToCalendarEvent(resizedEvent, clientEvent.getEvent());

        List<CalendarEvent> relatedEvents = getRelatedEvents(clientEvent.getRelatedEvents(), epManager);

        return new EventResizeEvent(fullCalendar, fromClient,
                new MouseEventDetails(clientEvent.getMouseDetails()),
                createOldValues(clientEvent.getOldEvent()),
                resizedEvent,
                relatedEvents,
                createViewInfo(clientEvent.getView()),
                createDelta(clientEvent.getStartDelta()),
                createDelta(clientEvent.getEndDelta()));
    }

    public DateClickEvent createDateClickEvent(DomDateClick clientEvent, boolean fromClient) {
        return new DateClickEvent(fullCalendar, fromClient,
                new MouseEventDetails(clientEvent.getMouseDetails()),
                parseAndTransform(clientEvent.getDate(), getComponentZoneId()),
                clientEvent.isAllDay(),
                createViewInfo(clientEvent.getView()));
    }

    public SelectEvent createSelectEvent(DomSelect clientEvent, boolean fromClient) {
        return new SelectEvent(
                fullCalendar, fromClient,
                clientEvent.getMouseDetails() != null ? new MouseEventDetails(clientEvent.getMouseDetails()) : null,
                parseAndTransform(clientEvent.getStart(), getComponentZoneId()),
                parseAndTransform(clientEvent.getEnd(), getComponentZoneId()),
                clientEvent.isAllDay(),
                createViewInfo(clientEvent.getView()));
    }

    public UnselectEvent createUnselectEvent(DomUnselect clientEvent, boolean fromClient) {
        return new UnselectEvent(
                fullCalendar, fromClient,
                createViewInfo(clientEvent.getView()),
                clientEvent.getMouseDetails() != null ? new MouseEventDetails(clientEvent.getMouseDetails()) : null);
    }

    public void setupLocalization() {
        setupDayGridLocalizedFormats();
        setupTimeGridLocalizedFormats();
        setupListLocalizedFormats();
        setupMultiLocalizedFormats();
        setupLocalizedFormats();

        defaultI18n = createDefaultI18n();
        setI18nInternal(defaultI18n);

        setupCalendarLocalizedUnitNames();
    }

    protected void setupDayGridLocalizedFormats() {
        DayGridDayViewProperties dayGridDay = getCalendarViewProperties(CalendarViewType.DAY_GRID_DAY);
        dayGridDay.setDayPopoverFormat(getMessage("dayGridDayDayPopoverFormat"));
        dayGridDay.setDayHeaderFormat(getMessage("dayGridDayDayHeaderFormat"));
        dayGridDay.setWeekNumberFormat(getMessage("dayGridDayWeekNumberFormat"));
        dayGridDay.setEventTimeFormat(getMessage("dayGridDayEventTimeFormat"));

        DayGridWeekViewProperties dayGridWeek = getCalendarViewProperties(CalendarViewType.DAY_GRID_WEEK);
        dayGridWeek.setDayPopoverFormat(getMessage("dayGridWeekDayDayPopoverFormat"));
        dayGridWeek.setDayHeaderFormat(getMessage("dayGridWeekDayHeaderFormat"));
        dayGridWeek.setWeekNumberFormat(getMessage("dayGridWeekWeekNumberFormat"));
        dayGridWeek.setEventTimeFormat(getMessage("dayGridWeekEventTimeFormat"));

        DayGridMonthViewProperties dayGridMonth = getCalendarViewProperties(CalendarViewType.DAY_GRID_MONTH);
        dayGridMonth.setDayPopoverFormat(getMessage("dayGridMonthDayPopoverFormat"));
        dayGridMonth.setDayHeaderFormat(getMessage("dayGridMonthDayHeaderFormat"));
        dayGridMonth.setWeekNumberFormat(getMessage("dayGridMonthWeekNumberFormat"));
        dayGridMonth.setEventTimeFormat(getMessage("dayGridMonthEventTimeFormat"));

        DayGridYearViewProperties dayGridYear = getCalendarViewProperties(CalendarViewType.DAY_GRID_YEAR);
        dayGridYear.setDayPopoverFormat(getMessage("dayGridYearDayPopoverFormat"));
        dayGridYear.setDayHeaderFormat(getMessage("dayGridYearDayHeaderFormat"));
        dayGridYear.setWeekNumberFormat(getMessage("dayGridYearWeekNumberFormat"));
        dayGridYear.setEventTimeFormat(getMessage("dayGridYearEventTimeFormat"));
        dayGridYear.setMonthStartFormat(getMessage("dayGridYearMonthStartFormat"));
    }

    protected void setupTimeGridLocalizedFormats() {
        TimeGridDayViewProperties timeGridDay = getCalendarViewProperties(CalendarViewType.TIME_GRID_DAY);
        timeGridDay.setDayPopoverFormat(getMessage("timeGridDayDayPopoverFormat"));
        timeGridDay.setDayHeaderFormat(getMessage("timeGridDayDayHeaderFormat"));
        timeGridDay.setWeekNumberFormat(getMessage("timeGridDayWeekNumberFormat"));
        timeGridDay.setEventTimeFormat(getMessage("timeGridDayEventTimeFormat"));
        timeGridDay.setSlotLabelFormat(getMessage("timeGridDaySlotLabelFormat"));

        TimeGridWeekViewProperties timeGridWeek = getCalendarViewProperties(CalendarViewType.TIME_GRID_WEEK);
        timeGridWeek.setDayPopoverFormat(getMessage("timeGridWeekDayPopoverFormat"));
        timeGridWeek.setDayHeaderFormat(getMessage("timeGridWeekDayHeaderFormat"));
        timeGridWeek.setWeekNumberFormat(getMessage("timeGridWeekWeekNumberFormat"));
        timeGridWeek.setEventTimeFormat(getMessage("timeGridWeekEventTimeFormat"));
        timeGridWeek.setSlotLabelFormat(getMessage("timeGridWeekSlotLabelFormat"));
    }

    protected void setupListLocalizedFormats() {
        ListDayViewProperties listDay = getCalendarViewProperties(CalendarViewType.LIST_DAY);
        listDay.setListDayFormat(getMessage("listDayListDayFormat"));
        listDay.setListDaySideFormat(getMessage("listDayListDaySideFormat"));

        ListWeekViewProperties listWeek = getCalendarViewProperties(CalendarViewType.LIST_WEEK);
        listWeek.setListDayFormat(getMessage("listWeekListDayFormat"));
        listWeek.setListDaySideFormat(getMessage("listWeekListDaySideFormat"));

        ListMonthViewProperties listMonth = getCalendarViewProperties(CalendarViewType.LIST_MONTH);
        listMonth.setListDayFormat(getMessage("listMonthListDayFormat"));
        listMonth.setListDaySideFormat(getMessage("listMonthListDaySideFormat"));

        ListYearViewProperties listYear = getCalendarViewProperties(CalendarViewType.LIST_YEAR);
        listYear.setListDayFormat(getMessage("listYearListDayFormat"));
        listYear.setListDaySideFormat(getMessage("listYearListDaySideFormat"));
    }

    protected void setupMultiLocalizedFormats() {
        MultiMonthYearProperties multiMonthYear = getCalendarViewProperties(CalendarViewType.MULTI_MONTH_YEAR);
        multiMonthYear.setMultiMonthTitleFormat(getMessage("multiMonthYearMultiMonthTitleFormat"));
    }

    protected void setupLocalizedFormats() {
        fullCalendar.setDayPopoverFormat(messages.getMessage(PACKAGE, "dayPopoverFormat"));
        fullCalendar.setDayHeaderFormat(messages.getMessage(PACKAGE, "dayHeaderFormat"));
        fullCalendar.setWeekNumberFormat(messages.getMessage(PACKAGE, "weekNumberFormat"));
        fullCalendar.setSlotNumberFormat(messages.getMessage(PACKAGE, "slotLabelFormat"));
        fullCalendar.setEventTimeFormat(messages.getMessage(PACKAGE, "eventTimeFormat"));
    }

    protected void setupCalendarLocalizedUnitNames() {
        JsonObject json = createCalendarLocalizedUnitNamesJson();

        json.put("locale", fullCalendar.getSerializer().serializeValue(currentAuthentication.getLocale()));

        fullCalendar.getElement().callJsFunction("_defineMomentJsLocale", json);
    }

    protected void setI18nInternal(FullCalendarI18n i18n) {
        JsonObject json = fullCalendar.getSerializer().serializeObject(i18n);

        json.put("locale", fullCalendar.getSerializer().serializeValue(currentAuthentication.getLocale()));

        fullCalendar.getElement().setPropertyJson("i18n", json);
    }

    protected <T extends AbstractCalendarViewProperties> T getCalendarViewProperties(CalendarViewType viewType) {
        return Objects.requireNonNull(fullCalendar.getCalendarViewProperties(viewType));
    }

    protected String getMessage(String key) {
        return messages.getMessage(PACKAGE, key);
    }

    protected List<String> getListMessage(String key) {
        return Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(getMessage(key));
    }

    protected ZoneId getComponentZoneId() {
        return getComponentTimeZone().toZoneId();
    }

    protected TimeZone getComponentTimeZone() {
        TimeZone timeZone = fullCalendar.getTimeZone();
        if (timeZone != null) {
            return timeZone;
        }
        TimeZone defaultTimeZone = fullCalendar.getOptions().getTimeZone().getDefaultValue();
        if (defaultTimeZone != null) {
            return defaultTimeZone;
        }
        return TimeZone.getDefault();
    }

    protected AbstractEventProviderManager getEventProviderManager(String sourceId) {
        return getEventProvidersMap().values().stream()
                .filter(epw -> epw.getSourceId().equals(sourceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no event provider with ID:" + sourceId));
    }

    protected CalendarEvent getCalendarEvent(DomCalendarEvent clientEvent, AbstractEventProviderManager epManager) {
        CalendarEvent calendarEvent = epManager.getCalendarEvent(clientEvent.getId());
        if (calendarEvent == null) {
            throw new IllegalStateException("Cannot find calendar event by client ID: " + clientEvent.getId());
        }
        return calendarEvent;
    }

    protected List<CalendarEvent> getRelatedEvents(List<DomCalendarEvent> relatedEvents,
                                                   AbstractEventProviderManager epManager) {
        List<CalendarEvent> calendarEvents = new ArrayList<>(relatedEvents.size());
        for (DomCalendarEvent changedEvent : relatedEvents) {
            CalendarEvent relatedEvent = epManager.getCalendarEvent(changedEvent.getId());
            if (relatedEvent == null) {
                throw new IllegalStateException("Cannot find calendar event by client ID: " + changedEvent.getId());
            }
            calendarEvents.add(relatedEvent);
            applyChangesToCalendarEvent(relatedEvent, changedEvent);
        }
        return calendarEvents;
    }

    @Nullable
    protected EventProviderContext createEventProviderContext(AbstractEventProviderManager epWrapper,
                                                              List<DomSegment> allData,
                                                              List<DomSegment> hiddenData) {
        List<DomSegment> visibleData = new ArrayList<>();
        allData.stream()
                .filter(s -> !hiddenData.contains(s))
                .forEach(visibleData::add);

        List<CalendarEvent> visibleEvents = convertToCalendarEvents(epWrapper, visibleData);
        List<CalendarEvent> hiddenEvents = convertToCalendarEvents(epWrapper, hiddenData);

        if (CollectionUtils.isEmpty(visibleEvents)
                && CollectionUtils.isEmpty(hiddenEvents)) {
            return null;
        }

        return new EventProviderContext(epWrapper.getEventProvider(), visibleEvents, hiddenEvents);
    }

    protected List<CalendarEvent> convertToCalendarEvents(AbstractEventProviderManager epWrapper, List<DomSegment> segments) {
        return segments.stream()
                .filter(e -> e.getEventSourceId().equals(epWrapper.getSourceId()))
                .map(e -> epWrapper.getCalendarEvent(e.getEventId()))
                .toList();
    }

    protected ViewInfo createViewInfo(DomViewInfo clientViewInfo) {
        return new ViewInfo(
                parseAndTransform(clientViewInfo.getActiveEnd(), getComponentZoneId()),
                parseAndTransform(clientViewInfo.getActiveStart(), getComponentZoneId()),
                parseAndTransform(clientViewInfo.getCurrentEnd(), getComponentZoneId()),
                parseAndTransform(clientViewInfo.getCurrentStart(), getComponentZoneId()),
                fullCalendar.getCalendarView(clientViewInfo.getType()),
                clientViewInfo.getTitle());
    }

    protected AbstractEventChangeEvent.OldValues createOldValues(DomCalendarEvent clientEvent) {
        LocalDateTime endDateTime = !Strings.isNullOrEmpty(clientEvent.getEnd())
                ? parseAndTransform(clientEvent.getEnd(), getComponentZoneId())
                : null;
        return new AbstractEventChangeEvent.OldValues(
                parseAndTransform(clientEvent.getStart(), getComponentZoneId()),
                endDateTime,
                clientEvent.isAllDay());
    }

    protected CalendarDuration createDelta(DomCalendarDuration clientDuration) {
        return CalendarDuration.ofYears(clientDuration.getYears())
                .plusMonths(clientDuration.getMonths())
                .plusDays(clientDuration.getDays())
                .plusMilliseconds(clientDuration.getMilliseconds());
    }

    protected Map<String, AbstractEventProviderManager> getEventProvidersMap() {
        return fullCalendar.getEventProvidersMap();
    }

    protected JsonFactory createJsonFactory() {
        return new JreJsonFactory();
    }

    protected void applyChangesToCalendarEvent(CalendarEvent calendarEvent,
                                               DomCalendarEvent clientEvent) {
        if (!Strings.isNullOrEmpty(clientEvent.getStart())) {
            LocalDateTime startDateTime = parseAndTransform(clientEvent.getStart(), getComponentZoneId());
            calendarEvent.setStartDateTime(startDateTime);
        }
        if (!Strings.isNullOrEmpty(clientEvent.getEnd())) {
            LocalDateTime endDateTime = parseAndTransform(clientEvent.getEnd(), getComponentZoneId());
            calendarEvent.setEndDateTime(endDateTime);
        }
        Boolean allDay = clientEvent.isAllDay();
        if (!Objects.equals(calendarEvent.getAllDay(), allDay)
                && (calendarEvent.getAllDay() != null || allDay)) {
            calendarEvent.setAllDay(allDay);
        }
    }

    protected FullCalendarI18n createDefaultI18n() {
        return new FullCalendarI18n()
                .withDirection(FullCalendarI18n.Direction.valueOf(getMessage("i18n.direction").toUpperCase()))
                .withDayOfWeek(Integer.parseInt(getMessage("i18n.dayOfWeek")))
                .withDayOfYear(Integer.parseInt(getMessage("i18n.dayOfYear")))
                .withWeekTextLong(getMessage("i18n.weekTextLong"))
                .withAllDayText(getMessage("i18n.allDayText"))
                .withMoreLinkText(getMessage("i18n.moreLinkText"))
                .withNoEventsText(getMessage("i18n.noEventsText"))
                .withCloseHint(getMessage("i18n.closeHint"))
                .withEventHint(getMessage("i18n.eventHint"))
                .withTimeHint(getMessage("i18n.timeHint"))
                .withNavLinkHint(getMessage("i18n.navLinkHint"))
                .withMoreLinkHint(getMessage("i18n.moreLinkHint"));
    }

    protected JsonObject createCalendarLocalizedUnitNamesJson() {
        JsonObject result = jsonFactory.createObject();
        FullCalendarSerializer serializer = fullCalendar.getSerializer();
        result.put("months", serializer.toJsonArrayFromString(getListMessage("months")));
        result.put("monthsShort", serializer.toJsonArrayFromString(getListMessage("monthsShort")));
        result.put("weekdays", serializer.toJsonArrayFromString(getListMessage("weekdays")));
        result.put("weekdaysShort", serializer.toJsonArrayFromString(getListMessage("weekdaysShort")));
        result.put("weekdaysMin", serializer.toJsonArrayFromString(getListMessage("weekdaysMin")));
        return result;
    }

    /**
     * Parses raw ISO date time to {@link ZonedDateTime} with component zoneId and then transform this value
     * to {@link LocalDateTime} with system default time zone.
     *
     * @param isoDateTime     raw ISO date time
     * @param componentZoneId {@link FullCalendar}'s zoneId
     * @return local date time
     */
    public static LocalDateTime parseAndTransform(String isoDateTime, ZoneId componentZoneId) {
        ZonedDateTime startZonedDateTime = parseIsoDateTime(isoDateTime, componentZoneId);
        return transformAsSystemDefault(startZonedDateTime);
    }

    private static LocalDateTime transformAsSystemDefault(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static ZonedDateTime parseIsoDateTime(String isoDateTime, ZoneId zoneId) {
        try {
            return ZonedDateTime.parse(isoDateTime);
        } catch (DateTimeParseException e) {
            // Exception means that offset part is missed
        }
        try {
            return LocalDateTime.parse(isoDateTime).atZone(zoneId);
        } catch (DateTimeParseException e) {
            // Exception means that time part is missed
        }
        try {
            return LocalDate.parse(isoDateTime).atStartOfDay(zoneId);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Cannot parse date: " + isoDateTime, e);
        }
    }
}
