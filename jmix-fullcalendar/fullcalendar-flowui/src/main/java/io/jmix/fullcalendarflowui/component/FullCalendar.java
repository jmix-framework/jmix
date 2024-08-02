package io.jmix.fullcalendarflowui.component;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.fullcalendarflowui.component.data.ItemEventProviderManager;
import io.jmix.fullcalendarflowui.component.data.LazyEventProviderManager;
import io.jmix.fullcalendarflowui.component.event.*;
import io.jmix.fullcalendarflowui.component.model.BusinessHours;
import io.jmix.fullcalendarflowui.component.model.option.FullCalendarOptions;
import io.jmix.fullcalendarflowui.component.serialization.serializer.CalendarEventSerializer;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import io.jmix.fullcalendarflowui.kit.component.data.*;
import io.jmix.fullcalendarflowui.kit.component.event.dom.*;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarView;
import io.jmix.fullcalendarflowui.kit.component.serialization.model.*;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixFullCalendarSerializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

public class FullCalendar extends JmixFullCalendar implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected DateTimeTransformations dateTimeTransformations;
    protected FullCalendarDelegate calendarDelegate;

    protected Function<MoreLinkClassNamesContext, List<String>> linkMoreClassNamesGenerator;

    protected Object eventConstraintGroupId;
    protected Object selectConstraintGroupId;

    @Override
    public void afterPropertiesSet() {
        dateTimeTransformations = applicationContext.getBean(DateTimeTransformations.class);
        calendarDelegate = applicationContext.getBean(FullCalendarDelegate.class, this);

        Locale locale = applicationContext.getBean(CurrentAuthentication.class).getLocale();
        options.setLocale(locale);

        initTimeZone();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Nullable
    public Object getEventConstraintGroupId() {
        return eventConstraintGroupId;
    }

    public void setEventConstraintGroupId(@Nullable Object groupId) {
        this.eventConstraintGroupId = groupId;

        String serializedGroupId = serializeGroupIdOrConstraint(groupId);

        getOptions().setEventConstraint(
                getOptions().getEventConstraint().isEnabled(),
                serializedGroupId,
                getOptions().getEventConstraint().getBusinessHours());
    }

    @Nullable
    public Object getSelectConstraintGroupId() {
        return selectConstraintGroupId;
    }

    public void setSelectConstraintGroupId(@Nullable Object groupId) {
        this.selectConstraintGroupId = groupId;

        String serializedGroupId = serializeGroupIdOrConstraint(groupId);

        getOptions().setSelectConstraint(
                getOptions().getSelectConstraint().isEnabled(),
                serializedGroupId,
                getOptions().getSelectConstraint().getBusinessHours());
    }

    public boolean isBusinessHoursEnabled() {
        return getOptions().getBusinessHours().isEnabled();
    }

    public void setBusinessHoursEnabled(boolean enabled) {
        getOptions().setBusinessHours(enabled, getOptions().getBusinessHours().getBusinessHours());
    }

    public List<BusinessHours> getBusinessHours() {
        return getOptions().getBusinessHours().getBusinessHours();
    }

    public void setBusinessHours(@Nullable List<BusinessHours> businessHours) {
        getOptions().setBusinessHours(getOptions().getBusinessHours().isEnabled(), businessHours);
    }

    public boolean isEventConstraintEnabled() {
        return getOptions().getEventConstraint().isEnabled();
    }

    public void setEventConstraintEnabled(boolean enabled) {
        getOptions().setEventConstraint(enabled,
                getOptions().getEventConstraint().getGroupId(),
                getOptions().getEventConstraint().getBusinessHours());
    }

    public List<BusinessHours> getEventConstraintBusinessHours() {
        return getOptions().getEventConstraint().getBusinessHours();
    }

    public void setEventConstraintBusinessHours(@jakarta.annotation.Nullable List<BusinessHours> businessHoursEventConstraint) {
        getOptions().setEventConstraint(
                getOptions().getEventConstraint().isEnabled(),
                getOptions().getEventConstraint().getGroupId(),
                businessHoursEventConstraint);
    }

    public boolean isSelectConstraintEnabled() {
        return getOptions().getSelectConstraint().isEnabled();
    }

    public void setSelectConstraintEnabled(boolean enabled) {
        getOptions().setSelectConstraint(
                enabled,
                getOptions().getSelectConstraint().getGroupId(),
                getOptions().getSelectConstraint().getBusinessHours());
    }

    public List<BusinessHours> getSelectConstraintBusinessHours() {
        return getOptions().getSelectConstraint().getBusinessHours();
    }

    public void setSelectConstraintBusinessHours(@jakarta.annotation.Nullable List<BusinessHours> businessHoursSelectConstraint) {
        getOptions().setSelectConstraint(
                getOptions().getSelectConstraint().isEnabled(),
                getOptions().getSelectConstraint().getGroupId(),
                businessHoursSelectConstraint);
    }

    public Registration addDatesSetListener(ComponentEventListener<DatesSetEvent> listener) {
        Preconditions.checkNotNullArgument(listener);

        return getEventBus().addListener(DatesSetEvent.class, listener);
    }

    /**
     * Adds a "more" link click listener. When listener is added the {@link #setMoreLinkCalendarView(CalendarView)}
     * value will be ignored.
     *
     * @param listener listener to add
     * @return A registration object for removing an event listener added to a calendar
     */
    public Registration addMoreLinkClickListener(ComponentEventListener<MoreLinkClickEvent> listener) {
        Preconditions.checkNotNullArgument(listener);

        Registration registration = getEventBus().addListener(MoreLinkClickEvent.class, listener);

        options.setMoreLinkClickFunction(true);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(MoreLinkClickEvent.class)) {
                options.setMoreLinkClickFunction(false);
            }
        };
    }

    public Registration addEventClickListener(ComponentEventListener<EventClickEvent> listener) {
        if (!getEventBus().hasListener(EventClickEvent.class)) {
            attachEventClickDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventClickEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventClickEvent.class)) {
                detachEventClickDomEventListener();
            }
        };
    }

    public Registration addEventMouseEnterListener(ComponentEventListener<EventMouseEnterEvent> listener) {
        if (!getEventBus().hasListener(EventMouseEnterEvent.class)) {
            attachEventMouseEnterDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventMouseEnterEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventMouseEnterEvent.class)) {
                detachEventMouseEnterDomEventListener();
            }
        };
    }

    public Registration addEventMouseLeaveListener(ComponentEventListener<EventMouseLeaveEvent> listener) {
        if (!getEventBus().hasListener(EventMouseLeaveEvent.class)) {
            attachEventMouseLeaveDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventMouseLeaveEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventMouseLeaveEvent.class)) {
                detachEventMouseLeaveDomEventListener();
            }
        };
    }

    public Registration addEventDropListener(ComponentEventListener<EventDropEvent> listener) {
        if (!getEventBus().hasListener(EventDropEvent.class)) {
            attachEventDropDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventDropEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventDropEvent.class)) {
                detachEventDropDomEventListener();
            }
        };
    }

    public Registration addEventResizeListener(ComponentEventListener<EventResizeEvent> listener) {
        if (!getEventBus().hasListener(EventResizeEvent.class)) {
            attachEventResizeDomEventListener();
        }

        Registration registration = getEventBus().addListener(EventResizeEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(EventResizeEvent.class)) {
                detachEventResizeDomEventListener();
            }
        };
    }

    public Registration addDateClickListener(ComponentEventListener<DateClickEvent> listener) {
        if (!getEventBus().hasListener(DateClickEvent.class)) {
            attachDateClickDomEventListener();
        }

        Registration registration = getEventBus().addListener(DateClickEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(DateClickEvent.class)) {
                detachDateClickDomEventListener();
            }
        };
    }

    public Registration addSelectListener(ComponentEventListener<SelectEvent> listener) {
        if (!getEventBus().hasListener(SelectEvent.class)) {
            attachSelectDomEventListener();
        }

        Registration registration = getEventBus().addListener(SelectEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(SelectEvent.class)) {
                detachSelectDomEventListener();
            }
        };
    }

    public Registration addUnselectListener(ComponentEventListener<UnselectEvent> listener) {
        if (!getEventBus().hasListener(UnselectEvent.class)) {
            attachUnselectDomEventListener();
        }

        Registration registration = getEventBus().addListener(UnselectEvent.class, listener);

        return () -> {
            registration.remove();
            if (!getEventBus().hasListener(UnselectEvent.class)) {
                detachUnselectDomEventListener();
            }
        };
    }

    @Nullable
    public Function<MoreLinkClassNamesContext, List<String>> getMoreLinkClassNamesGenerator() {
        return linkMoreClassNamesGenerator;
    }

    /**
     * Sets a class names generator for "+x more" link.
     * <p>
     * Note, generator has a precedence over a {@link #setMoreLinkClassNames(List)} and other "add class name" methods.
     *
     * @param classNamesGenerator the generator to set
     */
    public void setMoreLinkClassNamesGenerator(
            @Nullable Function<MoreLinkClassNamesContext, List<String>> classNamesGenerator) {
        this.linkMoreClassNamesGenerator = classNamesGenerator;

        options.setMoreLinkClassNames(options.getMoreLinkClassNames().getClassNames(), classNamesGenerator != null);
    }

    @Override
    protected FullCalendarOptions createOptions() {
        return new FullCalendarOptions();
    }

    @Override
    protected JmixFullCalendarSerializer createSerializer() {
        return new FullCalendarSerializer();
    }

    protected FullCalendarOptions getOptions() {
        return (FullCalendarOptions) options;
    }

    @Override
    protected JsonArray fetchCalendarItems(String sourceId, String start, String end) {
        return calendarDelegate.fetchCalendarItems(sourceId, start, end);
    }

    @Override
    @SuppressWarnings("UnnecessaryLocalVariable")
    protected JsonArray getMoreLinkClassNames(JsonObject jsonContext) {
        DomMoreLinkClassNames clientContext =
                deserializer.deserialize(jsonContext, DomMoreLinkClassNames.class);

        JsonArray classNames = calendarDelegate.getMoreLinkClassNames(clientContext);

        return classNames;
    }

    @Override
    protected void onDatesSet(DatesSetDomEvent event) {
        super.onDatesSet(event);

        DomDatesSet domDatesSet = deserializer.deserialize(event.getContext(), DomDatesSet.class);

        DatesSetEvent datesSetEvent = calendarDelegate.convertToDatesSetEvent(domDatesSet, event.isFromClient());

        getEventBus().fireEvent(datesSetEvent);
    }

    @Override
    protected void onMoreLinkClick(MoreLinkClickDomEvent event) {
        DomMoreLinkClick clientContext =
                deserializer.deserialize(event.getContext(), DomMoreLinkClick.class);

        MoreLinkClickEvent clickEvent = calendarDelegate.convertToMoreLinkClickEvent(clientContext, event.isFromClient());

        getEventBus().fireEvent(clickEvent);
    }

    @Override
    protected void onEventClick(EventClickDomEvent event) {
        DomEventMouse clientContext = deserializer
                .deserialize(event.getContext(), DomEventMouse.class);

        EventClickEvent clickEvent = calendarDelegate.convertToEventClickEvent(clientContext, event.isFromClient());

        getEventBus().fireEvent(clickEvent);
    }

    @Override
    protected void onEventMouseEnter(EventMouseEnterDomEvent event) {
        DomEventMouse clientContext = deserializer
                .deserialize(event.getContext(), DomEventMouse.class);

        EventMouseEnterEvent eventMouseEnterEvent = calendarDelegate
                .convertToEventMouseEnterEvent(clientContext, event.isFromClient());

        getEventBus().fireEvent(eventMouseEnterEvent);
    }

    @Override
    protected void onEventMouseLeave(EventMouseLeaveDomEvent event) {
        DomEventMouse clientContext = deserializer
                .deserialize(event.getContext(), DomEventMouse.class);

        EventMouseLeaveEvent eventMouseLeaveEvent = calendarDelegate
                .convertToEventMouseLeaveEvent(clientContext, event.isFromClient());

        getEventBus().fireEvent(eventMouseLeaveEvent);
    }

    @Override
    protected void onEventDrop(EventDropDomEvent event) {
        DomEventDrop clientEvent = deserializer
                .deserialize(event.getContext(), DomEventDrop.class);

        EventDropEvent eventDropEvent =
                calendarDelegate.convertToEventDropEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(eventDropEvent);
    }

    @Override
    protected void onEventResize(EventResizeDomEvent event) {
        DomEventResize clientEvent = deserializer
                .deserialize(event.getContext(), DomEventResize.class);

        EventResizeEvent eventResizeEvent =
                calendarDelegate.convertToEventResizeEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(eventResizeEvent);
    }

    @Override
    protected void onDateClick(DateClickDomEvent event) {
        DomDateClick clientEvent = deserializer.deserialize(event.getContext(), DomDateClick.class);

        DateClickEvent dateClickEvent = calendarDelegate.convertToDateClickEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(dateClickEvent);
    }

    @Override
    protected void onSelect(SelectDomEvent event) {
        DomSelect clientEvent = deserializer.deserialize(event.getContext(), DomSelect.class);

        SelectEvent selectEvent = calendarDelegate.convertToSelectEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(selectEvent);
    }

    @Override
    protected void onUnselect(UnselectDomEvent event) {
        DomUnselect clientEvent = deserializer.deserialize(event.getContext(), DomUnselect.class);

        UnselectEvent unselectEvent = calendarDelegate.convertToUnselectEvent(clientEvent, event.isFromClient());

        getEventBus().fireEvent(unselectEvent);
    }

    @Override
    protected AbstractItemEventProviderManager createItemEventProviderManager(ItemCalendarEventProvider eventProvider) {
        return new ItemEventProviderManager(eventProvider);
    }

    @Override
    protected AbstractLazyEventProviderManager createLazyEventProviderManager(LazyCalendarEventProvider eventProvider) {
        return new LazyEventProviderManager(eventProvider);
    }

    /**
     * Is used for getting event providers in component delegate (see {@link FullCalendarDelegate}.
     *
     * @return event providers map
     */
    protected Map<String, AbstractEventProviderManager> getEventProvidersMap() {
        return eventProvidersMap;
    }

    /**
     * Is used for getting calendar view in component delegate (see {@link FullCalendarDelegate}.
     *
     * @return event providers map
     */
    @Override
    protected CalendarView getCalendarView(String id) {
        return super.getCalendarView(id);
    }

    @Nullable
    protected String serializeGroupIdOrConstraint(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        String rawValue = CalendarEventSerializer.serializeGroupIdOrConstraint(value);

        return rawValue != null ? rawValue : crossEventProviderKeyMapper.key(value);
    }

    protected void initTimeZone() {
        TimeZone timeZone = applicationContext.getBean(CurrentAuthentication.class).getTimeZone();

        setTimeZone(timeZone);
    }
}
