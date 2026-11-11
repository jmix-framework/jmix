/*
 * Copyright 2026 Haulmont.
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

package data_retriever;

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.CallbackCalendarDataProvider.ItemsFetchContext;
import io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import test_support.FullCalendarFlowuiTestConfiguration;
import test_support.entity.LocalDateTimeEvent;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that a query for events overlapping the visible range finds all-day events on the edge days of the view,
 * when the calendar's time zone differs from the server's.
 */
@SpringBootTest(classes = {FullCalendarFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class EntityCalendarDataRetrieverTest {

    static final LocalDate VISIBLE_START = LocalDate.of(2026, 9, 27);
    static final LocalDate VISIBLE_END = LocalDate.of(2026, 11, 8);
    static final String QUERY = "select e from LocalDateTimeEvent e "
            + "where e.startDateTime < :fetchEndDate and e.endDateTime > :fetchStartDate";

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    FetchPlanRepository fetchPlanRepository;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @AfterEach
    void tearDown() {
        dataManager.remove(dataManager.load(LocalDateTimeEvent.class).all().list());
    }

    @Test
    void onItemsFetch_allDayEventOnFirstVisibleDayAndCalendarWestOfServer_loadsEvent() {
        LocalDateTimeEvent event = saveAllDayEvent(VISIBLE_START);

        List<CalendarEvent> loaded = fetch(timeZoneOffsetFromSystem(-11));

        assertEquals(List.of(event.getId()), loaded.stream().map(CalendarEvent::getId).toList());
    }

    @Test
    void onItemsFetch_allDayEventOnLastVisibleDayAndCalendarEastOfServer_loadsEvent() {
        LocalDateTimeEvent event = saveAllDayEvent(VISIBLE_END.minusDays(1));

        List<CalendarEvent> loaded = fetch(timeZoneOffsetFromSystem(11));

        assertEquals(List.of(event.getId()), loaded.stream().map(CalendarEvent::getId).toList());
    }

    LocalDateTimeEvent saveAllDayEvent(LocalDate date) {
        LocalDateTimeEvent event = metadata.create(LocalDateTimeEvent.class);
        event.setTitle("All-day event");
        event.setAllDay(true);
        event.setStartDateTime(date.atStartOfDay());
        event.setEndDateTime(date.plusDays(1).atStartOfDay());
        return dataManager.save(event);
    }

    List<CalendarEvent> fetch(TimeZone calendarTimeZone) {
        EntityCalendarDataRetriever retriever = applicationContext.getBean(EntityCalendarDataRetriever.class);
        retriever.setEntityClass(LocalDateTimeEvent.class);
        retriever.setQueryString(QUERY);
        retriever.setFetchPlan(fetchPlanRepository.getFetchPlan(LocalDateTimeEvent.class, FetchPlan.BASE));
        retriever.setAllDayProperty("allDay");
        retriever.setStartDateTimeProperty("startDateTime");
        retriever.setEndDateTimeProperty("endDateTime");

        ItemsFetchContext context = new ItemsFetchContext(retriever, VISIBLE_START, VISIBLE_END, calendarTimeZone);
        return systemAuthenticator.withSystem(() -> retriever.onItemsFetch(context));
    }

    TimeZone timeZoneOffsetFromSystem(int hours) {
        int systemOffset = ZoneId.systemDefault().getRules()
                .getOffset(VISIBLE_START.atStartOfDay())
                .getTotalSeconds();
        return TimeZone.getTimeZone(ZoneOffset.ofTotalSeconds(systemOffset + hours * 3600));
    }
}
