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

package io.jmix.fullcalendarflowui.facet.urlqueryparameters;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.facet.UrlQueryParametersFacet.UrlQueryParametersChangeEvent;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinder;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.FullCalendarUtils;
import io.jmix.fullcalendarflowui.component.event.DatesSetEvent;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayMode;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FullCalendarUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder {

    public static final String NAME = "calendarParameters";

    public static final String CALENDAR_DISPLAY_MODE_PARAM = "calendarDisplayMode";
    public static final String CALENDAR_DATE_PARAM = "calendarDate";

    protected final FullCalendar fullCalendar;
    protected final UrlParamSerializer urlParamSerializer;

    protected String calendarDisplayModeParam;
    protected String calendarDateParam;

    public FullCalendarUrlQueryParametersBinder(FullCalendar fullCalendar,
                                                UrlParamSerializer urlParamSerializer) {
        Preconditions.checkNotNullArgument(fullCalendar);
        Preconditions.checkNotNullArgument(urlParamSerializer);

        this.fullCalendar = fullCalendar;
        this.urlParamSerializer = urlParamSerializer;

        initComponent(fullCalendar);
    }

    @Override
    public FullCalendar getComponent() {
        return fullCalendar;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        Map<String, List<String>> parameters = queryParameters.getParameters();
        if (parameters.containsKey(getCalendarDisplayModeParam()) || parameters.containsKey(getCalendarDisplayModeParamInternal())) {
            String serializedDisplayMode = parameters.containsKey(getCalendarDisplayModeParamInternal())
                    ? parameters.get(getCalendarDisplayModeParamInternal()).get(0)
                    // the fallback option should be removed in future versions
                    : parameters.get(getCalendarDisplayModeParam()).get(0);
            String displayModeId = urlParamSerializer.deserialize(String.class, serializedDisplayMode);

            fullCalendar.setCalendarDisplayMode(FullCalendarUtils.getDisplayMode(fullCalendar, displayModeId));
        }
        if (parameters.containsKey(getCalendarDateParam()) || parameters.containsKey(getCalendarDateParamInternal())) {
            String serializedNavigateToDate = parameters.containsKey(getCalendarDateParamInternal())
                    ? parameters.get(getCalendarDateParamInternal()).get(0)
                    : parameters.get(getCalendarDateParam()).get(0);
            LocalDate date = urlParamSerializer.deserialize(LocalDate.class, serializedNavigateToDate);

            fullCalendar.navigateToDate(date);
        }
    }

    /**
     * @deprecated use {@link #getCalendarDisplayModeParamInternal()} instead
     */
    @Deprecated(since = "2.8", forRemoval = true)
    public String getCalendarDisplayModeParam() {
        return Strings.isNullOrEmpty(calendarDisplayModeParam) ? CALENDAR_DISPLAY_MODE_PARAM : calendarDisplayModeParam;
    }

    protected String getCalendarDisplayModeParamInternal() {
        return getOwnerId("calendar") + "_" + getCalendarDisplayModeParam();
    }

    public void setCalendarDisplayModeParam(@Nullable String calendarDisplayModeParam) {
        this.calendarDisplayModeParam = calendarDisplayModeParam;
    }

    /**
     * @deprecated use {@link #getCalendarDateParamInternal()} instead
     */
    @Deprecated(since = "2.8", forRemoval = true)
    public String getCalendarDateParam() {
        return Strings.isNullOrEmpty(calendarDateParam) ? CALENDAR_DATE_PARAM : calendarDateParam;
    }

    protected String getCalendarDateParamInternal() {
        return getOwnerId("calendar") + "_" + getCalendarDateParam();
    }

    public void setCalendarDateParam(@Nullable String calendarDateParam) {
        this.calendarDateParam = calendarDateParam;
    }

    protected void initComponent(FullCalendar fullCalendar) {
        fullCalendar.addDatesSetListener(this::onDatesSet);
    }

    protected void onDatesSet(DatesSetEvent event) {
        QueryParameters queryParameters = QueryParameters.simple(
                serializeQueryParameters(event.getDisplayModeInfo().getDisplayMode(), fullCalendar.getDate())
        );

        fireQueryParametersChanged(new UrlQueryParametersChangeEvent(this, queryParameters));
    }

    public ImmutableMap<String, String> serializeQueryParameters(CalendarDisplayMode calendarDisplayMode,
                                                                 LocalDate localDate) {
        return ImmutableMap.of(
                getCalendarDisplayModeParamInternal(), urlParamSerializer.serialize(calendarDisplayMode.getId()),
                getCalendarDateParamInternal(), urlParamSerializer.serialize(localDate)
        );
    }
}
