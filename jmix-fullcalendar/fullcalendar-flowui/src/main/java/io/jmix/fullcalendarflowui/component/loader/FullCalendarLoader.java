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
import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendar.Display;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.AbstractEntityEventProvider;
import io.jmix.fullcalendarflowui.component.data.ContainerCalendarEventProvider;
import io.jmix.fullcalendarflowui.component.data.LazyEntityCalendarEventRetriever;
import io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours;
import io.jmix.fullcalendarflowui.component.data.BaseCalendarEventProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEventProvider;
import io.jmix.fullcalendarflowui.component.data.LazyCalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import org.dom4j.Element;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FullCalendarLoader extends AbstractComponentLoader<FullCalendar> {

    protected ViewPropertiesLoader viewPropertiesLoader;

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
                resultComponent::setDefaultSlotNumberFormat);
        loadDuration(element, "defaultTimedEventDuration", resultComponent::setDefaultTimedEventDuration);
        loadResourceString(element, "defaultWeekNumberFormat", context.getMessageGroup(),
                resultComponent::setDefaultWeekNumberFormat);
        loadBoolean(element, "displayEventTime", resultComponent::setDisplayEventTime);
        loadInteger(element, "dragRevertDuration", resultComponent::setDragRevertDuration);
        loadBoolean(element, "dragScroll", resultComponent::setDragScroll);

        loadString(element, "eventBackgroundColor", resultComponent::setEventBackgroundColor);
        loadString(element, "eventBorderColor", resultComponent::setEventBorderColor);
        loadString(element, "eventConstraintGroupId", resultComponent::setEventConstraintGroupId);
        loadEnum(element, Display.class, "eventDisplay", resultComponent::setEventDisplay);
        loadInteger(element, "eventDragMinDistance", resultComponent::setEventDragMinDistance);
        loadBoolean(element, "eventDurationEditable", resultComponent::setEventDurationEditable);
        loadBoolean(element, "eventInteractive", resultComponent::setEventInteractive);
        loadInteger(element, "eventMaxStack", resultComponent::setEventMaxStack);
        loadStringList(element, "eventOrder", resultComponent::setEventOrder);
        loadBoolean(element, "eventOrderStrict", resultComponent::setEventOrderStrict);
        loadBoolean(element, "eventOverlap", resultComponent::setEventOverlap);
        loadBoolean(element, "eventResizableFromStart", resultComponent::setEventResizableFromStart);
        loadBoolean(element, "eventStartEditable", resultComponent::setEventStartEditable);
        loadString(element, "eventTextColor", resultComponent::setEventTextColor);
        loadBoolean(element, "expandRows", resultComponent::setExpandRows);

        loadEnum(element, DayOfWeek.class, "firstDayOfWeek", resultComponent::setFirstDayOfWeek);
        loadBoolean(element, "forceEventDuration", resultComponent::setForceEventDuration);

        loadString(element, "initialDate", (s) -> resultComponent.setInitialDate(LocalDate.parse(s)));

        loadInteger(element, "longPressDelay", resultComponent::setLongPressDelay);

        loadStringList(element, "moreLinkClassNames", resultComponent::setMoreLinkClassNames);
        loadMoreLinkView(element, resultComponent::setMoreLinkCalendarView);

        loadBoolean(element, "navigationLinksEnabled", resultComponent::setNavigationLinksEnabled);
        loadDuration(element, "nextDayThreshold", resultComponent::setNextDayThreshold);
        loadBoolean(element, "nowIndicatorVisible", resultComponent::setNowIndicatorVisible);

        loadBoolean(element, "progressiveEventRendering", resultComponent::setProgressiveEventRendering);

        loadDuration(element, "scrollTime", resultComponent::setScrollTime);
        loadBoolean(element, "scrollTimeReset", resultComponent::setScrollTimeReset);
        loadString(element, "selectConstraintGroupId", resultComponent::setSelectConstraintGroupId);
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

        viewProperties().loadCalendarViewProperties(element, resultComponent);
        viewProperties().loadCustomCalendarViews(element, resultComponent);
        loadInitialView(element, resultComponent);

        loadBusinessHours(element, resultComponent::setBusinessHours);
        loadHiddenDays(element, resultComponent);

        loadEventProviders(element, "containerEventProvider",
                (ep) -> resultComponent.addEventProvider((CalendarEventProvider) ep));
        loadEventProviders(element, "lazyEventProvider",
                (ep) -> resultComponent.addEventProvider((LazyCalendarEventProvider) ep));
    }

    protected void loadEventProviders(Element element, String providerTag,
                                      Consumer<BaseCalendarEventProvider> setter) {
        Element eventProvidersElement = element.element("eventProviders");
        if (eventProvidersElement != null) {
            eventProvidersElement.elements(providerTag)
                    .forEach(provider -> setter.accept(loadEventProvider(provider)));
        }
    }

    protected AbstractEntityEventProvider<?> loadEventProvider(Element eventProviderElement) {
        AbstractEntityEventProvider<?> calendarItems;
        if (eventProviderElement.getName().equals("containerEventProvider")) {
            InstanceContainer<?> container = loadDataContainer(eventProviderElement);
            calendarItems = createCalendarItems(eventProviderElement, container);

            loadBaseContainerProperties(eventProviderElement, calendarItems);
        } else if (eventProviderElement.getName().equals("lazyEventProvider")) {
            calendarItems = createLazyCalendarItems(eventProviderElement);

            loadBaseContainerProperties(eventProviderElement, calendarItems);

            Element itemsQueryElement = eventProviderElement.element("itemsQuery");
            if (itemsQueryElement != null) {
                loadItemsQuery(itemsQueryElement, (LazyEntityCalendarEventRetriever) calendarItems);
            }
        } else {
            throw new GuiDevelopmentException("Unknown event provider tag:" + eventProviderElement.getName(), context);
        }

        return calendarItems;
    }

    protected void loadBaseContainerProperties(Element eventProviderElement,
                                               AbstractEntityEventProvider<?> calendarItems) {
        loadString(eventProviderElement, "groupId", calendarItems::setGroupIdProperty);
        loadString(eventProviderElement, "allDay", calendarItems::setAllDayProperty);
        loadString(eventProviderElement, "startDateTime", calendarItems::setStartDateTimeProperty);
        loadString(eventProviderElement, "endDateTime", calendarItems::setEndDateTimeProperty);
        loadString(eventProviderElement, "title", calendarItems::setTitleProperty);
        loadString(eventProviderElement, "description", calendarItems::setDescriptionProperty);
        loadString(eventProviderElement, "interactive", calendarItems::setInteractiveProperty);
        loadString(eventProviderElement, "classNames", calendarItems::setClassNamesProperty);
        loadString(eventProviderElement, "startEditable", calendarItems::setStartEditableProperty);
        loadString(eventProviderElement, "durationEditable", calendarItems::setDurationEditableProperty);
        loadString(eventProviderElement, "display", calendarItems::setDisplayProperty);
        loadString(eventProviderElement, "overlap", calendarItems::setOverlapProperty);
        loadString(eventProviderElement, "constraint", calendarItems::setConstraintProperty);
        loadString(eventProviderElement, "backgroundColor", calendarItems::setBackgroundColorProperty);
        loadString(eventProviderElement, "borderColor", calendarItems::setBorderColorProperty);
        loadString(eventProviderElement, "textColor", calendarItems::setTextColorProperty);

        loadStringList(eventProviderElement, "additionalProperties", calendarItems::setAdditionalProperties);

        loadString(eventProviderElement, "recurringDaysOfWeek",
                calendarItems::setRecurringDaysOfWeekProperty);
        loadString(eventProviderElement, "recurringStartDate",
                calendarItems::setRecurringStartDateProperty);
        loadString(eventProviderElement, "recurringEndDate",
                calendarItems::setRecurringEndDateProperty);
        loadString(eventProviderElement, "recurringStartTime",
                calendarItems::setRecurringStartTimeProperty);
        loadString(eventProviderElement, "recurringEndTime",
                calendarItems::setRecurringEndTimeProperty);
    }

    protected AbstractEntityEventProvider<?> createCalendarItems(Element eventProviderElement,
                                                                 InstanceContainer<?> container) {
        String id = loadString(eventProviderElement, "id").orElse(null);
        return Strings.isNullOrEmpty(id)
                ? new ContainerCalendarEventProvider<>(container)
                : new ContainerCalendarEventProvider<>(id, container);
    }

    protected AbstractEntityEventProvider<?> createLazyCalendarItems(Element eventProviderElement) {
        String id = loadString(eventProviderElement, "id").orElse(null);
        return Strings.isNullOrEmpty(id)
                ? applicationContext.getBean(LazyEntityCalendarEventRetriever.class)
                : applicationContext.getBean(LazyEntityCalendarEventRetriever.class, id);
    }

    protected InstanceContainer<?> loadDataContainer(Element eventProviderElement) {
        String dataContainer = eventProviderElement.attributeValue("dataContainer");
        if (Strings.isNullOrEmpty(dataContainer)) {
            throw new GuiDevelopmentException("Event provider must specify a dataContainer attribute", context);
        }
        return context.getDataHolder().getContainer(dataContainer);
    }

    protected void loadItemsQuery(Element itemsQueryElement, LazyEntityCalendarEventRetriever lazyCalendarItems) {
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

    protected void loadMoreLinkView(Element element, Consumer<CalendarView> setter) {
        loadString(element, "moreLinkView")
                .ifPresent(t -> {
                    List<Enum<?>> calendarViews = List.of(CalendarViewType.values());

                    calendarViews.stream().filter(e -> e.name().contains(t))
                            .findFirst()
                            .ifPresentOrElse(
                                    e -> setter.accept((CalendarView) e),
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
        if (startTime == null && endTime == null && businessDays.isEmpty()) {
            return null;
        }
        return CalendarBusinessHours.of(startTime, endTime, businessDays.toArray(new DayOfWeek[0]));
    }

    protected void loadInitialView(Element element, FullCalendar resultComponent) {
        loadString(element, "initialView", (view) -> {
            CalendarView calendarView = viewProperties().getView(view, resultComponent);
            resultComponent.setInitialCalendarView(calendarView);
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

    protected ViewPropertiesLoader viewProperties() {
        if (viewPropertiesLoader == null) {
            viewPropertiesLoader = new ViewPropertiesLoader(loaderSupport, context);
        }
        return viewPropertiesLoader;
    }
}
