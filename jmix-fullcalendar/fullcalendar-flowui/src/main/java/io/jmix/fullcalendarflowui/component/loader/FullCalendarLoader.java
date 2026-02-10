package io.jmix.fullcalendarflowui.component.loader;

import com.google.common.base.Strings;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.impl.FetchPlanLoader;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.fullcalendarflowui.component.FullCalendarI18n;
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.model.Display;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.AbstractEntityCalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.ContainerCalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever;
import io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.ItemsCalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.CallbackCalendarDataProvider;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FullCalendarLoader extends AbstractComponentLoader<FullCalendar> {

    protected DisplayModePropertiesLoader displayModeProperties;

    @Override
    protected FullCalendar createComponent() {
        return factory.create(FullCalendar.class);
    }

    @Override
    public void loadComponent() {
        loadId(resultComponent, element);

        componentLoaderSupport.loadSizeAttributes(resultComponent, element);
        componentLoaderSupport.loadClassNames(resultComponent, element);

        loadBoolean(element, "allDayMaintainDurationEnabled",
                resultComponent::setAllDayMaintainDurationEnabled);
        loadBoolean(element, "dayHeadersVisible", resultComponent::setDayHeadersVisible);
        loadInteger(element, "dayMaxEvents", resultComponent::setDayMaxEvents);
        loadInteger(element, "dayMaxEventRows", resultComponent::setDayMaxEventRows);
        loadString(element, "dateAlignment", resultComponent::setDateAlignment);
        loadBoolean(element, "defaultAllDay", resultComponent::setDefaultAllDay);
        loadDuration(element, "defaultAllDayEventDuration", resultComponent::setDefaultAllDayEventDuration);
        loadResourceString(element, "defaultDayHeaderFormat", context.getMessageGroup(),
                resultComponent::setDefaultDayHeaderFormat);
        loadBoolean(element, "defaultDayMaxEventRowsEnabled",
                resultComponent::setDefaultDayMaxEventRowsEnabled);
        loadBoolean(element, "defaultDayMaxEventsEnabled",
                resultComponent::setDefaultDayMaxEventsEnabled);
        loadResourceString(element, "defaultDayPopoverFormat", context.getMessageGroup(),
                resultComponent::setDefaultDayPopoverFormat);
        loadBoolean(element, "defaultBusinessHoursEnabled",
                resultComponent::setDefaultBusinessHoursEnabled);
        loadResourceString(element, "defaultEventTimeFormat", context.getMessageGroup(),
                resultComponent::setDefaultEventTimeFormat);
        loadResourceString(element, "defaultSlotLabelFormat", context.getMessageGroup(),
                resultComponent::setDefaultSlotLabelFormat);
        loadDuration(element, "defaultTimedEventDuration", resultComponent::setDefaultTimedEventDuration);
        loadResourceString(element, "defaultWeekNumberFormat", context.getMessageGroup(),
                resultComponent::setDefaultWeekNumberFormat);
        loadBoolean(element, "displayEventTime", resultComponent::setDisplayEventTime);
        loadInteger(element, "dragRevertDuration", resultComponent::setDragRevertDuration);
        loadBoolean(element, "dragScroll", resultComponent::setDragScroll);

        loadString(element, "eventBackgroundColor", resultComponent::setEventBackgroundColor);
        loadString(element, "eventBorderColor", resultComponent::setEventBorderColor);
        loadString(element, "eventConstraintGroupId", resultComponent::setEventConstraintGroupId);
        loadBoolean(element, "eventConstraintBusinessHoursEnabled",
                resultComponent::setEventConstraintBusinessHoursEnabled);
        loadEnum(element, Display.class, "eventDisplay", resultComponent::setEventDisplay);
        loadInteger(element, "eventDragMinDistance", resultComponent::setEventDragMinDistance);
        loadBoolean(element, "eventDurationEditable", resultComponent::setEventDurationEditable);
        loadBoolean(element, "eventInteractive", resultComponent::setEventInteractive);
        loadInteger(element, "eventLongPressDelay", resultComponent::setEventLongPressDelay);
        loadInteger(element, "eventMaxStack", resultComponent::setEventMaxStack);
        loadStringList(element, "eventOrder", resultComponent::setEventOrder);
        loadBoolean(element, "eventOrderStrict", resultComponent::setEventOrderStrict);
        loadBoolean(element, "eventOverlap", resultComponent::setEventOverlap);
        loadBoolean(element, "eventResizableFromStart", resultComponent::setEventResizableFromStart);
        loadInteger(element, "eventSingleClickThreshold", resultComponent::setEventSingleClickThreshold);
        loadBoolean(element, "eventStartEditable", resultComponent::setEventStartEditable);
        loadString(element, "eventTextColor", resultComponent::setEventTextColor);
        loadBoolean(element, "expandRows", resultComponent::setExpandRows);

        loadBoolean(element, "forceEventDuration", resultComponent::setForceEventDuration);

        loadString(element, "initialDate", (s) -> resultComponent.setInitialDate(LocalDate.parse(s)));

        loadStringList(element, "moreLinkClassNames", resultComponent::setMoreLinkClassNames);
        loadMoreLinkDisplayMode(element, resultComponent::setMoreLinkCalendarDisplayMode);

        loadBoolean(element, "navigationLinksEnabled", resultComponent::setNavigationLinksEnabled);
        loadDuration(element, "nextDayThreshold", resultComponent::setNextDayThreshold);
        loadBoolean(element, "nowIndicatorVisible", resultComponent::setNowIndicatorVisible);

        loadBoolean(element, "progressiveEventRendering", resultComponent::setProgressiveEventRendering);

        loadDuration(element, "scrollTime", resultComponent::setScrollTime);
        loadBoolean(element, "scrollTimeReset", resultComponent::setScrollTimeReset);
        loadString(element, "selectConstraintGroupId", resultComponent::setSelectConstraintGroupId);
        loadBoolean(element, "selectConstraintBusinessHoursEnabled",
                resultComponent::setSelectConstraintBusinessHoursEnabled);
        loadBoolean(element, "selectionEnabled", resultComponent::setSelectionEnabled);
        loadInteger(element, "selectLongPressDelay", resultComponent::setSelectLongPressDelay);
        loadInteger(element, "selectMinDistance", resultComponent::setSelectMinDistance);
        loadBoolean(element, "selectMirror", resultComponent::setSelectMirror);
        loadBoolean(element, "selectOverlap", resultComponent::setSelectOverlap);
        loadDuration(element, "slotDuration", resultComponent::setSlotDuration);
        loadDuration(element, "slotLabelInterval", resultComponent::setSlotLabelInterval);
        loadDuration(element, "slotMaxTime", resultComponent::setSlotMaxTime);
        loadDuration(element, "slotMinTime", resultComponent::setSlotMinTime);
        loadDuration(element, "snapDuration", resultComponent::setSnapDuration);

        loadBoolean(element, "unselectAuto", resultComponent::setUnselectAuto);
        loadString(element, "unselectCancelSelector", resultComponent::setUnselectCancelSelector);

        loadBoolean(element, "weekendsVisible", resultComponent::setWeekendsVisible);
        loadBoolean(element, "weekNumbersVisible", resultComponent::setWeekNumbersVisible);
        loadInteger(element, "windowResizeDelay", resultComponent::setWindowResizeDelay);

        loadI18n(element, resultComponent::setI18n);

        displayModeProperties().loadCalendarDisplayModeProperties(element, resultComponent);
        displayModeProperties().loadCustomCalendarDisplayModes(element, resultComponent);
        loadInitialDisplayMode(element, resultComponent);

        loadBusinessHours(element, resultComponent::setBusinessHours);
        loadHiddenDays(element, resultComponent);

        loadDataProviders(element, "containerDataProvider",
                (dp) -> resultComponent.addDataProvider((ItemsCalendarDataProvider) dp));
        loadDataProviders(element, "callbackDataProvider",
                (dp) -> resultComponent.addDataProvider((CallbackCalendarDataProvider) dp));
    }

    protected void loadI18n(Element element, Consumer<FullCalendarI18n> setter) {
        FullCalendarI18n i18n = new FullCalendarI18n();

        boolean loaded = false;

        FullCalendarI18n.Direction direction =
                loadEnum(element, FullCalendarI18n.Direction.class, "direction").orElse(null);
        if (direction != null) {
            loaded = true;
            i18n.setDirection(direction);
        }
        DayOfWeek dayOfWeek = loadEnum(element, DayOfWeek.class, "firstDayOfWeek").orElse(null);
        if (dayOfWeek != null) {
            loaded = true;
            i18n.setFirstDayOfWeek(dayOfWeek);
        }
        Integer dayOfYear = loadInteger(element, "dayOfYear").orElse(null);
        if (dayOfYear != null) {
            loaded = true;
            i18n.setDayOfYear(dayOfYear);
        }
        String weekTextLong = loadResourceString(element, "weekTextLong", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(weekTextLong)) {
            loaded = true;
            i18n.setWeekTextLong(weekTextLong);
        }
        String allDayText = loadResourceString(element, "allDayText", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(allDayText)) {
            loaded = true;
            i18n.setAllDayText(allDayText);
        }
        String moreLinkText = loadResourceString(element, "moreLinkText", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(moreLinkText)) {
            loaded = true;
            i18n.setMoreLinkText(moreLinkText);
        }
        String noEventsText = loadResourceString(element, "noEventsText", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(noEventsText)) {
            loaded = true;
            i18n.setNoEventsText(noEventsText);
        }
        String closeHint = loadResourceString(element, "closeHint", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(closeHint)) {
            loaded = true;
            i18n.setCloseHint(closeHint);
        }
        String eventHint = loadResourceString(element, "eventHint", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(eventHint)) {
            loaded = true;
            i18n.setEventHint(eventHint);
        }
        String timeHint = loadResourceString(element, "timeHint", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(timeHint)) {
            loaded = true;
            i18n.setTimeHint(timeHint);
        }
        String navLinkHint = loadResourceString(element, "navLinkHint", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(navLinkHint)) {
            loaded = true;
            i18n.setNavLinkHint(navLinkHint);
        }
        String moreLinkHint = loadResourceString(element, "moreLinkHint", context.getMessageGroup())
                .orElse(null);
        if (!Strings.isNullOrEmpty(moreLinkHint)) {
            loaded = true;
            i18n.setMoreLinkHint(moreLinkHint);
        }

        if (loaded) {
            setter.accept(i18n);
        }
    }

    protected void loadDataProviders(Element element, String providerTag,
                                     Consumer<CalendarDataProvider> setter) {
        Element dataProvidersElement = element.element("dataProviders");
        if (dataProvidersElement != null) {
            dataProvidersElement.elements(providerTag)
                    .forEach(provider -> setter.accept(loadDataProvider(provider)));
        }
    }

    protected AbstractEntityCalendarDataProvider loadDataProvider(Element dataProviderElement) {
        AbstractEntityCalendarDataProvider calendarItems;
        if (dataProviderElement.getName().equals("containerDataProvider")) {
            InstanceContainer<?> container = loadDataContainer(dataProviderElement);
            calendarItems = createCalendarItems(dataProviderElement, container);

            loadBaseContainerProperties(dataProviderElement, calendarItems);
        } else if (dataProviderElement.getName().equals("callbackDataProvider")) {
            calendarItems = createLazyCalendarItems(dataProviderElement);

            loadBaseContainerProperties(dataProviderElement, calendarItems);

            Element itemsQueryElement = dataProviderElement.element("itemsQuery");
            if (itemsQueryElement != null) {
                loadItemsQuery(itemsQueryElement, (EntityCalendarDataRetriever) calendarItems);
            }
        } else {
            throw new GuiDevelopmentException("Unknown data provider tag:" + dataProviderElement.getName(), context);
        }

        return calendarItems;
    }

    protected void loadBaseContainerProperties(Element dataProviderElement,
                                               AbstractEntityCalendarDataProvider calendarItems) {
        loadString(dataProviderElement, "groupId", calendarItems::setGroupIdProperty);
        loadString(dataProviderElement, "allDay", calendarItems::setAllDayProperty);
        loadString(dataProviderElement, "startDateTime", calendarItems::setStartDateTimeProperty);
        loadString(dataProviderElement, "endDateTime", calendarItems::setEndDateTimeProperty);
        loadString(dataProviderElement, "title", calendarItems::setTitleProperty);
        loadString(dataProviderElement, "description", calendarItems::setDescriptionProperty);
        loadString(dataProviderElement, "interactive", calendarItems::setInteractiveProperty);
        loadString(dataProviderElement, "classNames", calendarItems::setClassNamesProperty);
        loadString(dataProviderElement, "startEditable", calendarItems::setStartEditableProperty);
        loadString(dataProviderElement, "durationEditable", calendarItems::setDurationEditableProperty);
        loadString(dataProviderElement, "display", calendarItems::setDisplayProperty);
        loadString(dataProviderElement, "overlap", calendarItems::setOverlapProperty);
        loadString(dataProviderElement, "constraint", calendarItems::setConstraintProperty);
        loadString(dataProviderElement, "backgroundColor", calendarItems::setBackgroundColorProperty);
        loadString(dataProviderElement, "borderColor", calendarItems::setBorderColorProperty);
        loadString(dataProviderElement, "textColor", calendarItems::setTextColorProperty);

        loadStringList(dataProviderElement, "additionalProperties", calendarItems::setAdditionalProperties);

        loadString(dataProviderElement, "recurringDaysOfWeek",
                calendarItems::setRecurringDaysOfWeekProperty);
        loadString(dataProviderElement, "recurringStartDate",
                calendarItems::setRecurringStartDateProperty);
        loadString(dataProviderElement, "recurringEndDate",
                calendarItems::setRecurringEndDateProperty);
        loadString(dataProviderElement, "recurringStartTime",
                calendarItems::setRecurringStartTimeProperty);
        loadString(dataProviderElement, "recurringEndTime",
                calendarItems::setRecurringEndTimeProperty);
    }

    protected AbstractEntityCalendarDataProvider createCalendarItems(Element dataProviderElement,
                                                                        InstanceContainer<?> container) {
        String id = loadString(dataProviderElement, "id").orElse(null);
        return Strings.isNullOrEmpty(id)
                ? new ContainerCalendarDataProvider<>(container)
                : new ContainerCalendarDataProvider<>(id, container);
    }

    protected AbstractEntityCalendarDataProvider createLazyCalendarItems(Element dataProviderElement) {
        String id = loadString(dataProviderElement, "id").orElse(null);
        return Strings.isNullOrEmpty(id)
                ? applicationContext.getBean(EntityCalendarDataRetriever.class)
                : applicationContext.getBean(EntityCalendarDataRetriever.class, id);
    }

    protected InstanceContainer<?> loadDataContainer(Element dataProviderElement) {
        String dataContainer = dataProviderElement.attributeValue("dataContainer");
        if (Strings.isNullOrEmpty(dataContainer)) {
            throw new GuiDevelopmentException("Data provider must specify a dataContainer attribute", context);
        }
        return context.getDataHolder().getContainer(dataContainer);
    }

    protected void loadItemsQuery(Element itemsQueryElement, EntityCalendarDataRetriever lazyCalendarItems) {
        Class<?> entityClass = loaderSupport.loadString(itemsQueryElement, "class")
                .map(ReflectionHelper::getClass)
                .orElseThrow(() -> new GuiDevelopmentException("Entity class must be specified", context));
        lazyCalendarItems.setEntityClass(entityClass);

        loadQuery(itemsQueryElement, lazyCalendarItems::setQueryString);
        lazyCalendarItems.setFetchPlan(loadFetchPlan(itemsQueryElement, entityClass));
    }

    protected void loadQuery(Element itemsQueryElement, Consumer<String> setter) {
        Element queryElement = itemsQueryElement.element("query");
        if (queryElement != null) {
            setter.accept(queryElement.getTextTrim());
        }
    }

    @Nullable
    protected FetchPlan loadFetchPlan(Element itemsQueryElement, Class<?> entityClass) {
        Element fetchPlanElement = itemsQueryElement.element("fetchPlan");
        if (fetchPlanElement != null) {
            return loadInlineFetchPlan(fetchPlanElement, entityClass);
        }

        FetchPlanRepository fetchPlanRepository = applicationContext.getBean(FetchPlanRepository.class);

        return loaderSupport.loadString(itemsQueryElement, "fetchPlan")
                .map(fetchPlanName ->
                        fetchPlanRepository.getFetchPlan(entityClass, fetchPlanName))
                .orElse(null);
    }

    protected FetchPlan loadInlineFetchPlan(Element fetchPlanElement, Class<?> entityClass) {
        FetchPlanLoader fetchPlanLoader = applicationContext.getBean(FetchPlanLoader.class);
        FetchPlanLoader.FetchPlanInfo fetchPlanInfo = fetchPlanLoader.getFetchPlanInfo(
                fetchPlanElement, applicationContext.getBean(Metadata.class).getClass(entityClass)
        );

        FetchPlanRepository fetchPlanRepository = applicationContext.getBean(FetchPlanRepository.class);

        FetchPlanBuilder builder = fetchPlanLoader.getFetchPlanBuilder(fetchPlanInfo, name ->
                fetchPlanRepository.getFetchPlan(fetchPlanInfo.getMetaClass(), name));

        fetchPlanLoader.loadFetchPlanProperties(fetchPlanElement, builder,
                fetchPlanInfo.isSystemProperties(), fetchPlanRepository::getFetchPlan);

        return builder.build();
    }

    protected void loadMoreLinkDisplayMode(Element element, Consumer<CalendarDisplayMode> setter) {
        loadString(element, "moreLinkDisplayMode")
                .ifPresent(t -> {
                    List<Enum<?>> calendarDisplayModes = List.of(CalendarDisplayModes.values());

                    calendarDisplayModes.stream().filter(e -> e.name().contains(t))
                            .findFirst()
                            .ifPresentOrElse(
                                    e -> setter.accept((CalendarDisplayMode) e),
                                    () -> setter.accept(() -> t));
                });
    }

    protected void loadStringList(Element element, String attribute, Consumer<List<String>> setter) {
        loadString(element, attribute)
                .ifPresent(names -> setter.accept(split(names)));
    }

    protected List<String> split(String names) {
        return Arrays.stream(names.split("[\\s,]+"))
                .filter(split -> !Strings.isNullOrEmpty(split))
                .toList();
    }

    protected void loadDuration(Element element, String attribute, Consumer<CalendarDuration> setter) {
        loadString(element, attribute)
                .ifPresent(s -> {
                    Duration javaDuration = Duration.parse(s);
                    setter.accept(CalendarDuration.ofDuration(javaDuration));
                });
    }

    protected void loadBusinessHours(Element element, Consumer<List<CalendarBusinessHours>> setter) {
        Element businessHoursElement = element.element("businessHours");
        if (businessHoursElement != null) {
            List<CalendarBusinessHours> businessHours = new ArrayList<>();
            for (Element entry : businessHoursElement.elements("entry")) {
                CalendarBusinessHours bh = loadBusinessHoursEntry(entry);
                if (bh != null) {
                    businessHours.add(bh);
                }
            }
            if (!businessHours.isEmpty()) {
                setter.accept(businessHours);
            }
        }
    }

    @Nullable
    protected CalendarBusinessHours loadBusinessHoursEntry(Element element) {
        LocalTime startTime = loadString(element, "startTime")
                .map(s -> LocalTime.ofSecondOfDay(Duration.parse(s).toSeconds()))
                .orElse(null);
        LocalTime endTime = loadString(element, "endTime")
                .map(s -> LocalTime.ofSecondOfDay(Duration.parse(s).toSeconds()))
                .orElse(null);

        List<DayOfWeek> businessDays = new ArrayList<>();
        element.elements("day")
                .forEach(e -> {
                    loadString(e, "name")
                            .ifPresent(n -> businessDays.add(DayOfWeek.valueOf(n)));
                });
        if (startTime == null) {
            return businessDays.isEmpty()
                    ? null
                    : CalendarBusinessHours.of(businessDays.toArray(new DayOfWeek[0]));
        }
        if (endTime == null) {
            return businessDays.isEmpty()
                    ? CalendarBusinessHours.of(startTime)
                    : CalendarBusinessHours.of(startTime, businessDays.toArray(new DayOfWeek[0]));
        }
        return businessDays.isEmpty()
                ? CalendarBusinessHours.of(startTime, endTime)
                : CalendarBusinessHours.of(startTime, endTime, businessDays.toArray(new DayOfWeek[0]));
    }

    protected void loadInitialDisplayMode(Element element, FullCalendar resultComponent) {
        loadString(element, "initialDisplayMode", (displayMode) -> {
            CalendarDisplayMode calendarDisplayMode = displayModeProperties().getDisplayMode(displayMode, resultComponent);
            resultComponent.setInitialCalendarDisplayMode(calendarDisplayMode);
        });
    }

    protected void loadHiddenDays(Element element, FullCalendar resultComponent) {
        Element hiddenDaysElement = element.element("hiddenDays");
        if (hiddenDaysElement == null) {
            return;
        }

        List<DayOfWeek> hiddenDays = new ArrayList<>();
        hiddenDaysElement.elements("day")
                .forEach(e -> {
                    loadString(e, "name")
                            .ifPresent(n -> hiddenDays.add(DayOfWeek.valueOf(n)));
                });

        if (!hiddenDays.isEmpty()) {
            resultComponent.setHiddenDays(hiddenDays);
        }
    }

    protected DisplayModePropertiesLoader displayModeProperties() {
        if (displayModeProperties == null) {
            displayModeProperties = new DisplayModePropertiesLoader(loaderSupport, context);
        }
        return displayModeProperties;
    }
}
