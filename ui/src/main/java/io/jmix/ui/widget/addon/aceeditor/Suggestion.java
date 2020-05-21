/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget.addon.aceeditor;

import io.jmix.ui.widget.client.addon.aceeditor.TransportSuggestion;

/**
 * A single suggestion.
 * 
 * Feel free to subclass.
 */
public class Suggestion {

	protected final String displayText;
	protected final String descriptionText;
	protected final String suggestionText;
	protected final int startPosition;
	protected final int endPosition;

	/**
	 * 
	 * @param displayText
	 *            the text shown in the popup list
	 * @param descriptionText
	 *            a longer description
	 */
	public Suggestion(String displayText,
			String descriptionText) {
		this(displayText, descriptionText, "");
	}
	
	/**
	 * 
	 * If suggestionText is "cat", the suggestion popup will stay there
	 * if user types "c" "ca" or "cat".
	 * 
	 * @param displayText
	 *            the text shown in the popup list
	 * @param descriptionText
	 *            a longer description
	 * @param suggestionText
	 */
	public Suggestion(String displayText,
			String descriptionText, String suggestionText) {
		this.displayText = displayText;
		this.descriptionText = descriptionText;
		this.suggestionText = suggestionText;
		this.startPosition = -1;
		this.endPosition = -1;
	}

	public Suggestion(String displayText, String descriptionText, String suggestionText,
					  int startPosition, int endPosition) {
		this.displayText = displayText;
		this.descriptionText = descriptionText;
		this.suggestionText = suggestionText;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
	
	public TransportSuggestion asTransport(int index) {
		TransportSuggestion ts = new TransportSuggestion();
		ts.displayText = displayText;
		ts.descriptionText = descriptionText;
		ts.suggestionText = suggestionText;
		ts.index = index;
		return ts;
	}

	public String getDisplayText() {
		return displayText;
	}

	public String getDescriptionText() {
		return descriptionText;
	}

	public String getSuggestionText() {
		return suggestionText;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}
}
