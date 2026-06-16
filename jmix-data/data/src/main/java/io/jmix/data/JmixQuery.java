package io.jmix.data;

import jakarta.persistence.TypedQuery;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface JmixQuery<T> extends TypedQuery<T> {

    String getQueryString();

    void setQueryString(String queryString);
}
