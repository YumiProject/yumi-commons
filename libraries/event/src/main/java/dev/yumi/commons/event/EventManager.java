/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *   Copyright 2016, 2017, 2018, 2019 FabricMC
 *   Copyright 2021 The Quilt Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.yumi.commons.event;

import dev.yumi.commons.YumiAssertions;
import dev.yumi.commons.event.invoker.DefaultInvokerFactory;
import dev.yumi.commons.event.invoker.InvokerFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents an {@link Event} manager.
 * <p>
 * An event manager allows the creation of new {@link Event} instances which share the same default phase identifier.
 *
 * @param <I> the phase identifier type
 */
public final class EventManager<I extends Comparable<? super I>> {
	private final I defaultPhaseId;

	public EventManager(@NotNull I defaultPhaseId) {
		this.defaultPhaseId = defaultPhaseId;
	}

	/**
	 * Creates a new instance of {@link Event}.
	 *
	 * @param invokerFactory the factory which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 * @see #create(Class)
	 * @see #create(Class, Function)
	 * @see #create(Class, Object, Function)
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 * @see #createWithPhases(Class, Comparable[])
	 * @see #createWithPhases(Class, Function, Comparable[])
	 */
	public <T> @NotNull Event<I, T> create(@NotNull InvokerFactory<T> invokerFactory) {
		return this.create(invokerFactory.type(), invokerFactory);
	}

	/**
	 * Creates a new instance of {@link Event} for which the invoker implementation is automatically generated.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 * @see #create(InvokerFactory)
	 * @see #create(Class, Function)
	 * @see #create(Class, Object, Function)
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 * @see #createWithPhases(Class, Comparable[])
	 * @see #createWithPhases(Class, Function, Comparable[])
	 */
	public <T> @NotNull Event<I, T> create(@NotNull Class<? super T> type) {
		return this.create(type, new DefaultInvokerFactory<>(type));
	}

	/**
	 * Creates a new instance of {@link Event}.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 * @see #create(Class)
	 * @see #create(InvokerFactory)
	 * @see #create(Class, Object, Function)
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 * @see #createWithPhases(Class, Comparable[])
	 * @see #createWithPhases(Class, Function, Comparable[])
	 */
	public <T> @NotNull Event<I, T> create(@NotNull Class<? super T> type, @NotNull Function<T[], T> implementation) {
		return new Event<>(type, this.defaultPhaseId, implementation);
	}

	/**
	 * Creates a new instance of {@link Event}.
	 * <p>
	 * This method adds a {@code emptyImplementation} parameter which provides an implementation of the invoker
	 * when no listeners are registered. Generally this method should only be used when the code path is very hot, such
	 * as the render or tick loops. Otherwise, the other {@link #create(Class, Function)} method should work
	 * in 99% of cases with little to no performance overhead.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param emptyImplementation the implementation of T to use when the array event has no listener registrations
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 * @see #create(Class)
	 * @see #create(InvokerFactory)
	 * @see #create(Class, Function)
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 * @see #createWithPhases(Class, Comparable[])
	 * @see #createWithPhases(Class, Function, Comparable[])
	 */
	public <T> @NotNull Event<I, T> create(
			@NotNull Class<? super T> type,
			@NotNull T emptyImplementation,
			@NotNull Function<T[], T> implementation
	) {
		return this.create(type, listeners -> switch (listeners.length) {
			case 0 -> emptyImplementation;
			case 1 -> listeners[0];
			// We can ensure the implementation may not remove elements from the backing array since the array given to
			// this method is a copy of the backing array.
			default -> implementation.apply(listeners);
		});
	}

	/**
	 * Create a new instance of {@link Event} with a list of default phases that get invoked in order.
	 * Exposing the identifiers of the default phases as {@code public static final} constants is encouraged.
	 * <p>
	 * An event phase is a named group of listeners, which may be ordered before or after other groups of listeners.
	 * This allows some listeners to take priority over other listeners.
	 * Adding separate events should be considered before making use of multiple event phases.
	 * <p>
	 * Phases may be freely added to events created with any of the factory functions,
	 * however using this function is preferred for widely used event phases.
	 * If more phases are necessary, discussion with the author of the event is encouraged.
	 * <p>
	 * Refer to {@link Event#addPhaseOrdering} for an explanation of event phases.
	 *
	 * @param invokerFactory the factory which generates an invoker implementation using an array of listeners
	 * @param defaultPhases the default phases of this event, in the correct order.
	 * Must contain {@link EventManager#getDefaultPhaseId() the default phase identifier}
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 * @see #create(Class)
	 * @see #create(InvokerFactory)
	 * @see #create(Class, Function)
	 * @see #create(Class, Object, Function)
	 * @see #createWithPhases(Class, Comparable[])
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 */
	@SuppressWarnings("unchecked")
	public <T> @NotNull Event<I, T> createWithPhases(
			@NotNull InvokerFactory<T> invokerFactory,
			@NotNull I... defaultPhases
	) {
		return this.createWithPhases(invokerFactory.type(), invokerFactory, defaultPhases);
	}

	/**
	 * Create a new instance of {@link Event} with a list of default phases that get invoked in order.
	 * Exposing the identifiers of the default phases as {@code public static final} constants is encouraged.
	 * <p>
	 * An event phase is a named group of listeners, which may be ordered before or after other groups of listeners.
	 * This allows some listeners to take priority over other listeners.
	 * Adding separate events should be considered before making use of multiple event phases.
	 * <p>
	 * Phases may be freely added to events created with any of the factory functions,
	 * however using this function is preferred for widely used event phases.
	 * If more phases are necessary, discussion with the author of the event is encouraged.
	 * <p>
	 * Refer to {@link Event#addPhaseOrdering} for an explanation of event phases.
	 * <p>
	 * This method uses an automatically generated invoker implementation.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param defaultPhases the default phases of this event, in the correct order.
	 * Must contain {@link EventManager#getDefaultPhaseId() the default phase identifier}
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 * @see #create(Class)
	 * @see #create(InvokerFactory)
	 * @see #create(Class, Function)
	 * @see #create(Class, Object, Function)
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 * @see #createWithPhases(Class, Function, Comparable[])
	 */
	@SuppressWarnings("unchecked")
	public <T> @NotNull Event<I, T> createWithPhases(
			@NotNull Class<? super T> type,
			@NotNull I... defaultPhases
	) {
		return this.createWithPhases(type, new DefaultInvokerFactory<>(type), defaultPhases);
	}

	/**
	 * Create a new instance of {@link Event} with a list of default phases that get invoked in order.
	 * Exposing the identifiers of the default phases as {@code public static final} constants is encouraged.
	 * <p>
	 * An event phase is a named group of listeners, which may be ordered before or after other groups of listeners.
	 * This allows some listeners to take priority over other listeners.
	 * Adding separate events should be considered before making use of multiple event phases.
	 * <p>
	 * Phases may be freely added to events created with any of the factory functions,
	 * however using this function is preferred for widely used event phases.
	 * If more phases are necessary, discussion with the author of the event is encouraged.
	 * <p>
	 * Refer to {@link Event#addPhaseOrdering} for an explanation of event phases.
	 *
	 * @param type the class representing the type of the invoker that is executed by the event
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param defaultPhases the default phases of this event, in the correct order.
	 * Must contain {@link EventManager#getDefaultPhaseId() the default phase identifier}
	 * @param <T> the type of the invoker executed by the event
	 * @return a new event instance
	 * @see #create(Class)
	 * @see #create(InvokerFactory)
	 * @see #create(Class, Function)
	 * @see #create(Class, Object, Function)
	 * @see #createWithPhases(Class, Comparable[])
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 */
	@SafeVarargs
	public final <T> @NotNull Event<I, T> createWithPhases(
			@NotNull Class<? super T> type,
			@NotNull Function<T[], T> implementation,
			@NotNull I... defaultPhases
	) {
		this.ensureContainsDefaultPhase(defaultPhases);
		YumiAssertions.ensureNoDuplicates(defaultPhases, id -> new IllegalArgumentException("Duplicate event phase: " + id));

		var event = this.create(type, implementation);

		for (int i = 1; i < defaultPhases.length; ++i) {
			event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
		}

		return event;
	}

	/**
	 * {@return the default phase identifier of all the events created by this event manager}
	 */
	@Contract(pure = true)
	public @NotNull I getDefaultPhaseId() {
		return this.defaultPhaseId;
	}

	private void ensureContainsDefaultPhase(I[] defaultPhases) {
		for (var id : defaultPhases) {
			if (id.equals(this.defaultPhaseId)) {
				return;
			}
		}

		throw new IllegalArgumentException("The event phases must contain Event.DEFAULT_PHASE.");
	}
}
