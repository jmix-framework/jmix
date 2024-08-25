
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
       --fc-small-font-size: var(--material-small-font-size);
       --fc-page-bg-color: var(--material-background-color);
       --fc-neutral-bg-color: var(--material-secondary-background-color);
       --fc-neutral-text-color: var(--material-secondary-text-color);
       --fc-border-color: var(--material-divider-color);

       --fc-event-bg-color: var(--material-primary-color);
       --fc-event-border-color: var(--material-primary-color);
       --fc-event-text-color: var(--material-primary-contrast-color);
       --fc-event-selected-overlay-color: var(--material-primary-color);
       
       /* TimeGrid events stack "show more" background-color */
       --fc-more-link-bg-color: #d0d0d0;                       /* default */
       --fc-more-link-text-color: inherit;                     /* default */
       
       --fc-event-resizer-thickness: 8px;                      /* default */
       --fc-event-resizer-dot-total-width: 8px;                /* default */
       --fc-event-resizer-dot-border-width: 1px;               /* default */
       
       --fc-non-business-color: var(--material-secondary-background-color);
       --fc-bg-event-color: rgb(143, 223, 130);                /* default */
       --fc-bg-event-opacity: 0.3;                             /* default */
       --fc-highlight-color: rgba(188, 232, 241, 0.3);         /* default */
       --fc-today-bg-color: rgba(255, 220, 40, 0.15);          /* default */
       --fc-now-indicator-color: red;                          /* default */
    }
`;

document.head.append(jmixFullCalendarStyle);