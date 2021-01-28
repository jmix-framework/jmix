package io.jmix.reports.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Id;
import java.util.UUID;

@JmixEntity(name = "report_ReportRole")
public class ReportRole {

    private static final long serialVersionUID = 1525738800225011452L;

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    protected Report report;

    @JmixProperty
    protected String roleName;

    @JmixProperty
    protected String roleCode;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
