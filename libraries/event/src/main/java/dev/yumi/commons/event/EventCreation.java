/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event;

import module org.jetbrains.annotations;

/**
 * Represents the event creation listener interface.
 *
 * @param <I> the type of phase identifier
 * @author LambdAurora
 * @version 1.0.0
 * @see EventManager#getCreationEvent()
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventCreation<I extends Comparable<? super I>> {
	/**
	 * Called when a new event is created using the given event manager.
	 *
	 * @param manager the event manager used to create the event
	 * @param event the event that has been created
	 */
	void onEventCreation(@NotNull EventManager<I> manager, @NotNull Event<I, ?> event);
}