package io.jmix.outside_reports;

import io.jmix.reports.annotation.InputParameterDef;
import io.jmix.reports.annotation.InputParameterDelegate;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.yarg.structure.DefaultValueProvider;

import java.util.Date;

@InputParameterDef(
        alias = "afterDate",
        name = "msg://SimpleReport.afterDate",
        type = ParameterType.DATETIME,
        required = true
)
public interface InheritanceAspectReport {

    @InputParameterDelegate(alias = "afterDate")
    default DefaultValueProvider<Date> afterDateDefaultValue() {
        return parameter -> new Date();
    }
}