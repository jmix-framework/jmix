package test_support.app.entity.jmix_entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EmbeddableWithoutJmix {

    @Column(name = "DATA")
    protected String data;


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
