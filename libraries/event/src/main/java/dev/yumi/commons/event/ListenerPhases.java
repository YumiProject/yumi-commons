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
 * Represents the container type of {@link ListenerPhase} to make it repeatable.
 *
 * @see ListenerPhase
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ListenerPhases {
	ListenerPhase[] value();
}
