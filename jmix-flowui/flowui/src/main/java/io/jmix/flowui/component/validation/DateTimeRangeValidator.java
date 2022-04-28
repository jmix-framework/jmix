package io.jmix.flowui.component.validation;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;

@Component("flowui_DateTimeRangeValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateTimeRangeValidator<T extends Comparable<T>> extends AbstractValidator<T> {

    protected CurrentAuthentication currentAuthentication;
    protected DatatypeRegistry datatypeRegistry;
    protected Messages messages;

    protected T min;
    protected T max;

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Nullable
    public T getMin() {
        return min;
    }

    public void setMin(@Nullable T min) {
        this.min = min;
    }

    @Nullable
    public T getMax() {
        return max;
    }

    public void setMax(@Nullable T max) {
        this.max = max;
    }

    @Override
    public void accept(@Nullable T value) throws ValidationException {
        // consider null value is in range
        if (value == null) {
            return;
        }

        if (min != null && value.compareTo(min) < 0) {
            Datatype datatype = datatypeRegistry.get(min.getClass());
            String formatted = datatype.format(min, currentAuthentication.getLocale());
            throw new ValidationException(
                    getTemplateErrorMessage(
                            messages.getMessage("validation.constraints.dateTimeRangeMinExceeded"),
                            Map.of("min", formatted)));
        }

        if (max != null && value.compareTo(max) > 0) {
            Datatype datatype = datatypeRegistry.get(max.getClass());
            String formatted = datatype.format(min, currentAuthentication.getLocale());
            throw new ValidationException(
                    getTemplateErrorMessage(
                            messages.getMessage("validation.constraints.dateTimeRangeMaxExceeded"),
                            Map.of("max", formatted)));
        }
    }
}
