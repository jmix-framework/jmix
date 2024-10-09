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

package io.jmix.fullcalendarflowui.kit.meta.loader;

import com.vaadin.flow.component.Component;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayMode;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes;
import jakarta.annotation.Nullable;
import org.dom4j.Element;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class StudioFullCalendarPreviewLoader implements StudioPreviewComponentLoader {

    private static final Map<String, Integer> daysOfWeek = Map.of(
            "SUNDAY", 0,
            "MONDAY", 1,
            "TUESDAY", 2,
            "WEDNESDAY", 3,
            "THURSDAY", 4,
            "FRIDAY", 5,
            "SATURDAY", 6);
    private static final JsonFactory jsonFactory = new JreJsonFactory();

    protected StudioDisplayModePropertiesLoader displayModePropertiesLoader;

    @Override
    public boolean isSupported(Element element) {
        return "http://jmix.io/schema/fullcalendar/ui".equals(element.getNamespaceURI())
                && "calendar".equals(element.getName());
    }

    // Todo rp: It does not load initial properties, need to debug somehow

    @Nullable
    @Override
    public Component load(Element element, Element viewElement) {
        JmixFullCalendar resultComponent = new JmixFullCalendar();
        resultComponent.addAttachListener(event -> loadOnAttach(element, resultComponent));

        loadSizeAttributes(resultComponent, element);
        loadClassNames(resultComponent, element);

        displayModeProperties().loadCalendarDisplayModeProperties(element, resultComponent);
        displayModeProperties().loadCustomCalendarDisplayModes(element, resultComponent);
        loadInitialDisplayMode(element, resultComponent);

        loadBoolean(element, "navigationLinksEnabled", resultComponent::setNavigationLinksEnabled);
        loadBoolean(element, "weekNumbersVisible", resultComponent::setWeekNumbersVisible);
        loadBoolean(element, "defaultDayMaxEventRowsEnabled", resultComponent::setDefaultDayMaxEventRowsEnabled);
        loadInteger(element, "dayMaxEventRows", resultComponent::setDayMaxEventRows);
        loadBoolean(element, "defaultDayMaxEventsEnabled", resultComponent::setDefaultDayMaxEventsEnabled);
        loadInteger(element, "dayMaxEvents", resultComponent::setDayMaxEvents);
        loadInteger(element, "eventMaxStack", resultComponent::setEventMaxStack);

        loadMoreLinkDisplayMode(element, resultComponent::setMoreLinkCalendarDisplayMode);
        loadStringList(element, "moreLinkClassNames", resultComponent::setMoreLinkClassNames);

        loadBoolean(element, "eventStartEditable", resultComponent::setEventStartEditable);
        loadBoolean(element, "eventDurationEditable", resultComponent::setEventDurationEditable);
        loadBoolean(element, "eventResizableFromStart", resultComponent::setEventResizableFromStart);
        loadInteger(element, "eventDragMinDistance", resultComponent::setEventDragMinDistance);
        loadBoolean(element, "eventOverlap", resultComponent::setEventOverlap);

        loadInteger(element, "dragRevertDuration", resultComponent::setDragRevertDuration);
        loadBoolean(element, "dragScroll", resultComponent::setDragScroll);

        loadBoolean(element, "allDayMaintainDurationEnabled",
                resultComponent::setAllDayMaintainDurationEnabled);
        loadDuration(element, "snapDuration", resultComponent::setSnapDuration);

        loadBoolean(element, "selectionEnabled", resultComponent::setSelectionEnabled);
        loadBoolean(element, "selectMirror", resultComponent::setSelectMirror);
        loadBoolean(element, "unselectAuto", resultComponent::setUnselectAuto);
        loadString(element, "unselectCancelSelector", resultComponent::setUnselectCancelSelector);
        loadBoolean(element, "selectOverlap", resultComponent::setSelectOverlap);

        loadInteger(element, "selectMinDistance", resultComponent::setSelectMinDistance);

        loadBoolean(element, "weekendsVisible", resultComponent::setWeekendsVisible);
        loadBoolean(element, "dayHeadersVisible", resultComponent::setDayHeadersVisible);

        loadDuration(element, "slotDuration", resultComponent::setSlotDuration);
        loadDuration(element, "slotLabelInterval", resultComponent::setSlotLabelInterval);
        loadDuration(element, "slotMinTime", resultComponent::setSlotMinTime);
        loadDuration(element, "slotMaxTime", resultComponent::setSlotMaxTime);
        loadDuration(element, "scrollTime", resultComponent::setScrollTime);
        loadBoolean(element, "scrollTimeReset", resultComponent::setScrollTimeReset);

        loadBoolean(element, "defaultAllDay", resultComponent::setDefaultAllDay);
        loadDuration(element, "defaultAllDayEventDuration", resultComponent::setDefaultAllDayEventDuration);
        loadDuration(element, "defaultTimedEventDuration", resultComponent::setDefaultTimedEventDuration);
        loadBoolean(element, "forceEventDuration", resultComponent::setForceEventDuration);

        loadString(element, "initialDate", (s) -> resultComponent.setInitialDate(LocalDate.parse(s)));
        loadString(element, "dateAlignment", resultComponent::setDateAlignment);

        loadBoolean(element, "expandRows", resultComponent::setExpandRows);
        loadInteger(element, "windowResizeDelay", resultComponent::setWindowResizeDelay);

        loadBoolean(element, "eventInteractive", resultComponent::setEventInteractive);

        loadInteger(element, "longPressDelay", resultComponent::setEventLongPressDelay);
        loadInteger(element, "selectLongPressDelay", resultComponent::setSelectLongPressDelay);

        loadBoolean(element, "nowIndicatorVisible", resultComponent::setNowIndicatorVisible);

        return resultComponent;
    }

    // Todo rp: Are not applied, need to debug somehow
    protected void loadOnAttach(Element element, JmixFullCalendar resultComponent) {
        loadHiddenDays(element, resultComponent);
        loadFirstDayOfWeek(element, resultComponent);
        loadDefaultBusinessHoursEnabled(element, resultComponent);
        loadBusinessHours(element, resultComponent);
    }

    protected void loadDuration(Element element, String attribute, Consumer<CalendarDuration> setter) {
        loadString(element, attribute)
                .ifPresent(s -> {
                    Duration javaDuration = Duration.parse(s);
                    setter.accept(CalendarDuration.ofDuration(javaDuration));
                });
    }

    protected void loadMoreLinkDisplayMode(Element element, Consumer<CalendarDisplayMode> setter) {
        loadString(element, "moreLinkDisplayMode")
                .ifPresent(t -> {
                    List<Enum<?>> displayModes = List.of(CalendarDisplayModes.values());

                    displayModes.stream().filter(e -> e.name().contains(t))
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

    protected void loadInitialDisplayMode(Element element, JmixFullCalendar resultComponent) {
        loadString(element, "initialDisplayMode", (displayModeId) -> {
            CalendarDisplayMode displayMode = displayModeProperties().getDisplayMode(displayModeId, resultComponent);
            resultComponent.setInitialCalendarDisplayMode(displayMode);
        });
    }

    protected void loadDefaultBusinessHoursEnabled(Element element, JmixFullCalendar resultComponent) {
        loadBoolean(element, "defaultBusinessHoursEnabled")
                .ifPresent(enabled -> resultComponent.getElement()
                        .callJsFunction("updateOption", "businessHours", enabled));
    }

    protected void loadFirstDayOfWeek(Element element, JmixFullCalendar resultComponent) {
        loadString(element, "firstDayOfWeek")
                .ifPresent(n -> {
                    resultComponent.getElement().callJsFunction("updateOption", daysOfWeek.get(n));
                });
    }

    protected void loadHiddenDays(Element element, JmixFullCalendar resultComponent) {
        Element hiddenDaysElement = element.element("hiddenDays");
        if (hiddenDaysElement == null) {
            return;
        }

        List<Integer> hiddenDays = new ArrayList<>();
        hiddenDaysElement.elements("day")
                .forEach(e -> loadString(e, "name")
                        .ifPresent(n -> hiddenDays.add(daysOfWeek.get(n))));

        if (!hiddenDays.isEmpty()) {
            JsonArray jsonArray = jsonFactory.createArray();

            for (int i = 0; i < hiddenDays.size(); i++) {
                jsonArray.set(i, hiddenDays.get(i));
            }

            resultComponent.getElement().callJsFunction("updateOption", "hiddenDays", jsonArray);
        }
    }

    protected void loadBusinessHours(Element element, JmixFullCalendar resultComponent) {
        Element businessHoursElement = element.element("businessHours");
        if (businessHoursElement != null) {
            List<NoOpBusinessHours> businessHours = new ArrayList<>();
            for (Element entry : businessHoursElement.elements("entry")) {
                NoOpBusinessHours bh = loadBusinessHoursEntry(entry);
                if (bh != null) {
                    businessHours.add(bh);
                }
            }
            if (!businessHours.isEmpty()) {
                resultComponent.getElement()
                        .callJsFunction("updateOption",
                                "businessHours", convertBusinessHoursToJson(businessHours));
            }
        }
    }

    protected JsonArray convertBusinessHoursToJson(List<NoOpBusinessHours> businessHours) {
        JsonArray jsonArray = jsonFactory.createArray();

        for (int i = 0; i < businessHours.size(); i++) {
            NoOpBusinessHours entry = businessHours.get(i);
            JsonObject jsonObject = jsonFactory.createObject();
            if (entry.getStartTime() != null) {
                jsonObject.put("startTime", entry.getStartTime().toString());
            }
            if (entry.getEndTime() != null) {
                jsonObject.put("endTime", entry.getEndTime().toString());
            }
            if (!entry.getDaysOfWeek().isEmpty()) {
                JsonArray daysOfweekJsonArray = jsonFactory.createArray();
                List<Integer> daysOfWeek = new ArrayList<>(entry.getDaysOfWeek());
                for (int j = 0; j < daysOfWeek.size(); j++) {
                    daysOfweekJsonArray.set(j, daysOfWeek.get(j));
                }
                jsonObject.put("daysOfWeek", daysOfweekJsonArray);
            }
            jsonArray.set(i, jsonObject);
        }

        return jsonArray;
    }

    @Nullable
    protected NoOpBusinessHours loadBusinessHoursEntry(Element element) {
        LocalTime startTime = loadString(element, "startTime")
                .map(LocalTime::parse)
                .orElse(null);
        LocalTime endTime = loadString(element, "endTime")
                .map(LocalTime::parse)
                .orElse(null);

        List<Integer> businessDays = new ArrayList<>();
        element.elements("day")
                .forEach(e -> {
                    loadString(e, "name")
                            .ifPresent(n -> businessDays.add(daysOfWeek.get(n)));
                });
        if (startTime == null && endTime == null && businessDays.isEmpty()) {
            return null;
        }
        return NoOpBusinessHours.of(startTime, endTime, businessDays.toArray(new Integer[0]));
    }

    protected StudioDisplayModePropertiesLoader displayModeProperties() {
        if (displayModePropertiesLoader == null) {
            displayModePropertiesLoader = new StudioDisplayModePropertiesLoader();
        }
        return displayModePropertiesLoader;
    }
}
