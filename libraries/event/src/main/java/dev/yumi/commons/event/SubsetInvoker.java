/*
 * Copyright 2024 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Represents a subset of listeners of a {@linkplain FilteredEvent filtered event}.
 *
 * @param <I> the phase identifier type
 * @param <T> the type of the listeners, and the type of the invoker used to execute an event
 * @param <C> the type of the context used to filter out which listeners should be invoked by this subset
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SubsetInvoker<I extends Comparable<? super I>, T, C> implements InvokableEvent<T> {
	/**
	 * The parent filtered event from which the subset comes from.
	 */
	private final FilteredEvent<I, T, C> parent;
	/**
	 * The context relevant to this subset of listeners.
	 */
	private final C context;
	/**
	 * The invoker to execute the callbacks.
	 */
	private volatile T invoker;

	public SubsetInvoker(FilteredEvent<I, T, C> parent, C context) {
		this.parent = parent;
		this.context = context;
		this.rebuildInvoker();
	}

	@Override
	public @NotNull T invoker() {
		return this.invoker;
	}

	@SuppressWarnings("unchecked")
	void rebuildInvoker() {
		var listeners = new ArrayList<T>();

		for (var entry : this.parent.sortedPhases) {
			var phase = (FilteredEvent<I, T, C>.FilteredPhaseData) entry;

			for (var listener : phase.listenersData) {
				if (listener.shouldListen(this.context)) {
					listeners.add(listener.listener());
				}
			}
		}

		this.invoker = this.parent.invokerFactory.apply(
				listeners.toArray(length -> (T[]) Array.newInstance(this.parent.type(), length))
		);
	}
}
