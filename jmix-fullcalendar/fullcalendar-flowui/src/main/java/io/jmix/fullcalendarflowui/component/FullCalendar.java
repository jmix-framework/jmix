package io.jmix.fullcalendarflowui.component;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
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
import io.jmix.fullcalendarflowui.component.event.MoreLinkClickEvent.EventProviderContext;
import io.jmix.fullcalendarflowui.component.model.BusinessHours;
import io.jmix.fullcalendarflowui.component.model.option.FullCalendarOptions;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.event.dom.*;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import io.jmix.fullcalendarflowui.kit.component.serialization.*;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixFullCalendarSerializer;
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

    public List<BaseCalendarEventProvider> getEventProviders() {
        return eventProvidersMap != null && !eventProvidersMap.isEmpty()
                ? eventProvidersMap.values().stream().map(AbstractEventProviderManager::getEventProvider).toList()
                : Collections.emptyList();
    }

    @Nullable
    public BaseCalendarEventProvider getEventProvider(String id) {
        Preconditions.checkNotEmptyString(id);

        AbstractEventProviderManager eventProviderManager = eventProvidersMap.get(id);

        if (eventProviderManager != null) {
            return eventProviderManager.getEventProvider();
        }
        return null;
    }

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

    public void removeEventProvider(BaseCalendarEventProvider eventProvider) {
        Preconditions.checkNotNullArgument(eventProvider);

        removeEventProvider(eventProvider.getId());
    }

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

    public void removeAllEventProviders() {
        getEventProviders().forEach(this::removeEventProvider);
    }

    @Nullable
    public Object getEventConstraintGroupId() {
        return eventConstraintGroupId;
    }

    public void setEventConstraintGroupId(@Nullable Object groupId) {
        this.eventConstraintGroupId = groupId;

        String serializedGroupId = getSerializer().serializeGroupIdOrConstraint(groupId);

        getOptions().getEventConstraint().setGroupId(serializedGroupId);
    }

    @Nullable
    public Object getSelectConstraintGroupId() {
        return selectConstraintGroupId;
    }

    public void setSelectConstraintGroupId(@Nullable Object groupId) {
        this.selectConstraintGroupId = groupId;

        String serializedGroupId = getSerializer().serializeGroupIdOrConstraint(groupId);

        getOptions().getSelectConstraint().setGroupId(serializedGroupId);
    }

    public boolean isDefaultBusinessHoursEnabled() {
        return getOptions().getBusinessHours().isEnabled();
    }

    public void setDefaultBusinessHoursEnabled(boolean enabled) {
        getOptions().getBusinessHours().setEnabled(enabled);
    }

    public List<BusinessHours> getBusinessHours() {
        return getOptions().getBusinessHours().getBusinessHours();
    }

    public void setBusinessHours(@Nullable List<BusinessHours> businessHours) {
        getOptions().getBusinessHours().setBusinessHours(businessHours);
    }

    public List<BusinessHours> getEventConstraintBusinessHours() {
        return getOptions().getEventConstraint().getBusinessHours();
    }

    public void setEventConstraintBusinessHours(@Nullable List<BusinessHours> businessHoursEventConstraint) {
        getOptions().getEventConstraint().setBusinessHours(businessHoursEventConstraint);
    }

    public boolean isSelectConstraintEnabled() {
        return getOptions().getSelectConstraint().isEnabled();
    }

    public void setSelectConstraintEnabled(boolean enabled) {
        getOptions().getSelectConstraint().setEnabled(enabled);
    }

    public List<BusinessHours> getSelectConstraintBusinessHours() {
        return getOptions().getSelectConstraint().getBusinessHours();
    }

    public void setSelectConstraintBusinessHours(@Nullable List<BusinessHours> businessHoursSelectConstraint) {
        getOptions().getSelectConstraint().setBusinessHours(businessHoursSelectConstraint);
    }

    public List<DayOfWeek> getHiddenDays() {
        List<DayOfWeek> hiddenDays = getOptions().getHiddenDays().getValue();
        return hiddenDays == null ? Collections.emptyList() : hiddenDays;
    }

    public void setHiddenDays(@Nullable List<DayOfWeek> hiddenDays) {
        getOptions().getHiddenDays().setValue(hiddenDays);
    }

    @Nullable
    public FullCalendarI18n getI18n() {
        return explicitI18n;
    }

    public void setI18n(@Nullable FullCalendarI18n i18n) {
        this.explicitI18n = i18n;

        setI18nInternal(defaultI18n.combine(explicitI18n));
    }

    @Nullable
    public DayOfWeek getFirstDayOfWeek() {
        return getOptions().getFirstDay().getValue();
    }

    public void setFirstDayOfWeek(@Nullable DayOfWeek firstDay) {
        getOptions().getFirstDay().setValue(firstDay);
    }

    @Nullable
    public Display getEventDisplay() {
        return getOptions().getEventDisplay().getValue();
    }

    public void setEventDisplay(@Nullable Display display) {
        getOptions().getEventDisplay().setValue(display);
    }

    public Registration addDatesSetListener(ComponentEventListener<DatesSetEvent> listener) {
        Preconditions.checkNotNullArgument(listener);

        return getEventBus().addListener(DatesSetEvent.class, listener);
    }

    /**
     * Adds a "more" link click listener. When listener is added the {@link #setMoreLinkCalendarView(CalendarView)}
     * value will be ignored.
     *
     * @param listener listener to add
     * @return A registration object for removing an event listener added to a calendar
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

    @Nullable
    public Function<MoreLinkClassNamesContext, List<String>> getMoreLinkClassNamesGenerator() {
        return linkMoreClassNamesGenerator;
    }

    /**
     * Sets a class names generator for "+x more" link.
     * <p>
     * Note, generator has a precedence over a {@link #setMoreLinkClassNames(List)} and other "add class name" methods.
     *
     * @param classNamesGenerator the generator to set
     */
    public void setMoreLinkClassNamesGenerator(
            @Nullable Function<MoreLinkClassNamesContext, List<String>> classNamesGenerator) {
        this.linkMoreClassNamesGenerator = classNamesGenerator;

        options.getMoreLinkClassNames().setFunctionEnabled(classNamesGenerator != null);
    }

    @Nullable
    public Function<DayHeaderClassNamesContext, List<String>> getDayHeaderClassNamesGenerator() {
        return dayHeaderClassNamesGenerator;
    }

    public void setDayHeaderClassNamesGenerator(
            @Nullable Function<DayHeaderClassNamesContext, List<String>> classNamesGenerator) {
        this.dayHeaderClassNamesGenerator = classNamesGenerator;

        options.getDayHeaderClassNames().setValue(classNamesGenerator != null);
    }

    @Nullable
    public Function<DayCellClassNamesContext, List<String>> getDayCellClassNamesGenerator() {
        return dayCellClassNamesGenerator;
    }

    public void setDayCellClassNamesGenerator(
            @Nullable Function<DayCellClassNamesContext, List<String>> dayCellClassNamesGenerator) {
        this.dayCellClassNamesGenerator = dayCellClassNamesGenerator;

        options.getDayCellClassNames().setValue(dayCellClassNamesGenerator != null);
    }

    @Nullable
    public Function<SlotLabelClassNamesContext, List<String>> getSlotLabelClassNamesGenerator() {
        return slotLabelClassNamesGenerator;
    }

    public void setSlotLabelClassNamesGenerator(
            @Nullable Function<SlotLabelClassNamesContext, List<String>> slotLabelClassNamesGenerator) {
        this.slotLabelClassNamesGenerator = slotLabelClassNamesGenerator;

        options.getSlotLabelClassNames().setValue(slotLabelClassNamesGenerator != null);
    }

    @Nullable
    public Function<NowIndicatorClassNamesContext, List<String>> getNowIndicatorClassNamesGenerator() {
        return nowIndicatorClassNamesGenerator;
    }

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

        getEventBus().fireEvent(
                new MoreLinkClickEvent(this, event.isFromClient(),
                        clientContext.isAllDay(),
                        toLocalDateTime(clientContext.getDateTime()),
                        createViewInfo(clientContext.getView()),
                        eventProviderContexts,
                        new MouseEventDetails(clientContext.getMouseDetails())
                )
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
                        new MouseEventDetails(clientEvent.getMouseDetails()),
                        createOldValues(clientEvent.getOldEvent()),
                        droppedEvent,
                        getRelatedEvents(clientEvent.getRelatedEvents(), epManager),
                        createViewInfo(clientEvent.getView()),
                        createDelta(clientEvent.getDelta()))
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
                        new MouseEventDetails(clientEvent.getMouseDetails()),
                        createOldValues(clientEvent.getOldEvent()),
                        resizedEvent,
                        getRelatedEvents(clientEvent.getRelatedEvents(), epManager),
                        createViewInfo(clientEvent.getView()),
                        createDelta(clientEvent.getStartDelta()),
                        createDelta(clientEvent.getEndDelta())));
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
                        clientContext.getShortText(),
                        clientContext.getText(),
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
     * Returns transformed date-time from component's time zone to system defaule ({@link TimeZone#getDefault()}).
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

    protected List<CalendarEvent> getRelatedEvents(List<DomCalendarEvent> relatedEvents,
                                                   AbstractEventProviderManager epManager) {
        List<CalendarEvent> calendarEvents = new ArrayList<>(relatedEvents.size());
        for (DomCalendarEvent changedEvent : relatedEvents) {
            CalendarEvent relatedEvent = getCalendarEvent(changedEvent, epManager);

            calendarEvents.add(relatedEvent);

            applyChangesToCalendarEvent(relatedEvent, changedEvent);
        }
        return calendarEvents;
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
                    createMoreLinkEventProviderContext(epManager, context.getAllData(), context.getHiddenData());

            if (eventProviderContext != null) {
                eventProviderContexts.add(eventProviderContext);
            }
        }
        return eventProviderContexts;
    }

    @Nullable
    protected EventProviderContext createMoreLinkEventProviderContext(AbstractEventProviderManager epWrapper,
                                                                      List<DomSegment> allData,
                                                                      List<DomSegment> hiddenData) {
        List<DomSegment> visibleData = new ArrayList<>();
        allData.stream().filter(s -> !hiddenData.contains(s)).forEach(visibleData::add);

        List<CalendarEvent> visibleEvents = toCalendarEvents(epWrapper, visibleData);
        List<CalendarEvent> hiddenEvents = toCalendarEvents(epWrapper, hiddenData);

        if (CollectionUtils.isEmpty(visibleEvents)
                && CollectionUtils.isEmpty(hiddenEvents)) {
            return null;
        }

        return new EventProviderContext(epWrapper.getEventProvider(), visibleEvents, hiddenEvents);
    }

    protected List<CalendarEvent> toCalendarEvents(AbstractEventProviderManager epWrapper, List<DomSegment> segments) {
        return segments.stream()
                .filter(e -> e.getEventSourceId().equals(epWrapper.getSourceId()))
                .map(e -> epWrapper.getCalendarEvent(e.getEventId()))
                .toList();
    }

    protected AbstractEventChangeEvent.OldValues createOldValues(DomCalendarEvent clientEvent) {
        LocalDateTime endDateTime = !Strings.isNullOrEmpty(clientEvent.getEnd())
                ? transformToLocalDateTime(clientEvent.getEnd())
                : null;
        return new AbstractEventChangeEvent.OldValues(
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
                parseIsoDate(clientViewInfo.getActiveEnd()),
                parseIsoDate(clientViewInfo.getActiveStart()),
                parseIsoDate(clientViewInfo.getCurrentEnd()),
                parseIsoDate(clientViewInfo.getCurrentStart()),
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
        // Add all event providers
        eventProvidersMap.values().forEach(ep -> {
            addEventProviderInternal(ep);
            if (ep instanceof EventProviderManager) {
                requestUpdateItemEventProvider(ep.getEventProvider().getId());
            }
        });
    }
}
