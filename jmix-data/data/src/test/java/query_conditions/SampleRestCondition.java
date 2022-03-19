package query_conditions;

import io.jmix.core.querycondition.Condition;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleRestCondition implements Condition {

    public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$\\{([\\w.$]+)}");

    protected String param;

    protected List<String> parameters = new ArrayList<>();

    public SampleRestCondition() {
    }

    public static SampleRestCondition create(String param) {
        SampleRestCondition condition = new SampleRestCondition();
        condition.setParam(param);
        return condition;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
        parseParameters(param);
    }

    @Override
    public Collection<String> getParameters() {
        return parameters;
    }

    @Nullable
    @Override
    public Condition actualize(Set<String> actualParameters) {
        return actualParameters.containsAll(getParameters()) ? this : null;
    }

    @Override
    public Condition copy() {
        return SampleRestCondition.create(param);
    }

    @Override
    public Set<String> getExcludedParameters(Set<String> actualParameters) {
        return Collections.emptySet();
    }

    protected void parseParameters(String value) {
        Matcher matcher = PARAMETER_PATTERN.matcher(value);
        while (matcher.find()) {
            String parameterName = matcher.group(1);
            parameters.add(parameterName);
        }
    }
}
