/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.collections;

import module org.jetbrains.annotations;
import module org.jspecify;

import java.util.*;
import java.util.function.Consumer;

/**
 * Provides various collections-related utilities.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@NullUnmarked
public final class YumiCollections {
	private YumiCollections() {
		throw new UnsupportedOperationException("YumiCollections only contains static definitions.");
	}

	/**
	 * Concatenates the given lists into a new list.
	 *
	 * @param first the first list to concatenate
	 * @param second the second list to concatenate
	 * @param more the other lists to concatenate if present
	 * @return the result of the concatenation of the given lists
	 * @param <T> the type of the lists
	 */
	@SafeVarargs
	public static <T> @NonNull List<T> concat(
			@NonNull List<? extends T> first,
			@NonNull List<? extends T> second,
			@NonNull List<? extends T>... more
	) {
		var list = new ArrayList<T>(
				first.size() + second.size() + Arrays.stream(more).mapToInt(Collection::size).sum()
		);
		list.addAll(first);
		list.addAll(second);
		for (var other : more) {
			list.addAll(other);
		}
		return list;
	}

	/**
	 * Concatenates the given sets into a new set.
	 *
	 * @param first the first set to concatenate
	 * @param second the second set to concatenate
	 * @param more the other sets to concatenate if present
	 * @return the result of the concatenation of the given sets
	 * @param <T> the type of the sets
	 */
	@SafeVarargs
	public static <T> @NonNull Set<T> concat(
			@NonNull Set<? extends T> first,
			@NonNull Set<? extends T> second,
			@NonNull Set<? extends T>... more
	) {
		var set = new HashSet<T>(
				first.size() + second.size() + Arrays.stream(more).mapToInt(Collection::size).sum()
		);
		set.addAll(first);
		set.addAll(second);
		for (var other : more) {
			set.addAll(other);
		}
		return set;
	}

	/**
	 * Executes the given action for all the permutations possible of the given collection.
	 *
	 * @param collection the collection to shuffle
	 * @param action the action to execute for every permutation
	 * @param <T> the type held by the collection
	 */
	public static <T> void forAllPermutations(
			@NonNull List<T> collection, @NonNull Consumer<@NonNull List<T>> action
	) {
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
	public static <T> void forAllPermutations(
			@NonNull List<T> selected, @NonNull List<T> toSelect, @NonNull Consumer<@NonNull List<T>> action
	) {
		if (toSelect.isEmpty()) {
			action.accept(selected);
		} else {
			for (int i = 0; i < toSelect.size(); ++i) {
				selected.add(toSelect.get(i));
				var remaining = new ArrayList<>(toSelect);
				remaining.remove(i);
				forAllPermutations(selected, remaining, action);
				selected.removeLast();
			}
		}
	}
}
