package io.jmix.securityflowui.component.rolefilter;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.RoleSourceEnum;
import org.apache.commons.lang3.StringUtils;

import org.springframework.lang.Nullable;

public class RoleFilterChangeEvent extends ComponentEvent<RoleFilter> {

    private String name;
    private String code;
    private RoleSourceEnum source;

    public RoleFilterChangeEvent(RoleFilter filter) {
        this(filter, null, null, null);
    }

    public RoleFilterChangeEvent(RoleFilter filter,
                                 @Nullable String name, @Nullable String code, @Nullable RoleSourceEnum source) {
        super(filter, true);

        this.name = name;
        this.code = code;
        this.source = source;
    }

    @Nullable
    public String getNameValue() {
        return name;
    }

    @Nullable
    public String getCodeValue() {
        return code;
    }

    @Nullable
    public RoleSourceEnum getSourceValue() {
        return source;
    }

    @Nullable
    protected String getSourceValueAsString() {
        return source != null ? source.getId() : null;
    }

    public boolean matches(BaseRole role) {
        return (name == null || StringUtils.containsIgnoreCase(role.getName(), name))
                && (code == null || StringUtils.containsIgnoreCase(role.getCode(), code))
                && (source == null || StringUtils.containsIgnoreCase(role.getSource(), getSourceValueAsString()));
    }
}
