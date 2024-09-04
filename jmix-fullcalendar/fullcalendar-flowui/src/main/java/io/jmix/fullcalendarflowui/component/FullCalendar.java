package io.jmix.fullcalendarflowui.component;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendar.Display;
import io.jmix.fullcalendarflowui.component.data.*;
import io.jmix.fullcalendarflowui.component.event.*;
import io.jmix.fullcalendarflowui.component.event.AbstractEventMoveEvent;
import io.jmix.fullcalendarflowui.component.event.AbstractEventMoveEvent.RelatedEventProviderContext;
import io.jmix.fullcalendarflowui.component.event.MoreLinkClickEvent.EventProviderContext;
import io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours;
import io.jmix.fullcalendarflowui.component.model.option.FullCalendarOptions;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.event.dom.*;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import io.jmix.fullcalendarflowui.kit.component.model.dom.*;
import io.jmix.fullcalendarflowui.kit.component.serialization.JmixFullCalendarSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

import static io.jmix.fullcalendarflowui.kit.component.CalendarDateTimeUtils.*;
import static io.jmix.fullcalendarflowui.kit.component.CalendarDateTimeUtils.parseAndTransform;

/**
 * UI component for visualizing events in a calendar using various views (month, week, etc.).
 * <p>
 * Component provides event rendering, drag-and-drop functionality, event editing, and customizable views.
 */
public class FullCalendar extends JmixFullCalendar implements ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(FullCalendar.class);

    protected ApplicationContext applicationContext;
    protected CurrentAuthentication currentAuthentication;
    protected Messages messages;

    protected JsonFactory jsonFactory;
    protected Map<String, AbstractEventProviderManager> eventProvidersMap = new HashMap<>(2);

    protected Function<MoreLinkClassNamesContext, List<String>> linkMoreClassNamesGenerator;
    protected Function<DayHeaderClassNamesContext, List<String>> dayHeaderClassNamesGenerator;
    protected Function<DayCellClassNamesContext, List<String>> dayCellClassNamesGenerator;
    protected Function<SlotLabelClassNamesContext, List<String>> slotLabelClassNamesGenerator;
    protected Function<NowIndicatorClassNamesContext, List<String>> nowIndicatorClassNamesGenerator;

    protected Object eventConstraintGroupId;
    protected Object selectConstraintGroupId;

    protected FullCalendarI18n defaultI18n;
    protected FullCalendarI18n explicitI18n;

    @Override
    public void afterPropertiesSet() {
        autoWireDependencies();
        initComponent();
    }

    protected void autoWireDependencies() {
        currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
        messages = applicationContext.getBean(Messages.class);
    }

    protected void initComponent() {
        jsonFactory = createJsonFactory();
        defaultI18n = createDefaultI18n();

        setupLocalization();

        initTimeZone();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * @return a list of event providers
     */
    public List<BaseCalendarEventProvider> getEventProviders() {
        return eventProvidersMap != null && !eventProvidersMap.isEmpty()
                ? eventProvidersMap.values().stream().map(AbstractEventProviderManager::getEventProvider).toList()
                : Collections.emptyList();
    }

    /**
     * Returns an event provider by its ID.
     *
     * @param id  event provider ID
     * @param <T> type of event provider
     * @return event provider or {@code null} if there is no event provider with passed ID
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BaseCalendarEventProvider> T getEventProvider(String id) {
        Preconditions.checkNotEmptyString(id);

        AbstractEventProviderManager eventProviderManager = eventProvidersMap.get(id);
        if (eventProviderManager != null) {
            return (T) eventProviderManager.getEventProvider();
        }
        return null;
    }

    /**
     * Adds new lazy event provider.
     *
     * @param eventProvider lazy event provider to add
     */
    public void addEventProvider(LazyCalendarEventProvider eventProvider) {
        Preconditions.checkNotNullArgument(eventProvider);

        if (eventProvidersMap.containsKey(eventProvider.getId())) {
            log.warn("Lazy event provider with the same '{}' ID already added", eventProvider.getId());
            return;
        }

        LazyEventProviderManager eventProviderManager = createLazyEventProviderManager(eventProvider);

        eventProvidersMap.put(eventProvider.getId(), eventProviderManager);

        if (initialized) {
            addEventProviderInternal(eventProviderManager);
        }
    }

    /**
     * Adds new event provider.
     *
     * @param eventProvider event provider to add
     */
    public void addEventProvider(CalendarEventProvider eventProvider) {
        Preconditions.checkNotNullArgument(eventProvider);

        if (eventProvidersMap.containsKey(eventProvider.getId())) {
            log.warn("Item event provider with the same '{}' ID already added", eventProvider.getId());
            return;
        }

        EventProviderManager eventProviderManager = createEventProviderManager(eventProvider);
        eventProviderManager.setItemSetChangeListener(this::onItemSetChangeListener);

        eventProvidersMap.put(eventProvider.getId(), eventProviderManager);

        if (initialized) {
            addEventProviderInternal(eventProviderManager);

            if (eventProvider.getItems().isEmpty()) {
                requestUpdateItemEventProvider(eventProvider.getId());
            }
        }
    }

    /**
     * Removes an event provider from component.
     *
     * @param eventProvider event provider to remove
     */
    public void removeEventProvider(BaseCalendarEventProvider eventProvider) {
        Preconditions.checkNotNullArgument(eventProvider);

        removeEventProvider(eventProvider.getId());
    }

    /**
     * Removes an event provider from component by ID.
     *
     * @param id ID of event provider to remove
     */
    public void removeEventProvider(String id) {
        Preconditions.checkNotEmptyString(id);

        AbstractEventProviderManager epManager = eventProvidersMap.get(id);
        if (epManager != null) {
            if (epManager instanceof EventProviderManager itemProvider) {
                itemProvider.setItemSetChangeListener(null);
            }
            getElement().callJsFunction("_removeEventSource", epManager.getSourceId());
        }
        eventProvidersMap.remove(id);
    }

    /**
     * Removes all event providers from component.
     */
    public void removeAllEventProviders() {
        getEventProviders().forEach(this::removeEventProvider);
    }

    /**
     * @return {@code true} if business hours used as event constraint
     */
    public boolean isEventConstraintBusinessHoursEnabled() {
        return getOptions().getEventConstraint().isBusinessHoursEnabled();
    }

    /**
     * Sets whether events being dragged or resized must be fully contained within the week’s business hours.
     * <p>
     * It also respects custom business hours {@link #setBusinessHours(List)}.
     * <p>
     * The default value is {@code false}.
     *
     * @param enabled whether to use business hours as event constraint
     */
    public void setEventConstraintBusinessHoursEnabled(boolean enabled) {
        getOptions().getEventConstraint().setBusinessHoursEnabled(enabled);
    }

    /**
     * @return a group ID that limits dragging and resizing events or {@code null} if not set
     */
    @Nullable
    public Object getEventConstraintGroupId() {
        return eventConstraintGroupId;
    }

    /**
     * Sets a group ID to limit the dragging and resizing of events. Events that are being dragged or resized
     * must be fully contained by at least one of the events linked to by the given group ID.
     * <p>
     * Takes precedence over {@link #setEventConstraintBusinessHoursEnabled(boolean)}.
     * <p>
     * The group ID can be an entity instance, string, enum or other types.
     *
     * @param groupId a group ID
     */
    public void setEventConstraintGroupId(@Nullable Object groupId) {
        this.eventConstraintGroupId = groupId;

        String serializedGroupId = getSerializer().serializeGroupIdOrConstraint(groupId);

        getOptions().getEventConstraint().setGroupId(serializedGroupId);
    }

    /**
     * @return a list of business hours that limits the dragging and resizing of events
     */
    public List<CalendarBusinessHours> getEventConstraintBusinessHours() {
        return getOptions().getEventConstraint().getBusinessHours();
    }

    /**
     * Sets a list of business hours that limits the dragging and resizing of events.
     * <p>
     * Takes precedence over {@link FullCalendar#setEventConstraintGroupId(Object)} and
     * {@link #setEventConstraintBusinessHoursEnabled(boolean)}
     *
     * @param businessHours business hours that will be available for event dragging and resizing
     */
    public void setEventConstraintBusinessHours(@Nullable List<CalendarBusinessHours> businessHours) {
        getOptions().getEventConstraint().setBusinessHours(businessHours);
    }

    /**
     * @return a group ID to limit selection or {@code null} if not set
     */
    @Nullable
    public Object getSelectConstraintGroupId() {
        return selectConstraintGroupId;
    }

    /**
     * @return {@code true} is business hours used as selection constraint
     */
    public boolean isSelectConstraintBusinessHoursEnabled() {
        return getOptions().getSelectConstraint().isBusinessHoursEnabled();
    }

    /**
     * Sets whether the selection must be fully contained within the week’s business hours.
     * <p>
     * It also respects custom business hours {@link #setBusinessHours(List)}.
     * <p>
     * The default value is {@code false}.
     *
     * @param enabled whether to use business hours as selection constraint
     */
    public void setSelectConstraintBusinessHoursEnabled(boolean enabled) {
        getOptions().getSelectConstraint().setBusinessHoursEnabled(enabled);
    }

    /**
     * Sets a group ID to limit selection. If group ID is set, only cells with events that contain the
     * same group ID can be selected.
     * <p>
     * Takes precedence over {@link #setSelectConstraintBusinessHoursEnabled(boolean)}.
     * <p>
     * Note, this property will be applied if {@link FullCalendar#setSelectionEnabled(boolean)} is enabled.
     *
     * @param groupId a group ID
     */
    public void setSelectConstraintGroupId(@Nullable Object groupId) {
        this.selectConstraintGroupId = groupId;

        String serializedGroupId = getSerializer().serializeGroupIdOrConstraint(groupId);

        getOptions().getSelectConstraint().setGroupId(serializedGroupId);
    }

    /**
     * @return a list of business hours that limits the selection
     */
    public List<CalendarBusinessHours> getSelectConstraintBusinessHours() {
        return getOptions().getSelectConstraint().getBusinessHours();
    }

    /**
     * Sets a list of business hours that limits the selection.
     * <p>
     * Takes precedence over {@link #setSelectConstraintBusinessHoursEnabled(boolean)} and
     * {@link #setSelectConstraintGroupId(Object)}.
     *
     * @param businessHours business hours that will be available for selection
     */
    public void setSelectConstraintBusinessHours(@Nullable List<CalendarBusinessHours> businessHours) {
        getOptions().getSelectConstraint().setBusinessHours(businessHours);
    }

    /**
     * @return {@code true} if default business hours enabled
     */
    public boolean isDefaultBusinessHoursEnabled() {
        return getOptions().getBusinessHours().isEnabled();
    }

    /**
     * Enables default business hours. The default business hour is: [monday-friday] from 9 AM to 5 PM.
     * <p>
     * Disabled by default.
     *
     * @param enabled whether to enable business hours
     */
    public void setDefaultBusinessHoursEnabled(boolean enabled) {
        getOptions().getBusinessHours().setEnabled(enabled);
    }

    /**
     * @return a list of business hours entries
     */
    public List<CalendarBusinessHours> getBusinessHours() {
        return getOptions().getBusinessHours().getBusinessHours();
    }

    /**
     * Sets a list of business hours entries.
     * <p>
     * Takes precedence over {@link #setDefaultBusinessHoursEnabled(boolean)}.
     *
     * @param businessHours list of custom entries of business hours
     */
    public void setBusinessHours(@Nullable List<CalendarBusinessHours> businessHours) {
        getOptions().getBusinessHours().setBusinessHours(businessHours);
    }

    /**
     * @return hidden days of week
     */
    public List<DayOfWeek> getHiddenDays() {
        List<DayOfWeek> hiddenDays = getOptions().getHiddenDays().getValue();
        return hiddenDays == null ? Collections.emptyList() : hiddenDays;
    }

    /**
     * Sets the list of days that will be excluded from being displayed.
     * <p>
     * By default, all days of week are visible unless {@link #setWeekendsVisible(boolean)} is set to {@code false}.
     *
     * @param hiddenDays days to hide
     */
    public void setHiddenDays(@Nullable List<DayOfWeek> hiddenDays) {
        getOptions().getHiddenDays().setValue(hiddenDays);
    }

    /**
     * @return the internationalization properties for this component or {@code null} if not set
     */
    @Nullable
    public FullCalendarI18n getI18n() {
        return explicitI18n;
    }

    /**
     * Set the internationalization properties for this component. The {@code null} value resets to default values.
     *
     * @param i18n the internationalized properties
     */
    public void setI18n(@Nullable FullCalendarI18n i18n) {
        this.explicitI18n = i18n;

        setI18nInternal(defaultI18n.combine(explicitI18n));
    }

    /**
     * @return first day of week or {@code null} if not set
     */
    @Nullable
    public DayOfWeek getFirstDayOfWeek() {
        return getOptions().getFirstDay().getValue();
    }

    /**
     * Sets the day that each week begins. The default value is taken from locale.
     *
     * @param firstDay first day of week
     */
    public void setFirstDayOfWeek(@Nullable DayOfWeek firstDay) {
        getOptions().getFirstDay().setValue(firstDay);
    }

    /**
     * @return display mode for events or {@code null} if not set
     */
    @Nullable
    public Display getEventDisplay() {
        return getOptions().getEventDisplay().getValue();
    }

    /**
     * Sets the display mode that controls the appearance of all events that do not specify display mode.
     * <p>
     * To control the display of specific events, use the display property of calendar event
     * {@link CalendarEvent#getDisplay()}.
     *
     * @param display display mode for events
     */
    public void setEventDisplay(@Nullable Display display) {
        getOptions().getEventDisplay().setValue(display);
    }

    /**
     * Adds dates set listener. The event is fired after the calendar’s date range has been initially set
     * or changed in some way and the DOM of component has been updated.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     * @see DatesSetEvent
     */
    public Registration addDatesSetListener(ComponentEventListener<DatesSetEvent> listener) {
        Preconditions.checkNotNullArgument(listener);

        return getEventBus().addListener(DatesSetEvent.class, listener);
    }

    /**
     * Adds a "more" link click listener. When listener is added, the {@link #setMoreLinkCalendarView(CalendarView)}
     * value will be ignored.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addMoreLinkClickListener(ComponentEventListener<MoreLinkClickEvent> listener) {
        Preconditions.checkNotNullArgument(listener);

        Registration registration = getEventBus().addListener(MoreLinkClickEvent.class, listener);

        options.getMoreLinkClick().setFunctionEnabled(true);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(MoreLinkClickEvent.class)) {
                options.getMoreLinkClick().setFunctionEnabled(false);
            }
        };
    }

    /**
     * Adds an event click listener.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addEventClickListener(ComponentEventListener<EventClickEvent> listener) {
        if (!getEventBus().hasListener(EventClickEvent.class)) {
            attachEventClickDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventClickEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventClickEvent.class)) {
                detachEventClickDomEventListener();
            }
        };
    }

    /**
     * Adds a listener that is invoked when the user mouses over a calendar event.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addEventMouseEnterListener(ComponentEventListener<EventMouseEnterEvent> listener) {
        if (!getEventBus().hasListener(EventMouseEnterEvent.class)) {
            attachEventMouseEnterDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventMouseEnterEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventMouseEnterEvent.class)) {
                detachEventMouseEnterDomEventListener();
            }
        };
    }

    /**
     * Adds a listener that is invoked when the user mouses out of a calendar event.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addEventMouseLeaveListener(ComponentEventListener<EventMouseLeaveEvent> listener) {
        if (!getEventBus().hasListener(EventMouseLeaveEvent.class)) {
            attachEventMouseLeaveDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventMouseLeaveEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventMouseLeaveEvent.class)) {
                detachEventMouseLeaveDomEventListener();
            }
        };
    }

    /**
     * Adds a listener that is invoked when dragging stops and the event has moved to a different day/time cell.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addEventDropListener(ComponentEventListener<EventDropEvent> listener) {
        if (!getEventBus().hasListener(EventDropEvent.class)) {
            attachEventDropDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventDropEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventDropEvent.class)) {
                detachEventDropDomEventListener();
            }
        };
    }

    /**
     * Adds a listener that is invoked when resizing stops and the calendar event has changed in duration.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addEventResizeListener(ComponentEventListener<EventResizeEvent> listener) {
        if (!getEventBus().hasListener(EventResizeEvent.class)) {
            attachEventResizeDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventResizeEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventResizeEvent.class)) {
                detachEventResizeDomEventListener();
            }
        };
    }

    /**
     * Adds a listener that is invoked when day cell or time cell is clicked.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addDateClickListener(ComponentEventListener<DateClickEvent> listener) {
        if (!getEventBus().hasListener(DateClickEvent.class)) {
            attachDateClickDomEventListener();
        }

        Registration registration = getEventBus().addListener(DateClickEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(DateClickEvent.class)) {
                detachDateClickDomEventListener();
            }
        };
    }

    /**
     * Adds a listener that is invoked when a date/time selection is made.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addSelectListener(ComponentEventListener<SelectEvent> listener) {
        if (!getEventBus().hasListener(SelectEvent.class)) {
            attachSelectDomEventListener();
        }

        Registration registration = getEventBus().addListener(SelectEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(SelectEvent.class)) {
                detachSelectDomEventListener();
            }
        };
    }

    /**
     * Adds a listener thaat is invoked when the current selection is cleared.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     * @see UnselectEvent
     */
    public Registration addUnselectListener(ComponentEventListener<UnselectEvent> listener) {
        if (!getEventBus().hasListener(UnselectEvent.class)) {
            attachUnselectDomEventListener();
        }

        Registration registration = getEventBus().addListener(UnselectEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(UnselectEvent.class)) {
                detachUnselectDomEventListener();
            }
        };
    }

    /**
     * @return a class names generator for "more" link or {@code null} if not set
     */
    @Nullable
    public Function<MoreLinkClassNamesContext, List<String>> getMoreLinkClassNamesGenerator() {
        return linkMoreClassNamesGenerator;
    }

    /**
     * Sets a class names generator for "more" link.
     * <p>
     * Note, generator has a precedence over a {@link #setMoreLinkClassNames(List)} and other
     * "add class name" methods.
     *
     * @param classNamesGenerator the generator to set
     */
    public void setMoreLinkClassNamesGenerator(
            @Nullable Function<MoreLinkClassNamesContext, List<String>> classNamesGenerator) {
        this.linkMoreClassNamesGenerator = classNamesGenerator;

        options.getMoreLinkClassNames().setFunctionEnabled(classNamesGenerator != null);
    }

    /**
     * @return a class names generator for day headers or {@code null} if not set
     */
    @Nullable
    public Function<DayHeaderClassNamesContext, List<String>> getDayHeaderClassNamesGenerator() {
        return dayHeaderClassNamesGenerator;
    }

    /**
     * Sets a class names generator for day headers. The day header is a cell that shows day of week and date in
     * some views.
     *
     * @param classNamesGenerator the generator to set
     */
    public void setDayHeaderClassNamesGenerator(
            @Nullable Function<DayHeaderClassNamesContext, List<String>> classNamesGenerator) {
        this.dayHeaderClassNamesGenerator = classNamesGenerator;

        options.getDayHeaderClassNames().setValue(classNamesGenerator != null);
    }

    /**
     * @return a day cell class names generator or {@code null} if not set
     */
    @Nullable
    public Function<DayCellClassNamesContext, List<String>> getDayCellClassNamesGenerator() {
        return dayCellClassNamesGenerator;
    }

    /**
     * Sets a day cell class names generator. The day cell appears in day grid views and
     * in time grid views as an all-day cell.
     *
     * @param dayCellClassNamesGenerator the generator to set
     */
    public void setDayCellClassNamesGenerator(
            @Nullable Function<DayCellClassNamesContext, List<String>> dayCellClassNamesGenerator) {
        this.dayCellClassNamesGenerator = dayCellClassNamesGenerator;

        options.getDayCellClassNames().setValue(dayCellClassNamesGenerator != null);
    }

    /**
     * @return slot label class names generator or {@code null} if not set
     */
    @Nullable
    public Function<SlotLabelClassNamesContext, List<String>> getSlotLabelClassNamesGenerator() {
        return slotLabelClassNamesGenerator;
    }

    /**
     * Sets a slot label class names generator. The slot label appears in time grid views. It is a cell with time label.
     *
     * @param slotLabelClassNamesGenerator the generator to set
     */
    public void setSlotLabelClassNamesGenerator(
            @Nullable Function<SlotLabelClassNamesContext, List<String>> slotLabelClassNamesGenerator) {
        this.slotLabelClassNamesGenerator = slotLabelClassNamesGenerator;

        options.getSlotLabelClassNames().setValue(slotLabelClassNamesGenerator != null);
    }

    /**
     * @return a now-indicator class names generator or {@code null} if not set
     */
    @Nullable
    public Function<NowIndicatorClassNamesContext, List<String>> getNowIndicatorClassNamesGenerator() {
        return nowIndicatorClassNamesGenerator;
    }

    /**
     * Sets a now-indicator class names generator. The now-indicator contains of two part: line and axis.
     * <p>
     * The now-indicator can be enabled by {@link #setNowIndicatorVisible(boolean)}.
     *
     * @param nowIndicatorClassNamesGenerator the generator to set
     */
    public void setNowIndicatorClassNamesGenerator(
            @Nullable Function<NowIndicatorClassNamesContext, List<String>> nowIndicatorClassNamesGenerator) {
        this.nowIndicatorClassNamesGenerator = nowIndicatorClassNamesGenerator;

        options.getNowIndicatorClassNames().setValue(nowIndicatorClassNamesGenerator != null);
    }

    @Override
    protected FullCalendarOptions createOptions() {
        return new FullCalendarOptions();
    }

    @Override
    protected JmixFullCalendarSerializer createSerializer() {
        return new FullCalendarSerializer();
    }

    protected FullCalendarOptions getOptions() {
        return (FullCalendarOptions) options;
    }

    protected FullCalendarSerializer getSerializer() {
        return (FullCalendarSerializer) serializer;
    }

    protected void setupLocalization() {
        setupDefaultLocalizedFormats();

        setupDayGridLocalizedFormats();
        setupTimeGridLocalizedFormats();
        setupListLocalizedFormats();
        setupMultiLocalizedFormats();

        setI18nInternal(defaultI18n);

        setCalendarUnitNames();
    }

    protected void setI18nInternal(FullCalendarI18n i18n) {
        JsonObject json = getSerializer().serializeObject(i18n);

        json.put("locale", getSerializer().serializeValue(currentAuthentication.getLocale()));

        getElement().setPropertyJson("i18n", json);
    }

    protected void setCalendarUnitNames() {
        JsonObject json = createCalendarLocalizedUnitNamesJson();

        json.put("locale", getSerializer().serializeValue(currentAuthentication.getLocale()));

        getElement().callJsFunction("_defineMomentJsLocale", json);
    }

    @Override
    protected JsonArray fetchCalendarItems(String sourceId, String start, String end) {
        LazyEventProviderManager epManager = (LazyEventProviderManager) getEventProviderManager(sourceId);

        return epManager.fetchAndSerialize(
                new LazyCalendarEventProvider.ItemsFetchContext(
                        epManager.getEventProvider(),
                        parseIsoDate(start),
                        parseIsoDate(end),
                        getComponentTimeZone()));
    }

    protected void addEventProviderInternal(AbstractEventProviderManager epManager) {
        log.debug("Perform add event provider");

        getElement().callJsFunction(epManager.getJsFunctionName(), epManager.getSourceId());
    }

    protected void onItemSetChangeListener(CalendarEventProvider.ItemSetChangeEvent event) {
        String providerId = event.getSource().getId();
        EventProviderManager eventProviderManager =
                (EventProviderManager) eventProvidersMap.get(providerId);

        switch (event.getOperation()) {
            case ADD, REMOVE, UPDATE -> {
                eventProviderManager.addIncrementalChange(event);
                requestIncrementalDataUpdate();
            }
            default -> requestUpdateItemEventProvider(providerId);
        }
    }

    protected void requestIncrementalDataUpdate() {
        if (incrementalUpdateExecution != null) {
            return;
        }

        getUI().ifPresent(ui -> incrementalUpdateExecution =
                ui.beforeClientResponse(this, this::performIncrementalDataUpdate));
    }

    protected void performIncrementalDataUpdate(ExecutionContext context) {
        List<EventProviderManager> eventProviders = eventProvidersMap.values().stream()
                .filter(ep -> ep instanceof EventProviderManager)
                .map(ep -> (EventProviderManager) ep)
                .toList();

        List<JsonValue> jsonValues = new ArrayList<>();
        for (EventProviderManager epManger : eventProviders) {
            jsonValues.addAll(epManger.serializeIncrementalData());
            epManger.clearIncrementalData();
        }

        getElement().callJsFunction("_updateSourcesWithIncrementalData",
                getSerializer().toJsonArrayJson(jsonValues));

        incrementalUpdateExecution = null;
    }

    protected void requestUpdateItemEventProvider(String eventProviderId) {
        // Do not call if it's still updating
        if (itemsEventProvidersExecutionMap.containsKey(eventProviderId)) {
            return;
        }
        getUI().ifPresent(ui -> {
            StateTree.ExecutionRegistration executionRegistration = ui.beforeClientResponse(this,
                    (context) -> performUpdateItemEventProvider(eventProviderId));
            itemsEventProvidersExecutionMap.put(eventProviderId, executionRegistration);
        });
    }

    protected void performUpdateItemEventProvider(String eventProviderId) {
        JsonObject resultJson = new JreJsonFactory().createObject();

        EventProviderManager eventProviderManager =
                (EventProviderManager) eventProvidersMap.get(eventProviderId);

        JsonValue dataJson = eventProviderManager.serializeData();

        resultJson.put("data", dataJson);
        resultJson.put("sourceId", eventProviderManager.getSourceId());

        getElement().callJsFunction("_updateSyncSourcesData", resultJson);

        itemsEventProvidersExecutionMap.remove(eventProviderId);
    }

    @Override
    protected void onDatesSet(DatesSetDomEvent event) {
        super.onDatesSet(event);

        DomDatesSet domDatesSet = deserializer.deserialize(event.getContext(), DomDatesSet.class);

        getEventBus().fireEvent(
                new DatesSetEvent(this, event.isFromClient(),
                        parseIsoDate(domDatesSet.getStartDate()),
                        parseIsoDate(domDatesSet.getEndDate()),
                        createViewInfo(domDatesSet.getView())));
    }

    @Override
    protected void onMoreLinkClick(MoreLinkClickDomEvent event) {
        DomMoreLinkClick clientContext = deserializer.deserialize(event.getContext(), DomMoreLinkClick.class);

        List<EventProviderContext> eventProviderContexts = createMoreLinkEventProviderContexts(clientContext);

        List<CalendarEvent> hiddenCalendarEvents = toCalendarEvents(clientContext.getHiddenEvents());
        List<CalendarEvent> visibleCalendarEvents = toCalendarEvents(clientContext.getAllEvents());
        visibleCalendarEvents.removeAll(hiddenCalendarEvents);

        getEventBus().fireEvent(
                new MoreLinkClickEvent(this, event.isFromClient(),
                        clientContext.isAllDay(),
                        toLocalDateTime(clientContext.getDateTime()),
                        visibleCalendarEvents,
                        hiddenCalendarEvents,
                        eventProviderContexts,
                        new MouseEventDetails(clientContext.getMouseDetails()),
                        createViewInfo(clientContext.getView()))
        );
    }

    @Override
    protected void onEventClick(EventClickDomEvent event) {
        DomEventMouse clientContext = deserializer.deserialize(event.getContext(), DomEventMouse.class);

        AbstractEventProviderManager eventProviderManager = getEventProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), eventProviderManager);

        getEventBus().fireEvent(
                new EventClickEvent(this, event.isFromClient(),
                        new MouseEventDetails(clientContext.getMouseDetails()),
                        calendarEvent,
                        eventProviderManager.getEventProvider(),
                        createViewInfo(clientContext.getView()))
        );
    }

    @Override
    protected void onEventMouseEnter(EventMouseEnterDomEvent event) {
        DomEventMouse clientContext = deserializer.deserialize(event.getContext(), DomEventMouse.class);

        AbstractEventProviderManager eventProviderManager = getEventProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), eventProviderManager);

        getEventBus().fireEvent(
                new EventMouseEnterEvent(this, event.isFromClient(),
                        new MouseEventDetails(clientContext.getMouseDetails()),
                        calendarEvent,
                        eventProviderManager.getEventProvider(),
                        createViewInfo(clientContext.getView())));
    }

    @Override
    protected void onEventMouseLeave(EventMouseLeaveDomEvent event) {
        DomEventMouse clientContext = deserializer.deserialize(event.getContext(), DomEventMouse.class);

        AbstractEventProviderManager eventProviderManager = getEventProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), eventProviderManager);

        getEventBus().fireEvent(
                new EventMouseLeaveEvent(this, event.isFromClient(),
                        new MouseEventDetails(clientContext.getMouseDetails()),
                        calendarEvent,
                        eventProviderManager.getEventProvider(),
                        createViewInfo(clientContext.getView())));
    }

    @Override
    protected void onEventDrop(EventDropDomEvent event) {
        DomEventDrop clientEvent = deserializer.deserialize(event.getContext(), DomEventDrop.class);

        AbstractEventProviderManager epManager = getEventProviderManager(clientEvent.getEvent().getSourceId());

        CalendarEvent droppedEvent = getCalendarEvent(clientEvent.getEvent(), epManager);
        applyChangesToCalendarEvent(droppedEvent, clientEvent.getEvent());

        getEventBus().fireEvent(
                new EventDropEvent(this, event.isFromClient(),
                        droppedEvent,
                        epManager.getEventProvider(),
                        getRelatedEventProviderContexts(clientEvent.getRelatedEvents()),
                        getRelatedEvents(clientEvent.getRelatedEvents()),
                        createOldValues(clientEvent.getOldEvent()),
                        createDelta(clientEvent.getDelta()),
                        new MouseEventDetails(clientEvent.getMouseDetails()),
                        createViewInfo(clientEvent.getView()))
        );
    }

    @Override
    protected void onEventResize(EventResizeDomEvent event) {
        DomEventResize clientEvent = deserializer.deserialize(event.getContext(), DomEventResize.class);

        AbstractEventProviderManager epManager = getEventProviderManager(clientEvent.getEvent().getSourceId());

        CalendarEvent resizedEvent = getCalendarEvent(clientEvent.getEvent(), epManager);
        applyChangesToCalendarEvent(resizedEvent, clientEvent.getEvent());

        getEventBus().fireEvent(
                new EventResizeEvent(this, event.isFromClient(),
                        resizedEvent,
                        epManager.getEventProvider(),
                        getRelatedEventProviderContexts(clientEvent.getRelatedEvents()),
                        getRelatedEvents(clientEvent.getRelatedEvents()),
                        createOldValues(clientEvent.getOldEvent()),
                        clientEvent.getStartDelta() == null ? null : createDelta(clientEvent.getStartDelta()),
                        clientEvent.getEndDelta() == null ? null : createDelta(clientEvent.getEndDelta()),
                        new MouseEventDetails(clientEvent.getMouseDetails()),
                        createViewInfo(clientEvent.getView())));
    }

    @Override
    protected void onDateClick(DateClickDomEvent event) {
        DomDateClick clientEvent = deserializer.deserialize(event.getContext(), DomDateClick.class);

        getEventBus().fireEvent(
                new DateClickEvent(this, event.isFromClient(),
                        new MouseEventDetails(clientEvent.getMouseDetails()),
                        toLocalDateTime(clientEvent.getDateTime()),
                        clientEvent.isAllDay(),
                        createViewInfo(clientEvent.getView())));
    }

    @Override
    protected void onSelect(SelectDomEvent event) {
        DomSelect clientEvent = deserializer.deserialize(event.getContext(), DomSelect.class);

        MouseEventDetails mouseEventDetails = clientEvent.getMouseDetails() != null
                ? new MouseEventDetails(clientEvent.getMouseDetails())
                : null;

        getEventBus().fireEvent(
                new SelectEvent(
                        this, event.isFromClient(),
                        mouseEventDetails,
                        toLocalDateTime(clientEvent.getStartDateTime()),
                        toLocalDateTime(clientEvent.getEndDateTime()),
                        clientEvent.isAllDay(),
                        createViewInfo(clientEvent.getView())));
    }

    @Override
    protected void onUnselect(UnselectDomEvent event) {
        DomUnselect clientEvent = deserializer.deserialize(event.getContext(), DomUnselect.class);

        MouseEventDetails mouseEventDetails = clientEvent.getMouseDetails() != null
                ? new MouseEventDetails(clientEvent.getMouseDetails())
                : null;

        getEventBus().fireEvent(
                new UnselectEvent(
                        this, event.isFromClient(),
                        createViewInfo(clientEvent.getView()),
                        mouseEventDetails));
    }

    @Override
    protected JsonArray getMoreLinkClassNames(JsonObject jsonContext) {
        if (getMoreLinkClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        DomMoreLinkClassNames clientContext = deserializer.deserialize(jsonContext, DomMoreLinkClassNames.class);

        List<String> classNames = getMoreLinkClassNamesGenerator().apply(
                new MoreLinkClassNamesContext(
                        this,
                        clientContext.getEventsCount(),
                        createViewInfo(clientContext.getView())));

        JsonArray classNamesJson = classNames == null
                ? jsonFactory.createArray()
                : getSerializer().toJsonArray(classNames);

        log.debug("Serialized 'MoreLinkClassNames': {}", classNamesJson.toJson());

        return classNamesJson;
    }

    @Override
    protected JsonArray getDayHeaderClassNames(JsonObject jsonContext) {
        if (getDayHeaderClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        DomDayHeaderClassNames clientContext = deserializer.deserialize(jsonContext, DomDayHeaderClassNames.class);

        List<String> classNames = getDayHeaderClassNamesGenerator().apply(
                new DayHeaderClassNamesContext(
                        this,
                        clientContext.getDate() == null ? null : parseIsoDate(clientContext.getDate()),
                        Objects.requireNonNull(DayOfWeek.fromId(clientContext.getDow())),
                        clientContext.isDisabled(),
                        clientContext.isFuture(),
                        clientContext.isOther(),
                        clientContext.isPast(),
                        clientContext.isToday(),
                        createViewInfo(clientContext.getView())));

        JsonArray classNamesJson = classNames == null
                ? jsonFactory.createArray()
                : getSerializer().toJsonArray(classNames);

        log.debug("Serialized day header's class names': {}", classNamesJson.toJson());

        return classNamesJson;
    }

    @Override
    protected JsonArray getDayCellClassNames(JsonObject jsonContext) {
        if (getDayCellClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        DomDayCellClassNames clientContext = deserializer.deserialize(jsonContext, DomDayCellClassNames.class);

        List<String> classNames = getDayCellClassNamesGenerator().apply(
                new DayCellClassNamesContext(
                        this,
                        parseIsoDate(clientContext.getDate()),
                        Objects.requireNonNull(DayOfWeek.fromId(clientContext.getDow())),
                        clientContext.isDisabled(),
                        clientContext.isFuture(),
                        clientContext.isOther(),
                        clientContext.isPast(),
                        clientContext.isToday(),
                        createViewInfo(clientContext.getView())));

        JsonArray classNamesJson = classNames == null
                ? jsonFactory.createArray()
                : getSerializer().toJsonArray(classNames);

        log.debug("Serialized day cell's class names: {}", classNamesJson.toJson());

        return classNamesJson;
    }

    @Override
    protected JsonArray getSlotLabelClassNames(JsonObject jsonContext) {
        if (getSlotLabelClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        DomSlotLabelClassNames clientContext = deserializer.deserialize(jsonContext, DomSlotLabelClassNames.class);

        List<String> classNames = getSlotLabelClassNamesGenerator().apply(
                new SlotLabelClassNamesContext(
                        this,
                        LocalTime.parse(clientContext.getTime()),
                        createViewInfo(clientContext.getView())));

        JsonArray classNamesJson = classNames == null
                ? jsonFactory.createArray()
                : getSerializer().toJsonArray(classNames);

        log.debug("Serialized slot labels' class names: {}", classNamesJson.toJson());

        return classNamesJson;
    }

    @Override
    protected JsonArray getNowIndicatorClassNames(JsonObject jsonContext) {
        if (getNowIndicatorClassNamesGenerator() == null) {
            return jsonFactory.createArray();
        }

        DomNowIndicatorClassNames clientContext =
                deserializer.deserialize(jsonContext, DomNowIndicatorClassNames.class);

        List<String> classNames = getNowIndicatorClassNamesGenerator().apply(
                new NowIndicatorClassNamesContext(
                        this,
                        clientContext.isAxis(),
                        toLocalDateTime(clientContext.getDateTime()),
                        createViewInfo(clientContext.getView())));

        JsonArray classNamesJson = classNames == null
                ? jsonFactory.createArray()
                : getSerializer().toJsonArray(classNames);

        log.debug("Serialized now-indicator's class names: {}", classNamesJson.toJson());

        return classNamesJson;
    }

    protected EventProviderManager createEventProviderManager(CalendarEventProvider eventProvider) {
        return new EventProviderManager(eventProvider, getSerializer(), this);
    }

    protected LazyEventProviderManager createLazyEventProviderManager(LazyCalendarEventProvider eventProvider) {
        return new LazyEventProviderManager(eventProvider, getSerializer(), this);
    }

    protected void initTimeZone() {
        TimeZone timeZone = applicationContext.getBean(CurrentAuthentication.class).getTimeZone();

        setTimeZone(timeZone);
    }

    protected JsonFactory createJsonFactory() {
        return new JreJsonFactory();
    }

    /**
     * Returns date-time as is from client without transformation
     *
     * @param isoDateTime string to parse
     * @return local date-time
     */
    protected LocalDateTime toLocalDateTime(String isoDateTime) {
        return parseIsoDateTime(isoDateTime, getComponentZoneId()).toLocalDateTime();
    }

    /**
     * Returns transformed date-time from component's time zone to system default ({@link TimeZone#getDefault()}).
     *
     * @param isoDateTime string to parse
     * @return transformed local date-time
     */
    protected LocalDateTime transformToLocalDateTime(String isoDateTime) {
        return parseAndTransform(isoDateTime, getComponentZoneId());
    }

    protected ZoneId getComponentZoneId() {
        return getComponentTimeZone().toZoneId();
    }

    protected TimeZone getComponentTimeZone() {
        TimeZone timeZone = getTimeZone();
        if (timeZone != null) {
            return timeZone;
        }
        TimeZone defaultTimeZone = getOptions().getTimeZone().getDefaultValue();
        if (defaultTimeZone != null) {
            return defaultTimeZone;
        }
        return TimeZone.getDefault();
    }

    protected AbstractEventProviderManager getEventProviderManager(String sourceId) {
        return eventProvidersMap.values().stream()
                .filter(epm -> epm.getSourceId().equals(sourceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no event provider with ID:" + sourceId));
    }

    /**
     * Returns all related calendar events, even if they are from different event providers.
     * <p>
     * Note, it applies changes to {@link CalendarEvent}.
     *
     * @param relatedEvents list of raw related events
     * @return related calendar events
     */
    protected List<CalendarEvent> getRelatedEvents(List<DomCalendarEvent> relatedEvents) {
        List<CalendarEvent> calendarEvents = new ArrayList<>(relatedEvents.size());
        for (DomCalendarEvent changedEvent : relatedEvents) {
            AbstractEventProviderManager epManager = getEventProviderManager(changedEvent.getSourceId());
            CalendarEvent relatedEvent = getCalendarEvent(changedEvent, epManager);

            calendarEvents.add(relatedEvent);

            applyChangesToCalendarEvent(relatedEvent, changedEvent);
        }
        return calendarEvents;
    }

    protected List<RelatedEventProviderContext> getRelatedEventProviderContexts(List<DomCalendarEvent> relatedEvents) {
        List<RelatedEventProviderContext> contexts = new ArrayList<>(eventProvidersMap.size());

        for (AbstractEventProviderManager epManager : eventProvidersMap.values()) {
            List<CalendarEvent> calendarEvents = relatedEvents.stream()
                    .filter(e -> epManager.getSourceId().equals(e.getSourceId()))
                    .map(e -> epManager.getCalendarEvent(e.getId()))
                    .toList();
            if (!calendarEvents.isEmpty()) {
                contexts.add(new RelatedEventProviderContext(epManager.getEventProvider(), calendarEvents));
            }
        }
        return contexts;
    }

    protected CalendarEvent getCalendarEvent(DomCalendarEvent clientEvent, AbstractEventProviderManager epManager) {
        CalendarEvent calendarEvent = epManager.getCalendarEvent(clientEvent.getId());
        if (calendarEvent == null) {
            throw new IllegalStateException("Cannot find calendar event by client ID: " + clientEvent.getId());
        }
        return calendarEvent;
    }

    protected void applyChangesToCalendarEvent(CalendarEvent calendarEvent, DomCalendarEvent clientEvent) {
        if (!Strings.isNullOrEmpty(clientEvent.getStart())) {
            calendarEvent.setStartDateTime(transformToLocalDateTime(clientEvent.getStart()));
        }
        if (!Strings.isNullOrEmpty(clientEvent.getEnd())) {
            calendarEvent.setEndDateTime(transformToLocalDateTime(clientEvent.getEnd()));
        }
        Boolean allDay = clientEvent.isAllDay();
        if (!Objects.equals(calendarEvent.getAllDay(), allDay)
                && (calendarEvent.getAllDay() != null || allDay)) {
            calendarEvent.setAllDay(allDay);
        }
    }

    protected List<EventProviderContext> createMoreLinkEventProviderContexts(DomMoreLinkClick context) {
        List<EventProviderContext> eventProviderContexts = new ArrayList<>();

        for (AbstractEventProviderManager epManager : eventProvidersMap.values()) {
            EventProviderContext eventProviderContext =
                    createMoreLinkEventProviderContext(epManager, context.getAllEvents(), context.getHiddenEvents());

            if (eventProviderContext != null) {
                eventProviderContexts.add(eventProviderContext);
            }
        }
        return eventProviderContexts;
    }

    @Nullable
    protected EventProviderContext createMoreLinkEventProviderContext(AbstractEventProviderManager epWrapper,
                                                                      List<DomCalendarEvent> allEvents,
                                                                      List<DomCalendarEvent> hiddenEvents) {
        List<DomCalendarEvent> visibleEvents = new ArrayList<>();
        allEvents.stream().filter(s -> !hiddenEvents.contains(s)).forEach(visibleEvents::add);

        List<CalendarEvent> visibleCalendarEvents = toCalendarEvents(epWrapper, visibleEvents);
        List<CalendarEvent> hiddenCalendarEvents = toCalendarEvents(epWrapper, hiddenEvents);

        if (CollectionUtils.isEmpty(visibleCalendarEvents)
                && CollectionUtils.isEmpty(hiddenCalendarEvents)) {
            return null;
        }

        return new EventProviderContext(epWrapper.getEventProvider(), visibleCalendarEvents, hiddenCalendarEvents);
    }

    protected List<CalendarEvent> toCalendarEvents(List<DomCalendarEvent> events) {
        List<CalendarEvent> calendarEvents = new ArrayList<>(events.size());
        for (DomCalendarEvent changedEvent : events) {
            AbstractEventProviderManager epManager = getEventProviderManager(changedEvent.getSourceId());
            calendarEvents.add(getCalendarEvent(changedEvent, epManager));
        }
        return calendarEvents;
    }

    protected List<CalendarEvent> toCalendarEvents(AbstractEventProviderManager epWrapper, List<DomCalendarEvent> segments) {
        return segments.stream()
                .filter(e -> e.getSourceId().equals(epWrapper.getSourceId()))
                .map(e -> epWrapper.getCalendarEvent(e.getId()))
                .toList();
    }

    protected AbstractEventMoveEvent.OldValues createOldValues(DomCalendarEvent clientEvent) {
        LocalDateTime endDateTime = !Strings.isNullOrEmpty(clientEvent.getEnd())
                ? transformToLocalDateTime(clientEvent.getEnd())
                : null;
        return new AbstractEventMoveEvent.OldValues(
                transformToLocalDateTime(clientEvent.getStart()),
                endDateTime,
                clientEvent.isAllDay());
    }

    protected CalendarDuration createDelta(DomCalendarDuration clientDuration) {
        return CalendarDuration.ofYears(clientDuration.getYears())
                .plusMonths(clientDuration.getMonths())
                .plusDays(clientDuration.getDays())
                .plusMilliseconds(clientDuration.getMilliseconds());
    }

    protected ViewInfo createViewInfo(DomViewInfo clientViewInfo) {
        return new ViewInfo(
                parseIsoDate(clientViewInfo.getActiveStart()),
                parseIsoDate(clientViewInfo.getActiveEnd()),
                parseIsoDate(clientViewInfo.getCurrentStart()),
                parseIsoDate(clientViewInfo.getCurrentEnd()),
                getCalendarView(clientViewInfo.getType()));
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
        result.put("months", getSerializer().toJsonArray(getParsedByCommaMessage("months")));
        result.put("monthsShort", getSerializer().toJsonArray(getParsedByCommaMessage("monthsShort")));
        result.put("weekdays", getSerializer().toJsonArray(getParsedByCommaMessage("weekdays")));
        result.put("weekdaysShort", getSerializer().toJsonArray(getParsedByCommaMessage("weekdaysShort")));
        result.put("weekdaysMin", getSerializer().toJsonArray(getParsedByCommaMessage("weekdaysMin")));
        return result;
    }

    protected List<String> getParsedByCommaMessage(String key) {
        return Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(getMessage(key));
    }

    protected String getMessage(String key) {
        return messages.getMessage(getClass(), key);
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
        MultiMonthYearViewProperties multiMonthYear = getCalendarViewProperties(CalendarViewType.MULTI_MONTH_YEAR);
        multiMonthYear.setMultiMonthTitleFormat(getMessage("multiMonthYearMultiMonthTitleFormat"));
    }

    protected void setupDefaultLocalizedFormats() {
        setDefaultDayPopoverFormat(getMessage("dayPopoverFormat"));
        setDefaultDayHeaderFormat(getMessage("dayHeaderFormat"));
        setDefaultWeekNumberFormat(getMessage("weekNumberFormat"));
        setDefaultSlotNumberFormat(getMessage("slotLabelFormat"));
        setDefaultEventTimeFormat(getMessage("eventTimeFormat"));
    }

    @Override
    protected void addEventProvidersOnAttach() {
        // Add all event providers using beforeClientResponse to respect the
        // order of calling requests from onAttach.
        getUI().ifPresent(ui ->
                ui.beforeClientResponse(this, (context) ->
                        eventProvidersMap.values().forEach(this::addEventProviderInternal)));

        eventProvidersMap.values().forEach(ep -> {
            if (ep instanceof EventProviderManager) {
                requestUpdateItemEventProvider(ep.getEventProvider().getId());
            }
        });
    }
}
