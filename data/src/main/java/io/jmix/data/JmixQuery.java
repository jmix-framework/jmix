package io.jmix.data;

import javax.persistence.TypedQuery;

public interface JmixQuery<T> extends TypedQuery<T> {

    String getQueryString();

    void setQueryString(String queryString);
}
