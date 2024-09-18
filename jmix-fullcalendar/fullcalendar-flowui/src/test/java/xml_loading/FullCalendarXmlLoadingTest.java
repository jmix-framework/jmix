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
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.model.Display;
import io.jmix.fullcalendarflowui.component.data.ContainerCalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.EntityCalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever;
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
                () -> assertTrue(calendar.isForceEventDuration()),
                () -> assertEquals(LocalDate.of(2024, 9, 1), calendar.getInitialDate()),
                () -> assertEquals(CalendarDisplayModes.TIME_GRID_WEEK, calendar.getInitialCalendarDisplayMode()),
                () -> assertEquals(1, calendar.getEventLongPressDelay()),
                () -> assertLinesMatch(List.of("moreLinkClassNames", "moreLinkClassNames1"), calendar.getMoreLinkClassNames()),
                () -> assertEquals(CalendarDisplayModes.LIST_DAY, calendar.getMoreLinkCalendarDisplayMode()),
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
        var calendar = testView.calendarCustomDisplayMode;

        List<CustomCalendarDisplayMode> customCalendarDisplayModes = calendar.getCustomCalendarDisplayModes();

        assertEquals(1, customCalendarDisplayModes.size());

        CustomCalendarDisplayMode customMode = customCalendarDisplayModes.get(0);
        assertEquals(GenericCalendarDisplayModes.DAY_GRID, customMode.getBaseDisplayMode());
        assertEquals(1, customMode.getDayCount());
        assertEquals(CalendarDuration.ofYears(1)
                .plusMonths(1)
                .plusWeeks(1)
                .plusDays(1)
                .plusHours(1)
                .plusMinutes(1)
                .plusSeconds(1)
                .plusMilliseconds(1), customMode.getDuration());
        assertDisplayModeProperty(customMode);
    }

    @Test
    @DisplayName("Load FullCalendar display mode properties from XML")
    public void loadFullCalendarViewsPropertiesFromXml() {
        viewNavigators.view(UiTestUtils.getCurrentView(), FullCalendarXmlLoadingTestView.class)
                .navigate();

        FullCalendarXmlLoadingTestView testView = UiTestUtils.getCurrentView();
        var calendar = testView.calendarDisplayModeProperties;

        DayGridDayProperties dayGridDay = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_DAY);
        assertBaseDayGridProperties(dayGridDay);
        assertDisplayModeProperty(dayGridDay);
        assertFalse(dayGridDay.isDisplayEventEnd());

        DayGridWeekProperties dayGridWeek = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_WEEK);
        assertBaseDayGridProperties(dayGridWeek);
        assertDisplayModeProperty(dayGridWeek);
        assertTrue(dayGridWeek.isDisplayEventEnd());

        DayGridMonthProperties dayGridMonth = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_MONTH);
        assertBaseDayGridProperties(dayGridMonth);
        assertDisplayModeProperty(dayGridMonth);
        assertFalse(dayGridMonth.isFixedWeekCount());
        assertFalse(dayGridMonth.isShowNonCurrentDates());
        assertTrue(dayGridMonth.isDisplayEventEnd());

        DayGridYearProperties dayGridYear = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_YEAR);
        assertBaseDayGridProperties(dayGridYear);
        assertDisplayModeProperty(dayGridYear);
        assertEquals("weekNumberFormat", dayGridYear.getMonthStartFormat());
        assertTrue(dayGridYear.isDisplayEventEnd());

        TimeGridDayProperties timeGridDay = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.TIME_GRID_DAY);
        assertBaseTimeGridProperties(timeGridDay);
        assertDisplayModeProperty(timeGridDay);

        TimeGridWeekProperties timeGridWeek = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.TIME_GRID_WEEK);
        assertBaseTimeGridProperties(timeGridWeek);
        assertDisplayModeProperty(timeGridWeek);

        ListDayProperties listDay = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.LIST_DAY);
        assertBaseListProperties(listDay);
        assertDisplayModeProperty(listDay);

        ListWeekProperties listWeek = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.LIST_WEEK);
        assertBaseListProperties(listWeek);
        assertDisplayModeProperty(listWeek);

        ListMonthProperties listMonth = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.LIST_MONTH);
        assertBaseListProperties(listMonth);
        assertDisplayModeProperty(listMonth);

        ListYearProperties listYear = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.LIST_YEAR);
        assertBaseListProperties(listYear);
        assertDisplayModeProperty(listYear);

        MultiMonthYearProperties multiMonthYear = calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.MULTI_MONTH_YEAR);
        assertFalse(multiMonthYear.isFixedWeekCount());
        assertEquals(1, multiMonthYear.getMultiMonthMaxColumns());
        assertEquals(1, multiMonthYear.getMultiMonthMinWidth());
        assertEquals("multiMonthTitleFormat", multiMonthYear.getMultiMonthTitleFormat());
        assertFalse(multiMonthYear.isShowNonCurrentDates());
    }

    @Test
    @DisplayName("Load FullCalendar I18n attributes from XML")
    public void loadFullCalendarI18nAttributesFromXml() {
        viewNavigators.view(UiTestUtils.getCurrentView(), FullCalendarXmlLoadingTestView.class)
                .navigate();

        FullCalendarXmlLoadingTestView testView = UiTestUtils.getCurrentView();
        var calendar = testView.calendarI18n;

        //noinspection DataFlowIssue
        Assertions.assertAll(
                () -> assertNotNull(calendar.getI18n()),
                () -> assertEquals("allDayText", calendar.getI18n().getAllDayText()),
                () -> assertEquals("closeHint", calendar.getI18n().getCloseHint()),
                () -> assertEquals(DayOfWeek.WEDNESDAY, calendar.getI18n().getFirstDayOfWeek()),
                () -> assertEquals(1, calendar.getI18n().getDayOfYear()),
                () -> assertEquals("eventHint", calendar.getI18n().getEventHint()),
                () -> assertEquals("moreLinkHint", calendar.getI18n().getMoreLinkHint()),
                () -> assertEquals("moreLinkText", calendar.getI18n().getMoreLinkText()),
                () -> assertEquals("navLinkHint", calendar.getI18n().getNavLinkHint()),
                () -> assertEquals("noEventsText", calendar.getI18n().getNoEventsText()),
                () -> assertEquals("timeHint", calendar.getI18n().getTimeHint()),
                () -> assertEquals("weekTextLong", calendar.getI18n().getWeekTextLong())
        );
    }

    @Test
    @DisplayName("Load FullCalendar data providers from XML")
    public void loadFullCalendarDataProvidersFromXml() {
        viewNavigators.view(UiTestUtils.getCurrentView(), FullCalendarXmlLoadingTestView.class)
                .navigate();

        FullCalendarXmlLoadingTestView testView = UiTestUtils.getCurrentView();
        var calendar = testView.calendarDataProviders;

        ContainerCalendarDataProvider<LocalDateTimeEvent> containerDataProvider =
                calendar.getDataProvider("containerDataProvider");
        assertNotNull(containerDataProvider);

        assertBaseEntityDataProviderProperties(containerDataProvider);
        assertNotNull(containerDataProvider.getContainer());

        EntityCalendarDataRetriever calendarDataRetriever = calendar.getDataProvider("callbackDataProvider");
        assertNotNull(calendarDataRetriever);

        assertBaseEntityDataProviderProperties(calendarDataRetriever);
        Assertions.assertAll(
                () -> assertEquals(LocalDateTimeEvent.class, calendarDataRetriever.getEntityClass()),
                () -> assertNotNull(calendarDataRetriever.getFetchPlan()),
                () -> assertNotNull(calendarDataRetriever.getQueryString())
        );
    }

    private static void assertBaseEntityDataProviderProperties(EntityCalendarDataProvider dataProvider) {
        Assertions.assertAll(
                () -> assertEquals("groupId", dataProvider.getGroupIdProperty()),
                () -> assertEquals("allDay", dataProvider.getAllDayProperty()),
                () -> assertEquals("startDateTime", dataProvider.getStartDateTimeProperty()),
                () -> assertEquals("endDateTime", dataProvider.getEndDateTimeProperty()),
                () -> assertEquals("title", dataProvider.getTitleProperty()),
                () -> assertEquals("description", dataProvider.getDescriptionProperty()),
                () -> assertEquals("interactive", dataProvider.getInteractiveProperty()),
                () -> assertEquals("classNames", dataProvider.getClassNamesProperty()),
                () -> assertEquals("startEditable", dataProvider.getStartEditableProperty()),
                () -> assertEquals("durationEditable", dataProvider.getDurationEditableProperty()),
                () -> assertEquals("display", dataProvider.getDisplayProperty()),
                () -> assertEquals("overlap", dataProvider.getOverlapProperty()),
                () -> assertEquals("constraint", dataProvider.getConstraintProperty()),
                () -> assertEquals("backgroundColor", dataProvider.getBackgroundColorProperty()),
                () -> assertEquals("borderColor", dataProvider.getBorderColorProperty()),
                () -> assertEquals("textColor", dataProvider.getTextColorProperty()),
                () -> assertEquals("recurringDaysOfWeek", dataProvider.getRecurringDaysOfWeekProperty()),
                () -> assertEquals("recurringStartTime", dataProvider.getRecurringStartTimeProperty()),
                () -> assertEquals("recurringEndTime", dataProvider.getRecurringEndTimeProperty()),
                () -> assertEquals("recurringStartDate", dataProvider.getRecurringStartDateProperty()),
                () -> assertEquals("recurringEndDate", dataProvider.getRecurringEndDateProperty())
        );
    }

    private static void assertBaseDayGridProperties(AbstractDayGridProperties displayModeProperties) {
        Assertions.assertAll(
                () -> assertEquals("dayPopoverFormat", displayModeProperties.getDayPopoverFormat()),
                () -> assertEquals("dayHeaderFormat", displayModeProperties.getDayHeaderFormat()),
                () -> assertEquals("eventTimeFormat", displayModeProperties.getEventTimeFormat()),
                () -> assertEquals("weekNumberFormat", displayModeProperties.getWeekNumberFormat())
        );
    }

    private static void assertBaseTimeGridProperties(AbstractTimeGridProperties displayModeProperties) {
        Assertions.assertAll(
                () -> assertEquals("dayPopoverFormat", displayModeProperties.getDayPopoverFormat()),
                () -> assertEquals("dayHeaderFormat", displayModeProperties.getDayHeaderFormat()),
                () -> assertEquals("eventTimeFormat", displayModeProperties.getEventTimeFormat()),
                () -> assertEquals("weekNumberFormat", displayModeProperties.getWeekNumberFormat()),
                () -> assertFalse(displayModeProperties.isDisplayEventEnd()),
                () -> assertFalse(displayModeProperties.isAllDaySlotVisible()),
                () -> assertEquals(1, displayModeProperties.getEventMinHeight()),
                () -> assertEquals(1, displayModeProperties.getEventShortHeight()),
                () -> assertFalse(displayModeProperties.isSlotEventOverlap()),
                () -> assertEquals("slotLabelFormat", displayModeProperties.getSlotLabelFormat())
        );
    }

    private static void assertBaseListProperties(AbstractListProperties displayModeProperties) {
        assertEquals("listDayFormat", displayModeProperties.getListDayFormat());
        assertEquals("listDaySideFormat", displayModeProperties.getListDaySideFormat());
        assertFalse(displayModeProperties.isListDaySideVisible());
        assertFalse(displayModeProperties.isListDayVisible());
    }

    private static void assertDisplayModeProperty(AbstractCalendarDisplayModeProperties displayModeProperties) {
        Map<String, Object> properties = displayModeProperties.getProperties();
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey("test"));

        Integer testValue = (Integer) properties.get("test");
        assertEquals(1, testValue);
    }
}
