/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import org.jspecify.annotations.NullMarked;

/// ## The Yumi Commons Event Framework
///
/// Events are used to represent specific points in a program which may need other pieces of software to listen and call code when
/// those points are reached.
///
/// Said events are represented using the [Event][dev.yumi.commons.event.Event] object which stores its listeners,
/// and events are created and managed with the help of an [event manager][dev.yumi.commons.event.EventManager].
@NullMarked
module dev.yumi.commons.event {
	requires transitive dev.yumi.commons.core;
	requires dev.yumi.commons.collections;
	requires org.jspecify;

	exports dev.yumi.commons.event;
	exports dev.yumi.commons.event.invoker;
}
