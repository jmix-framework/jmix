/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.libintegration;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TransactionParams;
import com.haulmont.cuba.core.global.*;
import io.jmix.core.TimeSource;
import io.jmix.reports.ReportingConfig;
import com.haulmont.yarg.exception.ValidationException;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@Component(GroovyScriptParametersProvider.NAME)
public class CubaGroovyScriptParametersProvider implements GroovyScriptParametersProvider {

    @Autowired
    protected ReportingConfig reportingConfig;

    @Override
    public Map<String, Object> prepareParameters(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParameters) {

        Map<String, Object> scriptParams = new HashMap<>();
        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.class);
        scriptParams.put("reportQuery", reportQuery);
        scriptParams.put("parentBand", parentBand);
        scriptParams.put("params", reportParameters);
        scriptParams.put("persistence", AppBeans.get(Persistence.class));
        scriptParams.put("metadata", AppBeans.get(Metadata.class));
        scriptParams.put("dataManager", AppBeans.get(DataManager.class));
        scriptParams.put("security", AppBeans.get(Security.class));
        scriptParams.put("timeSource", AppBeans.get(TimeSource.class));
        scriptParams.put("userSession", userSessionSource.getUserSession());
        scriptParams.put("userSessionSource", userSessionSource);
        scriptParams.put("transactional", new MethodClosure(this, "transactional"));
        scriptParams.put("validationException", new MethodClosure(this, "validationException"));

        return scriptParams;
    }

    protected void validationException(String message) {
        throw new ValidationException(message);
    }

    protected void transactional(Closure closure) {
        Persistence persistence = AppBeans.get(Persistence.class);
        Transaction tx;
        if (!persistence.isInTransaction() && reportingConfig.getUseReadOnlyTransactionForGroovy()) {
            tx = persistence.createTransaction(new TransactionParams().setReadOnly(true));
        } else {
            tx = persistence.getTransaction();
        }
        try {
            closure.call(persistence.getEntityManager());
            tx.commit();
        } finally {
            tx.end();
        }
    }
}
