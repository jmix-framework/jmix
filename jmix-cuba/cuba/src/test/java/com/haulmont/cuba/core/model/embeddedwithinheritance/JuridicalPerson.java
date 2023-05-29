package com.haulmont.cuba.core.model.embeddedwithinheritance;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID")
@Table(name = "TEST_EMBEDDED_JURIDICAL_PERSON")
@Entity(name = "test_EmbeddedJuridicalPerson")
@JmixEntity
public class JuridicalPerson extends Person {
    private static final long serialVersionUID = 7221494621044751320L;

    @Column(name = "LEGAL_NAME")
    protected String legalName;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }
}