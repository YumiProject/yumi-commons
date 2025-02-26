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

import dev.yumi.commons.collections.toposort.NodeSorting;
import dev.yumi.commons.collections.toposort.SortableNode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * An object which stores event listeners.
 * <p>
 * The factory methods for Event allows the user to provide an implementation of {@code T} which is used to
 * execute the listeners stored in this event instance. This allows a user to control how iteration works, whether an
 * event is cancelled after a specific listener is executed or to make an event
 * {@link ParameterInvokingEvent parameter invoking}.
 * <p>
 * Generally {@code T} should be a type which is a
 * <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-9.html#jls-9.8">functional interface</a>
 * to allow listeners registered to the event to be specified in a lambda, method reference form or implemented onto a
 * class. A way to ensure that an interface is a functional interface is to place a {@link FunctionalInterface}
 * annotation on the type. You can let {@code T} not be a functional interface, however it heavily complicates the process
 * of implementing an invoker and only allows listener implementations to be done by implementing an interface onto a
 * class or extending a class.
 * <p>
 * An Event can have phases, each listener is attributed to a phase ({@link Event#defaultPhaseId()} if unspecified),
 * and each phase can have a defined ordering. Each event phase is identified, ordering is done
 * by explicitly stating that event phase {@code A} will run before event phase {@code B}, for example.
 * See {@link Event#addPhaseOrdering(Comparable, Comparable)} for more information.
 *
 * <h2>Example: Registering listeners</h2>
 * <p>
 * The most common use of an event will be registering a listener which is executed by the event. To register a listener,
 * pass an instance of {@code T} into {@link #register}.
 *
 * <pre>{@code
 * // Events are created and managed by an EventManager.
 * // They are given a type for the phase identifiers and the default phase identifier.
 * static final EventManager<String> EVENT_MANAGER = new EventManager("default", Function.identity());
 *
 * // Events should use a dedicated functional interface for T rather than overloading multiple events to the same type
 * // to allow those who implement using a class to implement multiple events.
 * @FunctionalInterface
 * public interface Example {
 *     void doSomething();
 * }
 *
 * // You can also return this instance of Event from a method, may be useful where a parameter is needed to get
 * // the right instance of Event.
 * public static final Event<String, Example> EXAMPLE = EVENT_MANAGER.create(Example.class);
 *
 * public void registerEvents() {
 *     // Since T is a functional interface, we can use the lambda form.
 *     EXAMPLE.register(() -> {
 *         // Do something
 *     });
 *
 *     // Or we can use a method reference.
 *     EXAMPLE.register(this::runSomething);
 *
 *     // Or implement T using a class.
 *     // You can also use an anonymous class here; for brevity that is not included.
 *     EXAMPLE.register(new ImplementedOntoClass());
 * }
 *
 * public void runSomething() {
 *     // Do something else
 * }
 *
 * // When implementing onto a class, the class must implement the same type as the event invoker.
 * class ImplementedOntoClass implements Example {
 *     public void doSomething() {
 *         // Do something else again
 *     }
 * }
 * }</pre>
 *
 * <h2>Example: Executing an event</h2>
 * <p>
 * Executing an event is done by calling a method on the event invoker. Where {@code T} is Example, executing an event
 * is done through the following:
 *
 * <pre>{@code
 * EXAMPLE.invoker().doSomething();
 * }</pre>
 *
 * @param <I> the phase identifier type
 * @param <T> the type of the listeners, and the type of the invoker used to execute an event
 * @version 1.0.0
 * @since 1.0.0
 */
public sealed class Event<I extends Comparable<? super I>, T>
		implements InvokableEvent<T>
		permits FilteredEvent {
	/**
	 * The type of listener of this event.
	 */
	private final Class<? super T> type;
	/**
	 * The default phase identifier.
	 */
	private final I defaultPhaseId;
	/**
	 * The function used to generate the implementation of the invoker to call the listeners.
	 */
	final Function<T[], T> invokerFactory;
	final Lock lock = new ReentrantLock();
	/**
	 * The invoker to execute the callbacks.
	 */
	private volatile T invoker;
	/**
	 * The registered listeners.
	 */
	T[] listeners;
	/**
	 * The registered event phases.
	 */
	final Map<I, PhaseData<I, T>> phases = new LinkedHashMap<>();
	/**
	 * The event phases sorted in a way that satisfies dependencies.
	 */
	final List<PhaseData<I, T>> sortedPhases = new ArrayList<>();

	@SuppressWarnings("unchecked")
	Event(
			@NotNull Class<? super T> type,
			@NotNull I defaultPhaseId,
			@NotNull Function<T[], T> invokerFactory
	) {
		Objects.requireNonNull(type, "The class specifying the type of T in the event cannot be null.");
		Objects.requireNonNull(defaultPhaseId, "The default phase identifier of the event cannot be null.");
		Objects.requireNonNull(invokerFactory, "The function to generate the invoker implementation for T cannot be null.");

		this.type = type;
		this.defaultPhaseId = defaultPhaseId;
		this.invokerFactory = invokerFactory;
		this.listeners = (T[]) Array.newInstance(type, 0);
		this.update();
	}

	/**
	 * {@return the class of the kind of listeners accepted by this event}
	 */
	@Contract(pure = true)
	public @NotNull Class<? super T> type() {
		return this.type;
	}

	/**
	 * {@return the default phase identifier of this event}
	 */
	@Contract(pure = true)
	public @NotNull I defaultPhaseId() {
		return this.defaultPhaseId;
	}

	/**
	 * Registers a listener to this event.
	 *
	 * @param listener the listener to register
	 * @see #register(Comparable, Object)
	 */
	public void register(@NotNull T listener) {
		this.register(this.defaultPhaseId, listener);
	}

	/**
	 * Registers a listener to this event for a specific phase.
	 *
	 * @param phaseIdentifier the identifier of the phase to register the listener in
	 * @param listener the listener to register
	 * @see #register(Object)
	 */
	public void register(@NotNull I phaseIdentifier, @NotNull T listener) {
		Objects.requireNonNull(phaseIdentifier, "Cannot register a listener for a null phase.");
		Objects.requireNonNull(listener, "Cannot register a null listener.");

		this.lock.lock();
		try {
			this.getOrCreatePhase(phaseIdentifier, true).addListener(listener);
			this.rebuildInvoker(this.listeners.length + 1);
		} finally {
			this.lock.unlock();
		}
	}

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
	public void addPhaseOrdering(@NotNull I firstPhase, @NotNull I secondPhase) {
		Objects.requireNonNull(firstPhase, "Tried to order a null phase.");
		Objects.requireNonNull(secondPhase, "Tried to order a null phase.");

		if (firstPhase.equals(secondPhase)) {
			throw new IllegalArgumentException("Cannot make a phase depend on itself.");
		}

		this.lock.lock();
		try {
			var first = this.getOrCreatePhase(firstPhase, false);
			var second = this.getOrCreatePhase(secondPhase, false);

			PhaseData.link(first, second);
			this.sortPhases();
			this.rebuildInvoker(this.listeners.length);
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public @NotNull T invoker() {
		return this.invoker;
	}

	/* Implementation */

	PhaseData<I, T> getOrCreatePhase(@NotNull I id, boolean sortIfCreate) {
		var phase = this.phases.get(id);

		if (phase == null) {
			phase = new PhaseData<>(id, this.type);
			this.phases.put(id, phase);
			this.sortedPhases.add(phase);

			if (sortIfCreate) {
				this.sortPhases();
			}
		}

		return phase;
	}

	void sortPhases() {
		NodeSorting.sort(this.sortedPhases, "event phases");
	}

	void rebuildInvoker(int newLength) {
		if (this.sortedPhases.size() == 1) {
			// There's a single phase, so we can directly use its listeners.
			this.listeners = this.sortedPhases.get(0).listeners;
		} else {
			@SuppressWarnings("unchecked")
			var newListeners = (T[]) Array.newInstance(this.type, newLength);
			int nextStart = 0;

			for (var phase : this.sortedPhases) {
				int phaseListenersCount = phase.listeners.length;
				System.arraycopy(
						phase.listeners, 0,
						newListeners, nextStart,
						phaseListenersCount
				);
				nextStart += phaseListenersCount;
			}

			this.listeners = newListeners;
		}

		this.update();
	}

	void update() {
		// Make a copy of the array given to the invoker factory so the entries cannot be mutated.
		this.invoker = this.invokerFactory.apply(
				Arrays.copyOf(this.listeners, this.listeners.length)
		);
	}

	@Override
	public String toString() {
		return "Event{" +
				"type=" + this.type +
				", defaultPhaseId=" + this.defaultPhaseId +
				", invoker=" + this.invoker +
				", listeners=" + Arrays.toString(this.listeners) +
				", phases=" + this.phases +
				", sortedPhases=" + this.sortedPhases +
				'}';
	}

	/**
	 * Represents data for a specific event phase.
	 *
	 * @param <I> the phase identifier type
	 * @param <T> the type of the listeners
	 */
	@ApiStatus.Internal
	static sealed class PhaseData<I, T> extends SortableNode<I, PhaseData<I, T>>
			permits FilteredEvent.FilteredPhaseData {
		private final I id;
		T[] listeners;

		@SuppressWarnings("unchecked")
		PhaseData(@NotNull I id, @NotNull Class<? super T> listenerType) {
			Objects.requireNonNull(id);

			this.id = id;
			this.listeners = (T[]) Array.newInstance(listenerType, 0);
		}

		@Override
		public @NotNull I getId() {
			return this.id;
		}

		void addListener(@NotNull T listener) {
			int oldLength = this.listeners.length;
			this.listeners = Arrays.copyOf(this.listeners, oldLength + 1);
			this.listeners[oldLength] = listener;
		}
	}
}
