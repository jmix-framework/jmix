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

package io.jmix.fullcalendarflowui.component.data;

/**
 * Base class for simple event retrievers. For instance:
 * <pre>{@code
 * @ViewComponent
 * private FullCalendar calendar;
 * @Autowired
 * private CalendarEventService eventService;
 * @Subscribe
 * public void onInit(final InitEvent event) {
 *     calendar.addEventProvider(new CalendarEventRetriever() {
 *         @Override
 *         public List<CalendarEvent> onItemsFetch(ItemsFetchContext context) {
 *             return eventService.fetchEvents(context);
 *         }
 *     });
 * }
 * }</pre>
 */
public abstract class CalendarEventRetriever implements LazyCalendarEventProvider {

    protected String id;

    public CalendarEventRetriever() {
        this(EventProviderUtils.generateId());
    }

    public CalendarEventRetriever(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
