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

public class InvokerSubset<I extends Comparable<? super I>, T, C> implements InvokableEvent<T> {
	private final FilteredEvent<I, T, C> parent;
	private final C context;
	/**
	 * The invoker to execute the callbacks.
	 */
	private volatile T invoker;

	public InvokerSubset(FilteredEvent<I, T, C> parent, C context) {
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
