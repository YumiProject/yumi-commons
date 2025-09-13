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

	/**
	 * Returns a predicate that returns {@code true} if any of the given predicates returns {@code true}.
	 * <p>
	 * Note: this overload always returns {@code false}.
	 *
	 * @param <T> the type of the predicate
	 * @return {@link #alwaysFalse()}
	 * @see #anyOf(Predicate)
	 * @see #anyOf(Predicate, Predicate)
	 * @see #anyOf(Predicate[])
	 */
	public static <T> @NotNull Predicate<T> anyOf() {
		return alwaysFalse();
	}

	/**
	 * Returns a predicate that returns {@code true} if any of the given predicates returns {@code true}.
	 * <p>
	 * Note: this overload always returns the given predicate.
	 *
	 * @param <T> the type of the predicate
	 * @return {@code predicate}
	 * @see #anyOf()
	 * @see #anyOf(Predicate, Predicate)
	 * @see #anyOf(Predicate[])
	 */
	public static <T> @NotNull Predicate<T> anyOf(@NotNull Predicate<T> predicate) {
		return predicate;
	}

	/**
	 * {@return a predicate that returns {@code true} if any of the given predicates returns {@code true}}
	 *
	 * @param <T> the type of the predicate
	 * @see #anyOf()
	 * @see #anyOf(Predicate)
	 * @see #anyOf(Predicate[])
	 */
	public static <T> @NotNull Predicate<T> anyOf(@NotNull Predicate<T> a, @NotNull Predicate<T> b) {
		return a.or(b);
	}

	/**
	 * {@return a predicate that returns {@code true} if any of the given predicates returns {@code true}}
	 *
	 * @param <T> the type of the predicate
	 * @see #anyOf()
	 * @see #anyOf(Predicate)
	 * @see #anyOf(Predicate, Predicate)
	 */
	@SafeVarargs
	public static <T> @NotNull Predicate<T> anyOf(@NotNull Predicate<T>... predicates) {
		return o -> {
			for (var predicate : predicates) {
				if (predicate.test(o)) {
					return true;
				}
			}

			return false;
		};
	}

	/**
	 * Returns a predicate that returns {@code true} if all the given predicates returns {@code true}.
	 * <p>
	 * Note: this overload always returns {@code false}.
	 *
	 * @param <T> the type of the predicate
	 * @return {@link #alwaysFalse()}
	 * @see #allOf(Predicate)
	 * @see #allOf(Predicate, Predicate)
	 * @see #allOf(Predicate[])
	 */
	public static <T> @NotNull Predicate<T> allOf() {
		return alwaysFalse();
	}

	/**
	 * Returns a predicate that returns {@code true} if all the given predicates returns {@code true}.
	 * <p>
	 * Note: this overload always returns the given predicate.
	 *
	 * @param <T> the type of the predicate
	 * @return {@code predicate}
	 * @see #allOf()
	 * @see #allOf(Predicate, Predicate)
	 * @see #allOf(Predicate[])
	 */
	public static <T> @NotNull Predicate<T> allOf(@NotNull Predicate<T> predicate) {
		return predicate;
	}

	/**
	 * {@return a predicate that returns {@code true} if all the given predicates returns {@code true}}
	 *
	 * @param <T> the type of the predicate
	 * @see #allOf()
	 * @see #allOf(Predicate)
	 * @see #allOf(Predicate[])
	 */
	public static <T> @NotNull Predicate<T> allOf(@NotNull Predicate<T> a, @NotNull Predicate<T> b) {
		return a.and(b);
	}

	/**
	 * {@return a predicate that returns {@code true} if all the given predicates returns {@code true}}
	 *
	 * @param <T> the type of the predicate
	 * @see #allOf()
	 * @see #allOf(Predicate)
	 * @see #allOf(Predicate, Predicate)
	 */
	@SafeVarargs
	public static <T> @NotNull Predicate<T> allOf(@NotNull Predicate<T>... predicates) {
		return o -> {
			for (var predicate : predicates) {
				if (!predicate.test(o)) {
					return false;
				}
			}

			return true;
		};
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
