package test_support.entity.cascade_operations;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

@JmixEntity(name = "test$JpaCascadeEmbeddable")
@Embeddable
public class JpaCascadeEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "BAR_INSIDE_ID")
    private JpaCascadeBar barInside;

    public JpaCascadeBar getBarInside() {
        return barInside;
    }

    public void setBarInside(JpaCascadeBar barInside) {
        this.barInside = barInside;
    }
}
