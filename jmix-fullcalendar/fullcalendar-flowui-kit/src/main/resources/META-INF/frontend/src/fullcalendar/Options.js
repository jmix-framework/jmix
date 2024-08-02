const NO_VIEW_MORE_LINK_CLICK = 'NO_VIEW';

export const MORE_LINK_CLICK_FUNCTION = 'moreLinkClickFunction';
export const MORE_LINK_CLASS_NAMES = 'moreLinkClassNames';
export const INITIAL_VIEW = 'initialView';
export const UNSELECT_AUTO = 'unselectAuto';
export const UNSELECT_CANCEL = 'unselectCancel';
export const SELECT_MIN_DISTANCE = 'selectMinDistance';
const EVENT_OVERLAP = 'eventOverlap';
const SELECT_OVERLAP = 'selectOverlap';
const EVENT_CONSTRAINT = 'eventConstraint';
const BUSINESS_HOURS = 'businessHours';
const SELECT_CONSTRAINT = 'selectConstraint';
const SELECT_ALLOW = 'selectAllow';

export function processInitialOptions(serverOptions) {
    const options = {};
    if (!serverOptions) {
        return options;
    }
    options[SELECT_MIN_DISTANCE] = serverOptions[SELECT_MIN_DISTANCE];

    const unselectAuto = serverOptions[UNSELECT_AUTO];
    if (unselectAuto !== null && unselectAuto !== undefined) {
        options[UNSELECT_AUTO] = unselectAuto;
    }
    const initialView = serverOptions[INITIAL_VIEW];
    if (initialView !== null && initialView !== undefined) {
        options[INITIAL_VIEW] = initialView;
    }
    const unselectCancel = serverOptions[UNSELECT_CANCEL];
    if (unselectCancel) {
        options[UNSELECT_CANCEL] = unselectCancel;
    }
    return options;
}

class Options {

    constructor(calendar, context) {
        this.calendar = calendar;
        this.context = context;
    }

    updateOptions(options) {
        if (!options) {
            return;
        }
        this.calendar.batchRendering(() => {
            for (const key in options) {
                if (!this._skipOption(key)) {
                    this.updateOption(key, options[key]);
                }
            }

            this._updateMoreLinkClickFunction(options);
            this._updateMoreLinkClassNames(options);
            this._updateEventOverlap(options);
            this._updateEventConstraint(options);
            this._updateBusinessHours(options);
            this._updateSelectOverlap(options);
            this._updateSelectConstraint(options);
            this._updateSelectAllow(options);
        });
    }

    updateOption(key, value) {
        this.calendar.setOption(key, value);
    }

    _skipOption(key) {
        return MORE_LINK_CLICK_FUNCTION === key
            || MORE_LINK_CLASS_NAMES === key
            || EVENT_OVERLAP === key
            || EVENT_CONSTRAINT === key
            || BUSINESS_HOURS === key
            || SELECT_OVERLAP === key
            || SELECT_CONSTRAINT === key
            || SELECT_ALLOW === key
    }

    _updateMoreLinkClickFunction(options) {
        const moreLinkClickFunction = options[MORE_LINK_CLICK_FUNCTION]
        if (moreLinkClickFunction) {
            this.updateOption('moreLinkClick', this._onMoreLinkClick.bind(this));
        }
    }

    _updateMoreLinkClassNames(options) {
        const moreLinkClassNames = options[MORE_LINK_CLASS_NAMES];
        if (!moreLinkClassNames) {
            return;
        }
        if (moreLinkClassNames.hasOwnProperty('function') && moreLinkClassNames['function']) {
            this.updateOption(MORE_LINK_CLASS_NAMES, this._onMoreLinkClassNames.bind(this));
        } else if (moreLinkClassNames.hasOwnProperty('classNames')) {
            this.updateOption(MORE_LINK_CLASS_NAMES, moreLinkClassNames['classNames']);
        }
    }

    _updateEventOverlap(options) {
        const eventOverlap = options[EVENT_OVERLAP];
        if (!eventOverlap) {
            return;
        }
        if (eventOverlap.hasOwnProperty('jsFunction') && eventOverlap['jsFunction']) {
            const jsFunction = this._parseJavaScriptFunction(eventOverlap['jsFunction']);
            if (jsFunction) {
                this.updateOption(EVENT_OVERLAP, jsFunction);
            }
        } else if (eventOverlap.hasOwnProperty('enabled')) {
            this.updateOption(EVENT_OVERLAP, eventOverlap['enabled']);
        }
    }

    _updateEventConstraint(options) {
        const eventConstraint = options[EVENT_CONSTRAINT];
        if (!eventConstraint) {
            return;
        }
        if (eventConstraint.hasOwnProperty('businessHours') && eventConstraint['businessHours']) {
            this.updateOption(EVENT_CONSTRAINT, eventConstraint['businessHours']);
        } else if (eventConstraint.hasOwnProperty('groupId') && eventConstraint['groupId']) {
            this.updateOption(EVENT_CONSTRAINT, eventConstraint['groupId']);
        } else if (eventConstraint.hasOwnProperty('enabled')) {
            this.updateOption(EVENT_CONSTRAINT, eventConstraint['enabled'] ? "businessHours" : undefined);
        }
    }

    _updateBusinessHours(options) {
        const businessHours = options[BUSINESS_HOURS];
        if (!businessHours) {
            return;
        }
        if (businessHours.hasOwnProperty('businessHours') && businessHours['businessHours']) {
            this.updateOption(BUSINESS_HOURS, businessHours['businessHours']);
        } else if (businessHours.hasOwnProperty('enabled')) {
            this.updateOption(BUSINESS_HOURS, businessHours['enabled']);
        }
    }

    _updateSelectOverlap(options) {
        const selectOverlap = options[SELECT_OVERLAP];
        if (!selectOverlap) {
            return;
        }
        if (selectOverlap.hasOwnProperty("jsFunction") && selectOverlap['jsFunction']) {
            const jsFunction = this._parseJavaScriptFunction(selectOverlap['jsFunction']);
            if (jsFunction) {
                this.updateOption(SELECT_OVERLAP, jsFunction);
            }
        } else if (selectOverlap.hasOwnProperty('enabled')) {
            this.updateOption(SELECT_OVERLAP, selectOverlap['enabled']);
        }
    }

    _updateSelectConstraint(options) {
        const selectConstraint = options[SELECT_CONSTRAINT];
        if (!selectConstraint) {
            return;
        }
        if (selectConstraint.hasOwnProperty('businessHours') && selectConstraint['businessHours']) {
            this.updateOption(SELECT_CONSTRAINT, selectConstraint['businessHours']);
        } else if (selectConstraint.hasOwnProperty('groupId') && selectConstraint['groupId']) {
            this.updateOption(SELECT_CONSTRAINT, selectConstraint['groupId']);
        } else if (selectConstraint.hasOwnProperty('enabled')) {
            this.updateOption(SELECT_CONSTRAINT, selectConstraint['enabled'] ? "businessHours" : undefined);
        }
    }

    _updateSelectAllow(options) {
        const selectAllow = options[SELECT_ALLOW];
        if (selectAllow === undefined) {
            return;
        }
        if (selectAllow === null) {
            this.updateOption(SELECT_ALLOW, selectAllow);
        } else {
            const jsFunction = this._parseJavaScriptFunction(selectAllow);
            if (jsFunction) {
                this.updateOption(SELECT_ALLOW, jsFunction);
            }
        }
    }

    addListener(optionName, listener) {
        if (!this.listeners) {
            this.listeners = {};
        }
        if (!this.listeners[optionName]) {
            this.listeners[optionName] = [];
        }
        this.listeners[optionName].push(listener);
    }

    _onMoreLinkClick(e) {
        if (this.listeners[MORE_LINK_CLICK_FUNCTION]) {
            this.listeners[MORE_LINK_CLICK_FUNCTION].forEach((listener) => listener(e));
        }
        return NO_VIEW_MORE_LINK_CLICK;
    }

    _onMoreLinkClassNames(e) {
        if (this.listeners[MORE_LINK_CLASS_NAMES]) {
            this.listeners[MORE_LINK_CLASS_NAMES].forEach((listener) => listener(e));
        }
        return [];
    }

    _parseJavaScriptFunction(stringFunction) {
        let startArgsIndex = stringFunction.indexOf('(');
        let endArgsIndex = stringFunction.indexOf(')');

        let startBodyIndex = stringFunction.indexOf('{');
        let endBodyIndex = stringFunction.lastIndexOf('}');

        if (startArgsIndex === -1 || endArgsIndex === -1 || startBodyIndex === -1 || endBodyIndex === -1) {
            console.warn('Unparsable native JavaScript function: ' + stringFunction);
            return;
        }

        let args = stringFunction.slice(startArgsIndex + 1, endArgsIndex)
            .split(',')
            .map(element => element.trim());
        let body = stringFunction.slice(startBodyIndex + 1, endBodyIndex).trim();

        return new Function(args, body);
    }
}

export default Options;