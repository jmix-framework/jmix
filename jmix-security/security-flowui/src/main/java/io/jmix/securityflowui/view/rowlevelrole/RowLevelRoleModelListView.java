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

package io.jmix.securityflowui.view.rowlevelrole;

import com.google.common.io.Files;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.RoleModelConverter;
import io.jmix.security.model.RoleSourceEnum;
import io.jmix.security.model.RowLevelRoleModel;
import io.jmix.security.role.RolePersistence;
import io.jmix.security.role.RowLevelRoleRepository;
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
import static io.jmix.security.model.RoleSourceEnum.DATABASE;

@Route(value = "sec/rowlevelrolemodels", layout = DefaultMainViewParent.class)
@ViewController("sec_RowLevelRoleModel.list")
@ViewDescriptor("row-level-role-model-list-view.xml")
@LookupComponent("roleModelsTable")
@DialogMode(width = "50em")
public class RowLevelRoleModelListView extends StandardListView<RowLevelRoleModel> {

    private static final Logger log = LoggerFactory.getLogger(RowLevelRoleModelListView.class);

    @ViewComponent
    private DataGrid<RowLevelRoleModel> roleModelsTable;
    @ViewComponent
    private CollectionContainer<RowLevelRoleModel> roleModelsDc;
    @ViewComponent
    private FileUploadField importField;

    @Autowired
    private Messages messages;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private RowLevelRoleRepository roleRepository;
    @Autowired
    private UrlParamSerializer urlParamSerializer;
    @Autowired
    private Downloader downloader;
    @Autowired(required = false)
    private RolePersistence rolePersistence;

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
        List<RowLevelRoleModel> roleModels = roleRepository.getAllRoles().stream()
                .filter(role -> event == null || event.matches(role))
                .map(roleModelConverter::createRowLevelRoleModel)
                .sorted(Comparator.comparing(RowLevelRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.setItems(roleModels);
    }

    @Install(to = "roleModelsTable.create", subject = "routeParametersProvider")
    public RouteParameters roleModelsTableCreateRouteParametersProvider() {
        return new RouteParameters(RowLevelRoleModelDetailView.ROUTE_PARAM_NAME, StandardDetailView.NEW_ENTITY_ID);
    }

    @Install(to = "roleModelsTable.edit", subject = "routeParametersProvider")
    public RouteParameters roleModelsTableEditRouteParametersProvider() {
        RowLevelRoleModel selectedItem = roleModelsTable.getSingleSelectedItem();
        if (selectedItem != null) {
            String serializedCode = urlParamSerializer.serialize(selectedItem.getCode());
            return new RouteParameters(RowLevelRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode);
        }

        return null;
    }

    @Install(to = "roleModelsTable.remove", subject = "delegate")
    private void roleModelsTableRemoveDelegate(final Collection<RowLevelRoleModel> collection) {
        getRolePersistence().removeRoles(collection);
    }

    @Install(to = "roleModelsTable.remove", subject = "enabledRule")
    private boolean roleModelsTableRemoveEnabledRule() {
        return isDatabaseRoleSelected();
    }

    private boolean isDatabaseRoleSelected() {
        Set<RowLevelRoleModel> selected = roleModelsTable.getSelectedItems();
        if (selected.size() == 1) {
            RowLevelRoleModel roleModel = selected.iterator().next();
            return RoleSourceEnum.DATABASE.equals(roleModel.getSource());
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
        List<RowLevelRoleModel> dbRowLevelRoles = getExportEntityList();

        if (dbRowLevelRoles.isEmpty()) {
            notifications.create(messages.getMessage(RowLevelRoleModelListView.class, "nothingToExport"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            byte[] data = getRolePersistence().exportRowLevelRoles(dbRowLevelRoles, downloadFormat.equals(ZIP));

            downloader.download(data, String.format("RowLevelRoles.%s", downloadFormat.getFileExt()), downloadFormat);

        } catch (Exception e) {
            log.warn("Unable to export row-level roles", e);
            notifications.create(messages.getMessage(RowLevelRoleModelListView.class, "error.exportFailed"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected List<RowLevelRoleModel> getExportEntityList() {
        Collection<RowLevelRoleModel> selected = roleModelsTable.getSelectedItems();
        if (selected.isEmpty() && roleModelsTable.getItems() != null) {
            selected = roleModelsDc.getItems();
        }

        return selected.stream()
                .filter(rowLevelRoleModel -> DATABASE.equals(rowLevelRoleModel.getSource()))
                .peek(rowLevelRoleModel -> {
                    String databaseId = rowLevelRoleModel.getCustomProperties().get("databaseId");
                    rowLevelRoleModel.setId(UUID.fromString(databaseId));
                })
                .collect(Collectors.toList());
    }

    @Subscribe("importField")
    public void onImportFieldFileUploadSucceed(FileUploadSucceededEvent<FileUploadField> event) {
        try {
            byte[] bytes = importField.getValue();
            Assert.notNull(bytes, "Uploaded file does not contains data");

            List<Object> importedEntities = getRolePersistence().importRowLevelRoles(
                    bytes,
                    ZIP.getFileExt().equals(Files.getFileExtension(event.getFileName()))
            );

            if (importedEntities.size() > 0) {
                loadRoles(null);
                notifications.create(messages.getMessage(RowLevelRoleModelListView.class, "importSuccessful"))
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            }
        } catch (Exception e) {
            log.warn("Unable to import row-level roles", e);
            notifications.create(messages.getMessage(RowLevelRoleModelListView.class, "error.importFailed"))
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