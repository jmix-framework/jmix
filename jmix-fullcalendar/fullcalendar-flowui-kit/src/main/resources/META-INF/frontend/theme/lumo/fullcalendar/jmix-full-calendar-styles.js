/*
 * Copyright 2024 Haulmont.
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

const jmixFullCalendarStyle = document.createElement("style");
jmixFullCalendarStyle.textContent = `
    jmix-full-calendar {
       --fc-small-font-size: var(--lumo-font-size-s);
       --fc-page-bg-color: var(--lumo-base-color);
       --fc-neutral-bg-color: var(--lumo-contrast-10pct);
       --fc-neutral-text-color: var(--lumo-secondary-text-color);
       --fc-border-color: var(--lumo-contrast-20pct);

       --fc-event-bg-color: var(--lumo-primary-color-50pct);
       --fc-event-border-color: var(--lumo-primary-color-50pct);
       --fc-event-text-color: var(--lumo-primary-contrast-color);
       --fc-event-selected-overlay-color: var(--lumo-primary-color-10pct);;
       
       /* TimeGrid events stack "show more" background-color */
       --fc-more-link-bg-color: #a6a6a6;
       --fc-more-link-text-color: inherit;                     /* default */
       
       --fc-event-resizer-thickness: 8px;                      /* default */
       --fc-event-resizer-dot-total-width: 8px;                /* default */
       --fc-event-resizer-dot-border-width: 1px;               /* default */
       
       --fc-non-business-color: var(--lumo-contrast-5pct);
       --fc-bg-event-color: rgb(143, 223, 130);                /* default */
       --fc-bg-event-opacity: 0.3;                             /* default */
       --fc-highlight-color: var(--lumo-primary-color-10pct);
       --fc-today-bg-color: rgba(255, 220, 40, 0.15);          /* default */
       --fc-now-indicator-color: red;                          /* default */
    }

    jmix-full-calendar .fc-event {
        border-radius: var(--lumo-border-radius-m);
        padding-left: var(--lumo-space-xs);
        padding-right: var(--lumo-space-xs);
    }

    jmix-full-calendar .fc-event:focus-visible {
        border-radius: var(--lumo-border-radius-m);
        box-shadow: 0 0 0 var(--vaadin-focus-ring-width, 2px) var(--vaadin-focus-ring-color, var(--lumo-primary-color-50pct));
        outline: 0;
    }
    
    jmix-full-calendar .fc .fc-highlight {
        background-image: linear-gradient(var(--fc-highlight-color), var(--fc-highlight-color));
        background-repeat: repeat;
        box-shadow: 0 1px 0 0 var(--fc-highlight-color);
    }
    
    jmix-full-calendar .fc-more-link:hover {
        background-color: var(--lumo-contrast-10pct);
    }
    
    jmix-full-calendar .fc-daygrid-day-number:focus-visible,
    jmix-full-calendar .fc-daygrid-week-number:focus-visible {
        border-radius: var(--lumo-border-radius-m);
        box-shadow: 0 0 0 var(--vaadin-focus-ring-width, 2px) var(--vaadin-focus-ring-color, var(--lumo-primary-color-50pct));
        outline: 0;
    }
    
    jmix-full-calendar .fc-timegrid-event:hover,
    jmix-full-calendar .fc-daygrid-block-event:hover {
        cursor: var(--lumo-clickable-cursor);
        filter: brightness(0.9) contrast(1.2);
    }
    
    jmix-full-calendar .fc-daygrid-dot-event.fc-event-mirror,
    jmix-full-calendar .fc-daygrid-dot-event:hover {
        cursor: var(--lumo-clickable-cursor);
        background-color: var(--lumo-contrast-5pct);
    }
    
    /* More link in events stack in time slots */
    jmix-full-calendar .fc-timegrid-col-events .fc-more-link:hover {
        filter: brightness(0.9) contrast(1.2);
        background-color: var(--fc-more-link-bg-color);
    }
    
    jmix-full-calendar .jmix-day-cell-bottom-text {
        padding: var(--lumo-space-xs);
        cursor: var(--lumo-clickable-cursor);
    }
    
    jmix-full-calendar .fc-daygrid-day.jmix-has-bottom-text {
        position: relative;
    }
`;

document.head.append(jmixFullCalendarStyle);