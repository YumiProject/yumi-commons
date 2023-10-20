/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event;

import dev.yumi.commons.collections.toposort.SortableNode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

@ApiStatus.Internal
final class EventPhaseData<I, T> extends SortableNode<I, EventPhaseData<I, T>> {
	private final I id;
	T[] listeners;

	@SuppressWarnings("unchecked")
	EventPhaseData(@NotNull I id, @NotNull Class<? super T> listenerType) {
		Objects.requireNonNull(id);

		this.id = id;
		this.listeners = (T[]) Array.newInstance(listenerType, 0);
	}

	@Override
	public @NotNull I getId() {
		return this.id;
	}

	public void addListener(@NotNull T listener) {
		int oldLength = this.listeners.length;
		this.listeners = Arrays.copyOf(this.listeners, oldLength + 1);
		this.listeners[oldLength] = listener;
	}
}
