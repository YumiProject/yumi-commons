/*
 * Copyright 2024 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an {@linkplain Event event} which can filter its listeners given an invocation context.
 * <p>
 * This type of event will respect the same rules and assumptions as regular events.
 *
 * <h2>Example: Registering filtered listeners</h2>
 * <p>
 * Similar to how you would register a listener in a regular event, you pass an instance of {@code T} into {@link #register}.
 * Using the same {@link #register(Object) register} methods from {@link Event} will result in registering a global listener
 * which will be invoked no matter the context.
 * To make your listener context-specific you need to add a predicate given a context of type {@code C}.
 *
 * <pre>{@code
 * // Events are created and managed by an EventManager.
 * // They are given a type for the phase identifiers and the default phase identifier.
 * static final EventManager<String> EVENT_MANAGER = new EventManager("default");
 *
 * // Events should use a dedicated functional interface for T rather than overloading multiple events to the same type
 * // to allow those who implement using a class to implement multiple events.
 * @FunctionalInterface
 * public interface Example {
 *     void doSomething();
 * }
 *
 * // Filtered events also have an invocation context.
 * public record EventContext(String value) {}
 *
 * // You can also return this instance of Event from a method, may be useful where a parameter is needed to get
 * // the right instance of Event.
 * public static final Event<String, Example> EXAMPLE = EVENT_MANAGER.create(Example.class);
 *
 * public void registerEvents() {
 *     // Since T is a functional interface, we can use the lambda form.
 *     EXAMPLE.register(() -> {
 *         // Do something
 *     }, context -> context.value().equals("test"));
 *
 *     // Or we can use a method reference.
 *     EXAMPLE.register(this::runSomething, context -> context.value().equals("some other context"));
 *
 *     // Or implement T using a class.
 *     // You can also use an anonymous class here; for brevity that is not included.
 *     EXAMPLE.register(new ImplementedOntoClass()); // This is a global listener.
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
 * <h2>Example: Executing a filtered event</h2>
 * <p>
 * While you could execute a filtered event the same way a regular event is executed,
 * it would only invoke global listeners due to it not being aware of a context.
 * Executing a filtered event is done by first creating a subset of listeners from the event using {@link #forContext(Object)} for a given context,
 * then calling a method on the event invoker. Where {@code T} is Example, executing a filtered event
 * is done through the following:
 *
 * <pre>{@code
 * InvokerSubset subset = EXAMPLE.forContext(new EventContext("test"));
 *
 * // Invoke the listeners relevant to the given context.
 * subset.invoker().doSomething();
 * }</pre>
 * <p>
 * This architecture has advantages only if the subset is stored and only re-created if the context is different.
 * Otherwise, a regular {@linkplain Event event} would do just fine.
 *
 * @param <I> the phase identifier type
 * @param <T> the type of the listeners, and the type of the invoker used to execute an event
 * @param <C> the type of the context used to filter out which listeners should be invoked
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FilteredEvent<I extends Comparable<? super I>, T, C> extends Event<I, T> {
	/**
	 * Reference queue for cleared invoker subsets.
	 */
	private final ReferenceQueue<InvokerSubset<I, T, C>> queue = new ReferenceQueue<>();
	/**
	 * A cache of currently alive invoker subsets.
	 */
	private final Set<WeakReference<InvokerSubset<I, T, C>>> subsets = new HashSet<>();

	FilteredEvent(@NotNull Class<? super T> type, @NotNull I defaultPhaseId, @NotNull Function<T[], T> invokerFactory) {
		super(type, defaultPhaseId, invokerFactory);
	}

	/**
	 * Registers a listener to this event, which will be called only if the execution context matches the filter.
	 *
	 * @param listener the listener to register
	 * @param filter the predicate to test whether the listener to register should be called within a given context
	 * @see #register(Object)
	 * @see #register(Comparable, Object)
	 * @see #register(Comparable, Object, Predicate)
	 */
	public void register(@NotNull T listener, @NotNull Predicate<C> filter) {
		this.register(this.defaultPhaseId(), listener, filter);
	}

	/**
	 * Registers a listener to this event for a specific phase, which will called only if the execution context matches the filter.
	 *
	 * @param phaseIdentifier the identifier of the phase to register the listener in
	 * @param listener the listener to register
	 * @param filter the predicate to test whether the listener to register should be called within a given context
	 * @see #register(Object)
	 * @see #register(Object, Predicate)
	 * @see #register(Comparable, Object)
	 */
	public void register(@NotNull I phaseIdentifier, @NotNull T listener, @NotNull Predicate<C> filter) {
		Objects.requireNonNull(phaseIdentifier, "Cannot register a listener for a null phase.");
		Objects.requireNonNull(listener, "Cannot register a null listener.");
		Objects.requireNonNull(filter, "Cannot register a listener with a null context filter.");

		this.lock.lock();
		try {
			this.getOrCreatePhase(phaseIdentifier, true).addListener(new Listener<>(listener, filter));
			this.rebuildInvoker(this.listeners.length);
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Creates an invoker for a subset of listeners that matches the given context.
	 * <p>
	 * Each subset will be dynamically updated if new listeners which match the given context are registered to this event.
	 *
	 * @param context the current context
	 * @return an invoker for a subset of listeners
	 */
	public InvokerSubset<I, T, C> forContext(C context) {
		var subset = new InvokerSubset<>(this, context);

		this.lock.lock();
		try {
			this.purge();
			this.subsets.add(new WeakReference<>(subset, this.queue));
		} finally {
			this.lock.unlock();
		}

		return subset;
	}

	// This may be marked as redundant in some IDEs, but javac is clear: there is an unchecked warning here.
	// Though, that warning is very weird: the generics of FilteredPhaseData match this event's generics
	// while the generics in the phase fields also match them.
	@SuppressWarnings({"unchecked", "RedundantSuppression"})
	@Override
	FilteredPhaseData getOrCreatePhase(@NotNull I id, boolean sortIfCreate) {
		var phase = this.phases.get(id);

		if (phase == null) {
			phase = new FilteredPhaseData(id, this.type());
			this.phases.put(id, phase);
			this.sortedPhases.add(phase);

			if (sortIfCreate) {
				this.sortPhases();
			}
		}

		return (FilteredPhaseData) phase;
	}

	@Override
	void rebuildInvoker(int newLength) {
		super.rebuildInvoker(newLength);

		this.purge();

		for (var subset : this.subsets) {
			var value = subset.get();

			if (value != null) {
				value.rebuildInvoker();
			}
		}
	}

	private void purge() {
		for (var ref = this.queue.poll(); ref != null; ref = this.queue.poll()) {
			//noinspection SuspiciousMethodCalls
			this.subsets.remove(ref);
		}
	}

	@ApiStatus.Internal
	final class FilteredPhaseData extends Event.PhaseData<I, T> {
		Listener<T, C>[] listenersData;

		@SuppressWarnings("unchecked")
		FilteredPhaseData(@NotNull I id, @NotNull Class<? super T> listenerType) {
			super(id, listenerType);
			this.listenersData = new Listener[0];
		}

		@Override
		void addListener(@NotNull T listener) {
			super.addListener(listener);
			this.addListener(new Listener<>(listener, null));
		}

		void addListener(@NotNull Listener<T, C> listener) {
			int oldLength = this.listenersData.length;
			this.listenersData = Arrays.copyOf(this.listenersData, oldLength + 1);
			this.listenersData[oldLength] = listener;
		}
	}

	/**
	 * Represents a listener.
	 *
	 * @param listener the listener itself
	 * @param selector the selector of this listener, may be null if this listener is global
	 * @param <T> the type of listener
	 * @param <C> the type of filtering context
	 */
	@ApiStatus.Internal
	record Listener<T, C>(T listener, @Nullable Predicate<C> selector) {
		/**
		 * {@return {@code true} if this listener should listen to the given context, or {@code false} otherwise}
		 *
		 * @param context the filtering context
		 */
		boolean shouldListen(C context) {
			return this.selector == null || this.selector.test(context);
		}
	}
}
