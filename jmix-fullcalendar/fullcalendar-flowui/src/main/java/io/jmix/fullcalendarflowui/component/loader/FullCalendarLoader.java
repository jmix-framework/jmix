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
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.AbstractEntityCalendarItems;
import io.jmix.fullcalendarflowui.component.data.ContainerCalendarItems;
import io.jmix.fullcalendarflowui.component.data.LazyCalendarItems;
import io.jmix.fullcalendarflowui.component.model.BusinessHours;
import io.jmix.fullcalendarflowui.kit.component.data.CalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.data.ItemCalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.data.LazyCalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarView;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarViewType;
import org.dom4j.Element;
import org.springframework.lang.Nullable;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FullCalendarLoader extends AbstractComponentLoader<FullCalendar> {

    @Override
    protected FullCalendar createComponent() {
        return factory.create(FullCalendar.class);
    }

    @Override
    public void loadComponent() {
        loadId(resultComponent, element);

        componentLoaderSupport.loadSizeAttributes(resultComponent, element);
        componentLoaderSupport.loadClassNames(resultComponent, element);

        loadEnum(element, CalendarViewType.class, "initialView", resultComponent::setInitialCalendarView);
        loadBoolean(element, "navigationLinksEnabled", resultComponent::setNavigationLinksEnabled);
        loadBoolean(element, "weekNumbersVisible", resultComponent::setWeekNumbersVisible);
        loadBoolean(element, "dayMaxEventRowsEnabled", resultComponent::setDayMaxEventRowsEnabled);
        loadInteger(element, "dayMaxEventRows", resultComponent::setDayMaxEventRows);
        loadBoolean(element, "dayMaxEventsEnabled", resultComponent::setDayMaxEventsEnabled);
        loadInteger(element, "dayMaxEvents", resultComponent::setDayMaxEvents);
        loadInteger(element, "eventMaxStack", resultComponent::setEventMaxStack);

        loadMoreLinkView(element, resultComponent::setMoreLinkCalendarView);
        loadStringList(element, "moreLinkClassNames", resultComponent::setMoreLinkClassNames);

        loadBoolean(element, "eventStartEditable", resultComponent::setEventStartEditable);
        loadBoolean(element, "eventDurationEditable", resultComponent::setEventDurationEditable);
        loadBoolean(element, "eventResizableFromStart", resultComponent::setEventResizableFromStart);
        loadInteger(element, "eventDragMinDistance", resultComponent::setEventDragMinDistance);
        loadBoolean(element, "eventOverlapEnabled", resultComponent::setEventOverlapEnabled);
        loadBoolean(element, "eventConstraintEnabled", resultComponent::setEventOverlapEnabled);
        loadString(element, "eventConstraintGroupId", resultComponent::setEventConstraintGroupId);

        loadInteger(element, "dragRevertDuration", resultComponent::setDragRevertDuration);
        loadBoolean(element, "dragScrollEnabled", resultComponent::setDragScrollEnabled);

        loadBoolean(element, "allDayMaintainDurationEnabled", resultComponent::setAllDayMaintainDurationEnabled);
        loadSnapDuration(element, resultComponent::setSnapDuration);

        loadBoolean(element, "businessHoursEnabled", resultComponent::setBusinessHoursEnabled);
        loadBusinessHours(element, resultComponent::setBusinessHours);

        loadBoolean(element, "selectionEnabled", resultComponent::setSelectionEnabled);
        loadBoolean(element, "selectMirrorEnabled", resultComponent::setSelectMirrorEnabled);
        loadBoolean(element, "unselectAutoEnabled", resultComponent::setUnselectAutoEnabled);
        loadString(element, "unselectCancelClassName", resultComponent::setUnselectCancelClassName);
        loadBoolean(element, "selectOverlapEnabled", resultComponent::setSelectOverlapEnabled);
        loadBoolean(element, "selectConstraintEnabled", resultComponent::setSelectConstraintEnabled);
        loadString(element, "selectConstraintGroupId", resultComponent::setSelectConstraintGroupId);
        loadInteger(element, "selectMinDistance", resultComponent::setSelectMinDistance);

        loadEventProviders(element, "containerEventProvider",
                (ep) -> resultComponent.addEventProvider((ItemCalendarEventProvider) ep));
        loadEventProviders(element, "lazyEventProvider",
                (ep) -> resultComponent.addEventProvider((LazyCalendarEventProvider) ep));
    }

    protected void loadEventProviders(Element element, String providerTag, Consumer<CalendarEventProvider> setter) {
        Element eventProvidersElement = element.element("eventProviders");
        if (eventProvidersElement != null) {
            eventProvidersElement.elements(providerTag)
                    .forEach(provider -> setter.accept(loadEventProvider(provider)));
        }
    }

    protected AbstractEntityCalendarItems<?> loadEventProvider(Element eventProviderElement) {
        AbstractEntityCalendarItems<?> calendarItems;
        if (eventProviderElement.getName().equals("containerEventProvider")) {
            InstanceContainer<?> container = loadDataContainer(eventProviderElement);
            calendarItems = createCalendarItems(eventProviderElement, container);

            loadBaseContainerProperties(eventProviderElement, calendarItems);
        } else if (eventProviderElement.getName().equals("lazyEventProvider")) {
            calendarItems = createLazyCalendarItems(eventProviderElement);

            loadBaseContainerProperties(eventProviderElement, calendarItems);

            Element itemsQueryElement = eventProviderElement.element("itemsQuery");
            if (itemsQueryElement != null) {
                loadItemsQuery(itemsQueryElement, (LazyCalendarItems) calendarItems);
            }
        } else {
            throw new GuiDevelopmentException("Unknown event provider tag:" + eventProviderElement.getName(), context);
        }

        return calendarItems;
    }

    protected void loadBaseContainerProperties(Element eventProviderElement, AbstractEntityCalendarItems<?> calendarItems) {
        loadString(eventProviderElement, "groupIdProperty", calendarItems::setGroupIdProperty);
        loadString(eventProviderElement, "allDayProperty", calendarItems::setAllDayProperty);
        loadString(eventProviderElement, "startDateTimeProperty", calendarItems::setStartDateTimeProperty);
        loadString(eventProviderElement, "endDateTimeProperty", calendarItems::setEndDateTimeProperty);
        loadString(eventProviderElement, "titleProperty", calendarItems::setTitleProperty);
        loadString(eventProviderElement, "descriptionProperty", calendarItems::setDescriptionProperty);
        loadString(eventProviderElement, "classNamesProperty", calendarItems::setClassNamesProperty);
        loadString(eventProviderElement, "startEditableProperty", calendarItems::setStartEditableProperty);
        loadString(eventProviderElement, "durationEditableProperty", calendarItems::setDurationEditableProperty);
        loadString(eventProviderElement, "displayProperty", calendarItems::setDisplayProperty);
        loadString(eventProviderElement, "overlapProperty", calendarItems::setOverlapProperty);
        loadString(eventProviderElement, "constraintProperty", calendarItems::setConstraintProperty);
        loadString(eventProviderElement, "backgroundColorProperty", calendarItems::setBackgroundColorProperty);
        loadString(eventProviderElement, "borderColorProperty", calendarItems::setBorderColorProperty);
        loadString(eventProviderElement, "textColorProperty", calendarItems::setTextColorProperty);
    }

    protected AbstractEntityCalendarItems<?> createCalendarItems(Element eventProviderElement,
                                                                 InstanceContainer<?> container) {
        String id = loadString(eventProviderElement, "id").orElse(null);
        return Strings.isNullOrEmpty(id)
                ? new ContainerCalendarItems<>(container)
                : new ContainerCalendarItems<>(id, container);
    }

    protected AbstractEntityCalendarItems<?> createLazyCalendarItems(Element eventProviderElement) {
        String id = loadString(eventProviderElement, "id").orElse(null);
        return Strings.isNullOrEmpty(id)
                ? applicationContext.getBean(LazyCalendarItems.class)
                : applicationContext.getBean(LazyCalendarItems.class, id);
    }

    protected InstanceContainer<?> loadDataContainer(Element eventProviderElement) {
        String dataContainer = eventProviderElement.attributeValue("dataContainer");
        if (Strings.isNullOrEmpty(dataContainer)) {
            throw new GuiDevelopmentException("Event provider must specify a dataContainer attribute", context);
        }
        return context.getDataHolder().getContainer(dataContainer);
    }

    protected void loadItemsQuery(Element itemsQueryElement, LazyCalendarItems lazyCalendarItems) {
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

    protected void loadSnapDuration(Element element, Consumer<CalendarDuration> setter) {
        loadString(element, "snapDuration")
                .ifPresent(s -> {
                    try {
                        LocalTime time = LocalTime.parse(s);
                        setter.accept(CalendarDuration.ofHours(time.getHour())
                                .plusMinutes(time.getMinute())
                                .plusSeconds(time.getSecond())
                                .plusMilliseconds((int) TimeUnit.NANOSECONDS.toMillis(time.getNano())));
                    } catch (DateTimeException e) {
                        throw new GuiDevelopmentException("Invalid snap duration format. Use one of the following" +
                                " formats: hh:mm:ss.sss, hh:mm:ss, hh:mm", context);
                    }
                });
    }

    protected void loadBusinessHours(Element element, Consumer<List<BusinessHours>> setter) {
        Element businessHoursElement = element.element("businessHours");
        if (businessHoursElement != null) {
            List<BusinessHours> businessHours = new ArrayList<>();
            for (Element entry : businessHoursElement.elements("entry")) {
                BusinessHours bh = loadBusinessHoursEntry(entry);
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
    protected BusinessHours loadBusinessHoursEntry(Element element) {
        LocalTime startTime = loadString(element, "startTime")
                .map(LocalTime::parse)
                .orElse(null);
        LocalTime endTime = loadString(element, "startTime")
                .map(LocalTime::parse)
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
        return BusinessHours.of(startTime, endTime, businessDays.toArray(new DayOfWeek[0]));
    }
}
