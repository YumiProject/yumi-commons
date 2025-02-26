package dev.yumi.commons.function;

import java.util.function.Function;

/**
 * Represents a function that produces a short-valued result.
 * This is the {@code short}-producing primitive specialization for {@link Function}.
 * <p>
 * This is a {@linkplain java.util.function functional interface}
 * whose functional method is {@link #applyAsShort(Object)}.
 *
 * @param <T> the type of the input to the function
 *
 * @version 1.0.0
 * @see Function
 * @since 1.0.0
 */
@FunctionalInterface
public interface ToShortFunction<T> {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param value the function argument
	 * @return the function result
	 */
	short applyAsShort(T value);

	/**
	 * {@return a function that always returns {@code 0}}
	 * @param <T> the type of the input objects to the function
	 */
	static <T> ToShortFunction<T> zero() {
		return ignored -> (short) 0;
	}
}
