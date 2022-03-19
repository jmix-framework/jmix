/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.listener.AfterCompleteTransactionListener;
import com.haulmont.cuba.core.listener.BeforeCommitTransactionListener;
import io.jmix.core.Entity;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("jmix_CubaTransactionListenersManager")
public class CubaTransactionListenersManager implements
        ApplicationContextAware,
        io.jmix.data.impl.BeforeCommitTransactionListener,
        io.jmix.data.impl.AfterCompleteTransactionListener {

    protected List<BeforeCommitTransactionListener> beforeCommitTxListeners;

    protected List<AfterCompleteTransactionListener> afterCompleteTxListeners;

    @Autowired
    private Persistence persistence;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, BeforeCommitTransactionListener> beforeCommitMap = applicationContext.getBeansOfType(BeforeCommitTransactionListener.class);
        beforeCommitTxListeners = new ArrayList<>(beforeCommitMap.values());
        beforeCommitTxListeners.sort(new OrderComparator());

        Map<String, AfterCompleteTransactionListener> afterCompleteMap = applicationContext.getBeansOfType(AfterCompleteTransactionListener.class);
        afterCompleteTxListeners = new ArrayList<>(afterCompleteMap.values());
        afterCompleteTxListeners.sort(new OrderComparator());
    }

    @Override
    public void beforeCommit(String storeName, Collection<Object> managedEntities) {
        for (BeforeCommitTransactionListener listener : beforeCommitTxListeners) {
            listener.beforeCommit(persistence.getEntityManager(storeName), castCollection(managedEntities));
        }
    }

    @Override
    public void afterComplete(boolean committed, Collection<Object> detachedEntities) {
        for (AfterCompleteTransactionListener listener : afterCompleteTxListeners) {
            listener.afterComplete(committed, castCollection(detachedEntities));
        }
    }

    protected Collection<Entity> castCollection(Collection<Object> collection) {
        return collection.stream()
                .map(o -> (Entity) o)
                .collect(Collectors.toList());
    }
}
