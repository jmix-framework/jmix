package spec.haulmont.cuba.core.query_conditions;

import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.PropertyCondition;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleRestCondition extends PropertyCondition {

    public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$\\{([\\w.$]+)}");

    public SampleRestCondition(String param) {
        super(Collections.singletonList(new Entry("param", param)));
    }

    @Override
    protected void parseParameters() {
        for (Entry entry : entries) {
            Matcher matcher = PARAMETER_PATTERN.matcher(entry.value);
            while (matcher.find()) {
                String parameter = matcher.group(1);
                if (!parameters.contains(parameter))
                    parameters.add(parameter);
            }
        }
    }

    @Override
    public Condition copy() {
        return new SampleRestCondition(entries.get(0).value);
    }
}
