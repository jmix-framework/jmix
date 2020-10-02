package com.haulmont.cuba.core.model.embeddedwithinheritance;


import com.haulmont.cuba.core.model.common.User;
import io.jmix.core.metamodel.annotation.ModelObject;

import javax.persistence.*;
import java.util.Date;

@ModelObject(name = "test_EmbeddedVerificationInfo")
@Embeddable
public class VerificationInfo {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_")
    protected Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    protected User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}