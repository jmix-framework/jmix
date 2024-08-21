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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Internal
@Component("fcaldr_FullCalendarHelper")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FullCalendarDelegate {

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

    @SuppressWarnings("UnnecessaryLocalVariable")
    public JsonArray fetchCalendarItems(String sourceId, String start, String end) {
        LazyEventProviderManager eventProviderWrapper = (LazyEventProviderManager) getEventProvidersMap().values().stream()
                .filter(ep -> ep.getSourceId().equals(sourceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot fetch items for lazy event provider," +
                        " since there is no event provider with client ID:" + sourceId));

        JsonArray jsonEvents = eventProviderWrapper.fetchAndSerialize(
                new LazyCalendarEventProvider.ItemsFetchContext(
                        eventProviderWrapper.getEventProvider(),
                        parseAndTransform(start, getComponentZoneId()),
                        parseAndTransform(end, getComponentZoneId()),
                        fullCalendar.getTimeZone()));
        return jsonEvents;
    }

    public DatesSetEvent convertToDatesSetEvent(DomDatesSet domDatesSet, boolean fromClient) {
        return new DatesSetEvent(
                fullCalendar,
                fromClient,
                parseAndTransform(domDatesSet.getStartDateTime(), getComponentZoneId()),
                parseAndTransform(domDatesSet.getEndDateTime(), getComponentZoneId()),
                createViewInfo(domDatesSet.getView()));
    }

    public MoreLinkClickEvent convertToMoreLinkClickEvent(DomMoreLinkClick clientContext, boolean fromClient) {
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

    public EventClickEvent convertToEventClickEvent(DomEventMouse clientContext, boolean fromClient) {
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

    public EventMouseEnterEvent convertToEventMouseEnterEvent(DomEventMouse clientContext, boolean fromClient) {
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

    public EventMouseLeaveEvent convertToEventMouseLeaveEvent(DomEventMouse clientContext, boolean fromClient) {
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

    public EventDropEvent convertToEventDropEvent(DomEventDrop clientEvent, boolean fromClient) {
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

    public EventResizeEvent convertToEventResizeEvent(DomEventResize clientEvent, boolean fromClient) {
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

    public DateClickEvent convertToDateClickEvent(DomDateClick clientEvent, boolean fromClient) {
        return new DateClickEvent(fullCalendar, fromClient,
                new MouseEventDetails(clientEvent.getMouseDetails()),
                parseAndTransform(clientEvent.getDate(), getComponentZoneId()),
                clientEvent.isAllDay(),
                createViewInfo(clientEvent.getView()));
    }

    public SelectEvent convertToSelectEvent(DomSelect clientEvent, boolean fromClient) {
        return new SelectEvent(
                fullCalendar, fromClient,
                clientEvent.getMouseDetails() != null ? new MouseEventDetails(clientEvent.getMouseDetails()) : null,
                parseAndTransform(clientEvent.getStart(), getComponentZoneId()),
                parseAndTransform(clientEvent.getEnd(), getComponentZoneId()),
                clientEvent.isAllDay(),
                createViewInfo(clientEvent.getView()));
    }

    public UnselectEvent convertToUnselectEvent(DomUnselect clientEvent, boolean fromClient) {
        return new UnselectEvent(
                fullCalendar, fromClient,
                createViewInfo(clientEvent.getView()),
                clientEvent.getMouseDetails() != null ? new MouseEventDetails(clientEvent.getMouseDetails()) : null);
    }

    public JsonArray getMoreLinkClassNamesJson(DomMoreLinkClassNames clientContext) {
        if (fullCalendar.getMoreLinkClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        MoreLinkClassNamesContext context = new MoreLinkClassNamesContext(
                clientContext.getEventsCount(),
                clientContext.getShortText(),
                clientContext.getText(),
                createViewInfo(clientContext.getView()));

        List<String> classNames = fullCalendar.getMoreLinkClassNamesGenerator().apply(context);
        if (classNames == null) {
            return jsonFactory.createArray();
        }

        JsonArray result = jsonFactory.createArray();
        for (int i = 0; i < classNames.size(); i++) {
            result.set(i, classNames.get(i));
        }

        return result;
    }

    public void setupLocalization() {
        setupDayGridLocalizedFormats();
        setupTimeGridLocalizedFormats();
        setupListLocalizedFormats();
        setupLocalizedFormats();

        defaultI18n = createDefaultI18n();
        setI18nInternal(defaultI18n);

        setupCalendarLocalizedUnitNames();
    }

    protected void setupDayGridLocalizedFormats() {
        DayGridDayViewProperties dayGridDay = getCalendarViewProperties(CalendarViewType.DAY_GRID_DAY);
        dayGridDay.setDayPopoverFormat(messages.getMessage(PACKAGE, "dayGridDayDayPopoverFormat"));
        dayGridDay.setDayHeaderFormat(messages.getMessage(PACKAGE, "dayGridDayDayHeaderFormat"));
        dayGridDay.setWeekNumberFormat(messages.getMessage(PACKAGE, "dayGridDayWeekNumberFormat"));
        dayGridDay.setEventTimeFormat(messages.getMessage(PACKAGE, "dayGridDayEventTimeFormat"));

        DayGridWeekViewProperties dayGridWeek = getCalendarViewProperties(CalendarViewType.DAY_GRID_WEEK);
        dayGridWeek.setDayPopoverFormat(messages.getMessage(PACKAGE, "dayGridWeekDayDayPopoverFormat"));
        dayGridWeek.setDayHeaderFormat(messages.getMessage(PACKAGE, "dayGridWeekDayHeaderFormat"));
        dayGridWeek.setWeekNumberFormat(messages.getMessage(PACKAGE, "dayGridWeekWeekNumberFormat"));
        dayGridWeek.setEventTimeFormat(messages.getMessage(PACKAGE, "dayGridWeekEventTimeFormat"));

        DayGridMonthViewProperties dayGridMonth = getCalendarViewProperties(CalendarViewType.DAY_GRID_MONTH);
        dayGridMonth.setDayPopoverFormat(messages.getMessage(PACKAGE, "dayGridMonthDayPopoverFormat"));
        dayGridMonth.setDayHeaderFormat(messages.getMessage(PACKAGE, "dayGridMonthDayHeaderFormat"));
        dayGridMonth.setWeekNumberFormat(messages.getMessage(PACKAGE, "dayGridMonthWeekNumberFormat"));
        dayGridMonth.setEventTimeFormat(messages.getMessage(PACKAGE, "dayGridMonthEventTimeFormat"));

        DayGridYearViewProperties dayGridYear = getCalendarViewProperties(CalendarViewType.DAY_GRID_YEAR);
        dayGridYear.setDayPopoverFormat(messages.getMessage(PACKAGE, "dayGridYearDayPopoverFormat"));
        dayGridYear.setDayHeaderFormat(messages.getMessage(PACKAGE, "dayGridYearDayHeaderFormat"));
        dayGridYear.setWeekNumberFormat(messages.getMessage(PACKAGE, "dayGridYearWeekNumberFormat"));
        dayGridYear.setEventTimeFormat(messages.getMessage(PACKAGE, "dayGridYearEventTimeFormat"));
        dayGridYear.setMonthStartFormat(messages.getMessage(PACKAGE, "dayGridYearMonthStartFormat"));
    }

    protected void setupTimeGridLocalizedFormats() {
        TimeGridDayViewProperties timeGridDay = getCalendarViewProperties(CalendarViewType.TIME_GRID_DAY);
        timeGridDay.setDayPopoverFormat(messages.getMessage(PACKAGE, "timeGridDayDayPopoverFormat"));
        timeGridDay.setDayHeaderFormat(messages.getMessage(PACKAGE, "timeGridDayDayHeaderFormat"));
        timeGridDay.setWeekNumberFormat(messages.getMessage(PACKAGE, "timeGridDayWeekNumberFormat"));
        timeGridDay.setEventTimeFormat(messages.getMessage(PACKAGE, "timeGridDayEventTimeFormat"));
        timeGridDay.setSlotLabelFormat(messages.getMessage(PACKAGE, "timeGridDaySlotLabelFormat"));

        TimeGridWeekViewProperties timeGridWeek = getCalendarViewProperties(CalendarViewType.TIME_GRID_WEEK);
        timeGridWeek.setDayPopoverFormat(messages.getMessage(PACKAGE, "timeGridWeekDayPopoverFormat"));
        timeGridWeek.setDayHeaderFormat(messages.getMessage(PACKAGE, "timeGridWeekDayHeaderFormat"));
        timeGridWeek.setWeekNumberFormat(messages.getMessage(PACKAGE, "timeGridWeekWeekNumberFormat"));
        timeGridWeek.setEventTimeFormat(messages.getMessage(PACKAGE, "timeGridWeekEventTimeFormat"));
        timeGridWeek.setSlotLabelFormat(messages.getMessage(PACKAGE, "timeGridWeekSlotLabelFormat"));
    }

    protected void setupListLocalizedFormats() {
        ListDayViewProperties listDay = getCalendarViewProperties(CalendarViewType.LIST_DAY);
        listDay.setListDayFormat(messages.getMessage(PACKAGE, "listDayListDayFormat"));
        listDay.setListDaySideFormat(messages.getMessage(PACKAGE, "listDayListDaySideFormat"));

        ListWeekViewProperties listWeek = getCalendarViewProperties(CalendarViewType.LIST_WEEK);
        listWeek.setListDayFormat(messages.getMessage(PACKAGE, "listWeekListDayFormat"));
        listWeek.setListDaySideFormat(messages.getMessage(PACKAGE, "listWeekListDaySideFormat"));

        ListMonthViewProperties listMonth = getCalendarViewProperties(CalendarViewType.LIST_MONTH);
        listMonth.setListDayFormat(messages.getMessage(PACKAGE, "listMonthListDayFormat"));
        listMonth.setListDaySideFormat(messages.getMessage(PACKAGE, "listMonthListDaySideFormat"));

        ListYearViewProperties listYear = getCalendarViewProperties(CalendarViewType.LIST_YEAR);
        listYear.setListDayFormat(messages.getMessage(PACKAGE, "listYearListDayFormat"));
        listYear.setListDaySideFormat(messages.getMessage(PACKAGE, "listYearListDaySideFormat"));
    }

    protected void setupLocalizedFormats() {
        fullCalendar.setDayPopoverFormat(messages.getMessage(PACKAGE, "dayPopoverFormat"));
        fullCalendar.setDayHeaderFormat(messages.getMessage(PACKAGE, "dayHeaderFormat"));
        fullCalendar.setWeekNumberFormat(messages.getMessage(PACKAGE, "weekNumberFormat"));
        fullCalendar.setSlotNumberFormat(messages.getMessage(PACKAGE, "slotLabelFormat"));
        fullCalendar.setEventTimeFormat(messages.getMessage(PACKAGE, "eventTimeFormat"));
    }

    protected <T extends AbstractCalendarViewProperties> T getCalendarViewProperties(CalendarViewType viewType) {
        return Objects.requireNonNull(fullCalendar.getCalendarViewProperties(viewType));
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

    protected Optional<String> getOptMessage(String key) {
        String message = messages.getMessage(PACKAGE, key);
        return Optional.ofNullable(message.equals(key) ? null : message);
    }

    protected Optional<List<String>> getListMessage(String key) {
        String message = getOptMessage(key).orElse(null);
        if (message != null) {
            List<String> messages = Splitter.on(",")
                    .trimResults()
                    .omitEmptyStrings()
                    .splitToList(message);
            return Optional.of(messages);
        }
        return Optional.empty();
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
        FullCalendarI18n i18n = new FullCalendarI18n();

        getOptMessage("i18n.direction")
                .ifPresent(d -> i18n.setDirection(FullCalendarI18n.Direction.valueOf(d.toUpperCase())));
        getOptMessage("i18n.dayOfWeek").ifPresent(d -> i18n.setDayOfWeek(Integer.parseInt(d)));
        getOptMessage("i18n.dayOfYear").ifPresent(d -> i18n.setDayOfYear(Integer.parseInt(d)));

        getOptMessage("i18n.weekTextLong").ifPresent(i18n::setWeekTextLong);
        getOptMessage("i18n.allDayText").ifPresent(i18n::setAllDayText);
        getOptMessage("i18n.moreLinkText").ifPresent(i18n::setMoreLinkText);
        getOptMessage("i18n.noEventsText").ifPresent(i18n::setNoEventsText);
        getOptMessage("i18n.closeHint").ifPresent(i18n::setCloseHint);
        getOptMessage("i18n.eventHint").ifPresent(i18n::setEventHint);
        getOptMessage("i18n.timeHint").ifPresent(i18n::setTimeHint);
        getOptMessage("i18n.navLinkHint").ifPresent(i18n::setNavLinkHint);
        getOptMessage("i18n.moreLinkHint").ifPresent(i18n::setMoreLinkHint);
        return i18n;
    }

    protected JsonObject createCalendarLocalizedUnitNamesJson() {
        JsonObject result = jsonFactory.createObject();
        FullCalendarSerializer serializer = fullCalendar.getSerializer();
        getListMessage("months").ifPresent(s -> result.put("months", serializer.toJsonArrayFromString(s)));
        getListMessage("monthsShort").ifPresent(s -> result.put("monthsShort", serializer.toJsonArrayFromString(s)));
        getListMessage("weekdays").ifPresent(s -> result.put("weekdays", serializer.toJsonArrayFromString(s)));
        getListMessage("weekdaysShort").ifPresent(s -> result.put("weekdaysShort", serializer.toJsonArrayFromString(s)));
        getListMessage("weekdaysMin").ifPresent(s -> result.put("weekdaysMin", serializer.toJsonArrayFromString(s)));
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

    private ZoneId getComponentZoneId() {
        return fullCalendar.getTimeZone().toZoneId();
    }
}
