import * as calendarUtils from "./jmix-full-calendar-utils";

const NO_VIEW_MORE_LINK_CLICK = 'NO_VIEW';

export const MORE_LINK_CLICK = 'moreLinkClick';
export const MORE_LINK_CLASS_NAMES = 'moreLinkClassNames';
export const UNSELECT_CANCEL = 'unselectCancel';
export const DAY_HEADER_CLASS_NAMES = 'dayHeaderClassNames';
const EVENT_OVERLAP = 'eventOverlap';
const SELECT_OVERLAP = 'selectOverlap';
const EVENT_CONSTRAINT = 'eventConstraint';
const BUSINESS_HOURS = 'businessHours';
const SELECT_CONSTRAINT = 'selectConstraint';
const SELECT_ALLOW = 'selectAllow';
const VIEWS = 'views';
const DAY_MAX_EVENT_ROWS = 'dayMaxEventRows';
const DAY_MAX_EVENTS = 'dayMaxEvents';

export function processInitialOptions(serverOptions) {
    const options = serverOptions;
    if (!serverOptions) {
        return {};
    }

    const views = serverOptions[VIEWS];
    if (views) {
        options[VIEWS] = processViews(views);
    }
    const unselectCancel = serverOptions[UNSELECT_CANCEL];
    if (!unselectCancel) {
        delete options[UNSELECT_CANCEL];
    }

    for (const property in options) {
        const value = options[property];
        if (value === null || value === undefined) {
            delete options[property];
        }
    }

    return options;
}

function processViews(viewsObject) {
    for (let view in viewsObject) {
        if (view === 'customViews') {
            continue;
        }
        viewsObject[view] = {...viewsObject[view], ...viewsObject[view].properties && {...viewsObject[view].properties}};
        viewsObject[view] = calendarUtils.deleteNullProperties(viewsObject[view]);
        delete viewsObject[view].properties;
    }

    if (viewsObject.customViews) {
        for (const view of viewsObject.customViews) {
            viewsObject[view.calendarView] = {
                type: view.type,
                ...(view.dayCount) && {dayCount: view.dayCount},
                ...(view.duration) && {duration: view.duration},
                ...view.properties && {...view.properties}
            };
        }
    }
    delete viewsObject.customViews;

    return viewsObject;
}

class Options {

    constructor(calendar, context) {
        this.calendar = calendar;
        this.context = context;
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

            this._updateMoreLinkClick(options);
            this._updateMoreLinkClassNames(options);
            this._updateEventOverlap(options);
            this._updateEventConstraint(options);
            this._updateBusinessHours(options);
            this._updateSelectOverlap(options);
            this._updateSelectConstraint(options);
            this._updateSelectAllow(options);

            this._updateDayMaxEventRows(options);
            this._updateDayMaxEvents(options);

            this._updateDayHeaderClassNames(options);
        });
    }

    updateOption(key, value) {
        this.calendar.setOption(key, value);
    }

    _skipOption(key) {
        return MORE_LINK_CLICK === key
            || MORE_LINK_CLASS_NAMES === key
            || DAY_HEADER_CLASS_NAMES === key
            || EVENT_OVERLAP === key
            || EVENT_CONSTRAINT === key
            || BUSINESS_HOURS === key
            || SELECT_OVERLAP === key
            || SELECT_CONSTRAINT === key
            || SELECT_ALLOW === key
            || DAY_MAX_EVENT_ROWS === key
            || DAY_MAX_EVENTS === key
    }

    _updateMoreLinkClick(options) {
        const moreLinkClick = options[MORE_LINK_CLICK]

        if (moreLinkClick) {
            this.updateOption('moreLinkClick', moreLinkClick.calendarView);

            if (moreLinkClick.functionEnabled) {
                this.updateOption('moreLinkClick', this._onMoreLinkClick.bind(this));
            }
        }
    }

    _updateMoreLinkClassNames(options) {
        const moreLinkClassNames = options[MORE_LINK_CLASS_NAMES];

        if (moreLinkClassNames) {
            this.updateOption(MORE_LINK_CLASS_NAMES, moreLinkClassNames.classNames);

            if (moreLinkClassNames.functionEnabled) {
                this.updateOption(MORE_LINK_CLASS_NAMES, this._onMoreLinkClassNames.bind(this));
            }
        }
    }

    _updateEventOverlap(options) {
        const eventOverlap = options[EVENT_OVERLAP];

        if (eventOverlap) {
            this.updateOption(EVENT_OVERLAP, eventOverlap.enabled);

            if (eventOverlap.jsFunction) {
                const jsFunction = calendarUtils.parseJavaScriptFunction(eventOverlap['jsFunction']);
                if (jsFunction) {
                    this.updateOption(EVENT_OVERLAP, jsFunction);
                }
            }
        }
    }

    _updateEventConstraint(options) {
        const eventConstraint = options[EVENT_CONSTRAINT];

        if (eventConstraint) {
            const bHours = eventConstraint.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                this.updateOption(EVENT_CONSTRAINT, bHours);
                return;
            }
            if (eventConstraint.groupId) {
                this.updateOption(EVENT_CONSTRAINT, eventConstraint.groupId);
                return;
            }

            this.updateOption(EVENT_CONSTRAINT, eventConstraint.enabled ? "businessHours" : undefined);
        }
    }

    _updateBusinessHours(options) {
        const businessHours = options[BUSINESS_HOURS];

        if (businessHours) {
            this.updateOption(BUSINESS_HOURS, businessHours.enabled);

            const bHours = businessHours.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                this.updateOption(BUSINESS_HOURS, bHours);
            }
        }
    }

    _updateSelectOverlap(options) {
        const selectOverlap = options[SELECT_OVERLAP];

        if (selectOverlap) {
            this.updateOption(SELECT_OVERLAP, selectOverlap.enabled);

            if (selectOverlap.jsFunction) {
                const jsFunction = calendarUtils.parseJavaScriptFunction(selectOverlap.jsFunction);
                if (jsFunction) {
                    this.updateOption(SELECT_OVERLAP, jsFunction);
                }
            }
        }
    }

    _updateSelectConstraint(options) {
        const selectConstraint = options[SELECT_CONSTRAINT];

        if (selectConstraint) {
            const bHours = selectConstraint.businessHours;
            if (bHours && (Array.isArray(bHours) && bHours.length > 0)) {
                this.updateOption(SELECT_CONSTRAINT, bHours);
                return;
            }
            if (selectConstraint.groupId) {
                this.updateOption(SELECT_CONSTRAINT, selectConstraint.groupId);
                return;
            }

            this.updateOption(SELECT_CONSTRAINT, selectConstraint.enabled ? "businessHours" : undefined);
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
            const jsFunction = calendarUtils.parseJavaScriptFunction(selectAllow);
            if (jsFunction) {
                this.updateOption(SELECT_ALLOW, jsFunction);
            }
        }
    }

    _updateDayMaxEventRows(options) {
        const dayMaxEventRows = options[DAY_MAX_EVENT_ROWS];

        if (dayMaxEventRows) {
            this.updateOption(DAY_MAX_EVENT_ROWS, dayMaxEventRows.limited);

            if (dayMaxEventRows.max) {
                this.updateOption(DAY_MAX_EVENT_ROWS, dayMaxEventRows.max);
            }
        }
    }

    _updateDayMaxEvents(options) {
        const dayMaxEvents = options[DAY_MAX_EVENTS];

        if (dayMaxEvents) {
            this.updateOption(DAY_MAX_EVENTS, dayMaxEvents.limited);

            if (dayMaxEvents.max) {
                this.updateOption(DAY_MAX_EVENTS, dayMaxEvents.max);
            }
        }
    }

    _updateDayHeaderClassNames(options) {
        const dayHeaderClassNames = options[DAY_HEADER_CLASS_NAMES];

        if (calendarUtils.isNotNullUndefined(dayHeaderClassNames)) {
            this.updateOption(DAY_HEADER_CLASS_NAMES, dayHeaderClassNames ? this._onDayHeaderClassNames.bind(this) : null);
        }
    }

    _onMoreLinkClick(e) {
        if (this.listeners[MORE_LINK_CLICK]) {
            this.listeners[MORE_LINK_CLICK].forEach((listener) => listener(e));
        }
        return NO_VIEW_MORE_LINK_CLICK;
    }

    _onMoreLinkClassNames(e) {
        if (this.listeners[MORE_LINK_CLASS_NAMES]) {
            this.listeners[MORE_LINK_CLASS_NAMES].forEach((listener) => listener(e));
        }
        return [];
    }

    _onDayHeaderClassNames(e) {
        if (this.listeners[DAY_HEADER_CLASS_NAMES]) {
            this.listeners[DAY_HEADER_CLASS_NAMES].forEach((listener) => listener(e));
        }
        return [];
    }
}

export default Options;