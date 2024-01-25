import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jmix.chartsflowui.kit.component.event.dto.JmixChartEventDetail;

import java.util.List;
import java.util.Map;

public class JmixChartTestEventDetail extends JmixChartEventDetail {

    private Map<String, String> testMap;

    private List<Integer> numbers;

    private TestDTO testDTO;

    private String nullField;

    @JsonProperty("named")
    private String alterNamed;

    @JsonIgnore
    private String ignored;

    public Map<String, String> getTestMap() {
        return testMap;
    }

    public void setTestMap(Map<String, String> testMap) {
        this.testMap = testMap;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }

    public TestDTO getTestDTO() {
        return testDTO;
    }

    public void setTestDTO(TestDTO testDTO) {
        this.testDTO = testDTO;
    }

    public String getNullField() {
        return nullField;
    }

    public void setNullField(String nullField) {
        this.nullField = nullField;
    }

    public String getAlterNamed() {
        return alterNamed;
    }

    public void setAlterNamed(String alterNamed) {
        this.alterNamed = alterNamed;
    }

    public String getIgnored() {
        return ignored;
    }

    public void setIgnored(String ignored) {
        this.ignored = ignored;
    }
}
