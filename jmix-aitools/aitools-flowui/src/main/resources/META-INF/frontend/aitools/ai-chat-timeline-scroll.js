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

/*
 * Keeps the AI chat timeline (a JmixVirtualList) pinned to the newest message
 * while an answer arrives, and stops as soon as the user scrolls up.
 *
 * @param {HTMLElement} context the timeline list element (the scroller)
 * @param {boolean}     force   reset "stick to bottom" and pin regardless of position
 */
export function stickToBottom(context, force) {
  const AT_BOTTOM = 2;

  if (!context.__stickInit) {
    context.__stickInit = true;
    context.__stick = true;

    // 'scroll' only re-engages sticking, once the user is back at the very
    // bottom; it never detaches. The event is coalesced to a later frame, so a
    // re-pin (the observer / rAF loop below) firing in the gap would beat a
    // detach decided here. Detach is driven synchronously from the gesture
    // itself, below.
    context.addEventListener('scroll', () => {
      if (context.scrollTop + context.clientHeight >= context.scrollHeight - AT_BOTTOM) {
        context.__stick = true;
      }
    }, { passive: true });

    // A wheel/trackpad scroll up, or a touch drag away from the bottom, stops
    // sticking immediately and distance-blind (even a few ticks near the
    // bottom), so the bounded window of post-answer re-pinning below cannot
    // pull a reading user back down.
    context.addEventListener('wheel', (e) => {
      if (e.deltaY < 0) { context.__stick = false; }
    }, { passive: true });
    context.addEventListener('touchmove', () => {
      if (context.scrollTop + context.clientHeight < context.scrollHeight - AT_BOTTOM) {
        context.__stick = false;
      }
    }, { passive: true });
  }

  if (force) {
    context.__stick = true;
  }
  if (!context.__stick) {
    return;
  }

  // Rows have variable height and assistant answers render through
  // <vaadin-markdown>, which lays its content out asynchronously in bursts after
  // the row's initial render. A one-shot pin lands short; instead a
  // MutationObserver re-pins on every DOM change and a bounded rAF loop re-pins
  // each frame for a ~2s window, then disconnects. The per-element stop handle
  // cancels an in-flight pin when a new request arrives so loops don't stack.
  if (context.__scrollPinStop) {
    context.__scrollPinStop();
  }

  const toBottom = () => {
    if (context.__stick) {
      context.scrollTop = context.scrollHeight;
    }
  };
  const mo = new MutationObserver(toBottom);
  mo.observe(context, { childList: true, subtree: true, characterData: true, attributes: true });

  const stop = () => {
    context.__scrollPinStop = null;
    mo.disconnect();
  };
  context.__scrollPinStop = stop;

  let frames = 0;

  const tick = () => {
    if (context.__scrollPinStop !== stop) {
      return;
    }
    if (!context.__stick) {
      stop();
      return;
    }
    toBottom();
    if (++frames < 120) {
      requestAnimationFrame(tick);
    } else {
      stop();
    }
  };
  requestAnimationFrame(tick);
}

window.jmixAiTools = window.jmixAiTools || {};
window.jmixAiTools.stickToBottom = stickToBottom;
