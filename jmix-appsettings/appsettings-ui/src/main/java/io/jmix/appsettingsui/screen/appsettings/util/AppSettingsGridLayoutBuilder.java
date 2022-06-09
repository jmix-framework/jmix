package io.jmix.appsettingsui.screen.appsettings.util;

import io.jmix.appsettings.AppSettings;
import io.jmix.appsettings.AppSettingsTools;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.Convert;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.jmix.appsettingsui.screen.appsettings.util.EntityUtils.isMany;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component("appset_AppSettingsGridLayoutBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AppSettingsGridLayoutBuilder {

    private static final int MAX_TEXT_FIELD_STRING_LENGTH = 255;
    private static final Integer MAX_CAPTION_LENGTH = 50;
    private static final String FIELD_WIDTH = "350px";

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected AppSettings appSettings;

    @Autowired
    protected AppSettingsTools appSettingsTools;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected DataComponents dataComponents;

    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;

    @Autowired
    protected Actions actions;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected AccessManager accessManager;

    private final InstanceContainer container;
    private io.jmix.ui.component.Component ownerComponent;

    public static AppSettingsGridLayoutBuilder of(ApplicationContext applicationContext, InstanceContainer container) {
        return applicationContext.getBean(AppSettingsGridLayoutBuilder.class, container);
    }

    protected AppSettingsGridLayoutBuilder(InstanceContainer container) {
        this.container = container;
    }

    public AppSettingsGridLayoutBuilder withOwnerComponent(io.jmix.ui.component.Component component) {
        this.ownerComponent = component;
        return this;
    }

    public GridLayout build() {
        MetaClass metaClass = container.getEntityMetaClass();
        List<MetaProperty> metaProperties = collectMetaProperties(metaClass, container.getItem()).stream()
                .sorted(Comparator.comparing(MetadataObject::getName))
                .collect(Collectors.toList());

        GridLayout gridLayout = uiComponents.create(GridLayout.class);
        gridLayout.setSpacing(true);
        gridLayout.setMargin(false, true, false, false);
        gridLayout.setColumns(3);
        gridLayout.setRows(metaProperties.size() + 1);

        if (ownerComponent != null) {
            ((ComponentContainer) ownerComponent).add(gridLayout);
        }

        Label currentValueLabel = uiComponents.create(Label.class);
        currentValueLabel.setValue(messages.getMessage(this.getClass(), "currentValueLabel"));
        currentValueLabel.setAlignment(io.jmix.ui.component.Component.Alignment.MIDDLE_LEFT);
        gridLayout.add(currentValueLabel, 1, 0);

        Label defaultValueLabel = uiComponents.create(Label.class);
        defaultValueLabel.setValue(messages.getMessage(this.getClass(), "defaultValueLabel"));
        currentValueLabel.setAlignment(io.jmix.ui.component.Component.Alignment.MIDDLE_LEFT);
        gridLayout.add(defaultValueLabel, 2, 0);

        for (int i = 0; i < metaProperties.size(); i++) {
            addRowToGrid(container, gridLayout, i, metaProperties.get(i));
        }

        return gridLayout;
    }

    protected List<MetaProperty> collectMetaProperties(MetaClass metaClass, Object item) {
        List<MetaProperty> result = new ArrayList<>();
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    //skip system properties
                    if (metadataTools.isSystem(metaProperty)) {
                        continue;
                    }
                    if (metaProperty.getType() != MetaProperty.Type.ENUM
                            && (EntityUtils.isByteArray(metaProperty) || EntityUtils.isUuid(metaProperty))) {
                        continue;
                    }
                    if (metadataTools.isAnnotationPresent(item, metaProperty.getName(), Convert.class)) {
                        continue;
                    }
                    result.add(metaProperty);
                    break;
                case COMPOSITION:
                case ASSOCIATION:
                    if (!isMany(metaProperty)) {
                        result.add(metaProperty);
                    }
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    protected void addRowToGrid(InstanceContainer container, GridLayout gridLayout, int currentRow, MetaProperty metaProperty) {
        MetaClass metaClass = container.getEntityMetaClass();
        Range range = metaProperty.getRange();

        UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaClass, metaProperty.getName());
        accessManager.applyRegisteredConstraints(attributeContext);
        if (!attributeContext.canView()) {
            return;
        }

        if (range.isClass()) {
            UiEntityContext entityContext = new UiEntityContext(range.asClass());
            accessManager.applyRegisteredConstraints(entityContext);
            if (!entityContext.isViewPermitted()) {
                return;
            }
        }

        //add label
        Label fieldLabel = uiComponents.create(Label.class);
        fieldLabel.setValue(getPropertyCaption(metaClass, metaProperty));
        fieldLabel.setAlignment(io.jmix.ui.component.Component.Alignment.MIDDLE_LEFT);
        gridLayout.add(fieldLabel, 0, currentRow + 1);

        //current field
        ValueSource valueSource = new ContainerValueSource<>(container, metaProperty.getName());
        ComponentGenerationContext componentContext = new ComponentGenerationContext(metaClass, metaProperty.getName());
        componentContext.setValueSource(valueSource);
        gridLayout.add(createField(metaProperty, range, componentContext), 1, currentRow + 1);

        //default value
        ComponentGenerationContext componentContextForDefaultField = new ComponentGenerationContext(metaClass, metaProperty.getName());
        ValueSource valueSourceForDefaultField = new ContainerValueSource<>(dataComponents.createInstanceContainer(metaClass.getJavaClass()), metaProperty.getName());
        componentContextForDefaultField.setValueSource(valueSourceForDefaultField);
        Field defaultValueField = createField(metaProperty, range, componentContextForDefaultField);
        defaultValueField.setValue(appSettingsTools.getDefaultPropertyValue(metaClass.getJavaClass(), metaProperty.getName()));
        defaultValueField.setEditable(false);
        gridLayout.add(defaultValueField, 2, currentRow + 1);
    }

    protected Field createField(MetaProperty metaProperty, Range range, ComponentGenerationContext componentContext) {
        Field field = (Field) uiComponentsGenerator.generate(componentContext);

        if (EntityUtils.requireTextArea(metaProperty, this.container.getItem(), MAX_TEXT_FIELD_STRING_LENGTH)) {
            field = uiComponents.create(TextArea.NAME);
        }

        if (EntityUtils.isBoolean(metaProperty)) {
            field = createBooleanField();
        }

        if (EntityUtils.isSecret(metaProperty)) {
            field = createPasswordField();
        }

        if (range.isClass()) {
            field = createEntityPickerField();
        }

        field.setValueSource(componentContext.getValueSource());
        field.setWidth(FIELD_WIDTH);
        return field;
    }

    protected EntityPicker createEntityPickerField() {
        EntityPicker pickerField = uiComponents.create(EntityPicker.class);
        EntityLookupAction lookupAction = actions.create(EntityLookupAction.class);
        lookupAction.setOpenMode(OpenMode.THIS_TAB);
        pickerField.addAction(lookupAction);
        pickerField.addAction(actions.create(EntityClearAction.class));
        return pickerField;
    }

    protected Field createBooleanField() {
        ComboBox field = uiComponents.create(ComboBox.NAME);
        field.setOptionsMap(ParamsMap.of(
                messages.getMessage("trueString"), Boolean.TRUE,
                messages.getMessage("falseString"), Boolean.FALSE));
        field.setTextInputAllowed(false);
        return field;
    }

    protected Field createPasswordField() {
        return uiComponents.<PasswordField>create(PasswordField.NAME);
    }

    protected String getPropertyCaption(MetaClass metaClass, MetaProperty metaProperty) {
        String caption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());
        if (caption.length() < MAX_CAPTION_LENGTH) {
            return caption;
        } else {
            return caption.substring(0, MAX_CAPTION_LENGTH);
        }
    }

}
