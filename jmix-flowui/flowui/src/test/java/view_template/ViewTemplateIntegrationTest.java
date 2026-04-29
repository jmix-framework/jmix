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

package view_template;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Views;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.flowui.view.template.impl.TemplateDetailView;
import io.jmix.flowui.view.template.impl.TemplateListView;
import io.jmix.flowui.view.template.impl.ViewTemplateDescriptorRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.FlowuiTestConfiguration;
import test_support.entity.viewtemplate.ViewTemplateBindingsEntity;
import test_support.entity.viewtemplate.ViewTemplateFilteringEntity;
import test_support.entity.viewtemplate.ViewTemplateParamsEntity;
import test_support.entity.viewtemplate.ViewTemplateTestEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for template-generated list and detail views.
 */
@UiTest(viewBasePackages = "test_support.entity.viewtemplate.generated_view")
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ViewTemplateIntegrationTest {

    protected static final String LIST_VIEW_ID = "test_ViewTemplateEntity.list";
    protected static final String DETAIL_VIEW_ID = "test_ViewTemplateEntity.edit";
    protected static final String PARAMS_VIEW_ID = "test_ViewTemplateParamsEntity.browse";
    protected static final String FILTERED_LIST_VIEW_ID = "test_ViewTemplateFilteringEntity.list";
    protected static final String FILTERED_DETAIL_VIEW_ID = "test_ViewTemplateFilteringEntity.edit";
    protected static final String BINDINGS_LIST_VIEW_ID = "test_ViewTemplateBindingsEntity.list";
    protected static final String BINDINGS_DETAIL_VIEW_ID = "test_ViewTemplateBindingsEntity.detail";
    protected static final String LIST_VIEW_ROUTE = "templates/view-template/list";
    protected static final String DETAIL_VIEW_BASE_ROUTE = "templates/view-template/detail";
    protected static final String DETAIL_VIEW_ROUTE = DETAIL_VIEW_BASE_ROUTE + "/:id";
    protected static final String GENERATED_VIEW_PACKAGE = "test_support.entity.viewtemplate.generated_view";
    protected static final String CUSTOM_LOOKUP_COMPONENT_ID = "customersGrid";
    protected static final String CUSTOM_EDITED_ENTITY_DC_ID = "customerDc";

    @Autowired
    Metadata metadata;

    @Autowired
    ViewRegistry viewRegistry;

    @Autowired
    ViewTemplateDescriptorRegistry descriptorRegistry;

    @Autowired
    MenuConfig menuConfig;

    @Autowired
    Views views;

    @Autowired
    ViewNavigationSupport navigationSupport;

    @Autowired
    ViewNavigators viewNavigators;

    @Autowired
    DataManager dataManager;

    @Test
    void testTemplateViewsRegisteredInViewRegistry() {
        ViewInfo listViewInfo = viewRegistry.getViewInfo(LIST_VIEW_ID);
        ViewInfo detailViewInfo = viewRegistry.getViewInfo(DETAIL_VIEW_ID);

        assertGeneratedControllerClass(listViewInfo, TemplateListView.class, LIST_VIEW_ID);
        assertGeneratedControllerClass(detailViewInfo, TemplateDetailView.class, DETAIL_VIEW_ID);

        MetaClass metaClass = metadata.getClass(ViewTemplateTestEntity.class);
        assertEquals(LIST_VIEW_ID, viewRegistry.getListViewInfo(metaClass).getId());
        assertEquals(DETAIL_VIEW_ID, viewRegistry.getDetailViewInfo(metaClass).getId());
        assertEquals(FILTERED_LIST_VIEW_ID, viewRegistry.getListViewInfo(ViewTemplateFilteringEntity.class).getId());
        assertEquals(FILTERED_DETAIL_VIEW_ID, viewRegistry.getDetailViewInfo(ViewTemplateFilteringEntity.class).getId());
    }

    @Test
    void testTemplateViewsHaveGeneratedRoutes() {
        ViewInfo listViewInfo = viewRegistry.getViewInfo(LIST_VIEW_ID);
        ViewInfo detailViewInfo = viewRegistry.getViewInfo(DETAIL_VIEW_ID);

        Route listRoute = listViewInfo.getControllerClass().getAnnotation(Route.class);
        assertNotNull(listRoute);
        assertEquals(LIST_VIEW_ROUTE, listRoute.value());

        Route detailRoute = detailViewInfo.getControllerClass().getAnnotation(Route.class);
        assertNotNull(detailRoute);
        assertEquals(DETAIL_VIEW_ROUTE, detailRoute.value());

        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        assertEquals(LIST_VIEW_ROUTE, routeConfiguration.getUrl(listViewInfo.getControllerClass()));
        assertEquals(
                DETAIL_VIEW_BASE_ROUTE + "/" + StandardDetailView.NEW_ENTITY_ID,
                routeConfiguration.getUrl(
                        detailViewInfo.getControllerClass(),
                        new RouteParameters(StandardDetailView.DEFAULT_ROUTE_PARAM, StandardDetailView.NEW_ENTITY_ID))
        );
    }

    @Test
    void testTemplateViewsRegisteredInMenuConfig() {
        MenuItem parentItem = findTemplateViewsRootItem().orElseThrow();
        assertTrue(parentItem.isMenu());

        MenuItem listItem = findChildItem(parentItem, LIST_VIEW_ID).orElseThrow();
        assertEquals(LIST_VIEW_ID, listItem.getView());
        assertEquals("test_ViewTemplateEntity list", listItem.getTitle());
        assertTrue(listItem.getUrlQueryParameters().isEmpty());

        MenuItem detailItem = findChildItem(parentItem, DETAIL_VIEW_ID).orElseThrow();
        assertEquals(DETAIL_VIEW_ID, detailItem.getView());
        assertEquals("Template entity editor", detailItem.getTitle());
        assertTrue(detailItem.getUrlQueryParameters().isEmpty());
    }

    @Test
    void testDefaultTemplateViewsCanBeCreatedById() {
        View<?> listView = views.create(LIST_VIEW_ID);
        assertInstanceOf(TemplateListView.class, listView);
        assertEquals(LIST_VIEW_ID, listView.getId().orElseThrow());
        assertEquals("test_ViewTemplateEntity list", listView.getPageTitle());
        assertComponentPresent(listView, "dataGrid");

        View<?> detailView = views.create(DETAIL_VIEW_ID);
        assertInstanceOf(TemplateDetailView.class, detailView);
        assertEquals(DETAIL_VIEW_ID, detailView.getId().orElseThrow());
        assertEquals("Template entity editor", detailView.getPageTitle());
        assertComponentPresent(detailView, "nameField");
        assertComponentPresent(detailView, "activeField");
    }

    @Test
    void testTemplateParamsPassedToFreemarker() {
        View<?> view = views.create(PARAMS_VIEW_ID);

        assertEquals(PARAMS_VIEW_ID, view.getId().orElseThrow());
        assertEquals("Params entity from params", view.getPageTitle());

        Component marker = UiComponentUtils.getComponent(view, "marker");
        assertInstanceOf(Span.class, marker);
        assertEquals("params marker", ((Span) marker).getText());

        assertEquals(PARAMS_VIEW_ID, viewRegistry.getListViewInfo(ViewTemplateParamsEntity.class).getId());
    }

    @Test
    void testDefaultTemplatesExcludeTechnicalProperties() {
        String listDescriptor = getDescriptor(LIST_VIEW_ID);
        String detailDescriptor = getDescriptor(DETAIL_VIEW_ID);

        assertTrue(listDescriptor.contains("<property name=\"name\""));
        assertTrue(listDescriptor.contains("<property name=\"active\""));
        assertTrue(listDescriptor.contains("<column property=\"name\"/>"));
        assertTrue(listDescriptor.contains("<column property=\"active\"/>"));
        assertTrue(listDescriptor.contains("<properties include=\".*\"/>"));
        assertFalse(listDescriptor.contains("name=\"id\""));
        assertFalse(listDescriptor.contains("name=\"version\""));
        assertFalse(listDescriptor.contains("name=\"createdBy\""));
        assertFalse(listDescriptor.contains("name=\"createTs\""));
        assertFalse(listDescriptor.contains("name=\"updatedBy\""));
        assertFalse(listDescriptor.contains("name=\"deleteTs\""));

        assertTrue(detailDescriptor.contains("id=\"nameField\""));
        assertTrue(detailDescriptor.contains("id=\"activeField\""));
        assertFalse(detailDescriptor.contains("id=\"idField\""));
        assertFalse(detailDescriptor.contains("id=\"createdByField\""));
        assertFalse(detailDescriptor.contains("id=\"deleteTsField\""));
    }

    @Test
    void testDefaultTemplatesApplyIncludeAndExcludeParameters() {
        String listDescriptor = getDescriptor(FILTERED_LIST_VIEW_ID);
        String detailDescriptor = getDescriptor(FILTERED_DETAIL_VIEW_ID);

        assertTrue(listDescriptor.contains("<property name=\"createdBy\""));
        assertTrue(listDescriptor.contains("<column property=\"createdBy\"/>"));
        assertTrue(listDescriptor.contains("<property name=\"customer\" fetchPlan=\"_base\"/>"));
        assertTrue(listDescriptor.contains("<properties include=\".*\"/>"));
        assertFalse(listDescriptor.contains("<property name=\"active\""));
        assertFalse(listDescriptor.contains("<column property=\"active\"/>"));
        assertFalse(listDescriptor.contains("name=\"secretToken\""));
        assertFalse(listDescriptor.contains("name=\"systemValue\""));
        assertFalse(listDescriptor.contains("name=\"address\""));
        assertFalse(listDescriptor.contains("name=\"tags\""));

        assertTrue(detailDescriptor.contains("id=\"createdByField\""));
        assertTrue(detailDescriptor.contains("property=\"customer\""));
        assertFalse(detailDescriptor.contains("id=\"activeField\""));
        assertFalse(detailDescriptor.contains("id=\"secretTokenField\""));
        assertFalse(detailDescriptor.contains("id=\"systemValueField\""));
        assertFalse(detailDescriptor.contains("id=\"addressField\""));
        assertFalse(detailDescriptor.contains("id=\"tagsField\""));
    }

    @Test
    void testStockTemplatesUseLiteralBuiltInTemplateIds() {
        String listDescriptor = getDescriptor(LIST_VIEW_ID);
        String detailDescriptor = getDescriptor(DETAIL_VIEW_ID);

        assertTrue(listDescriptor.contains("focusComponent=\"dataGrid\""));
        assertTrue(listDescriptor.contains("<dataGrid id=\"dataGrid\""));
        assertTrue(listDescriptor.contains("action=\"dataGrid.createAction\""));
        assertTrue(listDescriptor.contains("action=\"dataGrid.editAction\""));
        assertTrue(listDescriptor.contains("action=\"dataGrid.removeAction\""));

        assertTrue(detailDescriptor.contains("<instance id=\"entityDc\""));
        assertTrue(detailDescriptor.contains("<formLayout id=\"form\" dataContainer=\"entityDc\""));
    }

    @Test
    void testCustomTemplatesCanUseExplicitTemplateIds() {
        String listDescriptor = getDescriptor(BINDINGS_LIST_VIEW_ID);
        String detailDescriptor = getDescriptor(BINDINGS_DETAIL_VIEW_ID);

        assertTrue(listDescriptor.contains("focusComponent=\"" + CUSTOM_LOOKUP_COMPONENT_ID + "\""));
        assertTrue(listDescriptor.contains("<collection id=\"entityDc\""));
        assertTrue(listDescriptor.contains("<loader id=\"entityDl\""));
        assertTrue(listDescriptor.contains("<genericFilter id=\"genericFilter\""));
        assertTrue(listDescriptor.contains("<simplePagination id=\"pagination\""));
        assertTrue(listDescriptor.contains("<dataGrid id=\"" + CUSTOM_LOOKUP_COMPONENT_ID + "\""));
        assertTrue(listDescriptor.contains("<hbox id=\"lookupActions\""));
        assertTrue(listDescriptor.contains("<action id=\"selectAction\" type=\"lookup_select\"/>"));
        assertTrue(listDescriptor.contains("<action id=\"discardAction\" type=\"lookup_discard\"/>"));
        assertFalse(listDescriptor.contains("<dataGrid id=\"dataGrid\""));

        assertTrue(detailDescriptor.contains("<instance id=\"" + CUSTOM_EDITED_ENTITY_DC_ID + "\""));
        assertTrue(detailDescriptor.contains("<formLayout id=\"form\" dataContainer=\"" + CUSTOM_EDITED_ENTITY_DC_ID + "\""));
        assertFalse(detailDescriptor.contains("<instance id=\"entityDc\""));
    }

    @Test
    void testTemplateListViewUsesAnnotationLookupComponentId() {
        View<?> view = views.create(BINDINGS_LIST_VIEW_ID);
        TemplateListView listView = (TemplateListView) view;

        assertComponentPresent(view, CUSTOM_LOOKUP_COMPONENT_ID);
        assertComponentPresent(view, "genericFilter");
        assertComponentPresent(view, "lookupActions");
        assertTrue(UiComponentUtils.findComponent(view, "dataGrid").isEmpty());

        assertEquals(CUSTOM_LOOKUP_COMPONENT_ID,
                ((Component) listView.getLookupComponent()).getId().orElseThrow());

        CollectionContainer<ViewTemplateBindingsEntity> container =
                ViewControllerUtils.getViewData(view).getContainer("entityDc");
        ViewTemplateBindingsEntity entity = dataManager.create(ViewTemplateBindingsEntity.class);
        entity.setName("Customer");
        container.setItems(List.of(entity));

        @SuppressWarnings("unchecked")
        DataGrid<ViewTemplateBindingsEntity> dataGrid =
                (DataGrid<ViewTemplateBindingsEntity>) UiComponentUtils.getComponent(view, CUSTOM_LOOKUP_COMPONENT_ID);
        dataGrid.select(entity);

        AtomicReference<Collection<Object>> selectedItems = new AtomicReference<>();
        listView.setSelectionHandler(selectedItems::set);

        Component lookupActionsComponent = UiComponentUtils.getComponent(view, "lookupActions");
        assertTrue(lookupActionsComponent.isVisible());

        assertTrue(listView.getLookupComponent().getSelectedItems().contains(entity));
        ReflectionTestUtils.invokeMethod(listView, "doSelect", List.of(entity));

        assertNotNull(selectedItems.get());
        assertEquals(1, selectedItems.get().size());
        assertTrue(selectedItems.get().contains(entity));
    }

    @Test
    void testTemplateDetailViewUsesAnnotationEditedEntityContainerId() {
        View<?> view = views.create(BINDINGS_DETAIL_VIEW_ID);
        TemplateDetailView detailView = (TemplateDetailView) view;
        var viewData = ViewControllerUtils.getViewData(view);

        InstanceContainer<ViewTemplateBindingsEntity> editedEntityContainer =
                ReflectionTestUtils.invokeMethod(detailView, "getEditedEntityContainer");
        assertSame(viewData.getContainer(CUSTOM_EDITED_ENTITY_DC_ID), editedEntityContainer);
    }

    @Test
    void testTemplateViewCanNavigateById() {
        ViewInfo listViewInfo = viewRegistry.getViewInfo(LIST_VIEW_ID);

        navigationSupport.navigate(LIST_VIEW_ID);

        View<?> currentView = UiTestUtils.getCurrentView();
        assertEquals(listViewInfo.getControllerClass(), currentView.getClass());
        assertEquals(LIST_VIEW_ID, currentView.getId().orElseThrow());
    }

    @Test
    void testTemplateDetailViewCanBeOpenedByNavigation() {
        ViewInfo detailViewInfo = viewRegistry.getViewInfo(DETAIL_VIEW_ID);

        navigationSupport.navigate(LIST_VIEW_ID);
        View<?> listView = UiTestUtils.getCurrentView();

        viewNavigators.detailView(listView, ViewTemplateTestEntity.class)
                .newEntity()
                .navigate();

        View<?> currentView = UiTestUtils.getCurrentView();
        assertEquals(detailViewInfo.getControllerClass(), currentView.getClass());
        assertEquals(DETAIL_VIEW_ID, currentView.getId().orElseThrow());
    }

    @Test
    void testTemplateDetailViewCanEditEntityByNavigation() {
        ViewTemplateTestEntity entity = dataManager.create(ViewTemplateTestEntity.class);
        entity = dataManager.save(entity);
        ViewInfo detailViewInfo = viewRegistry.getViewInfo(DETAIL_VIEW_ID);

        navigationSupport.navigate(LIST_VIEW_ID);
        View<?> listView = UiTestUtils.getCurrentView();

        viewNavigators.detailView(listView, ViewTemplateTestEntity.class)
                .editEntity(entity)
                .navigate();

        View<?> currentView = UiTestUtils.getCurrentView();
        assertEquals(detailViewInfo.getControllerClass(), currentView.getClass());
        assertEquals(DETAIL_VIEW_ID, currentView.getId().orElseThrow());
    }

    protected Optional<MenuItem> findTemplateViewsRootItem() {
        return menuConfig.getRootItems().stream()
                .filter(item -> "templateViews".equals(item.getId()))
                .findFirst();
    }

    protected Optional<MenuItem> findChildItem(MenuItem parentItem, String id) {
        return parentItem.getChildren().stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst();
    }

    protected void assertComponentPresent(View<?> view, String id) {
        assertTrue(UiComponentUtils.findComponent(view, id).isPresent());
    }

    protected void assertGeneratedControllerClass(ViewInfo viewInfo,
                                                  Class<?> baseClass,
                                                  String viewId) {
        Class<?> controllerClass = viewInfo.getControllerClass();

        assertTrue(baseClass.isAssignableFrom(controllerClass));
        assertNotEquals(baseClass, controllerClass);
        assertEquals(GENERATED_VIEW_PACKAGE, controllerClass.getPackageName());
        assertEquals(viewId, controllerClass.getAnnotation(ViewController.class).id());
        assertTrue(viewInfo.getTemplatePath().orElseThrow()
                .startsWith(ViewTemplateDescriptorRegistry.PATH_PREFIX));
        assertEquals(viewInfo.getTemplatePath().orElseThrow(), controllerClass.getAnnotation(ViewDescriptor.class).path());
    }

    protected String getDescriptor(String viewId) {
        String descriptorPath = viewRegistry.getViewInfo(viewId).getTemplatePath().orElseThrow();
        return descriptorRegistry.getDescriptor(descriptorPath).orElseThrow();
    }
}
