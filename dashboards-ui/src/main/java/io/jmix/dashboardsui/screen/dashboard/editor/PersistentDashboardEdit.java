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

package io.jmix.dashboardsui.screen.dashboard.editor;

import io.jmix.core.CoreProperties;
import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.dashboards.entity.DashboardGroup;
import io.jmix.dashboards.entity.PersistentDashboard;
import io.jmix.dashboards.model.DashboardModel;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboardsui.DashboardException;
import io.jmix.dashboardsui.dashboard.assistant.DashboardViewAssistant;
import io.jmix.dashboards.converter.JsonConverter;
import io.jmix.dashboardsui.dashboard.event.DashboardRefreshEvent;
import io.jmix.dashboardsui.dashboard.event.WidgetAddedEvent;
import io.jmix.dashboardsui.dashboard.event.WidgetMovedEvent;
import io.jmix.dashboardsui.dashboard.event.model.*;
import io.jmix.dashboardsui.dashboard.tools.AccessConstraintsHelper;
import io.jmix.dashboardsui.dashboard.tools.DropLayoutTools;
import io.jmix.dashboardsui.event.DashboardUpdatedEvent;
import io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasEditorFragment;
import io.jmix.dashboardsui.screen.dashboard.editor.colspan.ColspanDialog;
import io.jmix.dashboardsui.screen.dashboard.editor.expand.ExpandDialog;
import io.jmix.dashboardsui.screen.dashboard.editor.palette.PaletteFragment;
import io.jmix.dashboardsui.screen.dashboard.editor.responsive.ResponsiveCreationDialog;
import io.jmix.dashboardsui.screen.dashboard.editor.style.StyleDialog;
import io.jmix.dashboardsui.screen.dashboard.editor.weight.WeightDialog;
import io.jmix.dashboardsui.screen.parameter.ParametersFragment;
import io.jmix.dashboardsui.screen.widget.WidgetEdit;
import io.jmix.ui.*;
import io.jmix.ui.component.*;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstanceLoader;
import io.jmix.ui.screen.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.dashboards.utils.DashboardLayoutUtils.findParentLayout;
import static io.jmix.dashboards.utils.DashboardLayoutUtils.isParentResponsiveLayout;
import static io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasFragment.DASHBOARD_MODEL;
import static io.jmix.ui.component.ResponsiveGridLayout.Breakpoint.*;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UiController("dshbrd_PersistentDashboard.edit")
@UiDescriptor("persistent-dashboard-edit.xml")
@EditedEntityContainer("persistentDashboardDc")
public class PersistentDashboardEdit extends StandardEditor<PersistentDashboard> {
    @Autowired
    protected InstanceContainer<DashboardModel> dashboardDc;
    @Autowired
    private CollectionLoader<DashboardGroup> groupsDl;
    @Autowired
    @Qualifier("dashboardEditForm2")
    protected Form form2;
    @Autowired
    @Qualifier("assistantBeanName")
    private ComboBox<String> assistantBeanNameField;
    @Autowired
    protected GroupBoxLayout paramsBox;
    @Autowired
    protected VBoxLayout paletteBox;
    @Autowired
    protected VBoxLayout canvasBox;
    @Autowired
    protected JsonConverter converter;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected FileUploadField importJsonField;
    @Autowired
    protected AccessConstraintsHelper accessHelper;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected Screens screens;
    @Autowired
    protected ScreenBuilders screenBuilders;

    protected CanvasEditorFragment canvasFragment;

    protected DropLayoutTools dropLayoutTools;

    @Autowired
    @Qualifier("isAvailableForAllUsersCheckBox")
    protected CheckBox availableCheckBox;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Messages messages;

    @Autowired
    private InstanceLoader<PersistentDashboard> persistentDashboardDl;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        groupsDl.load();

        setDashboardModel();

        assistantBeanNameField.setOptionsList(getAssistanceBeanNames());

        if (!accessHelper.getCurrentUsername().equals(dashboardDc.getItem().getCreatedBy())) {
            availableCheckBox.setVisible(false);
        }

        dropLayoutTools = applicationContext.getBean(DropLayoutTools.class, this, dashboardDc);
        initParametersFragment();
        initPaletteFragment();
        initCanvasFragment();
    }

    protected void setDashboardModel() {
        DashboardModel model;
        if (entityStates.isNew(getEditedEntity())) {
            model = metadata.create(DashboardModel.class);
            model.setVisualModel(metadata.create(RootLayout.class));
            model.setCreatedBy(accessHelper.getCurrentUsername());
        } else {
            persistentDashboardDl.load();
            model = converter.dashboardFromJson(getEditedEntity().getDashboardModel());
        }
        dashboardDc.setItem(model);
    }

    public DashboardModel getDashboardModel() {
        return dashboardDc.getItem();
    }

    protected void initParametersFragment() {
        paramsBox.removeAll();
        ScreenFragment parametersFragment = fragments.create(this, ParametersFragment.class,
                new MapScreenOptions(ParamsMap.of(ParametersFragment.PARAMETERS, getDashboardModel().getParameters(),
                        DASHBOARD_MODEL, getDashboardModel()))
        ).init();
        paramsBox.add(parametersFragment.getFragment());
    }

    protected void initPaletteFragment() {
        paletteBox.removeAll();
        ScreenFragment paletteFragment = fragments.create(this, PaletteFragment.class,
                new MapScreenOptions(ParamsMap.of(DASHBOARD_MODEL, getDashboardModel()))
        ).init();
        paletteBox.add(paletteFragment.getFragment());
    }

    protected void initCanvasFragment() {
        canvasBox.removeAll();
        canvasFragment = (CanvasEditorFragment) fragments.create(this, CanvasEditorFragment.class,
                new MapScreenOptions(ParamsMap.of(DASHBOARD_MODEL, getDashboardModel()))
        ).init();
        canvasBox.add(canvasFragment.getFragment());
    }

    @Subscribe("importJsonField")
    public void onImportJsonFieldFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        uploadJson();
    }

    @Subscribe("exportJsonBtn")
    public void onExportJsonBtnClick(Button.ClickEvent event) {
        String jsonModel = converter.dashboardToJson(getDashboardModel());

        if (isNotBlank(jsonModel)) {
            byte[] bytes = jsonModel.getBytes(UTF_8);
            String fileName = isNotBlank(getDashboardModel().getTitle()) ? getDashboardModel().getTitle() : "dashboard";
            downloader.download(new ByteArrayDataProvider(bytes, uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                    coreProperties.getTempDir()), format("%s.json", fileName));
        }
    }

    protected void uploadJson() {
        try (InputStream fileContent = importJsonField.getFileContent()) {
            String json = IOUtils.toString(Objects.requireNonNull(fileContent), UTF_8);
            DashboardModel newDashboardModel = metadata.create(DashboardModel.class);
            BeanUtils.copyProperties(converter.dashboardFromJson(json), newDashboardModel);

            dashboardDc.setItem(newDashboardModel);
            initParametersFragment();
            initPaletteFragment();
            canvasFragment.updateLayout(newDashboardModel);

        } catch (Exception e) {
            throw new DashboardException("Cannot import data from a file", e);
        }
    }

    @Subscribe("propagateBtn")
    public void onPropagateBtnClick(Button.ClickEvent event) {
        DashboardModel dashboard = getDashboardModel();
        uiEventPublisher.publishEvent(new DashboardUpdatedEvent(dashboard));
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void preCommit(DataContext.PreCommitEvent event) {
        DashboardModel dashboard = getDashboardModel();
        String jsonModel = converter.dashboardToJson(dashboard);
        getEditedEntity().setDashboardModel(jsonModel);
    }

    @Subscribe(id = "dashboardDc", target = Target.DATA_CONTAINER)
    public void onDashboardDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<DashboardModel> event) {
        DashboardModel dashboardModel = event.getItem();
        if (StringUtils.equals(event.getProperty(), "title")) {
            getEditedEntity().setName(dashboardModel.getTitle());
        } else if (StringUtils.equals(event.getProperty(), "code")) {
            getEditedEntity().setCode(dashboardModel.getCode());
        } else if (StringUtils.equals(event.getProperty(), "isAvailableForAllUsers")) {
            getEditedEntity().setIsAvailableForAllUsers(dashboardModel.getIsAvailableForAllUsers());
        } else {
            String jsonModel = converter.dashboardToJson(dashboardModel);
            getEditedEntity().setDashboardModel(jsonModel);
        }
    }

    @Subscribe(id = "dashboardDc", target = Target.DATA_CONTAINER)
    public void onDashboardDcItemChange(InstanceContainer.ItemChangeEvent<DashboardModel> event) {
        DashboardModel dashboardModel = event.getItem();
        if (dashboardModel != null) {
            getEditedEntity().setName(dashboardModel.getTitle());
            getEditedEntity().setCode(dashboardModel.getCode());
            getEditedEntity().setIsAvailableForAllUsers(dashboardModel.getIsAvailableForAllUsers());
            String jsonModel = converter.dashboardToJson(dashboardModel);
            getEditedEntity().setDashboardModel(jsonModel);
        }
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPostCommit(DataContext.PostCommitEvent event) {
        uiEventPublisher.publishEvent(new DashboardUpdatedEvent(getDashboardModel()));
    }

    public List<String> getAssistanceBeanNames() {
        Map<String, DashboardViewAssistant> assistantBeanMap = applicationContext.getBeansOfType(DashboardViewAssistant.class);
        BeanFactory bf = ((AbstractApplicationContext) applicationContext).getBeanFactory();
        return assistantBeanMap.keySet().stream()
                .filter(bf::isPrototype)
                .collect(toList());
    }

    @EventListener
    public void onWidgetMoved(WidgetMovedEvent event) {
        UUID targetLayoutId = event.getParentLayoutUuid();
        DashboardModel dashboard = getDashboardModel();

        dropLayoutTools.moveComponent(event.getSource(), targetLayoutId, event.getLocation());
        uiEventPublisher.publishEvent(new DashboardRefreshEvent(dashboard.getVisualModel(), event.getSource().getId()));
    }

    @EventListener
    public void onWeightChanged(WeightChangedEvent event) {
        DashboardLayout source = event.getSource();

        if (isParentResponsiveLayout(source)) {
            ResponsiveLayout parentLayout = (ResponsiveLayout) findParentLayout(getDashboardModel().getVisualModel(), source.getId());
            ResponsiveArea responsiveArea = parentLayout.getAreas().stream().
                    filter(ra -> source.getId().equals(ra.getComponent().getId())).
                    findFirst().orElseThrow(() -> new RuntimeException("Can't find layout with uuid " + source.getId()));

            screens.create(ResponsiveCreationDialog.class, OpenMode.DIALOG, new MapScreenOptions(getDisplaySizeMap(responsiveArea)))
                    .show()
                    .addAfterCloseListener(e -> {
                        StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                        ResponsiveCreationDialog dialog = (ResponsiveCreationDialog) e.getSource();
                        if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                            responsiveArea.setXs(dialog.getXs());
                            responsiveArea.setSm(dialog.getSm());
                            responsiveArea.setMd(dialog.getMd());
                            responsiveArea.setLg(dialog.getLg());
                            uiEventPublisher.publishEvent(new DashboardRefreshEvent(getDashboardModel().getVisualModel(), source.getId()));
                        }
                    });
        } else {
            screens.create(WeightDialog.class, OpenMode.DIALOG, new MapScreenOptions(ParamsMap.of(
                    WeightDialog.WIDGET, source)
            ))
                    .show()
                    .addAfterCloseListener(e -> {
                        StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                        if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                            uiEventPublisher.publishEvent(new DashboardRefreshEvent(getDashboardModel().getVisualModel(), source.getId()));
                        }
                    });
        }
    }

    @EventListener
    public void onColspanChanged(ColspanChangedEvent event) {
        DashboardLayout source = event.getSource();

        screens.create(ColspanDialog.class, OpenMode.DIALOG, new MapScreenOptions(ParamsMap.of(
                WeightDialog.WIDGET, source)
        ))
                .show()
                .addAfterCloseListener(e -> {
                    StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                    if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                        uiEventPublisher.publishEvent(new DashboardRefreshEvent(getDashboardModel().getVisualModel(), source.getId()));
                    }
                });
    }

    @EventListener
    public void onExpandChanged(ExpandChangedEvent event) {
        DashboardLayout source = event.getSource();

        screens.create(ExpandDialog.class, OpenMode.DIALOG, new MapScreenOptions(ParamsMap.of(
                ExpandDialog.WIDGET, source)
        ))
                .show()
                .addAfterCloseListener(e -> {
                    StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                    if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                        DashboardLayout expandLayout = ((ExpandDialog) e.getSource()).getExpand();
                        if (expandLayout != null) {
                            source.setExpand(expandLayout.getId());
                        } else {
                            source.setExpand(null);
                        }
                        uiEventPublisher.publishEvent(new DashboardRefreshEvent(getDashboardModel().getVisualModel(), source.getId()));
                    }
                });
    }

    @EventListener
    public void onStyleChanged(StyleChangedEvent event) {
        DashboardLayout source = event.getSource();

        screens.create(StyleDialog.class, OpenMode.DIALOG, new MapScreenOptions(ParamsMap.of(
                StyleDialog.WIDGET, source)))
                .show()
                .addAfterCloseListener(e -> {
                    StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                    if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                        DashboardModel dashboard = getDashboardModel();
                        StyleDialog styleDialog = (StyleDialog) e.getSource();
                        DashboardLayout layout = dashboard.getVisualModel().findLayout(source.getId());
                        layout.setStyleName(styleDialog.getLayoutStyleName());
                        layout.setHeight(styleDialog.getLayoutHeight());
                        layout.setHeightUnit(styleDialog.getLayoutHeightUnit());
                        layout.setWidth(styleDialog.getLayoutWidth());
                        layout.setWidthUnit(styleDialog.getLayoutWidthUnit());
                        uiEventPublisher.publishEvent(new DashboardRefreshEvent(dashboard.getVisualModel(), source.getId()));
                    }
                });
    }

    @EventListener
    public void onRemoveLayout(WidgetRemovedEvent event) {
        DashboardLayout dashboardLayout = getDashboardModel().getVisualModel();
        dashboardLayout.removeChild(event.getSource().getId());
        uiEventPublisher.publishEvent(new DashboardRefreshEvent(dashboardLayout));
    }

    @EventListener
    public void widgetAddedToTreeEventListener(WidgetAddedEvent event) {
        dropLayoutTools.addComponent(event.getSource(), event.getParentLayoutUuid(), event.getLocation());
    }

    @EventListener
    public void onOpenWidgetEditor(WidgetEditEvent event) {
        Widget widget = event.getSource().getWidget();
        screenBuilders.editor(Widget.class, this)
                .editEntity(widget)
                .withOpenMode(OpenMode.DIALOG)
                .build()
                .show()
                .addAfterCloseListener(e -> {
                    StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                    if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                        WidgetLayout widgetLayout = getDashboardModel().getWidgetLayout(widget.getId());
                        widgetLayout.setWidget(((WidgetEdit) e.getSource()).getEditedEntity());
                        uiEventPublisher.publishEvent(new DashboardRefreshEvent(getDashboardModel().getVisualModel(), widget.getId()));
                    }
                });
    }


    @Override
    protected void validateAdditionalRules(ValidationErrors errors) {
        super.validateAdditionalRules(errors);

        //remove errors from widget fragments
        errors.getAll().removeIf(error -> !"dashboardEditForm1".equals(error.component.getParent().getId()));

        List<Widget> dashboardWidgets = dashboardDc.getItem().getWidgets();
        Map<String, Long> widgetsCount = dashboardWidgets.stream()
                .collect(Collectors.groupingBy(Widget::getWidgetId, Collectors.counting()));
        List<String> nonUniqueIds = widgetsCount.entrySet().stream()
                .filter(es -> es.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(toList());

        if (nonUniqueIds.size() > 0) {
            errors.add(null, messages.formatMessage(PersistentDashboardEdit.class, "uniqueWidgetId",
                    String.join(",", nonUniqueIds)));
        }
    }

    private Map<String, Object> getDisplaySizeMap(ResponsiveArea responsiveArea) {
        Map<String, Object> map = new HashMap<>();
        map.put(XS.name(), responsiveArea.getXs() == null ? null : responsiveArea.getXs());
        map.put(SM.name(), responsiveArea.getSm() == null ? null : responsiveArea.getSm());
        map.put(MD.name(), responsiveArea.getMd() == null ? null : responsiveArea.getMd());
        map.put(LG.name(), responsiveArea.getLg() == null ? null : responsiveArea.getLg());
        return map;
    }
}
