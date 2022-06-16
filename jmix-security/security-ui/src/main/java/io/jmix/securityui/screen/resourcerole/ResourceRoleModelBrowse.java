/*
 * Copyright 2020 Haulmont.
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

package io.jmix.securityui.screen.resourcerole;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import io.jmix.core.*;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RoleSource;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityui.model.BaseRoleModel;
import io.jmix.securityui.model.ResourceRoleModel;
import io.jmix.securityui.model.RoleModelConverter;
import io.jmix.securityui.screen.role.RemoveRoleConsumer;
import io.jmix.securityui.screen.rolefilter.RoleFilter;
import io.jmix.securityui.screen.rolefilter.RoleFilterFragment;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.FileUploadField;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.SingleFileUploadField;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.ui.download.DownloadFormat.JSON;
import static io.jmix.ui.download.DownloadFormat.ZIP;

@UiController("sec_ResourceRoleModel.browse")
@UiDescriptor("resource-role-model-browse.xml")
@LookupComponent("roleModelsTable")
@Route("resourceRoles")
public class ResourceRoleModelBrowse extends StandardLookup<ResourceRoleModel> {

    private static final Logger log = LoggerFactory.getLogger(ResourceRoleModelBrowse.class);

    @Autowired
    private CollectionContainer<ResourceRoleModel> roleModelsDc;

    @Autowired
    private ResourceRoleRepository roleRepository;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private GroupTable<ResourceRoleModel> roleModelsTable;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Autowired
    private Messages messages;

    @Autowired
    private Notifications notifications;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    protected RemoveOperation removeOperation;

    @Autowired
    private RoleFilterFragment filterFragment;

    @Autowired
    private EntityImportExport entityImportExport;

    @Autowired
    private FetchPlans fetchPlans;

    @Autowired
    private EntityImportPlans entityImportPlans;

    @Autowired
    private Downloader downloader;

    @Autowired
    private FileUploadField importField;

    private RoleFilter roleFilter = new RoleFilter();

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadRoles();

        filterFragment.setChangeListener(roleFilter -> {
            this.roleFilter = roleFilter;
            loadRoles();
        });
    }

    @Subscribe("roleModelsTable.refresh")
    public void onRoleModelsTableRefresh(Action.ActionPerformedEvent event) {
        loadRoles();
    }

    @Install(to = "roleModelsTable.remove", subject = "enabledRule")
    private boolean roleModelsTableRemoveEnabledRule() {
        return isDatabaseRoleSelected();
    }

    @Subscribe("roleModelsTable.create")
    public void onRoleModelsTableCreateResourceRole(Action.ActionPerformedEvent event) {
        ResourceRoleModelEdit editor = screenBuilders.editor(roleModelsTable)
                .withScreenClass(ResourceRoleModelEdit.class)
                .newEntity()
                .withInitializer(roleModel -> {
                    roleModel.setSource(RoleSource.DATABASE);
                    roleModel.setScopes(Sets.newHashSet(SecurityScope.UI));
                })
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                        loadRoles();
                    }
                })
                .build();
        editor.setOpenedByCreateAction(true);
        editor.show();
    }


    @Subscribe("roleModelsTable.remove")
    public void onRoleModelsTableRemove(Action.ActionPerformedEvent event) {
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

    @Install(to = "roleModelsTable.source", subject = "valueProvider")
    public Object roleModelsTableSourceValueProvider(ResourceRoleModel roleModel) {
        return messages.getMessage("io.jmix.securityui.model/roleSource." + roleModel.getSource());
    }

    protected void loadRoles() {
        Collection<ResourceRole> roles = roleRepository.getAllRoles();
        List<ResourceRoleModel> roleModels = roles.stream()
                .filter(role -> roleFilter.matches(role))
                .map(roleModelConverter::createResourceRoleModel)
                .sorted(Comparator.comparing(ResourceRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.getMutableItems().clear();
        roleModelsDc.getMutableItems().addAll(roleModels);
    }

    protected boolean isDatabaseRoleSelected() {
        Set<ResourceRoleModel> selected = roleModelsTable.getSelected();
        if (selected.size() == 1) {
            ResourceRoleModel roleModel = selected.iterator().next();
            return RoleSource.DATABASE.equals(roleModel.getSource());
        }
        return false;
    }

    @Subscribe("exportBtn.exportJSON")
    public void onExportBtnExportJSON(Action.ActionPerformedEvent event) {
        export(JSON);
    }

    @Subscribe("exportBtn.exportZIP")
    public void onExportBtnExportZIP(Action.ActionPerformedEvent event) {
        export(ZIP);
    }

    protected void export(DownloadFormat downloadFormat) {
        Collection<ResourceRoleModel> selected = roleModelsTable.getSelected();
        if (selected.isEmpty() && roleModelsTable.getItems() != null) {
            selected = roleModelsTable.getItems().getItems();
        }

        List<Object> dbResourceRoles = selected.stream()
                .filter(resourceRoleModel -> RoleSource.DATABASE.equals(resourceRoleModel.getSource()))
                .peek(resourceRoleModel -> {
                    String databaseId = resourceRoleModel.getCustomProperties().get("databaseId");
                    resourceRoleModel.setId(UUID.fromString(databaseId));
                })
                .collect(Collectors.toList());

        if (dbResourceRoles.isEmpty()) {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withDescription(messages.getMessage(ResourceRoleModelBrowse.class, "nothingToExport"))
                    .show();
            return;
        }

        try {
            byte[] data;
            if (downloadFormat == JSON) {
                data = entityImportExport.exportEntitiesToJSON(dbResourceRoles, buildExportFetchPlan()).getBytes(StandardCharsets.UTF_8);
            } else {
                data = entityImportExport.exportEntitiesToZIP(dbResourceRoles, buildExportFetchPlan());
            }
            downloader.download(data, String.format("ResourceRoles.%s", downloadFormat.getFileExt()), downloadFormat);
        } catch (Exception e) {
            log.warn("Unable to export resource roles", e);
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(ResourceRoleModelBrowse.class, "exportFailed"))
                    .withDescription(e.getMessage())
                    .show();
        }
    }

    protected FetchPlan buildExportFetchPlan() {
        return fetchPlans.builder(ResourceRoleEntity.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("resourcePolicies", FetchPlan.BASE)
                .build();
    }

    @Subscribe("importField")
    public void onImportFieldFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        try {
            byte[] bytes = importField.getValue();
            Collection<Object> importedEntities;
            if (JSON.getFileExt().equals(Files.getFileExtension(event.getFileName()))) {
                importedEntities = entityImportExport.importEntitiesFromJson(new String(bytes, StandardCharsets.UTF_8), createEntityImportPlan());
            } else {
                importedEntities = entityImportExport.importEntitiesFromZIP(bytes, createEntityImportPlan());
            }

            if (importedEntities.size() > 0) {
                loadRoles();
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withDescription(messages.getMessage(ResourceRoleModelBrowse.class, "importSuccessful"))
                        .show();
            }
        } catch (Exception e) {
            log.warn("Unable to import resource roles", e);
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(ResourceRoleModelBrowse.class, "importFailed"))
                    .withDescription(e.getMessage())
                    .show();
        }
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