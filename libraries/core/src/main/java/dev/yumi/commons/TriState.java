/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * An enumeration that represents either {@code true}, {@code false}, or the absence of a value.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public enum TriState {
	/**
	 * Represents a value of {@code true}.
	 */
	TRUE {
		@Contract(value = "_ -> true", pure = true)
		@Override
		public boolean toBooleanOrElse(boolean fallback) {
			return true;
		}

		@Contract(value = "_ -> true")
		@Override
		public boolean toBooleanOrElseGet(@NotNull BooleanSupplier fallbackSupplier) {
			Objects.requireNonNull(fallbackSupplier, "fallbackSupplier may not be null");
			return true;
		}

		@Contract(value = "-> true", pure = true)
		@Override
		public boolean toBooleanOrElseThrow() {
			return true;
		}

		@Contract(value = "_ -> true")
		@Override
		public <X extends Throwable> boolean toBooleanOrElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X {
			Objects.requireNonNull(exceptionSupplier, "exceptionSupplier may not be null");
			return true;
		}
	},
	/**
	 * Represents a value of {@code false}.
	 */
	FALSE {
		@Contract(value = "_ -> false", pure = true)
		@Override
		public boolean toBooleanOrElse(boolean fallback) {
			return false;
		}

		@Contract(value = "_ -> false")
		@Override
		public boolean toBooleanOrElseGet(@NotNull BooleanSupplier fallbackSupplier) {
			Objects.requireNonNull(fallbackSupplier, "fallbackSupplier may not be null");
			return false;
		}

		@Contract(value = "-> false", pure = true)
		@Override
		public boolean toBooleanOrElseThrow() {
			return false;
		}

		@Contract(value = "_ -> false")
		@Override
		public <X extends Throwable> boolean toBooleanOrElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X {
			Objects.requireNonNull(exceptionSupplier, "exceptionSupplier may not be null");
			return false;
		}
	},
	/**
	 * Represents a default/fallback value.
	 */
	DEFAULT {
		@Contract(value = "_ -> param1", pure = true)
		@Override
		public boolean toBooleanOrElse(boolean fallback) {
			return fallback;
		}

		@Override
		public boolean toBooleanOrElseGet(@NotNull BooleanSupplier fallbackSupplier) {
			Objects.requireNonNull(fallbackSupplier, "fallbackSupplier may not be null");
			return fallbackSupplier.getAsBoolean();
		}

		@Contract(value = "-> fail", pure = true)
		@Override
		public boolean toBooleanOrElseThrow() {
			throw new NoSuchElementException("No value present");
		}

		@Contract(value = "_ -> fail")
		@Override
		public <X extends Throwable> boolean toBooleanOrElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X {
			Objects.requireNonNull(exceptionSupplier, "exceptionSupplier may not be null");
			throw exceptionSupplier.get();
		}
	};

	/**
	 * {@return the value of an "and" operation on this and the given operands}
	 *
	 * <table>
	 * 	<caption>Truth table of TriState's "and" operator</caption>
	 * 	<tr><th>A</th><th>B</th><th>Result</th></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #FALSE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #FALSE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #TRUE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #TRUE}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #DEFAULT}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #FALSE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #TRUE}</td><td>{@link #DEFAULT}</td></tr>
	 * </table>
	 *
	 * @param other the other operand
	 * @see #and(Supplier)
	 */
	@Contract(pure = true)
	public @NotNull TriState and(@NotNull TriState other) {
		return switch (this) {
			case TRUE -> other;
			case FALSE -> this;
			default -> other == TRUE ? DEFAULT : other;
		};
	}

	/**
	 * {@return the value of an "and" operation on this and the given operands}
	 *
	 * <table>
	 * 	<caption>Truth table of TriState's "and" operator</caption>
	 * 	<tr><th>A</th><th>B</th><th>Result</th></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #FALSE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #FALSE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #TRUE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #TRUE}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #DEFAULT}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #FALSE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #TRUE}</td><td>{@link #DEFAULT}</td></tr>
	 * </table>
	 *
	 * @param other the other operand, which is lazily evaluated
	 * @see #and(TriState)
	 */
	@Contract(pure = true)
	public @NotNull TriState and(@NotNull Supplier<TriState> other) {
		return switch (this) {
			case TRUE -> other.get();
			case FALSE -> this;
			default -> {
				var rightSide = other.get();
				if (rightSide == TRUE) yield DEFAULT;
				else yield rightSide;
			}
		};
	}

	/**
	 * {@return the value of an "or" operation on this and the given operands}
	 *
	 * <table>
	 * 	<caption>Truth table of TriState's "or" operator</caption>
	 * 	<tr><th>A</th><th>B</th><th>Result</th></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #FALSE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #FALSE}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #TRUE}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #TRUE}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #DEFAULT}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #FALSE}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #TRUE}</td><td>{@link #TRUE}</td></tr>
	 * </table>
	 *
	 * @param other the other operand
	 * @see #or(Supplier)
	 */
	@Contract(pure = true)
	public @NotNull TriState or(@NotNull TriState other) {
		return switch (this) {
			case TRUE -> this;
			case FALSE -> other;
			default -> other == FALSE ? this : other;
		};
	}

	/**
	 * {@return the value of an "or" operation on this and the given operands}
	 *
	 * <table>
	 * 	<caption>Truth table of TriState's "or" operator</caption>
	 * 	<tr><th>A</th><th>B</th><th>Result</th></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #FALSE}</td><td>{@link #FALSE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #FALSE}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #TRUE}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #TRUE}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #FALSE}</td><td>{@link #DEFAULT}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #TRUE}</td><td>{@link #DEFAULT}</td><td>{@link #TRUE}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #FALSE}</td><td>{@link #DEFAULT}</td></tr>
	 * 	<tr><td>{@link #DEFAULT}</td><td>{@link #TRUE}</td><td>{@link #TRUE}</td></tr>
	 * </table>
	 *
	 * @param other the other operand, which is lazily evaluated
	 * @see #or(TriState)
	 */
	@Contract(pure = true)
	public @NotNull TriState or(@NotNull Supplier<TriState> other) {
		return switch (this) {
			case TRUE -> this;
			case FALSE -> other.get();
			default -> {
				var rightSide = other.get();
				if (rightSide == FALSE) yield this;
				else yield rightSide;
			}
		};
	}

	/**
	 * Converts this triple state value into a boxed boolean.
	 *
	 * @return {@code null} if this is {@link #DEFAULT}, or the boolean value represented by this state otherwise
	 * @see #toBooleanOrElse(boolean)
	 * @see #toBooleanOrElseGet(BooleanSupplier)
	 */
	@Contract(pure = true)
	public @Nullable Boolean toBoolean() {
		return switch (this) {
			case TRUE -> true;
			case FALSE -> false;
			default -> null;
		};
	}

	/**
	 * Converts this triple state value into a boolean.
	 * When this triple state value is {@link #DEFAULT}, the boolean parameter will be returned instead.
	 *
	 * @param fallback the value to return if this triple state value is {@link #DEFAULT}
	 * @return the boolean value of this triple state, or the fallback value if this is {@link #DEFAULT}
	 * @see #toBoolean()
	 * @see #toBooleanOrElseGet(BooleanSupplier)
	 */
	@Contract(pure = true)
	public abstract boolean toBooleanOrElse(boolean fallback);

	/**
	 * Converts this triple state value into a boolean.
	 * When this triple state value is {@link #DEFAULT}, the supplied boolean will be returned instead.
	 *
	 * @param fallbackSupplier the supplier from which the value to return is fetched if this triple state value is {@link #DEFAULT}
	 * @return the boolean value of this triple state, or the boolean fetched from the fallback supplier if this is {@link #DEFAULT}
	 * @see #toBoolean()
	 * @see #toBooleanOrElse(boolean)
	 */
	public abstract boolean toBooleanOrElseGet(@NotNull BooleanSupplier fallbackSupplier);

	/**
	 * Converts this triple state value into a boolean.
	 * When this triple state value is {@link #DEFAULT}, the {@link NoSuchElementException} error is thrown.
	 *
	 * @return the boolean value of this triple state value
	 * @throws NoSuchElementException if this triple state value is {@link #DEFAULT}
	 */
	@Contract(pure = true)
	public abstract boolean toBooleanOrElseThrow();

	/**
	 * Converts this triple state value into a boolean.
	 * When this triple state value is {@link #DEFAULT}, the supplied exception is thrown.
	 *
	 * @param exceptionSupplier the exception supplier to throw if this triple state value is {@link #DEFAULT}
	 * @param <X> Type of the exception to be thrown
	 * @return the boolean value of this triple state value
	 * @throws X if this triple state value is {@link #DEFAULT}
	 */
	public abstract <X extends Throwable> boolean toBooleanOrElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X;

	/**
	 * Converts the given boolean into a {@code TriState}.
	 *
	 * @param bool the boolean value to convert
	 * @return the {@code TriState} value of the boolean
	 */
	@Contract(pure = true)
	public static @NotNull TriState from(boolean bool) {
		return bool ? TRUE : FALSE;
	}

	/**
	 * Converts the given boxed boolean into a {@code TriState}.
	 *
	 * @param bool the boxed boolean to convert
	 * @return the {@code TriState} value of the boolean
	 */
	@Contract(pure = true)
	public static @NotNull TriState from(@Nullable Boolean bool) {
		return bool == null ? DEFAULT : from(bool.booleanValue());
	}

	/**
	 * Converts the given string into a {@code TriState}.
	 * <p>
	 * The rules to match the different triple state values are as follows:
	 * <ul>
	 *     <li>{@link #TRUE} if value is {@code "true"} or {@code "on"};</li>
	 *     <li>{@link #FALSE} if value is {@code "false"} or {@code "off"};</li>
	 *     <li>{@link #DEFAULT} for every other cases.</li>
	 * </ul>
	 * The matching is case-insensitive.
	 *
	 * @param value the string value
	 * @return the parsed {@code TriState} value
	 */
	public static @NotNull TriState fromString(@Nullable String value) {
		if ("true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value)) {
			return TRUE;
		} else if ("false".equalsIgnoreCase(value) || "off".equalsIgnoreCase(value)) {
			return FALSE;
		} else {
			return DEFAULT;
		}
	}

	/**
	 * {@return the parsed triple state value from the given system property}
	 *
	 * @param property the system property name
	 */
	public static @NotNull TriState fromProperty(@NotNull String property) {
		String value = System.getProperty(property);

		return fromString(value);
	}
}
