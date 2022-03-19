package test_support.app.entity.sales;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "core_OrderLineA")
@JmixEntity
public class OrderLineA extends OrderLine {

    @Column(name = "PARAM1")
    protected String param1;

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }
}