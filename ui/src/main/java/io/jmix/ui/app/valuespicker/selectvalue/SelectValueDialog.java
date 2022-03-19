package io.jmix.ui.app.valuespicker.selectvalue;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.TimeZoneAwareDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Actions;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.builder.LookupBuilder;
import io.jmix.ui.component.*;
import io.jmix.ui.component.Component.Alignment;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.component.data.options.EnumOptions;
import io.jmix.ui.component.data.options.MapOptions;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Time;
import java.time.*;
import java.util.*;
import java.util.function.Function;

@UiController("selectValueDialog")
@UiDescriptor("select-value-dialog.xml")
public class SelectValueDialog<V> extends Screen implements SelectValueController<V> {

    private static final Logger log = LoggerFactory.getLogger(SelectValueDialog.class);

    @Autowired
    private Button commitBtn;
    @Autowired
    protected HBoxLayout addItemLayout;
    @Autowired
    protected ScrollBoxLayout valuesLayout;

    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected UiScreenProperties screenProperties;

    protected SelectValueContext<V> context;
    protected List<V> values = new ArrayList<>();
    protected Action commitAction;

    @Subscribe
    public void onInit(InitEvent event) {
        initActions();
    }

    @Override
    public void setSelectValueContext(SelectValueContext<V> context) {
        if (this.context != null) {
            throw new IllegalStateException("Screen has been initialized this SelectValueContext");
        }

        this.context = context;

        initAddComponentLayout();
        initValues();

        commitAction.setEnabled(context.isFieldEditable());
        if (!context.isFieldEditable()) {
            commitBtn.focus();
        }
    }

    @Override
    public List<V> getValue() {
        return Collections.unmodifiableList(values);
    }

    protected void initActions() {
        Icons icons = getApplicationContext().getBean(Icons.class);

        commitAction = new BaseAction("commit")
                .withCaption(messages.getMessage("actions.Ok"))
                .withIcon(icons.get(JmixIcon.OK))
                .withPrimary(true)
                .withShortcut(screenProperties.getCommitShortcut())
                .withHandler(this::commit);

        getWindow().addAction(commitAction);

        Action cancelAction = new BaseAction("cancel")
                .withCaption(messages.getMessage("actions.Cancel"))
                .withIcon(icons.get(JmixIcon.CANCEL))
                .withShortcut(screenProperties.getCloseShortcut())
                .withHandler(this::cancel);

        getWindow().addAction(cancelAction);
    }

    protected void initAddComponentLayout() {
        addItemLayout.removeAll();
        Field<V> field = createField();
        field.setId("listValueField");

        if (CollectionUtils.isNotEmpty(context.getValidators())) {
            for (Validator<V> validator : context.getValidators()) {
                field.addValidator(validator);
            }
        }

        addItemLayout.add(field);
        addItemLayout.expand(field);

        field.setEditable(context.isFieldEditable());
        if (context.isFieldEditable()) {
            if (field instanceof Component.Focusable) {
                ((Component.Focusable) field).focus();
            }
        }

        if (context.getJavaClass() != null) {
            Button addBtn = uiComponents.create(Button.class);
            addBtn.setId("add");
            addBtn.setCaption(messages.getMessage("actions.Add"));
            addBtn.addClickListener(e ->
                    _addValue(field)
            );

            addItemLayout.add(addBtn);
            addBtn.setEnabled(context.isFieldEditable());
        }
    }

    protected Field<V> createField() {
        if (context.getOptions() != null) {
            ComboBox<V> comboBox = createComboBox();
            comboBox.setOptions(context.getOptions());
            return comboBox;
        } else if (!Strings.isNullOrEmpty(context.getEntityName())) {
            return createEntityField(context.getEntityName());
        } else if (context.getEnumClass() != null) {
            return createEnumField(context.getEnumClass());
        } else if (context.getJavaClass() != null) {
            return createDatatypeField(context.getJavaClass());
        } else {
            throw new IllegalStateException("Cannot create a component. " +
                    "Not enough information to infer its type");
        }
    }

    protected void _addValue(Field<V> field) {
        V value = field.getValue();

        if (value != null && isValid(field)) {
            field.setValue(null);

            if (!valueExists(value)) {
                addValueToLayout(value);
            }
        }
    }

    protected boolean isValid(Field<V> field) {
        try {
            field.validate();
        } catch (ValidationException e) {
            if (log.isTraceEnabled()) {
                log.trace("Validation failed", e);
            } else if (log.isDebugEnabled()) {
                log.debug("Validation failed: " + e);
            }
            return false;
        }

        return true;
    }

    protected boolean valueExists(V value) {
        return values.contains(value);
    }

    protected Field<V> createDatatypeField(Class<?> type) {
        //noinspection unchecked
        Datatype<V> datatype = (Datatype<V>) datatypeRegistry.get(type);

        if (type.equals(UUID.class)) {
            return createUuidField(datatype);
        } else if (type.equals(java.sql.Date.class)
                || type.equals(Date.class)
                || type.equals(LocalDate.class)
                || type.equals(LocalDateTime.class)
                || type.equals(OffsetDateTime.class)) {
            return createDateField(datatype);
        } else if (type.equals(Time.class)
                || type.equals(LocalTime.class)
                || type.equals(OffsetTime.class)) {
            return createTimeField(datatype);
        } else {
            return createTextField(datatype);
        }
    }

    protected TextField<V> createTextField(Datatype<V> datatype) {
        TextField<V> textField = uiComponents.create(TextField.NAME);
        textField.setDatatype(datatype);

        if (context.isFieldEditable()) {
            textField.addEnterPressListener(enterPressEvent ->
                    _addValue(textField));
        }
        return textField;
    }

    protected Field<V> createUuidField(Datatype<V> datatype) {
        MaskedField<V> maskedField = uiComponents.create(MaskedField.NAME);
        maskedField.setMask("hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh");
        maskedField.setSendNullRepresentation(false);
        maskedField.setDatatype(datatype);

        if (context.isFieldEditable()) {
            maskedField.addEnterPressListener(enterPressEvent ->
                    _addValue(maskedField));
        }

        return maskedField;
    }

    protected DateField<V> createDateField(Datatype<V> datatype) {
        DateField<V> dateField = uiComponents.create(DateField.NAME);
        dateField.setDatatype(datatype);

        if (context.getResolution() != null) {
            dateField.setResolution(context.getResolution());
        }

        if (context.getTimeZone() != null) {
            dateField.setTimeZone(context.getTimeZone());
        }

        return dateField;
    }

    protected TimeField<V> createTimeField(Datatype<V> datatype) {
        TimeField<V> timeField = uiComponents.create(TimeField.NAME);
        timeField.setDatatype(datatype);

        return timeField;
    }

    protected Field createEntityField(String entityName) {
        Metadata metadata = getApplicationContext().getBean(Metadata.class);
        MetaClass metaClass = metadata.getClass(entityName);

        return context.isUseComboBox()
                ? createEntityComboBox(metaClass)
                : createEntityPicker(metaClass);
    }

    protected Field createEntityPicker(MetaClass metaClass) {
        EntityPicker<Object> entityPicker = uiComponents.create(EntityPicker.NAME);
        entityPicker.setMetaClass(metaClass);

        Actions actions = getApplicationContext().getBean(Actions.class);

        BaseAction lookupAction = (BaseAction) actions.create(EntityLookupAction.ID);

        lookupAction.addActionPerformedListener(this::lookupActionPerformed);
        entityPicker.addAction(lookupAction);

        return entityPicker;
    }

    protected Field createEntityComboBox(MetaClass metaClass) {
        EntityComboBox<Object> entityComboBox = uiComponents.create(EntityComboBox.NAME);

        Options<Object> options;

        DataComponents dataComponents = getApplicationContext().getBean(DataComponents.class);
        CollectionContainer<Object> container =
                dataComponents.createCollectionContainer(metaClass.getJavaClass());
        CollectionLoader<Object> loader = dataComponents.createCollectionLoader();
        loader.setQuery("select e from " + metaClass.getName() + " e");
        loader.setFetchPlan(FetchPlan.INSTANCE_NAME);
        loader.setContainer(container);
        loader.load();
        options = new ContainerOptions<>(container);

        entityComboBox.setOptions(options);
        entityComboBox.setOptionCaptionProvider(((Function) context.getOptionCaptionProvider()));

        entityComboBox.addValueChangeListener(event -> {
            V selectedEntity = (V) event.getValue();
            if (selectedEntity != null && !valueExists(selectedEntity)) {
                addValueToLayout(selectedEntity);
            }
            entityComboBox.setValue(null);
        });

        return entityComboBox;
    }

    @SuppressWarnings("unchecked")
    protected void lookupActionPerformed(Action.ActionPerformedEvent actionPerformedEvent) {
        //noinspection unchecked
        EntityPicker<Object> entityPicker = (EntityPicker<Object>) actionPerformedEvent.getComponent();
        ScreenBuilders screenBuilders = getApplicationContext().getBean(ScreenBuilders.class);

        LookupBuilder<Object> builder = screenBuilders.lookup(entityPicker)
                .withSelectHandler(items -> {
                    if (CollectionUtils.isNotEmpty(items)) {
                        for (Object item : items) {
                            if (item != null && !valueExists((V) item)) {
                                this.addValueToLayout((V) item);
                            }
                        }
                    }

                    entityPicker.setValue(null);
                });

        if (!Strings.isNullOrEmpty(context.getLookupScreenId())) {
            builder.withScreenId(context.getLookupScreenId());
        }

        builder.show();
    }

    protected ComboBox<V> createComboBox() {
        ComboBox<V> comboBox = uiComponents.create(ComboBox.NAME);

        comboBox.setOptionCaptionProvider(context.getOptionCaptionProvider());
        comboBox.addValueChangeListener(e -> {
            V selectedValue = e.getValue();
            if (selectedValue != null && !valueExists(selectedValue)) {
                this.addValueToLayout(selectedValue);
            }
            comboBox.setValue(null);
        });

        return comboBox;
    }

    protected ComboBox<V> createEnumField(Class<? extends Enum> enumClass) {
        ComboBox<V> comboBox = createComboBox();
        comboBox.setOptions(new EnumOptions(enumClass));
        return comboBox;
    }

    protected void initValues() {
        for (V value : context.getInitialValues()) {
            addValueToLayout(value);
        }
    }

    protected void addValueToLayout(V value) {
        String labelValue = getValueCaption(value);

        Label<String> itemLab = uiComponents.create(Label.NAME);
        itemLab.setValue(labelValue);
        itemLab.setAlignment(Alignment.MIDDLE_LEFT);

        BoxLayout itemLayout = uiComponents.create(HBoxLayout.NAME);
        itemLayout.setId("itemLayout");
        itemLayout.setSpacing(true);
        itemLayout.add(itemLab);

        LinkButton delItemBtn = uiComponents.create(LinkButton.class);
        delItemBtn.setIconFromSet(JmixIcon.REMOVE);
        delItemBtn.addClickListener(e -> {
            values.remove(value);
            valuesLayout.remove(itemLayout);
        });

        itemLayout.add(delItemBtn);

        delItemBtn.setEnabled(context.isFieldEditable());

        valuesLayout.add(itemLayout);
        values.add(value);
    }

    protected String getValueCaption(V value) {
        Options<V> options = context.getOptions();
        if (options instanceof MapOptions) {
            Map<String, V> optionsMap = ((MapOptions<V>) options).getItemsCollection();
            return optionsMap.entrySet()
                    .stream()
                    .filter(entry -> Objects.equals(entry.getValue(), value))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("MapOptions doesn't contain key for value: " + value))
                    .getKey();
        }

        Function<V, String> optionCaptionProvider = context.getOptionCaptionProvider();
        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(value);
        }

        TimeZone timeZone = context.getTimeZone();
        Class<?> javaClass = context.getJavaClass();
        if (timeZone != null
                && javaClass != null) {
            Datatype<?> datatype = datatypeRegistry.get(javaClass);
            if (datatype instanceof TimeZoneAwareDatatype) {
                return ((TimeZoneAwareDatatype) datatype).format(value,
                        currentAuthentication.getLocale(), timeZone);
            }
        }

        return metadataTools.format(value);
    }

    public void commit(Action.ActionPerformedEvent actionPerformedEvent) {
        close(WINDOW_COMMIT_AND_CLOSE_ACTION);
    }

    private void cancel(Action.ActionPerformedEvent actionPerformedEvent) {
        close(WINDOW_CLOSE_ACTION);
    }
}
