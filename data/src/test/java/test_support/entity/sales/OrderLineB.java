package test_support.entity.sales;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "sales_OrderLineB")
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
