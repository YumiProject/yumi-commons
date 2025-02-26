package dev.yumi.commons.function;

import java.util.function.Function;

/**
 * Represents a function that produces a float-valued result.
 * This is the {@code float}-producing primitive specialization for {@link Function}.
 * <p>
 * This is a {@linkplain java.util.function functional interface}
 * whose functional method is {@link #applyAsFloat(Object)}.
 *
 * @param <T> the type of the input to the function
 *
 * @version 1.0.0
 * @see Function
 * @since 1.0.0
 */
@FunctionalInterface
public interface ToFloatFunction<T> {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param value the function argument
	 * @return the function result
	 */
	float applyAsFloat(T value);

	/**
	 * {@return a function that always returns {@code 0.f}}
	 * @param <T> the type of the input objects to the function
	 */
	static <T> ToFloatFunction<T> zero() {
		return ignored -> 0.f;
	}
}
