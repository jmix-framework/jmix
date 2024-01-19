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

package io.jmix.chartsflowui.kit.component.model.visualMap;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.HasSymbols;
import io.jmix.chartsflowui.kit.component.model.shared.SelectedMode;

import java.util.ArrayList;
import java.util.List;

public class PiecewiseVisualMap extends AbstractVisualMap<PiecewiseVisualMap> {

    protected Integer splitNumber;

    protected List<Piece> pieces;

    protected String[] categories;

    protected Boolean minOpen;

    protected Boolean maxOpen;

    protected SelectedMode selectedMode;

    protected Boolean showLabel;

    protected Integer itemGap;

    protected HasSymbols.SymbolType itemSymbol;

    public PiecewiseVisualMap() {
        super(VisualMapType.PIECEWISE);
    }

    public static class Piece extends ChartObservableObject {

        protected Double min;

        protected Double max;

        protected String label;

        protected Double value;

        protected Color color;

        public Double getMin() {
            return min;
        }

        public void setMin(Double min) {
            this.min = min;
            markAsDirty();
        }

        public Double getMax() {
            return max;
        }

        public void setMax(Double max) {
            this.max = max;
            markAsDirty();
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
            markAsDirty();
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
            markAsDirty();
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            markAsDirty();
        }

        public Piece withMin(Double min) {
            setMin(min);
            return this;
        }

        public Piece withMax(Double max) {
            setMax(max);
            return this;
        }

        public Piece withLabel(String label) {
            setLabel(label);
            return this;
        }

        public Piece withValue(Double value) {
            setValue(value);
            return this;
        }

        public Piece withColor(Color color) {
            setColor(color);
            return this;
        }
    }

    public Integer getSplitNumber() {
        return splitNumber;
    }

    public void setSplitNumber(Integer splitNumber) {
        this.splitNumber = splitNumber;
        markAsDirty();
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(List<Piece> pieces) {
        if (this.pieces != null) {
            this.pieces.forEach(this::removeChild);
        }

        this.pieces = pieces;
        if (pieces != null) {
            pieces.forEach(this::addChild);
        }
    }

    public void setPieces(Piece... pieces) {
        setPieces(pieces == null ? null : List.of(pieces));
    }

    public void removePiece(Piece piece) {
        if (pieces != null && pieces.remove(piece)) {
            removeChild(piece);
        }
    }

    public void addPiece(Piece piece) {
        if (pieces == null) {
            pieces = new ArrayList<>();
        }

        if (pieces.contains(piece)) {
            return;
        }

        pieces.add(piece);
        addChild(piece);
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String... categories) {
        this.categories = categories;
        markAsDirty();
    }

    public Boolean getMinOpen() {
        return minOpen;
    }

    public void setMinOpen(Boolean minOpen) {
        this.minOpen = minOpen;
        markAsDirty();
    }

    public Boolean getMaxOpen() {
        return maxOpen;
    }

    public void setMaxOpen(Boolean maxOpen) {
        this.maxOpen = maxOpen;
        markAsDirty();
    }

    public SelectedMode getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(SelectedMode selectedMode) {
        this.selectedMode = selectedMode;
        markAsDirty();
    }

    public Boolean getShowLabel() {
        return showLabel;
    }

    public void setShowLabel(Boolean showLabel) {
        this.showLabel = showLabel;
        markAsDirty();
    }

    public Integer getItemGap() {
        return itemGap;
    }

    public void setItemGap(Integer itemGap) {
        this.itemGap = itemGap;
        markAsDirty();
    }

    public HasSymbols.SymbolType getItemSymbol() {
        return itemSymbol;
    }

    public void setItemSymbol(HasSymbols.SymbolType itemSymbol) {
        this.itemSymbol = itemSymbol;
        markAsDirty();
    }

    public PiecewiseVisualMap withSplitNumber(Integer splitNumber) {
        setSplitNumber(splitNumber);
        return this;
    }

    public PiecewiseVisualMap withPieces(Piece... pieces) {
        setPieces(pieces);
        return this;
    }

    public PiecewiseVisualMap withPiece(Piece piece) {
        addPiece(piece);
        return this;
    }

    public PiecewiseVisualMap withCategories(String... categories) {
        setCategories(categories);
        return this;
    }

    public PiecewiseVisualMap withMinOpen(Boolean minOpen) {
        setMinOpen(minOpen);
        return this;
    }

    public PiecewiseVisualMap withMaxOpen(Boolean maxOpen) {
        setMaxOpen(maxOpen);
        return this;
    }

    public PiecewiseVisualMap withSelectedMode(SelectedMode selectedMode) {
        setSelectedMode(selectedMode);
        return this;
    }

    public PiecewiseVisualMap withShowLabel(Boolean showLabel) {
        setShowLabel(showLabel);
        return this;
    }

    public PiecewiseVisualMap withItemGap(Integer itemGap) {
        setItemGap(itemGap);
        return this;
    }

    public PiecewiseVisualMap withItemSymbol(HasSymbols.SymbolType itemSymbol) {
        setItemSymbol(itemSymbol);
        return this;
    }
}
