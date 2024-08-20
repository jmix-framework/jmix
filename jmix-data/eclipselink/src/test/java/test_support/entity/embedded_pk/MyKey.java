package test_support.entity.embedded_pk;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@JmixEntity
@Embeddable
public class MyKey {
    @Column(name = "CODE1", nullable = false)
    @NotNull
    private String code1;

    @Column(name = "CODE2", nullable = false)
    @NotNull
    private Integer code2;

    public Integer getCode2() {
        return code2;
    }

    public void setCode2(Integer code2) {
        this.code2 = code2;
    }

    public String getCode1() {
        return code1;
    }

    public void setCode1(String code1) {
        this.code1 = code1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code2, code1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyKey entity = (MyKey) o;
        return Objects.equals(this.code2, entity.code2) &&
                Objects.equals(this.code1, entity.code1);
    }
}