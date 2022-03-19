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

package io.jmix.dashboardsui.screen.dashboard.editor.palette;

import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.components.grid.TreeGridDragSource;
import com.vaadin.ui.components.grid.TreeGridDropEvent;
import com.vaadin.ui.components.grid.TreeGridDropTarget;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.dashboards.entity.WidgetTemplate;
import io.jmix.dashboards.model.DashboardModel;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.visualmodel.DashboardLayout;
import io.jmix.dashboards.model.visualmodel.RootLayout;
import io.jmix.dashboardsui.DashboardStyleConstants;
import io.jmix.dashboardsui.component.impl.PaletteButton;
import io.jmix.dashboards.converter.JsonConverter;
import io.jmix.dashboardsui.dashboard.event.*;
import io.jmix.dashboardsui.dashboard.tools.factory.ActionsProvider;
import io.jmix.dashboardsui.dashboard.tools.factory.PaletteComponentsFactory;
import io.jmix.dashboardsui.repository.WidgetRepository;
import io.jmix.dashboardsui.screen.dashboard.editor.DashboardLayoutsTree;
import io.jmix.dashboardsui.screen.dashboard.editor.DashboardLayoutHolderComponent;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiEventPublisher;
import io.jmix.ui.WindowParam;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.Tree;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import io.jmix.ui.widget.JmixTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.dashboards.utils.DashboardLayoutUtils.findLayout;
import static io.jmix.dashboards.utils.DashboardLayoutUtils.findParentLayout;

@UiController("dshbrd_Palette.fragment")
@UiDescriptor("palette-fragment.xml")
public class PaletteFragment extends ScreenFragment implements DashboardLayoutHolderComponent {
    public static final String SCREEN_NAME = "dshbrd_Palette.fragment";

    private static Logger log = LoggerFactory.getLogger(PaletteFragment.class);

    @Autowired
    protected BoxLayout ddWidgetBox;
    @Autowired
    protected BoxLayout ddLayoutBox;
    @Autowired
    protected BoxLayout ddWidgetTemplateBox;
    @Autowired
    protected CollectionContainer<WidgetTemplate> widgetTemplatesDc;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected PaletteComponentsFactory factory;
    @Autowired
    protected JsonConverter converter;
    @Autowired
    protected WidgetRepository widgetRepository;
    @Autowired
    protected CollectionContainer<DashboardLayout> dashboardLayoutTreeDc;
    @Autowired
    private CollectionLoader<DashboardLayout> dashboardLayoutTreeDl;
    @Autowired
    protected Tree<DashboardLayout> widgetTree;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @WindowParam
    protected DashboardModel dashboardModel;
    @Autowired
    protected ActionsProvider actionsProvider;
    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    private CollectionLoader<WidgetTemplate> widgetTemplatesDl;

    protected DashboardLayoutsTree tree = new DashboardLayoutsTree();

    @Subscribe
    public void onAttach(AttachEvent e) {
        initWidgetBox();
        initLayoutBox();
        initWidgetTemplateBox();
        initWidgetTreeBox();
    }

    @EventListener
    public void onCreateWidgetTemplate(CreateWidgetTemplateEvent event) {
        Widget widget = event.getSource();
        WidgetTemplate widgetTemplate = metadata.create(WidgetTemplate.class);
        widgetTemplate.setWidgetModel(converter.widgetToJson(widget));
        screenBuilders.editor(WidgetTemplate.class, this)
                .newEntity(widgetTemplate)
                .withContainer(widgetTemplatesDc)
                .withOpenMode(OpenMode.DIALOG)
                .build()
                .show();
    }

    @Install(to = "dashboardLayoutTreeDl", target = Target.DATA_LOADER)
    protected List<DashboardLayout> dashboardLayoutTreeDlLoadDelegate(LoadContext<DashboardLayout> loadContext) {
        return tree.getLayouts();
    }

    protected void initWidgetBox() {
        for (Widget widget : getSketchWidgets()) {
            PaletteButton widgetBtn = factory.createWidgetButton(widget);
            ddWidgetBox.add(widgetBtn);
        }
    }

    protected void initLayoutBox() {
        ddLayoutBox.add(factory.createVerticalLayoutButton());
        ddLayoutBox.add(factory.createHorizontalLayoutButton());
        ddLayoutBox.add(factory.createGridLayoutButton());
        ddLayoutBox.add(factory.createCssLayoutButton());
        ddLayoutBox.add(factory.createResponsiveLayoutButton());
    }

    protected void initWidgetTreeBox() {
        tree.setVisualModel(dashboardModel.getVisualModel());
        dashboardLayoutTreeDl.load();

        dashboardLayoutTreeDc.addItemChangeListener(e -> {
            DashboardLayout dashboardLayout = widgetTree.getSingleSelected();
            if (dashboardLayout != null) {
                uiEventPublisher.publishEvent(new WidgetSelectedEvent(dashboardLayout.getId(), WidgetSelectedEvent.Target.TREE));
                createActions(widgetTree, dashboardLayout);
            }
        });
        widgetTree.expandTree();
        widgetTree.setStyleName(DashboardStyleConstants.DASHBOARD_TREE);

        TreeGridDragSource<DashboardLayout> dropSource =
                new TreeGridDragSource<DashboardLayout>(widgetTree.unwrap(JmixTree.class).getCompositionRoot());
        dropSource.setEffectAllowed(EffectAllowed.MOVE);
        dropSource.addGridDragStartListener(e -> {
            DashboardLayout containerLayout = e.getDraggedItems().get(0);
            dropSource.setDragData(containerLayout);
        });
        dropSource.addGridDragEndListener(e -> dropSource.setDragData(null));
        TreeGridDropTarget<DashboardLayout> dropTarget =
                new TreeGridDropTarget<DashboardLayout>(widgetTree.unwrap(JmixTree.class).getCompositionRoot(), DropMode.ON_TOP_OR_BETWEEN);
        dropTarget.addTreeGridDropListener(this::onTreeGridDrop);

    }

    private void onTreeGridDrop(TreeGridDropEvent<DashboardLayout> e) {
        if (e.getDropTargetRow().isPresent() && e.getDragData().isPresent()) {
            DashboardLayout target = e.getDropTargetRow().get();
            DashboardLayout source = (DashboardLayout) e.getDragData().get();
            WidgetDropLocation dropLocation = getDropLocation(e.getDropLocation());
            if (source.getId() == null) {
                uiEventPublisher.publishEvent(new WidgetAddedEvent(source, target.getId(), dropLocation));
            } else {
                uiEventPublisher.publishEvent(new WidgetMovedEvent(source, target.getId(), dropLocation));
            }
        }
    }

    private WidgetDropLocation getDropLocation(DropLocation dropLocation) {
        switch (dropLocation) {
            case ABOVE:
                return WidgetDropLocation.TOP;
            case ON_TOP:
                return WidgetDropLocation.MIDDLE;
            case BELOW:
                return WidgetDropLocation.BOTTOM;
        }
        return WidgetDropLocation.MIDDLE;
    }

    private void createActions(Tree<DashboardLayout> widgetTree, DashboardLayout layout) {
        widgetTree.removeAllActions();
        List<Action> actions = actionsProvider.getLayoutActions(layout);
        for (Action action : actions) {
            widgetTree.addAction(action);
        }
    }

    protected void initWidgetTemplateBox() {
        widgetTemplatesDc.addCollectionChangeListener(e -> updateWidgetTemplates());
        widgetTemplatesDl.load();
    }

    protected void updateWidgetTemplates() {
        ddWidgetTemplateBox.removeAll();
        Collection<WidgetTemplate> templates = widgetTemplatesDc.getItems().stream()
                .sorted(Comparator.comparing(WidgetTemplate::getName))
                .collect(Collectors.toList());
        for (WidgetTemplate wt : templates) {
            try {
                PaletteButton widgetBtn = factory.createWidgetTemplateButton(wt);
                ddWidgetTemplateBox.add(widgetBtn);
            } catch (Exception e) {
                log.error(String.format("Unable to create widget template %s <%s>. Cause: %s", wt.getName(), wt.getId(), e.getMessage()), e);
            }
        }
    }

    protected List<? extends Widget> getSketchWidgets() {
        return widgetRepository.getWidgetTypesInfo()
                .stream()
                .map(type -> {
                    Widget widget = metadata.create(Widget.class);
                    widget.setName(type.getName());
                    widget.setFragmentId(type.getFragmentId());
                    widget.setDashboard(dashboardModel);
                    return widget;
                })
                .sorted(Comparator.comparing(Widget::getName))
                .collect(Collectors.toList());
    }

    @EventListener
    public void onWidgetSelectedEvent(WidgetSelectedEvent event) {
        if (WidgetSelectedEvent.Target.TREE != event.getTarget()) {
            UUID layoutUuid = event.getSource();
            DashboardLayout layout = tree.getVisualModel().findLayout(layoutUuid);
            widgetTree.setSelected(layout);
            widgetTree.expand(layout);
        }
    }

    @EventListener
    public void onLayoutRefreshedEvent(DashboardRefreshEvent event) {
        DashboardLayout selected = event.getSelectId() != null ?
                findLayout(tree.getVisualModel(), event.getSelectId())
                : widgetTree.getSingleSelected();
        DashboardLayout parent = selected != null ?
                findParentLayout(tree.getVisualModel(), selected) : null;
        tree.setVisualModel(event.getSource());
        dashboardModel.setVisualModel((RootLayout) event.getSource());
        dashboardLayoutTreeDl.load();
        if (selected != null) {
            DashboardLayout dashboardLayout = dashboardLayoutTreeDc.getItemOrNull(selected.getId());
            if (dashboardLayout == null) {
                selected = parent;
                if (selected != null) {
                    uiEventPublisher.publishEvent(new WidgetSelectedEvent(selected.getId(), WidgetSelectedEvent.Target.TREE));
                }
            }
        }
        widgetTree.repaint();
    }

    public CollectionContainer<DashboardLayout> getDashboardLayoutTreeDc() {
        return dashboardLayoutTreeDc;
    }
}
