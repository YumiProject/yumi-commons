/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A set of various {@link Predicate}-related utilities.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class YumiPredicates {
	private YumiPredicates() {
		throw new UnsupportedOperationException("YumiPredicates only contains static definitions.");
	}

	/**
	 * {@return {@linkplain Predicate a predicate} that always returns {@code true}}
	 *
	 * @param <T> the type of the input to the predicate
	 * @see #alwaysFalse()
	 */
	@SuppressWarnings("unchecked")
	public static <T> @NotNull Predicate<T> alwaysTrue() {
		return (Predicate<T>) ALWAYS_TRUE;
	}

	/**
	 * {@return {@linkplain Predicate a predicate} that always returns {@code false}}
	 *
	 * @param <T> the type of the input to the predicate
	 * @see #alwaysTrue()
	 */
	@SuppressWarnings("unchecked")
	public static <T> @NotNull Predicate<T> alwaysFalse() {
		return (Predicate<T>) ALWAYS_FALSE;
	}

	//region Implementations
	private static final Predicate<?> ALWAYS_TRUE = new Predicate<>() {
		@Override
		public boolean test(Object o) {
			return true;
		}

		@Override
		public String toString() {
			return "YumiPredicates.alwaysTrue()";
		}
	};
	private static final Predicate<?> ALWAYS_FALSE = new Predicate<>() {
		@Override
		public boolean test(Object o) {
			return false;
		}

		@Override
		public String toString() {
			return "YumiPredicates.alwaysFalse()";
		}
	};
	//endregion
}
