package io.jmix.fullcalendarflowui.component;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendarflowui.component.data.*;
import io.jmix.fullcalendarflowui.component.event.*;
import io.jmix.fullcalendarflowui.component.model.BusinessHours;
import io.jmix.fullcalendarflowui.component.model.option.FullCalendarOptions;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.dom.*;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarView;
import io.jmix.fullcalendarflowui.kit.component.serialization.*;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixFullCalendarSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Function;

public class FullCalendar extends JmixFullCalendar implements ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(FullCalendar.class);

    protected ApplicationContext applicationContext;
    protected CurrentAuthentication currentAuthentication;
    protected DateTimeTransformations dateTimeTransformations;
    protected FullCalendarDelegate calendarDelegate;

    protected Map<String, AbstractEventProviderManager> eventProvidersMap = new HashMap<>(2);

    protected Function<MoreLinkClassNamesContext, List<String>> linkMoreClassNamesGenerator;
    protected Function<DayHeaderClassNamesContext, List<String>> dayHeaderClassNamesGenerator;
    protected Function<DayCellClassNamesContext, List<String>> dayCellClassNamesGenerator;
    protected Function<SlotLabelClassNamesContext, List<String>> slotLabelClassNamesGenerator;

    protected Object eventConstraintGroupId;
    protected Object selectConstraintGroupId;

    @Override
    public void afterPropertiesSet() {
        autoWireDependencies();
        initComponent();
    }

    protected void autoWireDependencies() {
        currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
        dateTimeTransformations = applicationContext.getBean(DateTimeTransformations.class);
    }

    protected void initComponent() {
        calendarDelegate = createCalendarDelegate();

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

    public boolean isBusinessHoursEnabled() {
        return getOptions().getBusinessHours().isEnabled();
    }

    public void setBusinessHoursEnabled(boolean enabled) {
        getOptions().getBusinessHours().setEnabled(enabled);
    }

    public List<BusinessHours> getBusinessHours() {
        return getOptions().getBusinessHours().getBusinessHours();
    }

    public void setBusinessHours(@Nullable List<BusinessHours> businessHours) {
        getOptions().getBusinessHours().setBusinessHours(businessHours);
    }

    public boolean isEventConstraintEnabled() {
        return getOptions().getEventConstraint().isEnabled();
    }

    public void setEventConstraintEnabled(boolean enabled) {
        getOptions().getEventConstraint().setEnabled(enabled);
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
        return calendarDelegate.getI18n();
    }

    public void setI18n(@Nullable FullCalendarI18n i18n) {
        calendarDelegate.setI18n(i18n);
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
        calendarDelegate.setupLocalization();
    }

    @Override
    protected JsonArray fetchCalendarItems(String sourceId, String start, String end) {
        return calendarDelegate.fetchCalendarItems(sourceId, start, end);
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
                serializer.toJsonArrayFromJsonValue(jsonValues));

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
    protected JsonArray getMoreLinkClassNames(JsonObject jsonContext) {
        DomMoreLinkClassNames clientContext =
                deserializer.deserialize(jsonContext, DomMoreLinkClassNames.class);

        return calendarDelegate.getMoreLinkClassNames(clientContext);
    }

    @Override
    protected void onDatesSet(DatesSetDomEvent event) {
        super.onDatesSet(event);

        DomDatesSet domDatesSet = deserializer.deserialize(event.getContext(), DomDatesSet.class);

        DatesSetEvent datesSetEvent = calendarDelegate.createDatesSetEvent(domDatesSet, event.isFromClient());

        getEventBus().fireEvent(datesSetEvent);
    }

    @Override
    protected void onMoreLinkClick(MoreLinkClickDomEvent event) {
        DomMoreLinkClick clientContext =
                deserializer.deserialize(event.getContext(), DomMoreLinkClick.class);

        MoreLinkClickEvent clickEvent = calendarDelegate.createMoreLinkClickEvent(clientContext, event.isFromClient());

        getEventBus().fireEvent(clickEvent);
    }

    @Override
    protected void onEventClick(EventClickDomEvent event) {
        DomEventMouse clientContext = deserializer
                .deserialize(event.getContext(), DomEventMouse.class);

        EventClickEvent clickEvent = calendarDelegate.createEventClickEvent(clientContext, event.isFromClient());

        getEventBus().fireEvent(clickEvent);
    }

    @Override
    protected void onEventMouseEnter(EventMouseEnterDomEvent event) {
        DomEventMouse clientContext = deserializer
                .deserialize(event.getContext(), DomEventMouse.class);

        EventMouseEnterEvent eventMouseEnterEvent = calendarDelegate
                .createEventMouseEnterEvent(clientContext, event.isFromClient());

        getEventBus().fireEvent(eventMouseEnterEvent);
    }

    @Override
    protected void onEventMouseLeave(EventMouseLeaveDomEvent event) {
        DomEventMouse clientContext = deserializer
                .deserialize(event.getContext(), DomEventMouse.class);

        EventMouseLeaveEvent eventMouseLeaveEvent = calendarDelegate
                .createEventMouseLeaveEvent(clientContext, event.isFromClient());

        getEventBus().fireEvent(eventMouseLeaveEvent);
    }

    @Override
    protected void onEventDrop(EventDropDomEvent event) {
        DomEventDrop clientEvent = deserializer
                .deserialize(event.getContext(), DomEventDrop.class);

        EventDropEvent eventDropEvent = calendarDelegate.createEventDropEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(eventDropEvent);
    }

    @Override
    protected void onEventResize(EventResizeDomEvent event) {
        DomEventResize clientEvent = deserializer
                .deserialize(event.getContext(), DomEventResize.class);

        EventResizeEvent eventResizeEvent =
                calendarDelegate.createEventResizeEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(eventResizeEvent);
    }

    @Override
    protected void onDateClick(DateClickDomEvent event) {
        DomDateClick clientEvent = deserializer.deserialize(event.getContext(), DomDateClick.class);

        DateClickEvent dateClickEvent = calendarDelegate.createDateClickEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(dateClickEvent);
    }

    @Override
    protected void onSelect(SelectDomEvent event) {
        DomSelect clientEvent = deserializer.deserialize(event.getContext(), DomSelect.class);

        SelectEvent selectEvent = calendarDelegate.createSelectEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(selectEvent);
    }

    @Override
    protected void onUnselect(UnselectDomEvent event) {
        DomUnselect clientEvent = deserializer.deserialize(event.getContext(), DomUnselect.class);

        UnselectEvent unselectEvent = calendarDelegate.createUnselectEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(unselectEvent);
    }

    @Override
    protected JsonArray getDayHeaderClassNames(JsonObject jsonContext) {
        DomDayHeaderClassNames clientContext = deserializer.deserialize(jsonContext, DomDayHeaderClassNames.class);

        return calendarDelegate.getDayHeaderClassNames(clientContext);
    }

    @Override
    protected JsonArray getDayCellClassNames(JsonObject jsonContext) {
        DomDayCellClassNames clientContext = deserializer.deserialize(jsonContext, DomDayCellClassNames.class);

        return calendarDelegate.getDayCellClassNames(clientContext);
    }

    @Override
    protected JsonArray getSlotLabelClassNames(JsonObject jsonContext) {
        DomSlotLabelClassNames clientContext = deserializer.deserialize(jsonContext, DomSlotLabelClassNames.class);

        return calendarDelegate.getSlotLabelClassNames(clientContext);
    }

    protected EventProviderManager createEventProviderManager(CalendarEventProvider eventProvider) {
        return new EventProviderManager(eventProvider, this);
    }

    protected LazyEventProviderManager createLazyEventProviderManager(LazyCalendarEventProvider eventProvider) {
        return new LazyEventProviderManager(eventProvider, this);
    }

    /**
     * Is used for getting event providers in component delegate (see {@link FullCalendarDelegate}.
     *
     * @return event providers map
     */
    protected Map<String, AbstractEventProviderManager> getEventProvidersMap() {
        return eventProvidersMap;
    }

    /**
     * Is used for getting calendar view in component delegate (see {@link FullCalendarDelegate}.
     *
     * @return event providers map
     */
    @Override
    protected CalendarView getCalendarView(String id) {
        return super.getCalendarView(id);
    }

    protected void initTimeZone() {
        TimeZone timeZone = applicationContext.getBean(CurrentAuthentication.class).getTimeZone();

        setTimeZone(timeZone);
    }

    protected FullCalendarDelegate createCalendarDelegate() {
        return applicationContext.getBean(FullCalendarDelegate.class,
                this, applicationContext.getBean(Messages.class), currentAuthentication);
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

    @Override
    protected void clearEventProvidersOnDetach() {
        // As DataHolder is a shared object between calendars on a page,
        // we must remove event sources from it when component is detached.
        if (!eventProvidersMap.values().isEmpty()) {
            JsonArray sourceIds = serializer.toJsonArrayFromString(
                    eventProvidersMap.values().stream()
                            .map(AbstractEventProviderManager::getSourceId)
                            .toList());
            UI.getCurrent().getPage().executeJs(
                    "window.Vaadin.Flow.jmixFullCalendarConnector.removeSources($0)", sourceIds);
        }
        getSerializer().clearData();
    }
}
