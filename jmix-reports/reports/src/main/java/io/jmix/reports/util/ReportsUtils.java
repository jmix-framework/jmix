/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.util;

import io.jmix.core.*;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.exception.ReportingException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.Date;

@Component("report_ReportsUtils")
public class ReportsUtils {
    protected static final int MAX_REPORT_NAME_LENGTH = 255;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected TransactionTemplate transaction;

    @PersistenceContext
    protected EntityManager em;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected EntityStates entityStates;

    public String generateReportName(String sourceName) {
        return generateReportName(sourceName, 0);
    }

    public Report reloadReportIfNeeded(Report report, String fetchPlanName) {
        if (report.getIsTmp()) {
            return report;
        }
        if (!entityStates.isLoadedWithFetchPlan(report, fetchPlanName)) {
            return dataManager.load(Id.of(report))
                    .fetchPlan(fetchPlanName)
                    .one();
        }
        return report;
    }

    public Date currentDateOrTime(ParameterType parameterType) {
        Date now = timeSource.currentTimestamp();
        switch (parameterType) {
            case TIME:
                now = truncateToTime(now);
                break;
            case DATETIME:
                break;
            case DATE:
                now = truncateToDay(now);
                break;
            default:
                throw new ReportingException("Not Date/Time related parameter types are not supported.");
        }
        return now;
    }

    protected Date truncateToDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    protected Date truncateToTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    protected String generateReportName(String sourceName, int iteration) {
        if (iteration == 1) {
            iteration++; //like in win 7: duplicate of file 'a.txt' is 'a (2).txt', NOT 'a (1).txt'
        }
        String reportName = StringUtils.stripEnd(sourceName, null);
        if (iteration > 0) {
            String newReportName = String.format("%s (%s)", reportName, iteration);
            if (newReportName.length() > MAX_REPORT_NAME_LENGTH) {

                String abbreviatedReportName = StringUtils.abbreviate(reportName, MAX_REPORT_NAME_LENGTH -
                        String.valueOf(iteration).length() - 3);// 3 cause it us " ()".length

                reportName = String.format("%s (%s)", abbreviatedReportName, iteration);
            } else {
                reportName = newReportName;
            }

        }

        String finalReportName = reportName;
        Long countOfReportsWithSameName = transaction.execute(status -> (Long) em.createQuery("select count(r) from report_Report r where r.name = :name")
                .setParameter("name", finalReportName)
                .getSingleResult());

        if (countOfReportsWithSameName != null && countOfReportsWithSameName > 0) {
            return generateReportName(sourceName, ++iteration);
        }

        return reportName;
    }
}
