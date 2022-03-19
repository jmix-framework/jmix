package com.haulmont.cuba.core.model.embeddedwithinheritance;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING)
@NamePattern("%s|name")
@Table(name = "TEST_EMBEDDED_PERSON")
@Entity(name = "embedded_EmbeddedPerson")
@JmixEntity
public class Person extends StandardEntity {
    private static final long serialVersionUID = 8994168805710890512L;

    @Column(name = "NAME")
    protected String name;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    protected VerificationInfo verificationInfo;

    public VerificationInfo getVerificationInfo() {
        return verificationInfo;
    }

    public void setVerificationInfo(VerificationInfo verificationInfo) {
        this.verificationInfo = verificationInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}