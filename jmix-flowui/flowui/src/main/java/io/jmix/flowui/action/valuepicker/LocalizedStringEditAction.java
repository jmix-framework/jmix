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

package io.jmix.flowui.action.valuepicker;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.LocalizedStringSupport;
import io.jmix.core.MetadataTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.app.localizedstring.LocalizedStringEditDialog;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.SupportsFormatter;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Opens a dialog that edits a localized string value per locale: the default value and one text per available
 * locale. The dialog is the only way to enter a localized value through the picker.
 */
@ActionType(LocalizedStringEditAction.ID)
public class LocalizedStringEditAction
        extends PickerAction<LocalizedStringEditAction, PickerComponent<String>, String> {

    public static final String ID = "value_localizedStringEdit";

    protected MetadataTools metadataTools;
    protected DialogWindows dialogWindows;
    protected LocalizedStringSupport localizedStringSupport;

    protected boolean multiline;

    public LocalizedStringEditAction() {
        this(ID);
    }

    public LocalizedStringEditAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.valuePicker.localizedStringEdit.description");
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setLocalizedStringSupport(LocalizedStringSupport localizedStringSupport) {
        this.localizedStringSupport = localizedStringSupport;
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties uiComponentProperties) {
        setShortcutCombination(KeyCombination.create(
                uiComponentProperties.getPickerLocalizedStringEditShortcut()));
    }

    @Autowired
    protected void setIcons(Icons icons) {
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.GLOBE);
        }
    }

    @Override
    public void setTarget(@Nullable PickerComponent<String> target) {
        Preconditions.checkArgument(target == null || target instanceof HasValue,
                "Target must implement " + HasValue.class.getName());
        super.setTarget(target);

        // A picker bound to an entity property shows the text through the datatype of the property; any other
        // picker would format the value as a plain string and show the stored one. A value of another type is
        // formatted the default way, so that execute() can report it.
        if (target instanceof SupportsFormatter<?> picker && picker.getFormatter() == null) {
            //noinspection unchecked
            ((SupportsFormatter<Object>) picker).setFormatter(value -> value instanceof String raw
                    ? localizedStringSupport.resolve(raw)
                    : metadataTools.format(value));
        }
    }

    /**
     * @return the value set through {@link #setMultiline(boolean)}, which is the requested minimum rather than
     * what the dialog shows
     */
    public boolean isMultiline() {
        return multiline;
    }

    /**
     * Requests multi-line fields. The dialog widens the request on its own: it uses multi-line fields for a
     * {@code @Lob} property and for a stored value that already carries a line break, because a single-line
     * field would drop it.
     */
    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public LocalizedStringEditAction withMultiline(boolean multiline) {
        setMultiline(multiline);
        return this;
    }

    @Override
    public void execute() {
        checkTarget();
        // Before the view is built, so that a wrong target does not leave a half-opened dialog behind.
        targetValue();

        dialogWindows.view(findOrigin(), LocalizedStringEditDialog.class)
                .withViewConfigurer(this::configureDialog)
                .withAfterCloseListener(this::onDialogClose)
                .open();
    }

    protected View<?> findOrigin() {
        return UiComponentUtils.getView((Component) target);
    }

    protected void configureDialog(LocalizedStringEditDialog dialog) {
        dialog.setValue(targetValue());
        dialog.setMultiline(isMultilineEffective());
        dialog.setMaxLength(resolveMaxLength());
        dialog.setRequired(target instanceof HasRequired hasRequired && hasRequired.isRequired());
    }

    protected void onDialogClose(DialogWindow.AfterCloseEvent<LocalizedStringEditDialog> event) {
        if (event.closedWith(StandardOutcome.SAVE)) {
            target.setValueFromClient(event.getView().getValue());
        }
    }

    /**
     * The target is only known to be a {@link HasValue}, so a target holding another type is reported here
     * rather than through a cast that fails at the first click.
     */
    @Nullable
    protected String targetValue() {
        Object value = ((HasValue<?, ?>) target).getValue();
        if (value != null && !(value instanceof String)) {
            throw new IllegalStateException(String.format(
                    "Action '%s' requires a target holding a String value, but the target holds %s",
                    getId(), value.getClass().getName()));
        }

        return (String) value;
    }

    protected boolean isMultilineEffective() {
        if (multiline) {
            return true;
        }

        MetaPropertyPath path = findMetaPropertyPath();
        // The same question the expression builder and the container sorter ask, so that the three agree on
        // which properties are LOBs.
        return path != null && metadataTools.isLob(path.getMetaProperty());
    }

    /**
     * Resolves the length limit from the column length and the bean validation annotations, so that the dialog
     * refuses a value the property would not accept.
     * <p>
     * The strictest of the declared limits applies, because the value has to satisfy all of them.
     *
     * @return the limit, or null when the property declares none
     */
    @Nullable
    protected Integer resolveMaxLength() {
        MetaPropertyPath path = findMetaPropertyPath();
        if (path == null) {
            return null;
        }

        Map<String, Object> annotations = path.getMetaProperty().getAnnotations();
        Integer maxLength = strictestLength(null, (Integer) annotations.get(MetadataTools.LENGTH_ANN_NAME));
        maxLength = strictestLength(maxLength, (Integer) annotations.get(Size.class.getName() + "_max"));
        maxLength = strictestLength(maxLength, (Integer) annotations.get(Length.class.getName() + "_max"));

        return maxLength;
    }

    /**
     * @return the stricter of the two limits. {@link Integer#MAX_VALUE} is not a limit: it is what
     * {@code @Size} and {@code @Length} store when only the lower bound is declared.
     */
    @Nullable
    protected Integer strictestLength(@Nullable Integer current, @Nullable Integer candidate) {
        if (candidate == null || candidate == Integer.MAX_VALUE) {
            return current;
        }

        return current == null ? candidate : Math.min(current, candidate);
    }

    @Nullable
    protected MetaPropertyPath findMetaPropertyPath() {
        ValueSource<String> valueSource = target.getValueSource();
        return valueSource instanceof EntityValueSource<?, ?> entityValueSource
                ? entityValueSource.getMetaPropertyPath()
                : null;
    }
}
