package test_support.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum EcoRank implements EnumClass<Integer> {

    EURO1(1),
    EURO2(2),
    EURO3(3);

    private Integer id;

    EcoRank(Integer value) {
        this.id = value;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static EcoRank fromId(Integer id) {
        for (EcoRank at : EcoRank.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}