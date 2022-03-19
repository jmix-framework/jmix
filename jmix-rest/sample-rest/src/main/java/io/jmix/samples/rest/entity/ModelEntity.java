package io.jmix.samples.rest.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

@JmixEntity(name = "rest_ModelEntity")
public class ModelEntity extends StandardEntity {

    @JmixProperty(mandatory = true)
    protected String stencilId;

    @InstanceName
    @JmixProperty(mandatory = true)
    protected String title;

    protected String description;

    protected Boolean editable = false;

    protected Integer orderNo;

    public String getStencilId() {
        return stencilId;
    }

    public void setStencilId(String stencilId) {
        this.stencilId = stencilId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }
}