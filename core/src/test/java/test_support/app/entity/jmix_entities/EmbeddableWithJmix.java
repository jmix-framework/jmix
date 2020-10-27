package test_support.app.entity.jmix_entities;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@JmixEntity
@Embeddable
public class EmbeddableWithJmix {

    @Column(name = "DATA")
    protected String data;


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
