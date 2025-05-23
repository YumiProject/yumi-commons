/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * <h2>The Event APIs</h2>
 * <p>
 * Events are used to represents specific points in a program which may need other softwares to listen and call code when
 * those points are reached.
 * <p>
 * Said events are represented using the {@link dev.yumi.commons.event.Event Event} object which stores its listeners,
 * and events are created and managed with the help of an {@link dev.yumi.commons.event.EventManager event manager}.
 *
 * @see dev.yumi.commons.event.Event
 * @see dev.yumi.commons.event.FilteredEvent
 * @see dev.yumi.commons.event.EventManager
 * @see dev.yumi.commons.event.InvokableEvent
 * @see dev.yumi.commons.event.ListenableEvent
 */

package dev.yumi.commons.event;
