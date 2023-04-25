package test_support.app.entity.sales;

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "core_OrderLineB")
@JmixEntity
public class OrderLineB extends OrderLine {

    @Column(name = "PARAM2")
    protected String param2;

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }
}
