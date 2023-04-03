/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityflowui.view.resourcerole;

import com.google.common.io.Files;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.*;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityflowui.component.rolefilter.RoleFilter;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import io.jmix.securityflowui.model.BaseRoleModel;
import io.jmix.securityflowui.model.ResourceRoleModel;
import io.jmix.securityflowui.model.RoleModelConverter;
import io.jmix.securityflowui.util.RemoveRoleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.flowui.download.DownloadFormat.JSON;
import static io.jmix.flowui.download.DownloadFormat.ZIP;
import static io.jmix.securityflowui.model.RoleSource.DATABASE;

@Route(value = "sec/resourcerolemodels", layout = DefaultMainViewParent.class)
@ViewController("sec_ResourceRoleModel.list")
@ViewDescriptor("resource-role-model-list-view.xml")
@LookupComponent("roleModelsTable")
@DialogMode(width = "50em", height = "37.5em")
public class ResourceRoleModelListView extends StandardListView<ResourceRoleModel> {
    private static final Logger log = LoggerFactory.getLogger(ResourceRoleModelListView.class);

    @ViewComponent
    private DataGrid<ResourceRoleModel> roleModelsTable;
    @ViewComponent
    private FileUploadField importField;
    @ViewComponent
    private CollectionContainer<ResourceRoleModel> roleModelsDc;

    @Autowired
    private Messages messages;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;
    @Autowired
    private RemoveOperation removeOperation;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private ResourceRoleRepository roleRepository;
    @Autowired
    private UrlParamSerializer urlParamSerializer;
    @Autowired
    private EntityImportExport entityImportExport;
    @Autowired
    private EntityImportPlans entityImportPlans;
    @Autowired
    private Downloader downloader;
    @Autowired
    private FetchPlans fetchPlans;

    @Subscribe
    public void onInit(InitEvent event) {
        initFilter();
    }

    private void initFilter() {
        RoleFilter filter = uiComponents.create(RoleFilter.class);
        filter.addRoleFilterChangeListener(this::onRoleFilterChange);

        getContent().addComponentAsFirst(filter);
    }

    private void onRoleFilterChange(RoleFilterChangeEvent event) {
        loadRoles(event);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadRoles(null);
    }

    private void loadRoles(@Nullable RoleFilterChangeEvent event) {
        List<ResourceRoleModel> roleModels = roleRepository.getAllRoles().stream()
                .filter(role -> event == null || event.matches(role))
                .map(roleModelConverter::createResourceRoleModel)
                .sorted(Comparator.comparing(ResourceRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.setItems(roleModels);
    }

    @Install(to = "roleModelsTable.create", subject = "routeParametersProvider")
    public RouteParameters roleModelsTableCreateRouteParametersProvider() {
        return new RouteParameters(ResourceRoleModelDetailView.ROUTE_PARAM_NAME, StandardDetailView.NEW_ENTITY_ID);
    }

    @Install(to = "roleModelsTable.edit", subject = "routeParametersProvider")
    public RouteParameters roleModelsTableEditRouteParametersProvider() {
        ResourceRoleModel selectedItem = roleModelsTable.getSingleSelectedItem();
        if (selectedItem != null) {
            String serializedCode = urlParamSerializer.serialize(selectedItem.getCode());
            return new RouteParameters(ResourceRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode);
        }

        return null;
    }

    @Subscribe("roleModelsTable.remove")
    public void onRoleModelsTableRemove(ActionPerformedEvent event) {
        removeOperation.builder(roleModelsTable)
                .withConfirmation(true)
                .beforeActionPerformed(new RemoveRoleConsumer<>(roleRepository, notifications, messages))
                .afterActionPerformed((afterActionConsumer) -> {
                    List<RoleAssignmentEntity> roleAssignmentEntities = dataManager.load(RoleAssignmentEntity.class)
                            .query("e.roleCode IN :codes")
                            .parameter("codes", afterActionConsumer.getItems().stream()
                                    .map(BaseRoleModel::getCode)
                                    .collect(Collectors.toList()))
                            .list();
                    dataManager.remove(roleAssignmentEntities);
                })
                .remove();
    }

    @Install(to = "roleModelsTable.remove", subject = "enabledRule")
    public boolean roleModelsTableRemoveEnabledRule() {
        return isDatabaseRoleSelected();
    }

    private boolean isDatabaseRoleSelected() {
        Set<ResourceRoleModel> selected = roleModelsTable.getSelectedItems();
        if (selected.size() == 1) {
            ResourceRoleModel roleModel = selected.iterator().next();
            return DATABASE.equals(roleModel.getSource());
        }

        return false;
    }

    @Subscribe("roleModelsTable.exportJSON")
    public void onRoleModelsTableExportJSON(ActionPerformedEvent event) {
        export(JSON);
    }

    @Subscribe("roleModelsTable.exportZIP")
    public void onRoleModelsTableExportZIP(ActionPerformedEvent event) {
        export(ZIP);
    }

    protected void export(DownloadFormat downloadFormat) {
        List<Object> dbResourceRoles = getExportEntityList();

        if (dbResourceRoles.isEmpty()) {

            String description = messages.getMessage(ResourceRoleModelListView.class, "nothingToExport");
            notifications.create(description)
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            byte[] data = downloadFormat == JSON ?
                    entityImportExport.exportEntitiesToJSON(dbResourceRoles, buildExportFetchPlan()).getBytes(StandardCharsets.UTF_8) :
                    entityImportExport.exportEntitiesToZIP(dbResourceRoles, buildExportFetchPlan());
            downloader.download(data, String.format("ResourceRoles.%s", downloadFormat.getFileExt()), downloadFormat);

        } catch (Exception e) {
            log.warn("Unable to export resource roles", e);

            String title = messages.getMessage(ResourceRoleModelListView.class, "error.exportFailed");
            String description = e.getMessage();
            notifications.create(title, description)
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected List<Object> getExportEntityList() {
        Collection<ResourceRoleModel> selected = roleModelsTable.getSelectedItems();
        if (selected.isEmpty() && roleModelsTable.getItems() != null) {
            selected = roleModelsDc.getItems();
        }

        return selected.stream()
                .filter(resourceRoleModel -> DATABASE.equals(resourceRoleModel.getSource()))
                .peek(resourceRoleModel -> {
                    String databaseId = resourceRoleModel.getCustomProperties().get("databaseId");
                    resourceRoleModel.setId(UUID.fromString(databaseId));
                })
                .collect(Collectors.toList());
    }

    protected FetchPlan buildExportFetchPlan() {
        return fetchPlans.builder(ResourceRoleEntity.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("resourcePolicies", FetchPlan.BASE)
                .build();
    }

    @Subscribe("importField")
    public void onImportFieldFileUploadSucceed(FileUploadSucceededEvent<FileUploadField> event) {
        try {
            byte[] bytes = importField.getValue();
            Assert.notNull(bytes, "Uploaded file does not contains data");

            List<Object> importedEntities = getImportedEntityList(event.getFileName(), bytes);

            if (importedEntities.size() > 0) {
                loadRoles(null);

                String description = messages.getMessage(ResourceRoleModelListView.class, "importSuccessful");
                notifications.create(description)
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            }
        } catch (Exception e) {
            log.warn("Unable to import resource roles", e);

            String title = messages.getMessage(ResourceRoleModelListView.class, "error.importFailed");
            String description = e.getMessage();
            notifications.create(title, description)
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected List<Object> getImportedEntityList(String fileName, byte[] fileContent) {
        Collection<Object> importedEntities;
        if (JSON.getFileExt().equals(Files.getFileExtension(fileName))) {
            importedEntities = entityImportExport.importEntitiesFromJson(new String(fileContent, StandardCharsets.UTF_8), createEntityImportPlan());
        } else {
            importedEntities = entityImportExport.importEntitiesFromZIP(fileContent, createEntityImportPlan());
        }
        return new ArrayList<>(importedEntities);
    }

    protected EntityImportPlan createEntityImportPlan() {
        return entityImportPlans.builder(ResourceRoleEntity.class)
                .addLocalProperties()
                .addProperty(new EntityImportPlanProperty(
                        "resourcePolicies",
                        entityImportPlans.builder(ResourcePolicyEntity.class).addLocalProperties().build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                )
                .build();
    }
}
