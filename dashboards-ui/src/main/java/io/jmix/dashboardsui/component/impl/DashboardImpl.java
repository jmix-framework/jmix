/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboardsui.component.impl;

import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.dashboards.entity.PersistentDashboard;
import io.jmix.dashboards.model.DashboardModel;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.parameter.Parameter;
import io.jmix.dashboardsui.DashboardException;
import io.jmix.dashboardsui.component.CanvasLayout;
import io.jmix.dashboardsui.component.Dashboard;
import io.jmix.dashboardsui.dashboard.assistant.DashboardViewAssistant;
import io.jmix.dashboards.converter.JsonConverter;
import io.jmix.dashboardsui.dashboard.tools.AccessConstraintsHelper;
import io.jmix.dashboardsui.event.DashboardEvent;
import io.jmix.dashboardsui.event.DashboardUpdatedEvent;
import io.jmix.dashboardsui.event.ItemsSelectedEvent;
import io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasFragment;
import io.jmix.dashboardsui.transformation.ParameterTransformer;
import io.jmix.dashboardsui.widget.LookupWidget;
import io.jmix.dashboardsui.widget.RefreshableWidget;
import io.jmix.ui.*;
import io.jmix.ui.component.Timer;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.sys.UiControllerReflectionInspector;
import io.jmix.ui.sys.event.UiEventListenerMethodAdapter;
import io.jmix.ui.sys.event.UiEventsMulticaster;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasFragment.DASHBOARD;
import static io.jmix.dashboardsui.screen.dashboard.view.DashboardViewScreen.CODE;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@CompositeDescriptor("dashboard.xml")
public class DashboardImpl extends CompositeComponent<VBoxLayout> implements Dashboard {

    private Logger log = LoggerFactory.getLogger(DashboardImpl.class);

    protected VBoxLayout canvasBox;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected JsonConverter jsonConverter;

    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected AccessConstraintsHelper accessHelper;

    @Autowired
    protected UiComponents componentsFactory;

    @Autowired
    protected Facets facets;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected ParameterTransformer parameterTransformer;

    protected DashboardViewAssistant dashboardViewAssistant;

    @Autowired
    protected UiEventPublisher uiEventPublisher;

    protected CanvasFragment canvasFragment;

    @Autowired
    protected Messages messages;

    protected String code;
    protected String jsonPath;
    protected Integer timerDelay = 0;
    protected List<Parameter> xmlParameters = new ArrayList<>();
    protected String assistantBeanName;
    protected DashboardModel dashboardModel;

    public DashboardImpl() {
        addCreateListener(this::onCreate);
    }

    private void onCreate(CreateEvent createEvent) {
        canvasBox = getComposition();
    }

    @Override
    public void init(Map<String, Object> params) {
        setCode(StringUtils.isEmpty(code) ? (String) params.getOrDefault(CODE, "") : code);
        refresh(params);
        if (dashboardModel != null) {
            initAssistant(this);
            initTimer(getFrame());
            initLookupWidget();
        } else {
            Label<String> errorMessageLabel = componentsFactory.create(Label.TYPE_STRING);
            String errorMessage = messages.getMessage(DashboardImpl.class, "dashboardNotFound");
            if (isNotBlank(jsonPath)) {
                errorMessage = messages.formatMessage(DashboardImpl.class, "dashboardWithJsonNotFound", jsonPath);
            } else if (isNotBlank(code)) {
                errorMessage = messages.formatMessage(DashboardImpl.class, "dashboardWithCodeNotFound", code);
            }
            errorMessageLabel.setValue(errorMessage);
            canvasBox.add(errorMessageLabel);
            log.error(errorMessage);
        }
    }

    private void initLookupWidget() {
        List<LookupWidget> lookupWidgets = canvasFragment.getLookupWidgets();
        for (LookupWidget widget : lookupWidgets) {
            assignItemSelectedHandler(widget);
        }
    }

    private void assignItemSelectedHandler(LookupWidget widget) {
        ListComponent lookupComponent = widget.getLookupComponent();
        DataUnit items = lookupComponent.getItems();
        if (items != null) {
            items.addStateChangeListener(e -> {
                uiEventPublisher.publishEvent(new ItemsSelectedEvent((Widget) widget, lookupComponent.getSelected()));
            });
        }
    }

    @Override
    public void refresh() {
        if (isNotBlank(jsonPath)) {
            updateDashboard(loadDashboardByJson(jsonPath));
        } else if (isNotBlank(code)) {
            updateDashboard(loadDashboardByCode(code));
        }
    }

    /**
     * Refreshes widget with passed parameters.
     * Dashboard will be refreshed with merged existed and new parameters.
     * If existed parameter has the same name as one of the param from passed map, it will be overwritten by param from map.
     *
     * @param params map with new dashboard parameters
     */
    @Override
    public void refresh(Map<String, Object> params) {
        DashboardModel dashboardModel = null;

        if (isNotBlank(jsonPath)) {
            dashboardModel = loadDashboardByJson(jsonPath);
        } else if (isNotBlank(code)) {
            dashboardModel = loadDashboardByCode(code);
        }

        if (MapUtils.isNotEmpty(params) && Objects.nonNull(dashboardModel)) {
            Map<String, Parameter> newParams = params.keySet().stream()
                    .map(key -> {
                        Parameter parameter = metadata.create(Parameter.class);
                        parameter.setName(key);
                        parameter.setAlias(key);
                        parameter.setValue(parameterTransformer.createParameterValue(params.get(key)));
                        return parameter;
                    })
                    .collect(Collectors.toMap(Parameter::getName, Function.identity()));

            Map<String, Parameter> paramMap = dashboardModel.getParameters().stream().collect(Collectors.toMap(Parameter::getName, Function.identity()));
            paramMap.putAll(newParams);
            dashboardModel.setParameters(new ArrayList<>(paramMap.values()));
        }

        updateDashboard(dashboardModel);
    }

    private void initAssistant(Dashboard dashboard) {
        String assistantBeanName = StringUtils.isEmpty(getAssistantBeanName()) ? this.dashboardModel.getAssistantBeanName() : getAssistantBeanName();
        if (StringUtils.isNotEmpty(assistantBeanName)) {
            DashboardViewAssistant assistantBean = applicationContext.getBean(assistantBeanName, DashboardViewAssistant.class);
            BeanFactory bf = ((AbstractApplicationContext) applicationContext).getBeanFactory();
            if (assistantBean != null && bf.isPrototype(assistantBeanName)) {
                assistantBean.init(dashboard);
                dashboardViewAssistant = assistantBean;
            }
        }
    }

    private void initTimer(Component frame) {
        Window parentWindow = findWindow(frame);
        int timerDelay = getTimerDelay() == 0 ? dashboardModel.getTimerDelay() : getTimerDelay();
        if (timerDelay > 0 && parentWindow != null) {
            Timer dashboardUpdatedTimer = facets.create(Timer.class);
            dashboardUpdatedTimer.setOwner(getFrame());
            dashboardUpdatedTimer.setDelay(timerDelay * 1000);
            dashboardUpdatedTimer.setRepeating(true);
            dashboardUpdatedTimer.addTimerActionListener(timer -> uiEventPublisher.publishEvent(new DashboardUpdatedEvent(dashboardModel))
            );
            dashboardUpdatedTimer.start();
        }
    }

    @SuppressWarnings("unchecked")
    @EventListener
    public void dashboardEventListener(DashboardEvent dashboardEvent) throws InvocationTargetException, IllegalAccessException {
        refreshWidgets(dashboardEvent);

        if (dashboardViewAssistant == null) {
            return;
        }
        Class eventClass = dashboardEvent.getClass();
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(dashboardViewAssistant.getClass());
        List<Method> eventListenerMethods = Arrays.stream(methods)
                .filter(m -> m.getAnnotation(EventListener.class) != null)
                .filter(m -> m.getParameterCount() == 1)
                .collect(Collectors.toList());

        for (Method method : eventListenerMethods) {
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            java.lang.reflect.Parameter parameter = parameters[0];
            Class methodEventTypeArg = parameter.getType();
            if (methodEventTypeArg.isAssignableFrom(eventClass)) {
                method.invoke(dashboardViewAssistant, dashboardEvent);

            }
        }
    }

    protected void updateDashboard(@Nullable DashboardModel dashboardModel) {
        if (dashboardModel == null) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(DashboardImpl.class, "notLoadedDashboard"))
                    .show();
            return;
        }

        if (!accessHelper.isDashboardAllowedCurrentUser(dashboardModel)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(DashboardImpl.class, "notOpenBrowseDashboard"))
                    .show();
            return;
        }

        addXmlParameters(dashboardModel);
        updateCanvasFragment(dashboardModel);
        this.dashboardModel = dashboardModel;
    }

    protected DashboardModel loadDashboardByJson(String jsonPath) {
        Resource jsonRes = resourceLoader.getResource(format("classpath:%s", jsonPath));
        if (!jsonRes.exists()) {
            throw new DashboardException(format("There isn't the json file by the path: %s", jsonPath));
        }

        try {
            String json = IOUtils.toString(jsonRes.getInputStream(), UTF_8);
            return jsonConverter.dashboardFromJson(json);
        } catch (Exception e) {
            throw new DashboardException(format("Error reading the json by the path: %s", jsonPath), e);
        }
    }

    @Nullable
    protected DashboardModel loadDashboardByCode(String code) {
        PersistentDashboard entity = dataManager.load(PersistentDashboard.class)
                .query("select d from dshbrd_PersistentDashboard d where d.code = :code")
                .parameter("code", code)
                .fetchPlan("_local")
                .optional().orElse(null);
        if (entity == null || entity.getDashboardModel() == null) {
            return null;
        }
        return jsonConverter.dashboardFromJson(entity.getDashboardModel());
    }

    protected void addXmlParameters(DashboardModel dashboardModel) {
        List<Parameter> parameters = dashboardModel.getParameters();
        parameters.removeAll(getDuplicatesParams(dashboardModel));
        parameters.addAll(xmlParameters);
    }

    protected List<Parameter> getDuplicatesParams(DashboardModel dashboardModel) {
        return dashboardModel.getParameters().stream()
                .filter(param -> xmlParameters.stream()
                        .anyMatch(xmlParameter -> param.getAlias().equals(xmlParameter.getAlias())))
                .collect(Collectors.toList());
    }

    protected void updateCanvasFragment(DashboardModel dashboardModel) {
        if (canvasFragment == null) {
            canvasFragment = fragments.create(getFrame().getFrameOwner(), CanvasFragment.class, new MapScreenOptions(ParamsMap.of(
                    CanvasFragment.DASHBOARD_MODEL, dashboardModel, DASHBOARD, this
            )));
            canvasFragment.init();
            canvasBox.removeAll();
            canvasBox.add(canvasFragment.getFragment());
        } else {
            canvasFragment.updateLayout(dashboardModel);
        }
    }

    protected void refreshWidgets(DashboardEvent dashboardEvent) {
        List<RefreshableWidget> rws = canvasFragment.getRefreshableWidgets();
        for (RefreshableWidget rw : rws) {
            rw.refresh(dashboardEvent);
        }
    }

    @Nullable
    protected Window findWindow(Component frame) {
        Component parent = frame;
        while (parent != null) {
            if (parent instanceof Window) {
                return (Window) parent;
            } else {
                parent = parent.getParent();
            }
        }
        log.error(messages.getMessage(DashboardImpl.class, "dashboard.noWindow"));
        return null;
    }

    public ScreenFragment getWidget(String widgetId) {
        return searchWidgetFragment(canvasFragment.getvLayout(), widgetId);
    }

    @Nullable
    protected ScreenFragment searchWidgetFragment(Component layout, String widgetId) {
        if (CanvasWidgetLayout.class.isAssignableFrom(layout.getClass())) {
            CanvasWidgetLayout canvasWidgetLayout = (CanvasWidgetLayout) layout;
            if (widgetId.equals(canvasWidgetLayout.getWidget().getWidgetId())) {
                return canvasWidgetLayout.getWidgetComponent();
            }
            return null;
        }

        Collection<Component> components = Collections.EMPTY_LIST;
        if (layout instanceof CanvasLayout) {
            components = ((CanvasLayout) layout).getLayoutComponents();
        } else if (layout instanceof ComponentContainer) {
            components = ((ComponentContainer) layout).getOwnComponents();
        }

        for (Component child : components) {
            ScreenFragment result = searchWidgetFragment(child, widgetId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    @Override
    public void setXmlParameters(List<Parameter> parameters) {
        xmlParameters = parameters;
    }

    @Override
    public void setTimerDelay(int delay) {
        this.timerDelay = delay;
    }

    @Override
    public int getTimerDelay() {
        return timerDelay;
    }

    @Override
    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }

    @Override
    public String getAssistantBeanName() {
        return assistantBeanName;
    }

    @Override
    public void setAssistantBeanName(String assistantBeanName) {
        this.assistantBeanName = assistantBeanName;
    }
}
