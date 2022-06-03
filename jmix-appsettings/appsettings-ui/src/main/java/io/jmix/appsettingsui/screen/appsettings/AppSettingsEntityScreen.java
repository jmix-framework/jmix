package io.jmix.appsettingsui.screen.appsettings;

import io.jmix.appsettings.AppSettings;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.appsettingsui.screen.appsettings.util.AppSettingsGridLayoutBuilder;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.PersistenceHints;
import io.jmix.ui.Notifications;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.component.*;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.util.OperationResult;
import io.jmix.ui.util.UnknownOperationResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@UiController("appset_appSettingsEntity.screen")
@UiDescriptor("app-settings-entity-screen.xml")
public class AppSettingsEntityScreen extends Screen {

    private static final String SELECT_APP_SETTINGS_ENTITY_QUERY = "select e from %s e where e.id = 1";

    @Autowired
    protected AppSettings appSettings;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected DataComponents dataComponents;

    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected ComboBox<MetaClass> entitiesLookup;

    @Autowired
    protected GroupBoxLayout entityGroupBoxId;
    @Autowired
    protected ScrollBoxLayout fieldsScrollBox;
    @Autowired
    protected HBoxLayout actionsBox;

    private DataContext dataContext;
    private MetaClass currentMetaClass;
    private MetaClass prevMetaClass;
    private boolean isNewEntityModified = false;
    private boolean isEntityChangePrevented = false;
    private Object entityToEdit;

    @Subscribe
    public void onInit(InitEvent event) {
        entitiesLookup.setOptionsMap(getEntitiesLookupFieldOptions());
        entitiesLookup.addValueChangeListener(e -> {
            entityGroupBoxId.setVisible(e.getValue() != null);

            if (isEntityChangePrevented) {
                isEntityChangePrevented = false;
                return;
            }

            prevMetaClass = e.getPrevValue();
            currentMetaClass = e.getValue();

            if (dataContext != null && hasUnsavedChanges()) {
                handleEntityLookupChangeWithUnsavedChanges();
                return;
            }

            initEntityPropertiesGridLayout();
        });
    }

    protected void initEntityPropertiesGridLayout() {
        dataContext = dataComponents.createDataContext();
        getScreenData().setDataContext(dataContext);
        showEntityPropertiesGridLayout();
        dataContext.addChangeListener(changeEvent -> {
            if (entityStates.isNew(changeEvent.getEntity())) {
                this.isNewEntityModified = true;
            }
        });
    }

    protected Map<String, MetaClass> getEntitiesLookupFieldOptions() {
        return metadataTools.getAllJpaEntityMetaClasses().stream()
                .filter(this::isApplicationSettingsEntity)
                .filter(metaClass -> !metadataTools.isSystemLevel(metaClass))
                .filter(this::readPermitted)
                .collect(Collectors.toMap(
                        metaClass -> messageTools.getEntityCaption(metaClass) + " (" + metaClass.getName() + ")",
                        Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        TreeMap::new
                ));
    }

    protected boolean readPermitted(MetaClass metaClass) {
        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        return entityContext.isViewPermitted();
    }

    @SuppressWarnings("rawtypes")
    protected void showEntityPropertiesGridLayout() {
        fieldsScrollBox.removeAll();
        if (currentMetaClass != null) {
            InstanceContainer container = initInstanceContainerWithDbEntity();
            GridLayout gridLayout = AppSettingsGridLayoutBuilder.of(getApplicationContext(), container)
                    .withOwnerComponent(fieldsScrollBox)
                    .build();
            fieldsScrollBox.add(gridLayout);

            actionsBox.setVisible(true);
        }
    }

    @SuppressWarnings("rawtypes")
    protected InstanceContainer initInstanceContainerWithDbEntity() {
        InstanceContainer container = dataComponents.createInstanceContainer(currentMetaClass.getJavaClass());
        entityToEdit = dataManager.load(currentMetaClass.getJavaClass())
                .query(String.format(SELECT_APP_SETTINGS_ENTITY_QUERY, currentMetaClass.getName()))
                .fetchPlan(fetchPlans.builder(currentMetaClass.getJavaClass()).addFetchPlan(FetchPlan.LOCAL).build())
                .hint(PersistenceHints.SOFT_DELETION, false)
                .optional()
                .orElse(null);

        if (entityToEdit == null) {
            entityToEdit = dataContext.create(currentMetaClass.getJavaClass());
        } else {
            entityToEdit = dataContext.merge(entityToEdit);
        }

        container.setItem(entityToEdit);
        return container;
    }

    protected boolean isApplicationSettingsEntity(MetaClass metaClass) {
        return AppSettingsEntity.class.isAssignableFrom(metaClass.getJavaClass());
    }

    @Subscribe("saveButtonId")
    public void onSaveButtonClick(Button.ClickEvent event) {
        commitChanges();
    }

    @Subscribe("closeButtonId")
    public void onCloseButtonClick(Button.ClickEvent event) {
        if (dataContext != null && hasUnsavedChanges()) {
            handleCloseBtnClickWithUnsavedChanges();
        } else {
            close(WINDOW_CLOSE_ACTION);
        }
    }

    protected boolean hasUnsavedChanges() {
        for (Object modified : dataContext.getModified()) {
            if (!entityStates.isNew(modified)) {
                return true;
            }
        }
        //check whether "new" entity is modified in DataContext
        return isNewEntityModified;
    }

    protected void handleCloseBtnClickWithUnsavedChanges() {
        UnknownOperationResult result = new UnknownOperationResult();
        screenValidation.showSaveConfirmationDialog(this, new StandardCloseAction(Window.CLOSE_ACTION_ID))
                .onCommit(() -> result.resume(commitChanges().compose(() -> close(WINDOW_COMMIT_AND_CLOSE_ACTION))))
                .onDiscard(() -> result.resume(close(WINDOW_DISCARD_AND_CLOSE_ACTION)))
                .onCancel(result::fail);
    }

    protected void handleEntityLookupChangeWithUnsavedChanges() {
        UnknownOperationResult result = new UnknownOperationResult();
        screenValidation.showUnsavedChangesDialog(this, new StandardCloseAction(Window.CLOSE_ACTION_ID))
                .onDiscard(() -> result.resume(updateEntityLookupValue(false)))
                .onCancel(() -> result.resume(updateEntityLookupValue(true)));
    }

    protected OperationResult commitChanges() {
        ValidationErrors validationErrors = screenValidation.validateUiComponents(getWindow());
        if (!validationErrors.isEmpty()) {
            screenValidation.showValidationErrors(this, validationErrors);
            return OperationResult.fail();
        }

        appSettings.save(((AppSettingsEntity) entityToEdit));
        dataContext.clear();
        isNewEntityModified = false;
        showSaveNotification();

        return OperationResult.success();
    }

    protected OperationResult updateEntityLookupValue(boolean preventEntityLookupChange) {
        isEntityChangePrevented = preventEntityLookupChange;
        if (preventEntityLookupChange) {
            entitiesLookup.setValue(prevMetaClass);
            return OperationResult.fail();
        } else {
            isNewEntityModified = false;
            initEntityPropertiesGridLayout();
            return OperationResult.success();
        }
    }

    protected void showSaveNotification() {
        String caption = messages.formatMessage(this.getClass(), "entitySaved", messageTools.getEntityCaption(currentMetaClass));
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption(caption)
                .show();
    }

}