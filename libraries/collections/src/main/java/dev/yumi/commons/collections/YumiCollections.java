/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.collections;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides various collections-related utilities.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class YumiCollections {
	private YumiCollections() {
		throw new UnsupportedOperationException("YumiCollections only contains static definitions.");
	}

	/**
	 * Executes the given action for all the permutations possible of the given collection.
	 *
	 * @param collection the collection to shuffle
	 * @param action the action to execute for every permutation
	 * @param <T> the type held by the collection
	 */
	public static <T> void forAllPermutations(@NotNull List<T> collection, @NotNull Consumer<List<T>> action) {
		forAllPermutations(new ArrayList<>(), collection, action);
	}

	/**
	 * Executes the given action for all the permutations possible of the toSelect collection.
	 *
	 * @param selected the first elements of the list that is to be included in every permutation
	 * @param toSelect the elements to shuffle at the end of the list made for every permutation
	 * @param action the action to execute for every permutation
	 * @param <T> the type held by the collection
	 */
	@SuppressWarnings("SuspiciousListRemoveInLoop")
	public static <T> void forAllPermutations(@NotNull List<T> selected, @NotNull List<T> toSelect, @NotNull Consumer<List<T>> action) {
		if (toSelect.isEmpty()) {
			action.accept(selected);
		} else {
			for (int i = 0; i < toSelect.size(); ++i) {
				selected.add(toSelect.get(i));
				var remaining = new ArrayList<>(toSelect);
				remaining.remove(i);
				forAllPermutations(selected, remaining, action);
				selected.remove(selected.size() - 1);
			}
		}
	}
}
