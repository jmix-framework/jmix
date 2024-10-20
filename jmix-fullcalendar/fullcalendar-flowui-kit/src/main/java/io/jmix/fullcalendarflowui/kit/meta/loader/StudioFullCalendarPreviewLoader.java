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

    @Nullable
    @Override
    public Component load(Element element, Element viewElement) {
        JmixFullCalendar resultComponent = new JmixFullCalendar();
        resultComponent.addAttachListener(event -> loadOnAttach(element, resultComponent));

        loadBoolean(element, "visible", resultComponent::setVisible);
        loadSizeAttributes(resultComponent, element);
        loadClassNames(resultComponent, element);

        loadBoolean(element, "allDayMaintainDurationEnabled",
                resultComponent::setAllDayMaintainDurationEnabled);
        loadBoolean(element, "dayHeadersVisible", resultComponent::setDayHeadersVisible);
        loadInteger(element, "dayMaxEvents", resultComponent::setDayMaxEvents);
        loadInteger(element, "dayMaxEventRows", resultComponent::setDayMaxEventRows);
        loadString(element, "dateAlignment", resultComponent::setDateAlignment);
        loadBoolean(element, "defaultAllDay", resultComponent::setDefaultAllDay);
        loadDuration(element, "defaultAllDayEventDuration", resultComponent::setDefaultAllDayEventDuration);
        // Ignore defaultDayHeaderFormat
        loadBoolean(element, "defaultDayMaxEventRowsEnabled",
                resultComponent::setDefaultDayMaxEventRowsEnabled);
        loadBoolean(element, "defaultDayMaxEventsEnabled",
                resultComponent::setDefaultDayMaxEventsEnabled);
        // Ignore defaultDayPopoverFormat
        // See    loadDefaultBusinessHoursEnabled(element, resultComponent);
        // Ignore defaultEventTimeFormat
        // Ignore defaultSlotLabelFormat
        loadDuration(element, "defaultTimedEventDuration", resultComponent::setDefaultTimedEventDuration);
        // Ignore defaultWeekNumberFormat
        loadBoolean(element, "displayEventTime", resultComponent::setDisplayEventTime);
        loadInteger(element, "dragRevertDuration", resultComponent::setDragRevertDuration);
        loadBoolean(element, "dragScroll", resultComponent::setDragScroll);

        loadString(element, "eventBackgroundColor", resultComponent::setEventBackgroundColor);
        loadString(element, "eventBorderColor", resultComponent::setEventBorderColor);
        // Ignore eventConstraintGroupId
        // Ignore eventConstraintBusinessHoursEnabled
        // Ignore eventDisplay
        loadInteger(element, "eventDragMinDistance", resultComponent::setEventDragMinDistance);
        loadBoolean(element, "eventDurationEditable", resultComponent::setEventDurationEditable);
        loadBoolean(element, "eventInteractive", resultComponent::setEventInteractive);
        loadInteger(element, "eventLongPressDelay", resultComponent::setEventLongPressDelay);
        loadInteger(element, "eventMaxStack", resultComponent::setEventMaxStack);
        loadStringList(element, "eventOrder", resultComponent::setEventOrder);
        loadBoolean(element, "eventOrderStrict", resultComponent::setEventOrderStrict);
        loadBoolean(element, "eventOverlap", resultComponent::setEventOverlap);
        loadBoolean(element, "eventResizableFromStart", resultComponent::setEventResizableFromStart);
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
        // Ignore selectConstraintGroupId
        // Ignore selectConstraintBusinessHoursEnabled
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

        displayModeProperties().loadCalendarDisplayModeProperties(element, resultComponent);
        displayModeProperties().loadCustomCalendarDisplayModes(element, resultComponent);
        loadInitialDisplayMode(element, resultComponent);

        return resultComponent;
    }

    protected void loadOnAttach(Element element, JmixFullCalendar resultComponent) {
        loadDefaultBusinessHoursEnabled(element, resultComponent);
        loadHiddenDays(element, resultComponent);
        loadBusinessHours(element, resultComponent);
        loadFirstDayOfWeek(element, resultComponent);
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
                    resultComponent.getElement()
                            .callJsFunction("updateOption", "firstDay", daysOfWeek.get(n));
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
                .map(s -> LocalTime.ofSecondOfDay(Duration.parse(s).toSeconds()))
                .orElse(null);
        LocalTime endTime = loadString(element, "endTime")
                .map(s -> LocalTime.ofSecondOfDay(Duration.parse(s).toSeconds()))
                .orElse(null);

        List<Integer> businessDays = new ArrayList<>();
        element.elements("day")
                .forEach(e -> {
                    loadString(e, "name")
                            .ifPresent(n -> businessDays.add(daysOfWeek.get(n)));
                });
        if (startTime == null) {
            return businessDays.isEmpty()
                    ? null
                    : NoOpBusinessHours.of(businessDays.toArray(new Integer[0]));
        }
        if (endTime == null) {
            return businessDays.isEmpty()
                    ? NoOpBusinessHours.of(startTime)
                    : NoOpBusinessHours.of(startTime, businessDays.toArray(new Integer[0]));
        }
        return businessDays.isEmpty()
                ? NoOpBusinessHours.of(startTime, endTime)
                : NoOpBusinessHours.of(startTime, endTime, businessDays.toArray(new Integer[0]));
    }

    protected StudioDisplayModePropertiesLoader displayModeProperties() {
        if (displayModePropertiesLoader == null) {
            displayModePropertiesLoader = new StudioDisplayModePropertiesLoader();
        }
        return displayModePropertiesLoader;
    }
}
