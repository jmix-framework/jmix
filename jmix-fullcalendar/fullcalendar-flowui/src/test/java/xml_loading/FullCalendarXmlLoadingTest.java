/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xml_loading;

import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendar.Display;
import io.jmix.fullcalendarflowui.component.data.ContainerCalendarEventProvider;
import io.jmix.fullcalendarflowui.component.data.EntityCalendarEventProvider;
import io.jmix.fullcalendarflowui.component.data.LazyEntityCalendarEventRetriever;
import io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FullCalendarFlowuiTestConfiguration;
import test_support.entity.LocalDateTimeEvent;
import xml_loading.view.FullCalendarXmlLoadingTestView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UiTest(viewBasePackages = "xml_loading.view")
@SpringBootTest(classes = {FullCalendarFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class FullCalendarXmlLoadingTest {

    @Autowired
    private ViewNavigators viewNavigators;

    @Test
    @DisplayName("Load FullCalendar attributes from XML")
    public void loadFullCalendarAttributesFromXml() {
        viewNavigators.view(UiTestUtils.getCurrentView(), FullCalendarXmlLoadingTestView.class)
                .navigate();

        FullCalendarXmlLoadingTestView testView = UiTestUtils.getCurrentView();
        var calendar = testView.calendar;

        Assertions.assertAll(
                () -> assertFalse(calendar.isVisible()),
                () -> assertEquals("1px", calendar.getHeight()),
                () -> assertEquals("1px", calendar.getMaxHeight()),
                () -> assertEquals("1px", calendar.getMinHeight()),
                () -> assertEquals("1px", calendar.getMinWidth()),
                () -> assertEquals("1px", calendar.getWidth()),
                () -> assertEquals("classNames classNames1", calendar.getClassName()),
                () -> assertEquals("green", calendar.getStyle().get("color")),
                () -> assertTrue(calendar.isAllMaintainDurationEnabled()),
                () -> assertEquals("dateAlignment", calendar.getDateAlignment()),
                () -> assertFalse(calendar.isDayHeadersVisible()),
                () -> assertEquals(1, calendar.getDayMaxEventRows()),
                () -> assertEquals(1, calendar.getDayMaxEvents()),
                () -> assertTrue(calendar.isDefaultAllDay()),
                () -> assertEquals(CalendarDuration.ofDays(2), calendar.getDefaultAllDayEventDuration()),
                () -> assertTrue(calendar.isDefaultBusinessHoursEnabled()),
                () -> assertEquals("defaultDayHeaderFormat", calendar.getDefaultDayHeaderFormat()),
                () -> assertTrue(calendar.isDefaultDayMaxEventRowsEnabled()),
                () -> assertTrue(calendar.isDefaultDayMaxEventsEnabled()),
                () -> assertEquals("defaultDayPopoverFormat", calendar.getDefaultDayPopoverFormat()),
                () -> assertEquals("defaultEventTimeFormat", calendar.getDefaultEventTimeFormat()),
                () -> assertEquals("defaultSlotLabelFormat", calendar.getDefaultSlotLabelFormat()),
                () -> assertEquals(CalendarDuration.ofHours(2), calendar.getDefaultTimedEventDuration()),
                () -> assertEquals("defaultWeekNumberFormat", calendar.getDefaultWeekNumberFormat()),
                () -> assertFalse(calendar.isDisplayEventTime()),
                () -> assertEquals(1, calendar.getDragRevertDuration()),
                () -> assertFalse(calendar.isDragScroll()),
                () -> assertEquals("green", calendar.getEventBackgroundColor()),
                () -> assertEquals("green", calendar.getEventBorderColor()),
                () -> assertEquals("eventConstraintGroupId", calendar.getEventConstraintGroupId()),
                () -> assertTrue(calendar.isEventConstraintBusinessHoursEnabled()),
                () -> assertEquals(Display.BACKGROUND, calendar.getEventDisplay()),
                () -> assertEquals(1, calendar.getEventDragMinDistance()),
                () -> assertTrue(calendar.isEventDurationEditable()),
                () -> assertTrue(calendar.isEventInteractive()),
                () -> assertEquals(1, calendar.getEventMaxStack()),
                () -> assertLinesMatch(List.of("start"), calendar.getEventOrder()),
                () -> assertTrue(calendar.isEventOrderStrict()),
                () -> assertFalse(calendar.isEventOverlap()),
                () -> assertTrue(calendar.isEventResizableFromStart()),
                () -> assertTrue(calendar.isEventStartEditable()),
                () -> assertEquals("green", calendar.getEventTextColor()),
                () -> assertTrue(calendar.isExpandRows()),
                () -> assertEquals(DayOfWeek.TUESDAY, calendar.getFirstDayOfWeek()),
                () -> assertTrue(calendar.isForceEventDuration()),
                () -> assertEquals(LocalDate.of(2024, 9, 1), calendar.getInitialDate()),
                () -> assertEquals(CalendarViewType.TIME_GRID_WEEK, calendar.getInitialCalendarView()),
                () -> assertEquals(1, calendar.getEventLongPressDelay()),
                () -> assertLinesMatch(List.of("moreLinkClassNames", "moreLinkClassNames1"), calendar.getMoreLinkClassNames()),
                () -> assertEquals(CalendarViewType.LIST_DAY, calendar.getMoreLinkCalendarView()),
                () -> assertTrue(calendar.isNavigationLinksEnabled()),
                () -> assertEquals(CalendarDuration.ofHours(9), calendar.getNextDayThreshold()),
                () -> assertTrue(calendar.isNowIndicatorVisible()),
                () -> assertTrue(calendar.isProgressiveEventRendering()),
                () -> assertEquals(CalendarDuration.ofHours(7), calendar.getScrollTime()),
                () -> assertFalse(calendar.isScrollTimeReset()),
                () -> assertEquals("selectConstraintGroupId", calendar.getSelectConstraintGroupId()),
                () -> assertTrue(calendar.isSelectConstraintBusinessHoursEnabled()),
                () -> assertEquals(1, calendar.getSelectLongPressDelay()),
                () -> assertEquals(1, calendar.getSelectMinDistance()),
                () -> assertTrue(calendar.isSelectMirror()),
                () -> assertFalse(calendar.isSelectOverlap()),
                () -> assertTrue(calendar.isSelectionEnabled()),
                () -> assertEquals(CalendarDuration.ofMinutes(35), calendar.getSlotDuration()),
                () -> assertEquals(CalendarDuration.ofHours(1), calendar.getSlotLabelInterval()),
                () -> assertEquals(CalendarDuration.ofHours(23), calendar.getSlotMaxTime()),
                () -> assertEquals(CalendarDuration.ofHours(10), calendar.getSlotMinTime()),
                () -> assertEquals(CalendarDuration.ofMinutes(35), calendar.getSnapDuration()),
                () -> assertFalse(calendar.isUnselectAuto()),
                () -> assertEquals("form-layout > field-class-name", calendar.getUnselectCancelSelector()),
                () -> assertTrue(calendar.isWeekNumbersVisible()),
                () -> assertFalse(calendar.isWeekendsVisible()),
                () -> assertEquals(1, calendar.getWindowResizeDelay()),
                () -> assertIterableEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), calendar.getHiddenDays())
        );
    }

    @Test
    @DisplayName("Load FullCalendar business hours list from XML")
    public void loadFullCalendarBusinessHoursListFromXml() {
        viewNavigators.view(UiTestUtils.getCurrentView(), FullCalendarXmlLoadingTestView.class)
                .navigate();

        FullCalendarXmlLoadingTestView testView = UiTestUtils.getCurrentView();
        var calendar = testView.calendarBusinessHours;

        List<CalendarBusinessHours> businessHoursList = calendar.getBusinessHours();

        assertEquals(2, businessHoursList.size());

        CalendarBusinessHours businessHours1 = businessHoursList.get(0);
        assertEquals(DayOfWeek.WEDNESDAY, businessHours1.getDaysOfWeek().iterator().next());
        assertEquals(LocalTime.of(9, 0), businessHours1.getStartTime());
        assertEquals(LocalTime.of(10, 0), businessHours1.getEndTime());

        CalendarBusinessHours businessHours2 = businessHoursList.get(1);
        assertEquals(DayOfWeek.THURSDAY, businessHours2.getDaysOfWeek().iterator().next());
        assertEquals(LocalTime.of(9, 30), businessHours2.getStartTime());
        assertEquals(LocalTime.of(12, 30), businessHours2.getEndTime());
    }

    @Test
    @DisplayName("Load FullCalendar custom views from XML")
    public void loadFullCalendarCustomViewsFromXml() {
        viewNavigators.view(UiTestUtils.getCurrentView(), FullCalendarXmlLoadingTestView.class)
                .navigate();

        FullCalendarXmlLoadingTestView testView = UiTestUtils.getCurrentView();
        var calendar = testView.calendarCustomViews;

        List<CustomCalendarView> customCalendarViews = calendar.getCustomCalendarViews();

        assertEquals(1, customCalendarViews.size());

        CustomCalendarView customView = customCalendarViews.get(0);
        assertEquals(GenericCalendarViewType.DAY_GRID, customView.getType());
        assertEquals(1, customView.getDayCount());
        assertEquals(CalendarDuration.ofYears(1)
                .plusMonths(1)
                .plusWeeks(1)
                .plusDays(1)
                .plusHours(1)
                .plusMinutes(1)
                .plusSeconds(1)
                .plusMilliseconds(1), customView.getDuration());
        assertViewProperty(customView);
    }

    @Test
    @DisplayName("Load FullCalendar views properties from XML")
    public void loadFullCalendarViewsPropertiesFromXml() {
        viewNavigators.view(UiTestUtils.getCurrentView(), FullCalendarXmlLoadingTestView.class)
                .navigate();

        FullCalendarXmlLoadingTestView testView = UiTestUtils.getCurrentView();
        var calendar = testView.calendarViewProperties;

        DayGridDayViewProperties dayGridDay = calendar.getCalendarViewProperties(CalendarViewType.DAY_GRID_DAY);
        assertBaseDayGridProperties(dayGridDay);
        assertViewProperty(dayGridDay);
        assertFalse(dayGridDay.isDisplayEventEnd());

        DayGridWeekViewProperties dayGridWeek = calendar.getCalendarViewProperties(CalendarViewType.DAY_GRID_WEEK);
        assertBaseDayGridProperties(dayGridWeek);
        assertViewProperty(dayGridWeek);
        assertTrue(dayGridWeek.isDisplayEventEnd());

        DayGridMonthViewProperties dayGridMonth = calendar.getCalendarViewProperties(CalendarViewType.DAY_GRID_MONTH);
        assertBaseDayGridProperties(dayGridMonth);
        assertViewProperty(dayGridMonth);
        assertFalse(dayGridMonth.isFixedWeekCount());
        assertFalse(dayGridMonth.isShowNonCurrentDates());
        assertTrue(dayGridMonth.isDisplayEventEnd());

        DayGridYearViewProperties dayGridYear = calendar.getCalendarViewProperties(CalendarViewType.DAY_GRID_YEAR);
        assertBaseDayGridProperties(dayGridYear);
        assertViewProperty(dayGridYear);
        assertEquals("weekNumberFormat", dayGridYear.getMonthStartFormat());
        assertTrue(dayGridYear.isDisplayEventEnd());

        TimeGridDayViewProperties timeGridDay = calendar.getCalendarViewProperties(CalendarViewType.TIME_GRID_DAY);
        assertBaseTimeGridProperties(timeGridDay);
        assertViewProperty(timeGridDay);

        TimeGridWeekViewProperties timeGridWeek = calendar.getCalendarViewProperties(CalendarViewType.TIME_GRID_WEEK);
        assertBaseTimeGridProperties(timeGridWeek);
        assertViewProperty(timeGridWeek);

        ListDayViewProperties listDay = calendar.getCalendarViewProperties(CalendarViewType.LIST_DAY);
        assertBaseListProperties(listDay);
        assertViewProperty(listDay);

        ListWeekViewProperties listWeek = calendar.getCalendarViewProperties(CalendarViewType.LIST_WEEK);
        assertBaseListProperties(listWeek);
        assertViewProperty(listWeek);

        ListMonthViewProperties listMonth = calendar.getCalendarViewProperties(CalendarViewType.LIST_MONTH);
        assertBaseListProperties(listMonth);
        assertViewProperty(listMonth);

        ListYearViewProperties listYear = calendar.getCalendarViewProperties(CalendarViewType.LIST_YEAR);
        assertBaseListProperties(listYear);
        assertViewProperty(listYear);

        MultiMonthYearViewProperties multiMonthYear = calendar.getCalendarViewProperties(CalendarViewType.MULTI_MONTH_YEAR);
        assertFalse(multiMonthYear.isFixedWeekCount());
        assertEquals(1, multiMonthYear.getMultiMonthMaxColumns());
        assertEquals(1, multiMonthYear.getMultiMonthMinWidth());
        assertEquals("multiMonthTitleFormat", multiMonthYear.getMultiMonthTitleFormat());
        assertFalse(multiMonthYear.isShowNonCurrentDates());
    }

    @Test
    @DisplayName("Load FullCalendar event providers from XML")
    public void loadFullCalendarEventProvidersFromXml() {
        viewNavigators.view(UiTestUtils.getCurrentView(), FullCalendarXmlLoadingTestView.class)
                .navigate();

        FullCalendarXmlLoadingTestView testView = UiTestUtils.getCurrentView();
        var calendar = testView.calendarEventProviders;

        ContainerCalendarEventProvider<LocalDateTimeEvent> containerEventProvider =
                calendar.getEventProvider("containerEventProvider");
        assertNotNull(containerEventProvider);

        assertBaseEntityEventProviderProperties(containerEventProvider);
        assertNotNull(containerEventProvider.getContainer());

        LazyEntityCalendarEventRetriever lazyEventProvider = calendar.getEventProvider("lazyEventProvider");
        assertNotNull(lazyEventProvider);

        assertBaseEntityEventProviderProperties(lazyEventProvider);
        Assertions.assertAll(
                () -> assertEquals(LocalDateTimeEvent.class, lazyEventProvider.getEntityClass()),
                () -> assertNotNull(lazyEventProvider.getFetchPlan()),
                () -> assertNotNull(lazyEventProvider.getQueryString())
        );
    }

    private static void assertBaseEntityEventProviderProperties(EntityCalendarEventProvider eventProvider) {
        Assertions.assertAll(
                () -> assertEquals("groupId", eventProvider.getGroupIdProperty()),
                () -> assertEquals("allDay", eventProvider.getAllDayProperty()),
                () -> assertEquals("startDateTime", eventProvider.getStartDateTimeProperty()),
                () -> assertEquals("endDateTime", eventProvider.getEndDateTimeProperty()),
                () -> assertEquals("title", eventProvider.getTitleProperty()),
                () -> assertEquals("description", eventProvider.getDescriptionProperty()),
                () -> assertEquals("interactive", eventProvider.getInteractiveProperty()),
                () -> assertEquals("classNames", eventProvider.getClassNamesProperty()),
                () -> assertEquals("startEditable", eventProvider.getStartEditableProperty()),
                () -> assertEquals("durationEditable", eventProvider.getDurationEditableProperty()),
                () -> assertEquals("display", eventProvider.getDisplayProperty()),
                () -> assertEquals("overlap", eventProvider.getOverlapProperty()),
                () -> assertEquals("constraint", eventProvider.getConstraintProperty()),
                () -> assertEquals("backgroundColor", eventProvider.getBackgroundColorProperty()),
                () -> assertEquals("borderColor", eventProvider.getBorderColorProperty()),
                () -> assertEquals("textColor", eventProvider.getTextColorProperty()),
                () -> assertEquals("recurringDaysOfWeek", eventProvider.getRecurringDaysOfWeekProperty()),
                () -> assertEquals("recurringStartTime", eventProvider.getRecurringStartTimeProperty()),
                () -> assertEquals("recurringEndTime", eventProvider.getRecurringEndTimeProperty()),
                () -> assertEquals("recurringStartDate", eventProvider.getRecurringStartDateProperty()),
                () -> assertEquals("recurringEndDate", eventProvider.getRecurringEndDateProperty())
        );
    }

    private static void assertBaseDayGridProperties(AbstractDayGridViewProperties viewProperties) {
        Assertions.assertAll(
                () -> assertEquals("dayPopoverFormat", viewProperties.getDayPopoverFormat()),
                () -> assertEquals("dayHeaderFormat", viewProperties.getDayHeaderFormat()),
                () -> assertEquals("eventTimeFormat", viewProperties.getEventTimeFormat()),
                () -> assertEquals("weekNumberFormat", viewProperties.getWeekNumberFormat())
        );
    }

    private static void assertBaseTimeGridProperties(AbstractTimeGridViewProperties viewProperties) {
        Assertions.assertAll(
                () -> assertEquals("dayPopoverFormat", viewProperties.getDayPopoverFormat()),
                () -> assertEquals("dayHeaderFormat", viewProperties.getDayHeaderFormat()),
                () -> assertEquals("eventTimeFormat", viewProperties.getEventTimeFormat()),
                () -> assertEquals("weekNumberFormat", viewProperties.getWeekNumberFormat()),
                () -> assertFalse(viewProperties.isDisplayEventEnd()),
                () -> assertFalse(viewProperties.isAllDaySlot()),
                () -> assertEquals(1, viewProperties.getEventMinHeight()),
                () -> assertEquals(1, viewProperties.getEventShortHeight()),
                () -> assertFalse(viewProperties.isSlotEventOverlap()),
                () -> assertEquals("slotLabelFormat", viewProperties.getSlotLabelFormat())
        );
    }

    private static void assertBaseListProperties(AbstractListViewProperties viewProperties) {
        assertEquals("listDayFormat", viewProperties.getListDayFormat());
        assertEquals("listDaySideFormat", viewProperties.getListDaySideFormat());
    }

    private static void assertViewProperty(AbstractCalendarViewProperties viewProperties) {
        Map<String, Object> properties = viewProperties.getProperties();
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey("test"));

        Integer testValue = (Integer) properties.get("test");
        assertEquals(1, testValue);
    }
}
