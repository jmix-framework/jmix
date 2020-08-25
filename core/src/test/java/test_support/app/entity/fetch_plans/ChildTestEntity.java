package test_support.app.entity.fetch_plans;

import test_support.base.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity(name = "app_ChildTestEntity")
public class ChildTestEntity extends StandardEntity {

    private static final long serialVersionUID = -4176110169739408116L;

    @ManyToOne
    @Column(name = "PARENT_ID")
    protected ParentTestEntity parent;

    @Column(name = "BIRTH_DATE")
    protected Date birthDate;

    @Column(name = "NAME")
    protected String name;


    public ParentTestEntity getParent() {
        return parent;
    }

    public void setParent(ParentTestEntity parent) {
        this.parent = parent;
    }


    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
