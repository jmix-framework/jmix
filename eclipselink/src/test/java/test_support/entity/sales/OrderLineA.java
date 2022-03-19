package test_support.entity.sales;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@JmixEntity
@Entity(name = "sales_OrderLineA")
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