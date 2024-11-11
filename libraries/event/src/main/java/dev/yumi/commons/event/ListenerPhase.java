/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event;

import java.lang.annotation.*;

/**
 * Annotates a specific callback in a listener a specific phase to listen.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @see Event#addPhaseOrdering(Comparable, Comparable)
 * @see Event#register(Comparable, Object)
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ListenerPhases.class)
public @interface ListenerPhase {
	/**
	 * {@return the targeted callback interface}
	 */
	Class<?> callbackTarget();

	/**
	 * {@return the identifier of the phase to listen}
	 */
	String value();
}
