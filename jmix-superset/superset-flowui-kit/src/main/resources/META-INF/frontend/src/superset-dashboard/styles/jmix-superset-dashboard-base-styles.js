/*
 * Copyright 2026 Haulmont.
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

import { css } from 'lit';

export const jmixSupersetDashboardStyles = css`

  #dashboard {
    width: 100%;
    height: 100%;
    background-color: #f7f7f7;
  }

  #dashboard iframe {
    border: none;
    width: 100%;
    height: 100%;
  }

  #stub-image-container {
    align-items: center;
    display: flex;
    justify-content: center;
    height: 100%;
  }

  #stub-image-container img {
    width: 50px;
  }
`;