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

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationComponent;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "dynat/category/:id", layout = DefaultMainViewParent.class)
@ViewController("dynat_CategoryView.detail")
@ViewDescriptor("category-detail-view.xml")
@PrimaryDetailView(Category.class)
@EditedEntityContainer("categoryDc")
@DialogMode(width = "50em")
public class CategoryDetailView extends StandardDetailView<Category> {

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected MsgBundleTools msgBundleTools;

    @ViewComponent
    protected InstanceContainer<Category> categoryDc;
    @ViewComponent
    protected JmixComboBox<MetaClass> entityTypeField;
    @ViewComponent
    protected TypedTextField<String> nameField;
    @ViewComponent
    protected JmixTabSheet tabSheet;
    @ViewComponent
    protected VerticalLayout localizationTabContainer;

    protected AttributeLocalizationComponent localizationComponent;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initEntityTypeField();
        initLocalizationTab();
        setupFieldsLock();
    }

    protected void setupFieldsLock() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            entityTypeField.setEnabled(false);
        }
    }

    @Subscribe("entityTypeField")
    protected void onEntityTypeFieldValueChange(AbstractField.ComponentValueChangeEvent<ComboBox<MetaClass>, MetaClass> event) {
        if (event.getValue() != null) {
            getEditedEntity().setEntityType(event.getValue().getName());

            if (nameField.getTypedValue() == null) {
                getEditedEntity().setName(generateCategoryNameByEntityType());
            }
        }
    }

    protected String generateCategoryNameByEntityType() {
        String entityTypeCaption = messageTools.getEntityCaption(entityTypeField.getValue());
        String categoryEntityCaption = messageTools.getEntityCaption(categoryDc.getEntityMetaClass());
        return StringUtils.capitalize(entityTypeCaption) + " " + StringUtils.uncapitalize(categoryEntityCaption);
    }

    @Subscribe("isDefaultField")
    protected void onIsDefaultFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (Boolean.TRUE.equals(event.getValue())) {
            FetchPlan fetchPlan = fetchPlans.builder(Category.class)
                    .add("isDefault")
                    .build();
            LoadContext<Category> loadContext = new LoadContext<Category>(metadata.getClass(Category.class))
                    .setFetchPlan(fetchPlan);
            Category category = getEditedEntity();
            loadContext.setQueryString("select c from dynat_Category c where c.entityType = :entityType and not c.id = :id")
                    .setParameter("entityType", category.getEntityType())
                    .setParameter("id", category.getId());
            List<Category> foundCategories = dataManager.loadList(loadContext);
            foundCategories.forEach(item -> item.setIsDefault(false));

            dataManager.save(new SaveContext().saving(foundCategories));
        }
    }

    protected void initEntityTypeField() {
        Map<MetaClass, String> options = new HashMap<>(); //the map sorts metaclasses by the string key
        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.hasUuid(metaClass)) {
                continue;
            }
            options.put(metaClass, messageTools.getDetailedEntityCaption(metaClass));
        }
        entityTypeField.setItemLabelGenerator(options::get);
        entityTypeField.setItems(options.keySet().stream()
                .sorted(Comparator.comparing(MetadataObject::getName))
                .toList());

        if (getEditedEntity().getEntityType() != null) {
            entityTypeField.setValue(extendedEntities.getEffectiveMetaClass(getEditedEntity().getEntityType()));
        }
    }

    protected void initLocalizationTab() {
        if (coreProperties.getAvailableLocales().size() > 1) {
            Tab localizationTab = tabSheet.getTabAt(1); // 0 == "localizationTab
            localizationTab.setVisible(true);

            CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
            accessManager.applyRegisteredConstraints(crudEntityContext);

            localizationComponent = new AttributeLocalizationComponent(coreProperties,
                    msgBundleTools,
                    metadata,
                    messages,
                    messageTools,
                    uiComponents,
                    dataComponents,
                    getViewData().getDataContext());

            localizationComponent.setNameMsgBundle(getEditedEntity().getLocaleNames());
            localizationComponent.removeDescriptionColumn();
            localizationComponent.setEnabled(crudEntityContext.isUpdatePermitted());

            localizationTabContainer.add(localizationComponent);
            localizationTabContainer.expand(localizationComponent);
        }
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreSaveEvent event) {
        if (localizationComponent != null) {
            getEditedEntity().setLocaleNames(localizationComponent.getNameMsgBundle());
        }
    }

    public Category getCategory() {
        return getEditedEntity();
    }
}
