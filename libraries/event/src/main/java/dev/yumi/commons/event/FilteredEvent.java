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

public final class FilteredEvent<I extends Comparable<? super I>, T, C> extends Event<I, T> {
	/**
	 * Reference queue for cleared invoker subsets.
	 */
	private final ReferenceQueue<InvokerSubset<I, T, C>> queue = new ReferenceQueue<>();
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
			this.rebuildInvoker(this.listeners.length + 1);
		} finally {
			this.lock.unlock();
		}
	}

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

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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

	record Listener<T, C>(T listener, @Nullable Predicate<C> selector) {
		boolean shouldListen(C context) {
			return this.selector == null || this.selector.test(context);
		}
	}
}
