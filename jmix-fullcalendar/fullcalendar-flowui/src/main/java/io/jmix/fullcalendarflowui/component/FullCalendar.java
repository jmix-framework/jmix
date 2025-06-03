package io.jmix.fullcalendarflowui.component;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.fullcalendarflowui.component.contextmenu.FullCalendarContextMenu;
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.model.Display;
import io.jmix.fullcalendarflowui.component.data.*;
import io.jmix.fullcalendarflowui.component.event.*;
import io.jmix.fullcalendarflowui.component.event.AbstractEventMoveEvent;
import io.jmix.fullcalendarflowui.component.event.AbstractEventMoveEvent.RelatedDataProviderContext;
import io.jmix.fullcalendarflowui.component.event.MoreLinkClickEvent.DataProviderContext;
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
 * UI component for visualizing events in a calendar using various display modes (month, week, etc.).
 * <p>
 * Component provides event rendering, drag-and-drop functionality, event editing, and customizable display modes.
 */
public class FullCalendar extends JmixFullCalendar implements ApplicationContextAware, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(FullCalendar.class);

    protected ApplicationContext applicationContext;
    protected CurrentAuthentication currentAuthentication;
    protected Messages messages;

    protected Map<String, AbstractDataProviderManager> dataProvidersMap = new HashMap<>(2);
    protected Set<AbstractDataProviderManager> pendingDataProviders = new HashSet<>(2);

    protected Function<MoreLinkClassNamesContext, List<String>> linkMoreClassNamesGenerator;
    protected Function<DayHeaderClassNamesContext, List<String>> dayHeaderClassNamesGenerator;
    protected Function<DayCellClassNamesContext, List<String>> dayCellClassNamesGenerator;
    protected Function<SlotLabelClassNamesContext, List<String>> slotLabelClassNamesGenerator;
    protected Function<NowIndicatorClassNamesContext, List<String>> nowIndicatorClassNamesGenerator;
    protected Function<DayCellBottomTextClassNamesContext, List<String>> dayCellBottomTextClassNamesGenerator;
    protected Function<DayCellBottomTextContext, String> dayCellBottomTextGenerator;

    protected Object eventConstraintGroupId;
    protected Object selectConstraintGroupId;

    protected FullCalendarI18n defaultI18n;
    protected FullCalendarI18n explicitI18n;

    protected FullCalendarContextMenu contextMenu;

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
        defaultI18n = createDefaultI18n();

        setupLocalization();

        initTimeZone();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * @return a list of data providers
     */
    public List<CalendarDataProvider> getDataProviders() {
        return dataProvidersMap != null && !dataProvidersMap.isEmpty()
                ? dataProvidersMap.values().stream().map(AbstractDataProviderManager::getDataProvider).toList()
                : Collections.emptyList();
    }

    /**
     * Returns a data provider by its ID.
     *
     * @param id  data provider ID
     * @param <T> type of data provider
     * @return data provider or {@code null} if there is no data provider with passed ID
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends CalendarDataProvider> T getDataProvider(String id) {
        Preconditions.checkNotEmptyString(id);

        AbstractDataProviderManager dataProviderManager = dataProvidersMap.get(id);
        if (dataProviderManager != null) {
            return (T) dataProviderManager.getDataProvider();
        }
        return null;
    }

    /**
     * Adds new lazy data provider.
     *
     * @param dataProvider lazy data provider to add
     */
    public void addDataProvider(CallbackCalendarDataProvider dataProvider) {
        Preconditions.checkNotNullArgument(dataProvider);

        if (dataProvidersMap.containsKey(dataProvider.getId())) {
            log.warn("Lazy data provider with the same '{}' ID already added", dataProvider.getId());
            return;
        }

        CallbackDataProviderManager dataProviderManager = createCallbackDataProviderManager(dataProvider);

        dataProvidersMap.put(dataProvider.getId(), dataProviderManager);

        if (initialized) {
            addDataProviderInternal(dataProviderManager);
        } else {
            pendingDataProviders.add(dataProviderManager);
        }
    }

    /**
     * Adds new data provider.
     *
     * @param dataProvider data provider to add
     */
    public void addDataProvider(ItemsCalendarDataProvider dataProvider) {
        Preconditions.checkNotNullArgument(dataProvider);

        if (dataProvidersMap.containsKey(dataProvider.getId())) {
            log.warn("Item data provider with the same '{}' ID already added", dataProvider.getId());
            return;
        }

        ItemsDataProviderManager dataProviderManager = createDataProviderManager(dataProvider);
        dataProviderManager.setItemSetChangeListener(this::onItemSetChangeListener);

        dataProvidersMap.put(dataProvider.getId(), dataProviderManager);

        if (initialized) {
            addDataProviderInternal(dataProviderManager);
        } else {
            pendingDataProviders.add(dataProviderManager);
        }
    }

    /**
     * Removes a data provider from the component.
     *
     * @param dataProvider data provider to remove
     */
    public void removeDataProvider(CalendarDataProvider dataProvider) {
        Preconditions.checkNotNullArgument(dataProvider);

        removeDataProvider(dataProvider.getId());
    }

    /**
     * Removes a data provider from the component by ID.
     *
     * @param id ID of data provider to remove
     */
    public void removeDataProvider(String id) {
        Preconditions.checkNotEmptyString(id);

        AbstractDataProviderManager dataProviderManager = dataProvidersMap.get(id);
        if (dataProviderManager != null) {
            if (dataProviderManager instanceof ItemsDataProviderManager itemProvider) {
                itemProvider.setItemSetChangeListener(null);
            }
            getElement().callJsFunction("_removeEventSource", dataProviderManager.getSourceId());
        }
        dataProvidersMap.remove(id);
    }

    /**
     * Removes all data providers from the component.
     */
    public void removeAllDataProviders() {
        getDataProviders().forEach(this::removeDataProvider);
    }

    /**
     * Initiates data fetching from the given data provider.
     *
     * @param dataProvider data provider to refetch
     * @throws IllegalArgumentException if data provider does not belong to a given calendar
     */
    public void refetchDataProvider(CalendarDataProvider dataProvider) {
        Preconditions.checkNotNullArgument(dataProvider);

        refetchDataProvider(dataProvider.getId());
    }

    /**
     * Finds the data provider by ID and initiates data fetching.
     *
     * @param id data provider ID
     * @throws IllegalArgumentException if there is no data provider with the given ID
     */
    public void refetchDataProvider(String id) {
        Preconditions.checkNotEmptyString(id);

        CalendarDataProvider dataProvider = getDataProvider(id);
        if (dataProvider == null) {
            throw new IllegalArgumentException("There is no data provider with the given ID: '%s'".formatted(id));
        }

        if (dataProvider instanceof ItemsCalendarDataProvider) {
            requestUpdateItemDataProvider(id);
        } else if (dataProvider instanceof CallbackCalendarDataProvider) {
            requestUpdateCallbackDataProvider(id);
        }
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
     * Note, this property will be applied if {@link FullCalendar#setSelectionEnabled(boolean)} is enabled.
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
     * <p>
     * The default value is {@link Display#AUTO}.
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
     * Adds day navigation link click listener. The event is fired when the user clicks on the day heading navigation
     * link. The navigation link can be activated by the {@link FullCalendar#setNavigationLinksEnabled(boolean)}
     * property.
     * <p>
     * For instance:
     * <pre>{@code
     * @Subscribe("calendar")
     * public void onCalendarDayNavigationLinkClick(final DayNavigationLinkClickEvent event) {
     *     event.getSource().setCalendarDisplayMode(CalendarDisplayModes.TIME_GRID_DAY);
     *     event.getSource().navigateToDate(event.getDate());
     * }
     * }</pre>
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addDayNavigationLinkClickListener(ComponentEventListener<DayNavigationLinkClickEvent> listener) {
        Preconditions.checkNotNullArgument(listener);

        getOptions().getNavLinkDayClick().setValue(true);

        Registration registration = getEventBus().addListener(DayNavigationLinkClickEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(DayNavigationLinkClickEvent.class)) {
                getOptions().getNavLinkDayClick().setValue(false);
            }
        };
    }

    /**
     * Adds week navigation link click listener. The event is fired when the user clicks the week number navigation
     * link. The navigation link can be activated by the {@link FullCalendar#setNavigationLinksEnabled(boolean)}
     * property.
     * <p>
     * For instance:
     * <pre>{@code
     * @Subscribe("calendar")
     * public void onCalendarWeekNavigationLinkClick(final WeekNavigationLinkClickEvent event) {
     *     event.getSource().setCalendarDisplayMode(CalendarDisplayModes.TIME_GRID_WEEK);
     *     event.getSource().navigateToDate(event.getDate());
     * }
     * }</pre>
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a component
     */
    public Registration addWeekNavigationLinkClickListener(ComponentEventListener<WeekNavigationLinkClickEvent> listener) {
        Preconditions.checkNotNullArgument(listener);

        getOptions().getNavLinkWeekClick().setValue(true);

        Registration registration = getEventBus().addListener(WeekNavigationLinkClickEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(WeekNavigationLinkClickEvent.class)) {
                getOptions().getNavLinkWeekClick().setValue(false);
            }
        };
    }

    /**
     * Adds a "more" link click listener. When listener is added, the {@link #setMoreLinkCalendarDisplayMode(CalendarDisplayMode)}
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
     * Adds a listener that is invoked when the current selection is cleared.
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
     * Note, generator takes precedence over a {@link #setMoreLinkClassNames(List)} and other
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
     * some display modes.
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
     * Sets a day cell class names generator. The day cell appears in day-grid and time-grid display modes
     * as an all-day cell.
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
     * Sets a slot label class-names generator. The slot label appears in time-grid display modes.
     * It is a cell with time label.
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
     * Sets a now-indicator class names generator. The now-indicator consists of two parts: the line and the axis.
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

    /**
     * @return bottom text generator for day cells or {@code null} if not set
     */
    @Nullable
    public Function<DayCellBottomTextContext, String> getDayCellBottomTextGenerator() {
        return dayCellBottomTextGenerator;
    }

    /**
     * Sets bottom text generator for day cells. It applies only for {@link CalendarDisplayModes#DAY_GRID_MONTH} and
     * {@link CalendarDisplayModes#DAY_GRID_YEAR}.
     * <p>
     * Note sometimes generated text can be overlapped by events, so it is recommended to limit visible events count
     * (e.g. {@link #setDayMaxEvents(Integer)}, etc.).
     *
     * @param dayCellBottomTextGenerator the generator to set
     */
    public void setDayCellBottomTextGenerator(
            @Nullable Function<DayCellBottomTextContext, String> dayCellBottomTextGenerator) {
        this.dayCellBottomTextGenerator = dayCellBottomTextGenerator;

        options.getDayCellBottomText().setTextGeneratorEnabled(dayCellBottomTextGenerator != null);
    }

    /**
     * @return bottom text class names generator or {@code null} if not set
     */
    @Nullable
    public Function<DayCellBottomTextClassNamesContext, List<String>> getDayCellBottomTextClassNamesGenerator() {
        return dayCellBottomTextClassNamesGenerator;
    }

    /**
     * Sets bottom text class names generator. Note, the generator will be invoked only if cell's bottom text is not
     * {@code null}.
     *
     * @param dayCellBottomTextClassNamesGenerator the generator to set
     * @see #setDayCellBottomTextClassNamesGenerator(Function)
     */
    public void setDayCellBottomTextClassNamesGenerator(
            @Nullable Function<DayCellBottomTextClassNamesContext, List<String>> dayCellBottomTextClassNamesGenerator) {
        this.dayCellBottomTextClassNamesGenerator = dayCellBottomTextClassNamesGenerator;

        options.getDayCellBottomText().setClassNamesGeneratorEnabled(dayCellBottomTextClassNamesGenerator != null);
    }

    /**
     * @return context menu instance attached to the component
     */
    public FullCalendarContextMenu getContextMenu() {
        if (contextMenu == null) {
            contextMenu = new FullCalendarContextMenu(this);
        }
        return contextMenu;
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
    }

    protected void setI18nInternal(FullCalendarI18n i18n) {
        JsonObject json = getSerializer().serializeObject(i18n);

        json.put("locale", getSerializer().serializeValue(currentAuthentication.getLocale()));
        json.put("momentLocale", createCalendarLocalizedUnitNamesJson());

        getElement().setPropertyJson("i18n", json);
    }

    @Override
    protected JsonArray fetchCalendarItems(String sourceId, String start, String end) {
        CallbackDataProviderManager dataProviderManager = (CallbackDataProviderManager) getDataProviderManager(sourceId);

        return dataProviderManager.fetchAndSerialize(
                new CallbackCalendarDataProvider.ItemsFetchContext(
                        dataProviderManager.getDataProvider(),
                        parseIsoDate(start),
                        parseIsoDate(end),
                        getComponentTimeZone()));
    }

    protected void addDataProviderInternal(AbstractDataProviderManager dataProviderManager) {
        log.debug("Perform add data provider");

        getElement().callJsFunction(dataProviderManager.getJsFunctionName(), dataProviderManager.getSourceId());

        if (dataProviderManager instanceof ItemsDataProviderManager itemsDataProviderManager) {
            ItemsCalendarDataProvider dataProvider = itemsDataProviderManager.getDataProvider();
            if (!dataProvider.getItems().isEmpty()) {
                requestUpdateItemDataProvider(dataProvider.getId());
            }
        }
    }

    protected void onItemSetChangeListener(ItemsCalendarDataProvider.ItemSetChangeEvent event) {
        String providerId = event.getSource().getId();
        ItemsDataProviderManager dataProviderManager =
                (ItemsDataProviderManager) dataProvidersMap.get(providerId);

        switch (event.getOperation()) {
            case ADD, REMOVE, UPDATE -> {
                dataProviderManager.addIncrementalChange(event);
                requestIncrementalDataUpdate();
            }
            default -> requestUpdateItemDataProvider(providerId);
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
        List<ItemsDataProviderManager> dataProviders = dataProvidersMap.values().stream()
                .filter(ep -> ep instanceof ItemsDataProviderManager)
                .map(ep -> (ItemsDataProviderManager) ep)
                .toList();

        List<JsonValue> jsonValues = new ArrayList<>();
        for (ItemsDataProviderManager dataProviderManger : dataProviders) {
            jsonValues.addAll(dataProviderManger.serializeIncrementalData());
            dataProviderManger.clearIncrementalData();
        }

        getElement().callJsFunction("_updateSourcesWithIncrementalData",
                getSerializer().toJsonArrayJson(jsonValues));

        incrementalUpdateExecution = null;
    }

    protected void requestUpdateItemDataProvider(String dataProviderId) {
        // Do not call if it's still updating
        if (itemsDataProvidersExecutionMap.containsKey(dataProviderId)) {
            return;
        }
        getUI().ifPresent(ui -> {
            StateTree.ExecutionRegistration executionRegistration = ui.beforeClientResponse(this,
                    (context) -> performUpdateItemDataProvider(dataProviderId));
            itemsDataProvidersExecutionMap.put(dataProviderId, executionRegistration);
        });
    }

    protected void performUpdateItemDataProvider(String dataProviderId) {
        JsonObject resultJson = new JreJsonFactory().createObject();

        ItemsDataProviderManager dataProviderManager =
                (ItemsDataProviderManager) dataProvidersMap.get(dataProviderId);

        JsonValue dataJson = dataProviderManager.serializeData();

        resultJson.put("data", dataJson);
        resultJson.put("sourceId", dataProviderManager.getSourceId());

        getElement().callJsFunction("_updateSyncSourcesData", resultJson);

        itemsDataProvidersExecutionMap.remove(dataProviderId);
    }

    protected void requestUpdateCallbackDataProvider(String dataProviderId) {
        if (callbackDataProvidersExecutionMap.containsKey(dataProviderId)) {
            return;
        }
        getUI().ifPresent(ui -> {
            StateTree.ExecutionRegistration executionRegistration = ui.beforeClientResponse(this,
                    (context) -> performUpdateCallbackDataProvider(dataProviderId));
            callbackDataProvidersExecutionMap.put(dataProviderId, executionRegistration);
        });
    }

    protected void performUpdateCallbackDataProvider(String dataProviderId) {
        JsonObject resultJson = new JreJsonFactory().createObject();

        CallbackDataProviderManager dataProviderManager =
                (CallbackDataProviderManager) dataProvidersMap.get(dataProviderId);

        resultJson.put("sourceId", dataProviderManager.getSourceId());

        getElement().callJsFunction("_updateAsyncSourcesData", resultJson);

        callbackDataProvidersExecutionMap.remove(dataProviderId);
    }

    @Override
    protected void onDatesSet(DatesSetDomEvent event) {
        super.onDatesSet(event);

        DomDatesSet domDatesSet = deserializer.deserialize(event.getContext(), DomDatesSet.class);

        getEventBus().fireEvent(
                new DatesSetEvent(this, event.isFromClient(),
                        parseIsoDate(domDatesSet.getStartDate()),
                        parseIsoDate(domDatesSet.getEndDate()),
                        createDisplayModeInfo(domDatesSet.getView())));
    }

    @Override
    protected void onMoreLinkClick(MoreLinkClickDomEvent event) {
        DomMoreLinkClick clientContext = deserializer.deserialize(event.getContext(), DomMoreLinkClick.class);

        List<DataProviderContext> dataProviderContexts = createMoreLinkDataProviderContexts(clientContext);

        List<CalendarEvent> hiddenCalendarEvents = toCalendarEvents(clientContext.getHiddenEvents());
        List<CalendarEvent> visibleCalendarEvents = toCalendarEvents(clientContext.getAllEvents());
        visibleCalendarEvents.removeAll(hiddenCalendarEvents);

        getEventBus().fireEvent(
                new MoreLinkClickEvent(this, event.isFromClient(),
                        clientContext.isAllDay(),
                        toLocalDateTime(clientContext.getDateTime()),
                        visibleCalendarEvents,
                        hiddenCalendarEvents,
                        dataProviderContexts,
                        new MouseEventDetails(clientContext.getMouseDetails()),
                        createDisplayModeInfo(clientContext.getView()))
        );
    }

    @Override
    protected void onEventClick(EventClickDomEvent event) {
        DomEventMouse clientContext = deserializer.deserialize(event.getContext(), DomEventMouse.class);

        AbstractDataProviderManager dataProviderManager = getDataProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), dataProviderManager);

        getEventBus().fireEvent(
                new EventClickEvent(this, event.isFromClient(),
                        new MouseEventDetails(clientContext.getMouseDetails()),
                        calendarEvent,
                        dataProviderManager.getDataProvider(),
                        createDisplayModeInfo(clientContext.getView()))
        );
    }

    @Override
    protected void onEventMouseEnter(EventMouseEnterDomEvent event) {
        DomEventMouse clientContext = deserializer.deserialize(event.getContext(), DomEventMouse.class);

        AbstractDataProviderManager dataProviderManager = getDataProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), dataProviderManager);

        getEventBus().fireEvent(
                new EventMouseEnterEvent(this, event.isFromClient(),
                        new MouseEventDetails(clientContext.getMouseDetails()),
                        calendarEvent,
                        dataProviderManager.getDataProvider(),
                        createDisplayModeInfo(clientContext.getView())));
    }

    @Override
    protected void onEventMouseLeave(EventMouseLeaveDomEvent event) {
        DomEventMouse clientContext = deserializer.deserialize(event.getContext(), DomEventMouse.class);

        AbstractDataProviderManager dataProviderManager = getDataProviderManager(clientContext.getEvent().getSourceId());
        CalendarEvent calendarEvent = getCalendarEvent(clientContext.getEvent(), dataProviderManager);

        getEventBus().fireEvent(
                new EventMouseLeaveEvent(this, event.isFromClient(),
                        new MouseEventDetails(clientContext.getMouseDetails()),
                        calendarEvent,
                        dataProviderManager.getDataProvider(),
                        createDisplayModeInfo(clientContext.getView())));
    }

    @Override
    protected void onEventDrop(EventDropDomEvent event) {
        DomEventDrop clientEvent = deserializer.deserialize(event.getContext(), DomEventDrop.class);

        AbstractDataProviderManager dataProviderManager = getDataProviderManager(clientEvent.getEvent().getSourceId());

        CalendarEvent droppedEvent = getCalendarEvent(clientEvent.getEvent(), dataProviderManager);
        applyChangesToCalendarEvent(droppedEvent, clientEvent.getEvent());

        getEventBus().fireEvent(
                new EventDropEvent(this, event.isFromClient(),
                        droppedEvent,
                        dataProviderManager.getDataProvider(),
                        getRelatedDataProviderContexts(clientEvent.getRelatedEvents()),
                        getRelatedEvents(clientEvent.getRelatedEvents()),
                        createOldValues(clientEvent.getOldEvent()),
                        createDelta(clientEvent.getDelta()),
                        new MouseEventDetails(clientEvent.getMouseDetails()),
                        createDisplayModeInfo(clientEvent.getView()))
        );
    }

    @Override
    protected void onEventResize(EventResizeDomEvent event) {
        DomEventResize clientEvent = deserializer.deserialize(event.getContext(), DomEventResize.class);

        AbstractDataProviderManager dataProviderManager = getDataProviderManager(clientEvent.getEvent().getSourceId());

        CalendarEvent resizedEvent = getCalendarEvent(clientEvent.getEvent(), dataProviderManager);
        applyChangesToCalendarEvent(resizedEvent, clientEvent.getEvent());

        getEventBus().fireEvent(
                new EventResizeEvent(this, event.isFromClient(),
                        resizedEvent,
                        dataProviderManager.getDataProvider(),
                        getRelatedDataProviderContexts(clientEvent.getRelatedEvents()),
                        getRelatedEvents(clientEvent.getRelatedEvents()),
                        createOldValues(clientEvent.getOldEvent()),
                        clientEvent.getStartDelta() == null ? null : createDelta(clientEvent.getStartDelta()),
                        clientEvent.getEndDelta() == null ? null : createDelta(clientEvent.getEndDelta()),
                        new MouseEventDetails(clientEvent.getMouseDetails()),
                        createDisplayModeInfo(clientEvent.getView())));
    }

    @Override
    protected void onDateClick(DateClickDomEvent event) {
        DomDateClick clientEvent = deserializer.deserialize(event.getContext(), DomDateClick.class);

        getEventBus().fireEvent(
                new DateClickEvent(this, event.isFromClient(),
                        new MouseEventDetails(clientEvent.getMouseDetails()),
                        toLocalDateTime(clientEvent.getDateTime()),
                        clientEvent.isAllDay(),
                        createDisplayModeInfo(clientEvent.getView())));
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
                        createDisplayModeInfo(clientEvent.getView())));
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
                        createDisplayModeInfo(clientEvent.getView()),
                        mouseEventDetails));
    }

    @Override
    protected void onDayNavigationLinkClick(DayNavigationLinkClickDomEvent event) {
        getEventBus().fireEvent(new DayNavigationLinkClickEvent(this, event.isFromClient(),
                parseIsoDate(event.getDate())));
    }

    @Override
    protected void onWeekNavigationLinkClick(WeekNavigationLinkClickDomEvent event) {
        getEventBus().fireEvent(new WeekNavigationLinkClickEvent(this, event.isFromClient(),
                parseIsoDate(event.getDate())));
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
                        createDisplayModeInfo(clientContext.getView())));

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
                        createDisplayModeInfo(clientContext.getView())));

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
                        createDisplayModeInfo(clientContext.getView())));

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
                        createDisplayModeInfo(clientContext.getView())));

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
                        createDisplayModeInfo(clientContext.getView())));

        JsonArray classNamesJson = classNames == null
                ? jsonFactory.createArray()
                : getSerializer().toJsonArray(classNames);

        log.debug("Serialized now-indicator's class names: {}", classNamesJson.toJson());

        return classNamesJson;
    }

    @Override
    protected JsonObject getDayCellBottomTextInfo(JsonObject jsonContext) {
        if (getDayCellBottomTextGenerator() == null) {
            return jsonFactory.createObject();
        }

        DomDayCellBottomText clientContext = deserializer.deserialize(jsonContext, DomDayCellBottomText.class);

        String text = getDayCellBottomTextGenerator().apply(
                new DayCellBottomTextContext(
                        this,
                        parseIsoDate(clientContext.getDate()),
                        Objects.requireNonNull(DayOfWeek.fromId(clientContext.getDow())),
                        clientContext.isDisabled(),
                        clientContext.isFuture(),
                        clientContext.isOther(),
                        clientContext.isPast(),
                        clientContext.isToday(),
                        createDisplayModeInfo(clientContext.getView())));

        if (text == null) {
            return jsonFactory.createObject();
        }

        List<String> classNames = null;
        if (getDayCellBottomTextClassNamesGenerator() != null) {
            classNames = getDayCellBottomTextClassNamesGenerator().apply(
                    new DayCellBottomTextClassNamesContext(
                            this,
                            parseIsoDate(clientContext.getDate()),
                            Objects.requireNonNull(DayOfWeek.fromId(clientContext.getDow())),
                            clientContext.isDisabled(),
                            clientContext.isFuture(),
                            clientContext.isOther(),
                            clientContext.isPast(),
                            clientContext.isToday(),
                            text,
                            createDisplayModeInfo(clientContext.getView())
                    ));
        }

        JsonObject result = jsonFactory.createObject();
        result.put("text", text);
        if (classNames != null) {
            result.put("classNames", getSerializer().toJsonArray(classNames));
        }
        return result;
    }

    protected ItemsDataProviderManager createDataProviderManager(ItemsCalendarDataProvider dataProvider) {
        return new ItemsDataProviderManager(dataProvider, getSerializer(), this);
    }

    protected CallbackDataProviderManager createCallbackDataProviderManager(CallbackCalendarDataProvider dataProvider) {
        return new CallbackDataProviderManager(dataProvider, getSerializer(), this);
    }

    protected void initTimeZone() {
        TimeZone timeZone = applicationContext.getBean(CurrentAuthentication.class).getTimeZone();

        setTimeZone(timeZone);
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

    protected AbstractDataProviderManager getDataProviderManager(String sourceId) {
        return dataProvidersMap.values().stream()
                .filter(epm -> epm.getSourceId().equals(sourceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no data provider with ID:" + sourceId));
    }

    /**
     * Returns all related calendar events, even if they are from different data providers.
     * <p>
     * Note, it applies changes to {@link CalendarEvent}.
     *
     * @param relatedEvents list of raw related events
     * @return related calendar events
     */
    protected List<CalendarEvent> getRelatedEvents(List<DomCalendarEvent> relatedEvents) {
        List<CalendarEvent> calendarEvents = new ArrayList<>(relatedEvents.size());
        for (DomCalendarEvent changedEvent : relatedEvents) {
            AbstractDataProviderManager dataProviderManager = getDataProviderManager(changedEvent.getSourceId());
            CalendarEvent relatedEvent = getCalendarEvent(changedEvent, dataProviderManager);

            calendarEvents.add(relatedEvent);

            applyChangesToCalendarEvent(relatedEvent, changedEvent);
        }
        return calendarEvents;
    }

    protected List<RelatedDataProviderContext> getRelatedDataProviderContexts(List<DomCalendarEvent> relatedEvents) {
        List<RelatedDataProviderContext> contexts = new ArrayList<>(dataProvidersMap.size());

        for (AbstractDataProviderManager dataProviderManager : dataProvidersMap.values()) {
            List<CalendarEvent> calendarEvents = relatedEvents.stream()
                    .filter(e -> dataProviderManager.getSourceId().equals(e.getSourceId()))
                    .map(e -> dataProviderManager.getCalendarEvent(e.getId()))
                    .toList();
            if (!calendarEvents.isEmpty()) {
                contexts.add(new RelatedDataProviderContext(dataProviderManager.getDataProvider(), calendarEvents));
            }
        }
        return contexts;
    }

    protected CalendarEvent getCalendarEvent(DomCalendarEvent clientEvent,
                                             AbstractDataProviderManager dataProviderManager) {
        CalendarEvent calendarEvent = dataProviderManager.getCalendarEvent(clientEvent.getId());
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
        if (!Objects.equals(calendarEvent.getAllDay(), clientEvent.isAllDay())) {
            calendarEvent.setAllDay(clientEvent.isAllDay());
        }
    }

    protected List<DataProviderContext> createMoreLinkDataProviderContexts(DomMoreLinkClick context) {
        List<DataProviderContext> dataProviderContexts = new ArrayList<>();
        for (AbstractDataProviderManager dataProviderManager : dataProvidersMap.values()) {
            DataProviderContext dataProviderContext = createMoreLinkDataProviderContext(
                    dataProviderManager, context.getAllEvents(), context.getHiddenEvents());
            if (dataProviderContext != null) {
                dataProviderContexts.add(dataProviderContext);
            }
        }
        return dataProviderContexts;
    }

    @Nullable
    protected DataProviderContext createMoreLinkDataProviderContext(AbstractDataProviderManager dataProviderManager,
                                                                    List<DomCalendarEvent> allEvents,
                                                                    List<DomCalendarEvent> hiddenEvents) {
        List<DomCalendarEvent> visibleEvents = new ArrayList<>();
        allEvents.stream().filter(s -> !hiddenEvents.contains(s)).forEach(visibleEvents::add);

        List<CalendarEvent> visibleCalendarEvents = toCalendarEvents(dataProviderManager, visibleEvents);
        List<CalendarEvent> hiddenCalendarEvents = toCalendarEvents(dataProviderManager, hiddenEvents);

        if (CollectionUtils.isEmpty(visibleCalendarEvents)
                && CollectionUtils.isEmpty(hiddenCalendarEvents)) {
            return null;
        }

        return new DataProviderContext(dataProviderManager.getDataProvider(), visibleCalendarEvents,
                hiddenCalendarEvents);
    }

    protected List<CalendarEvent> toCalendarEvents(List<DomCalendarEvent> events) {
        List<CalendarEvent> calendarEvents = new ArrayList<>(events.size());
        for (DomCalendarEvent changedEvent : events) {
            AbstractDataProviderManager dataProviderManager = getDataProviderManager(changedEvent.getSourceId());
            calendarEvents.add(getCalendarEvent(changedEvent, dataProviderManager));
        }
        return calendarEvents;
    }

    protected List<CalendarEvent> toCalendarEvents(AbstractDataProviderManager dataProviderManager,
                                                   List<DomCalendarEvent> segments) {
        return segments.stream()
                .filter(e -> e.getSourceId().equals(dataProviderManager.getSourceId()))
                .map(e -> dataProviderManager.getCalendarEvent(e.getId()))
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

    protected DisplayModeInfo createDisplayModeInfo(DomViewInfo clientViewInfo) {
        return new DisplayModeInfo(
                parseIsoDate(clientViewInfo.getActiveStart()),
                parseIsoDate(clientViewInfo.getActiveEnd()),
                parseIsoDate(clientViewInfo.getCurrentStart()),
                parseIsoDate(clientViewInfo.getCurrentEnd()),
                getDisplayMode(clientViewInfo.getType()));
    }

    protected FullCalendarI18n createDefaultI18n() {
        return new FullCalendarI18n()
                .withDirection(FullCalendarI18n.Direction.valueOf(getMessage("i18n.direction").toUpperCase()))
                .withFirstDayOfWeek(DayOfWeek.valueOf(getMessage("i18n.firstDayOfWeek")))
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
        DayGridDayProperties dayGridDay = getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_DAY);
        dayGridDay.setDayPopoverFormat(getMessage("dayGridDayDayPopoverFormat"));
        dayGridDay.setDayHeaderFormat(getMessage("dayGridDayDayHeaderFormat"));
        dayGridDay.setWeekNumberFormat(getMessage("dayGridDayWeekNumberFormat"));
        dayGridDay.setEventTimeFormat(getMessage("dayGridDayEventTimeFormat"));

        DayGridWeekProperties dayGridWeek = getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_WEEK);
        dayGridWeek.setDayPopoverFormat(getMessage("dayGridWeekDayDayPopoverFormat"));
        dayGridWeek.setDayHeaderFormat(getMessage("dayGridWeekDayHeaderFormat"));
        dayGridWeek.setWeekNumberFormat(getMessage("dayGridWeekWeekNumberFormat"));
        dayGridWeek.setEventTimeFormat(getMessage("dayGridWeekEventTimeFormat"));

        DayGridMonthProperties dayGridMonth = getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_MONTH);
        dayGridMonth.setDayPopoverFormat(getMessage("dayGridMonthDayPopoverFormat"));
        dayGridMonth.setDayHeaderFormat(getMessage("dayGridMonthDayHeaderFormat"));
        dayGridMonth.setWeekNumberFormat(getMessage("dayGridMonthWeekNumberFormat"));
        dayGridMonth.setEventTimeFormat(getMessage("dayGridMonthEventTimeFormat"));

        DayGridYearProperties dayGridYear = getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_YEAR);
        dayGridYear.setDayPopoverFormat(getMessage("dayGridYearDayPopoverFormat"));
        dayGridYear.setDayHeaderFormat(getMessage("dayGridYearDayHeaderFormat"));
        dayGridYear.setWeekNumberFormat(getMessage("dayGridYearWeekNumberFormat"));
        dayGridYear.setEventTimeFormat(getMessage("dayGridYearEventTimeFormat"));
        dayGridYear.setMonthStartFormat(getMessage("dayGridYearMonthStartFormat"));
    }

    protected void setupTimeGridLocalizedFormats() {
        TimeGridDayProperties timeGridDay = getCalendarDisplayModeProperties(CalendarDisplayModes.TIME_GRID_DAY);
        timeGridDay.setDayPopoverFormat(getMessage("timeGridDayDayPopoverFormat"));
        timeGridDay.setDayHeaderFormat(getMessage("timeGridDayDayHeaderFormat"));
        timeGridDay.setWeekNumberFormat(getMessage("timeGridDayWeekNumberFormat"));
        timeGridDay.setEventTimeFormat(getMessage("timeGridDayEventTimeFormat"));
        timeGridDay.setSlotLabelFormat(getMessage("timeGridDaySlotLabelFormat"));

        TimeGridWeekProperties timeGridWeek = getCalendarDisplayModeProperties(CalendarDisplayModes.TIME_GRID_WEEK);
        timeGridWeek.setDayPopoverFormat(getMessage("timeGridWeekDayPopoverFormat"));
        timeGridWeek.setDayHeaderFormat(getMessage("timeGridWeekDayHeaderFormat"));
        timeGridWeek.setWeekNumberFormat(getMessage("timeGridWeekWeekNumberFormat"));
        timeGridWeek.setEventTimeFormat(getMessage("timeGridWeekEventTimeFormat"));
        timeGridWeek.setSlotLabelFormat(getMessage("timeGridWeekSlotLabelFormat"));
    }

    protected void setupListLocalizedFormats() {
        ListDayProperties listDay = getCalendarDisplayModeProperties(CalendarDisplayModes.LIST_DAY);
        listDay.setListDayFormat(getMessage("listDayListDayFormat"));
        listDay.setListDaySideFormat(getMessage("listDayListDaySideFormat"));

        ListWeekProperties listWeek = getCalendarDisplayModeProperties(CalendarDisplayModes.LIST_WEEK);
        listWeek.setListDayFormat(getMessage("listWeekListDayFormat"));
        listWeek.setListDaySideFormat(getMessage("listWeekListDaySideFormat"));

        ListMonthProperties listMonth = getCalendarDisplayModeProperties(CalendarDisplayModes.LIST_MONTH);
        listMonth.setListDayFormat(getMessage("listMonthListDayFormat"));
        listMonth.setListDaySideFormat(getMessage("listMonthListDaySideFormat"));

        ListYearProperties listYear = getCalendarDisplayModeProperties(CalendarDisplayModes.LIST_YEAR);
        listYear.setListDayFormat(getMessage("listYearListDayFormat"));
        listYear.setListDaySideFormat(getMessage("listYearListDaySideFormat"));
    }

    protected void setupMultiLocalizedFormats() {
        MultiMonthYearProperties multiMonthYear = getCalendarDisplayModeProperties(CalendarDisplayModes.MULTI_MONTH_YEAR);
        multiMonthYear.setMultiMonthTitleFormat(getMessage("multiMonthYearMultiMonthTitleFormat"));
    }

    protected void setupDefaultLocalizedFormats() {
        setDefaultDayPopoverFormat(getMessage("dayPopoverFormat"));
        setDefaultDayHeaderFormat(getMessage("dayHeaderFormat"));
        setDefaultWeekNumberFormat(getMessage("weekNumberFormat"));
        setDefaultSlotLabelFormat(getMessage("slotLabelFormat"));
        setDefaultEventTimeFormat(getMessage("eventTimeFormat"));
    }

    /**
     * Method is overridden to make it available from {@link FullCalendarUtils#getDisplayMode(FullCalendar, String)}.
     *
     * @param id display mode id
     * @return calendar display mode instance
     */
    @Override
    protected CalendarDisplayMode getDisplayMode(String id) {
        return super.getDisplayMode(id);
    }

    @Override
    protected void onCompleteInitialization() {
        super.onCompleteInitialization();

        // If data providers were added in period after calling onAttach()
        // and before onCompleteInitialization(). For instance, in ReadyEvent of View.
        if (!pendingDataProviders.isEmpty()) {
            pendingDataProviders.forEach(this::addDataProviderInternal);
            pendingDataProviders.clear();
        }
    }

    @Override
    protected void addDataProvidersOnAttach() {
        getUI().ifPresent(ui -> {
            ui.beforeClientResponse(this, (context) -> {
                dataProvidersMap.values().forEach(this::addDataProviderInternal);
            });
        });
    }
}
