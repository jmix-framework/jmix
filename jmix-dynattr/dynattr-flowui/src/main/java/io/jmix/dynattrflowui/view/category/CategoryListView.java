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

package io.jmix.dynattrflowui.view.category;

import com.google.common.io.Files;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.data.entity.ReferenceToEntity;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static io.jmix.flowui.download.DownloadFormat.JSON;
import static io.jmix.flowui.download.DownloadFormat.ZIP;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Route(value = "dynat/category", layout = DefaultMainViewParent.class)
@ViewController("dynat_CategoryView.list")
@ViewDescriptor("category-list-view.xml")
@PrimaryListView(Category.class)
@LookupComponent("categoriesGrid")
@DialogMode(width = "47.5em")
public class CategoryListView extends StandardListView<Category> {

    private static final Logger log = LoggerFactory.getLogger(CategoryListView.class);

    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DynAttrMetadata dynAttrMetadata;
    @Autowired
    protected EntityImportExport entityImportExport;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected EntityImportPlans entityImportPlans;
    @Autowired
    protected Downloader downloader;
    @Autowired
    private AccessManager accessManager;


    @ViewComponent
    protected DataGrid<Category> categoriesGrid;
    @ViewComponent
    protected CollectionContainer<CategoryAttribute> attributesDc;
    @ViewComponent
    protected InstanceContainer<Category> categoryDc;
    @ViewComponent
    protected InstanceLoader<Category> categoryDl;
    @ViewComponent
    private CollectionLoader<Category> categoriesDl;
    @ViewComponent
    protected FileUploadField importField;
    @ViewComponent
    private Button applyChangesBtn;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        Objects.requireNonNull(attributesDc.getSorter()).sort(Sort.by(Sort.Direction.ASC, "orderNo"));
        setupFieldsLock();
    }

    @Subscribe("categoriesGrid.applyChanges")
    protected void onCategoriesGridApplyChanges(ActionPerformedEvent event) {
        dynAttrMetadata.reload();
        notifications.create(messages.getMessage(CategoryListView.class, "notification.changesApplied"))
                .withType(Notifications.Type.SUCCESS)
                .show();
    }

    @Install(to = "categoriesGrid.edit", subject = "afterSaveHandler")
    private void categoriesGridEditAfterCommitHandler(Category category) {
        categoriesDl.load();
    }

    @Install(to = "categoriesGrid.create", subject = "afterSaveHandler")
    private void categoriesGridCreateAfterCommitHandler(Category category) {
        categoriesDl.load();
    }

    @Subscribe(id = "categoriesDc", target = Target.DATA_CONTAINER)
    protected void onCategoriesDcItemChange(InstanceContainer.ItemChangeEvent<Category> event) {
        Category category = event.getItem();
        if (category != null) {
            categoryDl.setEntityId(category.getId());
            categoryDl.load();
        } else {
            categoryDc.setItem(null);
        }
    }

    protected void setupFieldsLock() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            applyChangesBtn.setEnabled(false);
        }
    }

    @Subscribe("categoriesGrid.exportJSON")
    public void onExportBtnExportJSON(ActionPerformedEvent event) {
        export(JSON);
    }

    @Subscribe("categoriesGrid.exportZIP")
    public void onExportBtnExportZIP(ActionPerformedEvent event) {
        export(ZIP);
    }

    protected void export(DownloadFormat downloadFormat) {
        Collection<Category> selected = categoriesGrid.getSelectedItems();
        if (selected.isEmpty() && categoriesGrid.getItems() != null) {
            selected = categoriesGrid.getItems().getItems();
        }

        if (selected.isEmpty()) {
            notifications.create(messages.getMessage(CategoryListView.class, "nothingToExport"))
                    .withType(Notifications.Type.DEFAULT)
                    .show();
            return;
        }

        try {
            byte[] data;
            if (downloadFormat == JSON) {
                data = entityImportExport.exportEntitiesToJSON(new ArrayList<>(selected), buildExportFetchPlan()).getBytes(StandardCharsets.UTF_8);
            } else {
                data = entityImportExport.exportEntitiesToZIP(new ArrayList<>(selected), buildExportFetchPlan());
            }
            downloader.download(data, String.format("Categories.%s", downloadFormat.getFileExt()), downloadFormat);
        } catch (Exception e) {
            log.warn("Unable to export categories", e);
            notifications.create(messages.getMessage(CategoryListView.class, "exportFailed"), e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected FetchPlan buildExportFetchPlan() {
        return fetchPlans.builder(Category.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("categoryAttrs", FetchPlan.BASE)
                .build();
    }

    @Subscribe("importField")
    public void onImportFieldFileUploadSucceed(FileUploadSucceededEvent<FileUploadField> event) {
        try {
            byte[] bytes = importField.getValue();
            if (bytes == null) {
                throw new IllegalStateException("File data is empty");
            }
            Collection<Object> importedEntities;
            if (JSON.getFileExt().equals(Files.getFileExtension(event.getFileName()))) {
                importedEntities = entityImportExport.importEntitiesFromJson(new String(bytes, StandardCharsets.UTF_8), createEntityImportPlan());
            } else {
                importedEntities = entityImportExport.importEntitiesFromZIP(bytes, createEntityImportPlan());
            }

            if (!importedEntities.isEmpty()) {
                categoriesDl.load();
                notifications.create(messages.getMessage(CategoryListView.class, "importSuccessful"))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
            }
        } catch (Exception e) {
            log.warn("Unable to import categories", e);
            notifications.create(messages.getMessage(CategoryListView.class, "importFailed"), e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected EntityImportPlan createEntityImportPlan() {
        return entityImportPlans.builder(Category.class)
                .addLocalProperties()
                .addProperty(new EntityImportPlanProperty(
                        "categoryAttrs",
                        entityImportPlans.builder(CategoryAttribute.class)
                                .addLocalProperties()
                                .addEmbeddedProperty(
                                        "defaultEntity",
                                        entityImportPlans.builder(ReferenceToEntity.class)
                                                .addLocalProperties().
                                                build()
                                )
                                .build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                )
                .build();
    }
}
