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
import dev.yumi.commons.event.invoker.SequenceInvokerFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents an {@link Event} manager.
 * <p>
 * An event manager allows the creation of new {@link Event} instances which share the same default phase identifier.
 *
 * @param <I> the phase identifier type
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class EventManager<I extends Comparable<? super I>> {
	private final I defaultPhaseId;
	private final Function<String, I> phaseIdParser;
	private final Event<I, EventCreation<I>> creationEvent;

	public EventManager(@NotNull I defaultPhaseId, @NotNull Function<String, I> phaseIdParser) {
		this.defaultPhaseId = defaultPhaseId;
		this.phaseIdParser = phaseIdParser;
		this.creationEvent = new Event<>(EventCreation.class, defaultPhaseId, new SequenceInvokerFactory<>(EventCreation.class));
	}

	/**
	 * Creates a new instance of {@link Event}.
	 *
	 * @param invokerFactory the factory which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the listeners of the event
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
	 * <p>
	 * The invoker implementation is automatically generated given the following conditions:
	 * <ul>
	 * 	<li>the listener doesn't return anything;</li>
	 * 	<li>the listener returns a {@code boolean} as some kind of filter;</li>
	 * 	<li>the listener returns a {@link dev.yumi.commons.TriState}, for which {@link dev.yumi.commons.TriState#DEFAULT} is the default return value,
	 * 	and whenever a listener returns another value it returns early.</li>
	 * </ul>
	 *
	 * @param type the class representing the type of the listeners of the event
	 * @param <T> the type of the listeners of the event
	 * @return a new event instance
	 * @see #create(InvokerFactory)
	 * @see #create(Class, Function)
	 * @see #create(Class, Object, Function)
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 * @see #createWithPhases(Class, Comparable[])
	 * @see #createWithPhases(Class, Function, Comparable[])
	 * @see DefaultInvokerFactory the invoker factory used for this event
	 */
	public <T> @NotNull Event<I, T> create(@NotNull Class<? super T> type) {
		return this.create(type, new DefaultInvokerFactory<>(type));
	}

	/**
	 * Creates a new instance of {@link Event}.
	 *
	 * @param type the class representing the type of the listeners of the event
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the listeners of the event
	 * @return a new event instance
	 * @see #create(Class)
	 * @see #create(InvokerFactory)
	 * @see #create(Class, Object, Function)
	 * @see #createWithPhases(InvokerFactory, Comparable[])
	 * @see #createWithPhases(Class, Comparable[])
	 * @see #createWithPhases(Class, Function, Comparable[])
	 */
	public <T> @NotNull Event<I, T> create(@NotNull Class<? super T> type, @NotNull Function<T[], T> implementation) {
		var event = new Event<>(type, this.defaultPhaseId, implementation);
		this.creationEvent.invoker().onEventCreation(this, event);
		return event;
	}

	/**
	 * Creates a new instance of {@link Event}.
	 * <p>
	 * This method adds a {@code emptyImplementation} parameter which provides an implementation of the invoker
	 * when no listeners are registered. Generally this method should only be used when the code path is very hot, such
	 * as the render or tick loops. Otherwise, the other {@link #create(Class, Function)} method should work
	 * in 99% of cases with little to no performance overhead.
	 *
	 * @param type the class representing the type of the listeners of the event
	 * @param emptyImplementation the implementation of T to use when the array event has no listener registrations
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the listeners of the event
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
	 * Must contain {@link EventManager#defaultPhaseId() the default phase identifier}
	 * @param <T> the type of the listeners of the event
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
	 * @param type the class representing the type of the listeners of the event
	 * @param defaultPhases the default phases of this event, in the correct order.
	 * Must contain {@link EventManager#defaultPhaseId() the default phase identifier}
	 * @param <T> the type of the listeners of the event
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
	 * @param type the class representing the type of the listeners of the event
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param defaultPhases the default phases of this event, in the correct order.
	 * Must contain {@link EventManager#defaultPhaseId() the default phase identifier}
	 * @param <T> the type of the listeners of the event
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
		return this.createWithPhases(() -> new Event<>(type, this.defaultPhaseId, implementation), defaultPhases);
	}

	/**
	 * Creates a new instance of {@link FilteredEvent}.
	 *
	 * @param contextType the class of the context
	 * @param invokerFactory the factory which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the listeners of the event
	 * @param <C> the type of the filtering context
	 * @return a new filtered event instance
	 * @see #createFiltered(Class, Class)
	 * @see #createFiltered(Class, Class, Function)
	 * @see #createFiltered(Class, Class, Object, Function)
	 * @see #createFilteredWithPhases(Class, InvokerFactory, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Function, Comparable[])
	 */
	public <T, C> @NotNull FilteredEvent<I, T, C> createFiltered(@NotNull Class<? super C> contextType, @NotNull InvokerFactory<T> invokerFactory) {
		return this.createFiltered(invokerFactory.type(), contextType, invokerFactory);
	}

	/**
	 * Creates a new instance of {@link FilteredEvent} for which the invoker implementation is automatically generated.
	 * <p>
	 * The invoker implementation is automatically generated given the following conditions:
	 * <ul>
	 * 	<li>the listener doesn't return anything;</li>
	 * 	<li>the listener returns a {@code boolean} as some kind of filter;</li>
	 * 	<li>the listener returns a {@link dev.yumi.commons.TriState}, for which {@link dev.yumi.commons.TriState#DEFAULT} is the default return value,
	 * 	and whenever a listener returns another value it returns early.</li>
	 * </ul>
	 *
	 * @param type the class representing the type of the listeners of the event
	 * @param contextType the class of the context
	 * @param <T> the type of the listeners of the event
	 * @param <C> the type of the filtering context
	 * @return a new filtered event instance
	 * @see #createFiltered(Class, InvokerFactory)
	 * @see #createFiltered(Class, Class, Function)
	 * @see #createFiltered(Class, Class, Object, Function)
	 * @see #createFilteredWithPhases(Class, InvokerFactory, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Function, Comparable[])
	 * @see DefaultInvokerFactory the invoker factory used for this event
	 */
	public <T, C> @NotNull FilteredEvent<I, T, C> createFiltered(@NotNull Class<? super T> type, @NotNull Class<? super C> contextType) {
		return this.createFiltered(type, contextType, new DefaultInvokerFactory<>(type));
	}

	/**
	 * Creates a new instance of {@link FilteredEvent}.
	 *
	 * @param type the class representing the type of the listeners of the event
	 * @param contextType the class of the context
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the listeners of the event
	 * @param <C> the type of the filtering context
	 * @return a new filtered event instance
	 * @see #createFiltered(Class, Class)
	 * @see #createFiltered(Class, InvokerFactory)
	 * @see #createFiltered(Class, Class, Object, Function)
	 * @see #createFilteredWithPhases(Class, InvokerFactory, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Function, Comparable[])
	 */
	public <T, C> @NotNull FilteredEvent<I, T, C> createFiltered(
			@NotNull Class<? super T> type,
			@NotNull Class<? super C> contextType,
			@NotNull Function<T[], T> implementation
	) {
		var event = new FilteredEvent<I, T, C>(type, this.defaultPhaseId, implementation);
		this.creationEvent.invoker().onEventCreation(this, event);
		return event;
	}

	/**
	 * Creates a new instance of {@link FilteredEvent}.
	 * <p>
	 * This method adds a {@code emptyImplementation} parameter which provides an implementation of the invoker
	 * when no listeners are registered. Generally this method should only be used when the code path is very hot, such
	 * as the render or tick loops. Otherwise, the other {@link #createFiltered(Class, Class, Function)} method should work
	 * in 99% of cases with little to no performance overhead.
	 *
	 * @param type the class representing the type of the listeners of the event
	 * @param contextType the class of the context
	 * @param emptyImplementation the implementation of T to use when the array event has no listener registrations
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param <T> the type of the listeners of the event
	 * @param <C> the type of the filtering context
	 * @return a new filtered event instance
	 * @see #createFiltered(Class, Class)
	 * @see #createFiltered(Class, InvokerFactory)
	 * @see #createFiltered(Class, Class, Function)
	 * @see #createFilteredWithPhases(Class, InvokerFactory, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Function, Comparable[])
	 */
	public <T, C> @NotNull FilteredEvent<I, T, C> createFiltered(
			@NotNull Class<? super T> type,
			@NotNull Class<? super C> contextType,
			@NotNull T emptyImplementation,
			@NotNull Function<T[], T> implementation
	) {
		return this.createFiltered(type, contextType, listeners -> switch (listeners.length) {
			case 0 -> emptyImplementation;
			case 1 -> listeners[0];
			// We can ensure the implementation may not remove elements from the backing array since the array given to
			// this method is a copy of the backing array.
			default -> implementation.apply(listeners);
		});
	}

	/**
	 * Create a new instance of {@link FilteredEvent} with a list of default phases that get invoked in order.
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
	 * @param contextType the class of the context
	 * @param invokerFactory the factory which generates an invoker implementation using an array of listeners
	 * @param defaultPhases the default phases of this event, in the correct order.
	 * Must contain {@link EventManager#defaultPhaseId() the default phase identifier}
	 * @param <T> the type of the listeners of the event
	 * @param <C> the type of the filtering context
	 * @return a new filtered event instance
	 * @see #createFiltered(Class, Class)
	 * @see #createFiltered(Class, InvokerFactory)
	 * @see #createFiltered(Class, Class, Function)
	 * @see #createFiltered(Class, Class, Object, Function)
	 * @see #createFilteredWithPhases(Class, Class, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Function, Comparable[])
	 */
	@SuppressWarnings("unchecked")
	public <T, C> @NotNull FilteredEvent<I, T, C> createFilteredWithPhases(
			@NotNull Class<C> contextType,
			@NotNull InvokerFactory<T> invokerFactory,
			@NotNull I... defaultPhases
	) {
		return this.createFilteredWithPhases(invokerFactory.type(), contextType, invokerFactory, defaultPhases);
	}

	/**
	 * Create a new instance of {@link FilteredEvent} with a list of default phases that get invoked in order.
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
	 * @param contextType the class of the context
	 * @param defaultPhases the default phases of this event, in the correct order.
	 * Must contain {@link EventManager#defaultPhaseId() the default phase identifier}
	 * @param <T> the type of the listeners of the event
	 * @param <C> the type of the filtering context
	 * @return a new filtered event instance
	 * @see #createFiltered(Class, Class)
	 * @see #createFiltered(Class, InvokerFactory)
	 * @see #createFiltered(Class, Class, Function)
	 * @see #createFiltered(Class, Class, Object, Function)
	 * @see #createFilteredWithPhases(Class, InvokerFactory, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Function, Comparable[])
	 */
	@SuppressWarnings("unchecked")
	public <T, C> @NotNull FilteredEvent<I, T, C> createFilteredWithPhases(
			@NotNull Class<? super T> type,
			@NotNull Class<C> contextType,
			@NotNull I... defaultPhases
	) {
		return this.createFilteredWithPhases(type, contextType, new DefaultInvokerFactory<>(type), defaultPhases);
	}

	/**
	 * Create a new instance of {@link FilteredEvent} with a list of default phases that get invoked in order.
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
	 * @param contextType the class of the context
	 * @param implementation a function which generates an invoker implementation using an array of listeners
	 * @param defaultPhases the default phases of this event, in the correct order.
	 * Must contain {@link EventManager#defaultPhaseId() the default phase identifier}
	 * @param <T> the type of the listeners of the event
	 * @param <C> the type of the filtering context
	 * @return a new filtered event instance
	 * @see #createFiltered(Class, Class)
	 * @see #createFiltered(Class, InvokerFactory)
	 * @see #createFiltered(Class, Class, Function)
	 * @see #createFiltered(Class, Class, Object, Function)
	 * @see #createFilteredWithPhases(Class, InvokerFactory, Comparable[])
	 * @see #createFilteredWithPhases(Class, Class, Comparable[])
	 */
	@SafeVarargs
	public final <T, C> @NotNull FilteredEvent<I, T, C> createFilteredWithPhases(
			@NotNull Class<? super T> type,
			@NotNull Class<C> contextType,
			@NotNull Function<T[], T> implementation,
			@NotNull I... defaultPhases
	) {
		return this.createWithPhases(() -> new FilteredEvent<>(type, this.defaultPhaseId, implementation), defaultPhases);
	}

	/**
	 * Registers the listener to the specified events.
	 * <p>
	 * The registration of the listener will be rejected if one of the listed event involves generics in its listener type,
	 * as checking for valid registration is too expensive, please use the regular {@link Event#register(Object)} method
	 * for those as those checks will be delegated to the Java compiler.
	 *
	 * @param listener the listener object
	 * @param events the events to listen
	 * @throws IllegalArgumentException if the listener doesn't listen one of the events to listen, or if no events were specified
	 * @see Event#register(Object)
	 * @see Event#register(Comparable, Object)
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@SafeVarargs
	public final void listenAll(Object listener, Event<I, ?>... events) {
		if (events.length == 0) {
			throw new IllegalArgumentException("Tried to register a listener for an empty event list.");
		}

		var listenedPhases = this.getListenedPhases(listener.getClass());

		// Check whether we actually can register stuff. We only commit the registration if all events can.
		for (var event : events) {
			if (!event.type().isAssignableFrom(listener.getClass())) {
				throw new IllegalArgumentException("Given object " + listener + " is not a listener of event " + event);
			}

			if (event.type().getTypeParameters().length > 0) {
				throw new IllegalArgumentException("Cannot register a listener for the event " + event + " which is using generic parameters with listenAll.");
			}

			listenedPhases.putIfAbsent(event.type(), this.defaultPhaseId);
		}

		// We can register, so we do!
		for (var event : events) {
			((Event) event).register(listenedPhases.get(event.type()), listener);
		}
	}

	/**
	 * {@return the default phase identifier of all the events created by this event manager}
	 */
	@Contract(pure = true)
	public @NotNull I defaultPhaseId() {
		return this.defaultPhaseId;
	}

	/**
	 * {@return the event that is triggered when an event is created using this event manager}
	 */
	@Contract(pure = true)
	public @NotNull Event<I, EventCreation<I>> getCreationEvent() {
		return this.creationEvent;
	}

	/* Implementation */

	@SafeVarargs
	private <E extends Event<I, ?>> E createWithPhases(
			Supplier<E> eventSupplier,
			@NotNull I... defaultPhases
	) {
		this.ensureContainsDefaultPhase(defaultPhases);
		YumiAssertions.ensureNoDuplicates(defaultPhases, id -> new IllegalArgumentException("Duplicate event phase: " + id));

		var event = eventSupplier.get();

		for (int i = 1; i < defaultPhases.length; i++) {
			event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
		}

		this.creationEvent.invoker().onEventCreation(this, event);

		return event;
	}

	private Map<Class<?>, I> getListenedPhases(Class<?> listenerClass) {
		var map = new HashMap<Class<?>, I>();

		for (var annotation : listenerClass.getAnnotations()) {
			if (annotation instanceof ListenerPhase phase) {
				map.put(phase.callbackTarget(), this.phaseIdParser.apply(phase.value()));
			} else if (annotation instanceof ListenerPhases phases) {
				for (var phase : phases.value()) {
					map.put(phase.callbackTarget(), this.phaseIdParser.apply(phase.value()));
				}
			}
		}

		return map;
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
