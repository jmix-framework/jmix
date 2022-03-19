/*
 * Copyright 2020 Haulmont.
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

package facet.timer.screen;

import io.jmix.ui.component.Timer;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("test_TimerFacetTestFragment")
@UiDescriptor("timer-facet-test-fragment.xml")
public class TimerFacetTestFragment extends ScreenFragment {
    @Autowired
    private Timer testTimer;

    private int ticksCounter = 0;
    private boolean stopped = false;

    @Subscribe("testTimer")
    private void onTimerTick(Timer.TimerActionEvent event) {
        this.ticksCounter += 1;
    }

    @Subscribe("testTimer")
    private void onTimerStop(Timer.TimerStopEvent event) {
        this.stopped = true;
    }

    public Timer getTimer() {
        return testTimer;
    }

    public int getTicksCounter() {
        return ticksCounter;
    }

    public boolean isStopped() {
        return stopped;
    }
}