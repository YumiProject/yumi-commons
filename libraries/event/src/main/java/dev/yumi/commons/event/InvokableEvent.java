/*
 * Copyright 2024 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface InvokableEvent<T> {
	/**
	 * {@return the invoker instance used to execute this event}
	 * <p>
	 * The result of this method should not be stored since the invoker may become invalid
	 * at any time. Always call this method when you intend to execute an event.
	 */
	@Contract(pure = true)
	@NotNull T invoker();
}