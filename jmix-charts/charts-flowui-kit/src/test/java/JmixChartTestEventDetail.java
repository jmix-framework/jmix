import io.jmix.chartsflowui.kit.component.event.dto.JmixChartEventDetail;

import java.util.List;
import java.util.Map;

public class JmixChartTestEventDetail extends JmixChartEventDetail {

    private Map<String, String> testMap;

    private List<Integer> numbers;

    private TestDTO testDTO;

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
}
