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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RoleModelConverter;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RolePersistence;
import io.jmix.securityflowui.component.rolefilter.RoleFilter;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.flowui.download.DownloadFormat.JSON;
import static io.jmix.flowui.download.DownloadFormat.ZIP;
import static io.jmix.security.model.RoleSourceType.DATABASE;

@RouteAlias(value = "sec/resourcerolemodels", layout = DefaultMainViewParent.class)
@Route(value = "sec/resource-role-models", layout = DefaultMainViewParent.class)
@ViewController("sec_ResourceRoleModel.list")
@ViewDescriptor("resource-role-model-list-view.xml")
@LookupComponent("roleModelsTable")
@DialogMode(width = "50em")
public class ResourceRoleModelListView extends StandardListView<ResourceRoleModel> {
    private static final Logger log = LoggerFactory.getLogger(ResourceRoleModelListView.class);

    @ViewComponent
    private DataGrid<ResourceRoleModel> roleModelsTable;
    @ViewComponent
    private DropdownButton exportBtn;
    @ViewComponent
    private FileUploadField importField;
    @ViewComponent
    private CollectionContainer<ResourceRoleModel> roleModelsDc;

    @Autowired
    private Messages messages;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private ResourceRoleRepository roleRepository;
    @Autowired
    private UrlParamSerializer urlParamSerializer;
    @Autowired
    private Downloader downloader;
    @Autowired(required = false)
    private RolePersistence rolePersistence;

    @Subscribe
    public void onInit(InitEvent event) {
        initFilter();
        initActions();
    }

    private void initFilter() {
        RoleFilter filter = uiComponents.create(RoleFilter.class);
        filter.addRoleFilterChangeListener(this::onRoleFilterChange);

        getContent().addComponentAsFirst(filter);
    }

    private void onRoleFilterChange(RoleFilterChangeEvent event) {
        loadRoles(event);
    }

    private void initActions() {
        if (rolePersistence == null) {
            for (Action action : roleModelsTable.getActions()) {
                if (!action.getId().equals("edit")) {
                    action.setVisible(false);
                }
            }
            exportBtn.setVisible(false);
            importField.setVisible(false);
        }
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

    @Install(to = "roleModelsTable.remove", subject = "delegate")
    private void roleModelsTableRemoveDelegate(final Collection<ResourceRoleModel> collection) {
        getRolePersistence().removeRoles(collection);
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
        List<ResourceRoleModel> dbResourceRoles = getExportEntityList();

        if (dbResourceRoles.isEmpty()) {
            notifications.create(messages.getMessage(ResourceRoleModelListView.class, "nothingToExport"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            byte[] data = getRolePersistence().exportResourceRoles(dbResourceRoles, downloadFormat.equals(ZIP));

            downloader.download(data, String.format("ResourceRoles.%s", downloadFormat.getFileExt()), downloadFormat);

        } catch (Exception e) {
            log.warn("Unable to export resource roles", e);
            notifications.create(messages.getMessage(ResourceRoleModelListView.class, "error.exportFailed"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected List<ResourceRoleModel> getExportEntityList() {
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

    @Subscribe("importField")
    public void onImportFieldFileUploadSucceed(FileUploadSucceededEvent<FileUploadField, byte[]> event) {
        try {
            byte[] bytes = importField.getValue();
            Assert.notNull(bytes, "Uploaded file does not contains data");

            List<Object> importedEntities = getRolePersistence().importResourceRoles(
                    bytes,
                    ZIP.getFileExt().equals(Files.getFileExtension(event.getFileName()))
            );

            if (importedEntities.size() > 0) {
                loadRoles(null);
                notifications.create(messages.getMessage(ResourceRoleModelListView.class, "importSuccessful"))
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            }
        } catch (Exception e) {
            log.warn("Unable to import resource roles", e);
            notifications.create(messages.getMessage(ResourceRoleModelListView.class, "error.importFailed"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private RolePersistence getRolePersistence() {
        if (rolePersistence == null) {
            throw new IllegalStateException("RolePersistence is not available");
        }
        return rolePersistence;
    }
}
