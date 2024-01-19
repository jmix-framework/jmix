/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model;

import io.jmix.chartsflowui.kit.component.model.shared.Separator;

import java.util.ArrayList;
import java.util.List;

public class Aria extends ChartObservableObject {

    protected Boolean enabled;

    protected Label label;

    protected Decal decal;

    public static class Label extends ChartObservableObject {

        protected Boolean enabled;

        protected String description;

        protected General general;

        protected Series series;

        protected Data data;

        public static class General extends ChartObservableObject {

            protected String withTitle;

            protected String withoutTitle;

            public String getWithTitle() {
                return withTitle;
            }

            public void setWithTitle(String withTitle) {
                this.withTitle = withTitle;
                markAsDirty();
            }

            public String getWithoutTitle() {
                return withoutTitle;
            }

            public void setWithoutTitle(String withoutTitle) {
                this.withoutTitle = withoutTitle;
                markAsDirty();
            }

            public General withWithTitle(String withTitle) {
                setWithTitle(withTitle);
                return this;
            }

            public General withWithoutTitle(String withoutTitle) {
                setWithoutTitle(withoutTitle);
                return this;
            }
        }

        public static class Series extends ChartObservableObject {

            protected Integer maxCount;

            protected Single single;

            protected Multiple multiple;

            public static abstract class AbstractLabel<T extends AbstractLabel<T>> extends ChartObservableObject {

                protected String prefix;

                protected String withName;

                protected String withoutName;

                public String getPrefix() {
                    return prefix;
                }

                public void setPrefix(String prefix) {
                    this.prefix = prefix;
                    markAsDirty();
                }

                public String getWithName() {
                    return withName;
                }

                public void setWithName(String withName) {
                    this.withName = withName;
                    markAsDirty();
                }

                public String getWithoutName() {
                    return withoutName;
                }

                public void setWithoutName(String withoutName) {
                    this.withoutName = withoutName;
                    markAsDirty();
                }

                @SuppressWarnings("unchecked")
                public T withPrefix(String prefix) {
                    setPrefix(prefix);
                    return (T) this;
                }

                @SuppressWarnings("unchecked")
                public T withWithName(String withName) {
                    setWithName(withName);
                    return (T) this;
                }

                @SuppressWarnings("unchecked")
                public T withWithoutName(String withoutName) {
                    setWithoutName(withoutName);
                    return (T) this;
                }
            }

            public static class Single extends AbstractLabel<Single> {
            }

            public static class Multiple extends AbstractLabel<Multiple> {

                protected Separator separator;

                public Separator getSeparator() {
                    return separator;
                }

                public void setSeparator(Separator separator) {
                    if (this.separator != null) {
                        removeChild(this.separator);
                    }

                    this.separator = separator;
                    addChild(separator);
                }

                public Multiple withSeparator(Separator separator) {
                    setSeparator(separator);
                    return this;
                }
            }

            public Integer getMaxCount() {
                return maxCount;
            }

            public void setMaxCount(Integer maxCount) {
                this.maxCount = maxCount;
                markAsDirty();
            }

            public Single getSingle() {
                return single;
            }

            public void setSingle(Single single) {
                if (this.single != null) {
                    removeChild(this.single);
                }

                this.single = single;
                addChild(single);
            }

            public Multiple getMultiple() {
                return multiple;
            }

            public void setMultiple(Multiple multiple) {
                if (this.multiple != null) {
                    removeChild(this.multiple);
                }

                this.multiple = multiple;
                addChild(multiple);
            }

            public Series withMaxCount(Integer maxCount) {
                setMaxCount(maxCount);
                return this;
            }

            public Series withSingle(Single single) {
                setSingle(single);
                return this;
            }

            public Series withMultiple(Multiple multiple) {
                setMultiple(multiple);
                return this;
            }
        }

        public static class Data extends ChartObservableObject {

            protected Integer maxCount;

            protected String allData;

            protected String partialData;

            protected String withName;

            protected String withoutName;

            protected Separator separator;

            public Integer getMaxCount() {
                return maxCount;
            }

            public void setMaxCount(Integer maxCount) {
                this.maxCount = maxCount;
                markAsDirty();
            }

            public String getAllData() {
                return allData;
            }

            public void setAllData(String allData) {
                this.allData = allData;
                markAsDirty();
            }

            public String getPartialData() {
                return partialData;
            }

            public void setPartialData(String partialData) {
                this.partialData = partialData;
                markAsDirty();
            }

            public String getWithName() {
                return withName;
            }

            public void setWithName(String withName) {
                this.withName = withName;
                markAsDirty();
            }

            public String getWithoutName() {
                return withoutName;
            }

            public void setWithoutName(String withoutName) {
                this.withoutName = withoutName;
                markAsDirty();
            }

            public Separator getSeparator() {
                return separator;
            }

            public void setSeparator(Separator separator) {
                if (this.separator != null) {
                    removeChild(this.separator);
                }

                this.separator = separator;
                addChild(separator);
            }

            public Data withMaxCount(Integer maxCount) {
                setMaxCount(maxCount);
                return this;
            }

            public Data withAllData(String allData) {
                setAllData(allData);
                return this;
            }

            public Data withPartialData(String partialData) {
                setPartialData(partialData);
                return this;
            }

            public Data withWithName(String withName) {
                setWithName(withName);
                return this;
            }

            public Data withWithoutName(String withoutName) {
                setWithoutName(withoutName);
                return this;
            }

            public Data withSeparator(Separator separator) {
                setSeparator(separator);
                return this;
            }
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
            markAsDirty();
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
            markAsDirty();
        }

        public General getGeneral() {
            return general;
        }

        public void setGeneral(General general) {
            if (this.general != null) {
                removeChild(this.general);
            }

            this.general = general;
            addChild(general);
        }

        public Series getSeries() {
            return series;
        }

        public void setSeries(Series series) {
            if (this.series != null) {
                removeChild(this.series);
            }

            this.series = series;
            addChild(series);
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            if (this.data != null) {
                removeChild(this.data);
            }

            this.data = data;
            addChild(data);
        }

        public Label withEnabled(Boolean enabled) {
            setEnabled(enabled);
            return this;
        }

        public Label withDescription(String description) {
            setDescription(description);
            return this;
        }

        public Label withGeneral(General general) {
            setGeneral(general);
            return this;
        }

        public Label withSeries(Series series) {
            setSeries(series);
            return this;
        }

        public Label withData(Data data) {
            setData(data);
            return this;
        }
    }

    public static class Decal extends ChartObservableObject {

        protected Boolean show;

        protected List<io.jmix.chartsflowui.kit.component.model.shared.Decal> decals;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public List<io.jmix.chartsflowui.kit.component.model.shared.Decal> getDecals() {
            return decals;
        }

        public void setDecals(List<io.jmix.chartsflowui.kit.component.model.shared.Decal> decals) {
            if (this.decals != null) {
                this.decals.forEach(this::removeChild);
            }

            this.decals = decals;

            if (decals != null) {
                decals.forEach(this::addChild);
            }
        }

        public void setDecals(io.jmix.chartsflowui.kit.component.model.shared.Decal... decals) {
            setDecals(decals == null ? null : List.of(decals));
        }

        public void removeDecal(io.jmix.chartsflowui.kit.component.model.shared.Decal decal) {
            if (decals != null && decals.remove(decal)) {
                removeChild(decal);
            }
        }

        public void addDecal(io.jmix.chartsflowui.kit.component.model.shared.Decal decal) {
            if (decals == null) {
                decals = new ArrayList<>();
            }

            if (decals.contains(decal)) {
                return;
            }

            if (decal != null) {
                decals.add(decal);
                addChild(decal);
            }
        }

        public Decal withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public Decal withDecals(io.jmix.chartsflowui.kit.component.model.shared.Decal... decals) {
            setDecals(decals);
            return this;
        }

        public Decal withDecal(io.jmix.chartsflowui.kit.component.model.shared.Decal decal) {
            addDecal(decal);
            return this;
        }
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
        markAsDirty();
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        if (this.label != null) {
            removeChild(this.label);
        }

        this.label = label;
        addChild(label);
    }

    public Decal getDecal() {
        return decal;
    }

    public void setDecal(Decal decal) {
        if (this.decal != null) {
            removeChild(this.decal);
        }

        this.decal = decal;
        addChild(decal);
    }

    public Aria withEnabled(Boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    public Aria withLabel(Label label) {
        setLabel(label);
        return this;
    }

    public Aria withDecal(Decal decal) {
        setDecal(decal);
        return this;
    }
}
