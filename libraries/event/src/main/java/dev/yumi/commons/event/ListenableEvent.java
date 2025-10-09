/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event;

import module org.jetbrains.annotations;

/**
 * Represents an event or event-like object which can be listened.
 *
 * @param <I> the phase identifier type
 * @param <T> the type of the listeners
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 *
 * @see Event
 * @see FilteredEvent
 */
public interface ListenableEvent<I extends Comparable<? super I>, T> {
	/**
	 * {@return the default phase identifier of this event}
	 */
	@Contract(pure = true)
	@NotNull
	I defaultPhaseId();

	/**
	 * Registers a listener to this event.
	 *
	 * @param listener the listener to register
	 * @see #register(Comparable, Object)
	 */
	default void register(@NotNull T listener) {
		this.register(this.defaultPhaseId(), listener);
	}

	/**
	 * Registers a listener to this event for a specific phase.
	 *
	 * @param phaseIdentifier the identifier of the phase to register the listener in
	 * @param listener the listener to register
	 * @see #register(Object)
	 */
	void register(@NotNull I phaseIdentifier, @NotNull T listener);

	/**
	 * Adds new phase ordering constraints to this event for one phase be executed before the listeners of another phase.
	 * <p>
	 * Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * @param firstPhase the identifier of the phase that should run before the given second phase
	 * @param secondPhase the identifier of the phase that should run after the given first phase
	 * @see #register(Comparable, Object) register a listener with a phase
	 */
	void addPhaseOrdering(@NotNull I firstPhase, @NotNull I secondPhase);
}
