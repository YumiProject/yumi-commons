/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons;

import module org.jetbrains.annotations;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a value of two possible types. An {@code Either} is either a {@linkplain Left left value} or a {@linkplain Right right value}.
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {
	/**
	 * Constructs a {@link Left}.
	 * <br />
	 * <pre>{@code
	 * // Create an Either instance initiated with the left value 42.
	 * Either<Long, ?> either = Either.left(42L);
	 * }</pre>
	 *
	 * @param value the value
	 * @param <L> the type of the left value
	 * @param <R> the type of the right value
	 * @return a new {@code Left} instance
	 */
	@Contract(pure = true)
	static <L, R> @NotNull Left<L, R> left(L value) {
		return new Left<>(value);
	}

	/**
	 * Constructs a {@link Right}.
	 * <br />
	 * <pre>{@code
	 * // Create an Either instance initiated with the right value "people".
	 * Either<?, String> either = Either.right("world");
	 * }</pre>
	 *
	 * @param value the value
	 * @param <L> the type of the left value
	 * @param <R> the type of the right value
	 * @return a new {@code Right} instance
	 */
	@Contract(pure = true)
	static <L, R> @NotNull Right<L, R> right(R value) {
		return new Right<>(value);
	}

	/**
	 * Gets the left value of this either.
	 *
	 * @return the left value
	 * @throws NoSuchElementException if this either does not have a left value
	 * @see #getRight()
	 */
	@Contract(pure = true)
	L getLeft();

	/**
	 * Gets the right value of this either.
	 *
	 * @return the right value
	 * @throws NoSuchElementException if this either does not have a right value
	 * @see #getLeft()
	 */
	@Contract(pure = true)
	R getRight();

	/**
	 * Checks whether this either represents a left value.
	 *
	 * @return {@code true} if this either represents a left value, or {@code false} otherwise
	 */
	@Contract(pure = true)
	boolean isLeft();

	/**
	 * Checks whether this either represents a right value.
	 *
	 * @return {@code true} if this either represents a right value, or {@code false} otherwise
	 */
	@Contract(pure = true)
	boolean isRight();

	/**
	 * If this either represents a left value, performs the given action with the value, otherwise does nothing.
	 *
	 * @param action the action to be performed, if this either represents a left value
	 */
	default void ifLeft(@NotNull Consumer<? super L> action) {}

	/**
	 * If this either represents a right value, performs the given action with the value, otherwise does nothing.
	 *
	 * @param action the action to be performed, if this either represents a right value
	 */
	default void ifRight(@NotNull Consumer<? super R> action) {}

	/**
	 * If this either represents a left value, performs the given left action with the value, otherwise performs the right action.
	 *
	 * @param leftAction the action to be performed, if this either represents a left value
	 * @param rightAction the action to be performed, if this either represents a right value
	 */
	void ifLeftOrElse(@NotNull Consumer<? super L> leftAction, @NotNull Runnable rightAction);

	/**
	 * If this either represents a right value, performs the given right action with the value, otherwise performs the left action.
	 *
	 * @param rightAction the action to be performed, if this either represents a right value
	 * @param leftAction the action to be performed, if this either represents a left value
	 */
	void ifRightOrElse(@NotNull Consumer<? super R> rightAction, @NotNull Runnable leftAction);

	/**
	 * Performs the given action which corresponds to what this either represents with its value.
	 *
	 * @param leftAction the action to be performed, if this either represents a left value
	 * @param rightAction the action to be performed, if this either represents a right value
	 */
	void apply(
			@NotNull Consumer<? super L> leftAction,
			@NotNull Consumer<? super R> rightAction
	);

	/**
	 * If this either represents a left value,
	 * returns an {@code Either.Left} describing the result of applying the given mapping function to the value,
	 * otherwise returns itself.
	 *
	 * @param mapper the mapping function to apply to the value, if this either represents a left value
	 * @param <NL> the type to map the left value to
	 * @return an {@code Either.Left} describing the result of applying the mapping function to the value
	 * if this either represents a left value, or itself otherwise.
	 */
	<NL> Either<NL, R> mapLeft(@NotNull Function<? super L, ? extends NL> mapper);

	/**
	 * If this either represents a right value,
	 * returns an {@code Either.Right} describing the result of applying the given mapping function to the value,
	 * otherwise returns itself.
	 *
	 * @param mapper the mapping function to apply to the value, if this either represents a right value
	 * @param <NR> the type to map the right value to
	 * @return an {@code Either.Right} describing the result of applying the mapping function to the value
	 * if this either represents a right value, or itself otherwise.
	 */
	<NR> Either<L, NR> mapRight(@NotNull Function<? super R, ? extends NR> mapper);

	/**
	 * Maps this {@code Either} to an {@code Either} of different values.
	 *
	 * @param leftMapper the mapping function to apply to the value, if this either represents a left value
	 * @param rightMapper the mapping function to apply to the value, if this either represents a right value
	 * @param <NL> the type to map the left value to
	 * @param <NR> the type to map the right value to
	 * @return an {@code Either} describing the result of applying either of the mapping function to the value this holds
	 */
	<NL, NR> Either<NL, NR> map(
			@NotNull Function<? super L, ? extends NL> leftMapper,
			@NotNull Function<? super R, ? extends NR> rightMapper
	);

	/**
	 * If this either represents a left value,
	 * returns an {@code Either} given by the given mapping function,
	 * otherwise returns itself.
	 *
	 * @param mapper the mapping function to apply to the value, if this either represents a left value
	 * @param <NL> the type to map the left value to
	 * @return an {@code Either} describing the result of applying the mapping function to the value this holds
	 */
	<NL> Either<NL, R> flatMapLeft(
			@NotNull Function<? super L, ? extends Either<NL, R>> mapper
	);

	/**
	 * If this either represents a right value,
	 * returns an {@code Either} given by the given mapping function,
	 * otherwise returns itself.
	 *
	 * @param mapper the mapping function to apply to the value, if this either represents a right value
	 * @param <NR> the type to map the right value to
	 * @return an {@code Either} describing the result of applying the mapping function to the value this holds
	 */
	<NR> Either<L, NR> flatMapRight(
			@NotNull Function<? super R, ? extends Either<L, NR>> mapper
	);

	/**
	 * Folds this {@code Either} into a singular value.
	 *
	 * @param leftMapper the mapping function to apply to the value, if this either represents a left value
	 * @param rightMapper the mapping function to apply to the value, if this either represents a right value
	 * @param <U> the type to map either the left or right value to
	 * @return the mapped value given by either mapper functions
	 */
	<U> U fold(
			@NotNull Function<? super L, ? extends U> leftMapper,
			@NotNull Function<? super R, ? extends U> rightMapper
	);

	/**
	 * Swaps the right and left values.
	 * <p>
	 * If this either represents a left value, this method will return an either representing a right value with the same value.
	 * If this either represents a right value, this method will return an either representing a left value with the same value.
	 *
	 * @return an {@code Either} with the right and left swapped
	 */
	@Contract(value = "-> new", pure = true)
	@NotNull
	Either<R, L> swap();

	/**
	 * Represents the left value of an {@link Either}.
	 *
	 * @param value the left value
	 * @param <L> the type of the left value
	 * @param <R> the type of the right value
	 */
	record Left<L, R>(L value) implements Either<L, R> {
		@Override
		public L getLeft() {
			return this.value;
		}

		@Contract(value = "-> fail", pure = true)
		@Override
		public R getRight() {
			throw new NoSuchElementException("No right value present");
		}

		@Contract(value = "-> true", pure = true)
		@Override
		public boolean isLeft() {
			return true;
		}

		@Contract(value = "-> false", pure = true)
		@Override
		public boolean isRight() {
			return false;
		}

		@Override
		public void ifLeft(@NotNull Consumer<? super L> action) {
			action.accept(this.value);
		}

		@Override
		public void ifLeftOrElse(@NotNull Consumer<? super L> leftAction, @NotNull Runnable rightAction) {
			leftAction.accept(this.value);
		}

		@Override
		public void ifRightOrElse(@NotNull Consumer<? super R> rightAction, @NotNull Runnable leftAction) {
			leftAction.run();
		}

		@Override
		public void apply(@NotNull Consumer<? super L> leftAction, @NotNull Consumer<? super R> rightAction) {
			leftAction.accept(this.value);
		}

		@Override
		public <NL> Either<NL, R> mapLeft(@NotNull Function<? super L, ? extends NL> mapper) {
			return new Left<>(mapper.apply(this.value));
		}

		@SuppressWarnings("unchecked")
		@Contract(value = "_ -> this", pure = true)
		@Override
		public <NR> Either<L, NR> mapRight(@NotNull Function<? super R, ? extends NR> mapper) {
			// This is not the prettiest, but at runtime generics are a lie, so it allows to avoid an allocation.
			return (Either<L, NR>) this;
		}

		@Override
		public <NL, NR> Either<NL, NR> map(
				@NotNull Function<? super L, ? extends NL> leftMapper,
				@NotNull Function<? super R, ? extends NR> rightMapper
		) {
			return new Left<>(leftMapper.apply(this.value));
		}

		@Override
		public <NL> Either<NL, R> flatMapLeft(@NotNull Function<? super L, ? extends Either<NL, R>> mapper) {
			return mapper.apply(this.value);
		}

		@SuppressWarnings("unchecked")
		@Contract(value = "_ -> this", pure = true)
		@Override
		public <NR> Either<L, NR> flatMapRight(@NotNull Function<? super R, ? extends Either<L, NR>> mapper) {
			return (Either<L, NR>) this;
		}

		@Override
		public <U> U fold(
				@NotNull Function<? super L, ? extends U> leftMapper,
				@NotNull Function<? super R, ? extends U> rightMapper
		) {
			return leftMapper.apply(this.value);
		}

		@Override
		public @NotNull Either<R, L> swap() {
			return new Right<>(this.value);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || this.getClass() != o.getClass()) return false;
			var left = (Left<?, ?>) o;
			return Objects.equals(this.value, left.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.value);
		}

		@Override
		public @NotNull String toString() {
			return "Either{" +
					"left=" + this.value +
					'}';
		}
	}

	/**
	 * Represents the right value of an {@link Either}.
	 *
	 * @param value the right value
	 * @param <L> the type of the left value
	 * @param <R> the type of the right value
	 */
	record Right<L, R>(R value) implements Either<L, R> {
		@Contract(value = "-> fail", pure = true)
		@Override
		public L getLeft() {
			throw new NoSuchElementException("No left value present");
		}

		@Override
		public R getRight() {
			return this.value;
		}

		@Contract(value = "-> false", pure = true)
		@Override
		public boolean isLeft() {
			return false;
		}

		@Contract(value = "-> true", pure = true)
		@Override
		public boolean isRight() {
			return true;
		}

		@Override
		public void ifRight(@NotNull Consumer<? super R> action) {
			action.accept(this.value);
		}

		@Override
		public void ifLeftOrElse(@NotNull Consumer<? super L> leftAction, @NotNull Runnable rightAction) {
			rightAction.run();
		}

		@Override
		public void ifRightOrElse(@NotNull Consumer<? super R> rightAction, @NotNull Runnable leftAction) {
			rightAction.accept(this.value);
		}

		@Override
		public void apply(@NotNull Consumer<? super L> leftAction, @NotNull Consumer<? super R> rightAction) {
			rightAction.accept(this.value);
		}

		@Contract(value = "_ -> this", pure = true)
		@SuppressWarnings("unchecked")
		@Override
		public <NL> Either<NL, R> mapLeft(@NotNull Function<? super L, ? extends NL> mapper) {
			// This is not the prettiest, but at runtime generics are a lie, so it allows to avoid an allocation.
			return (Either<NL, R>) this;
		}

		@Override
		public <NR> Either<L, NR> mapRight(@NotNull Function<? super R, ? extends NR> mapper) {
			return new Right<>(mapper.apply(this.value));
		}

		@Override
		public <NL, NR> Either<NL, NR> map(
				@NotNull Function<? super L, ? extends NL> leftMapper,
				@NotNull Function<? super R, ? extends NR> rightMapper
		) {
			return new Right<>(rightMapper.apply(this.value));
		}

		@SuppressWarnings("unchecked")
		@Contract(value = "_ -> this", pure = true)
		@Override
		public <NL> Either<NL, R> flatMapLeft(@NotNull Function<? super L, ? extends Either<NL, R>> mapper) {
			return (Either<NL, R>) this;
		}

		@Override
		public <NR> Either<L, NR> flatMapRight(@NotNull Function<? super R, ? extends Either<L, NR>> mapper) {
			return mapper.apply(this.value);
		}

		@Override
		public <U> U fold(
				@NotNull Function<? super L, ? extends U> leftMapper,
				@NotNull Function<? super R, ? extends U> rightMapper
		) {
			return rightMapper.apply(this.value);
		}

		@Override
		public @NotNull Either<R, L> swap() {
			return new Left<>(this.value);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || this.getClass() != o.getClass()) return false;
			var right = (Right<?, ?>) o;
			return Objects.equals(this.value, right.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.value);
		}

		@Override
		public @NotNull String toString() {
			return "Either{" +
					"right=" + this.value +
					'}';
		}
	}
}
