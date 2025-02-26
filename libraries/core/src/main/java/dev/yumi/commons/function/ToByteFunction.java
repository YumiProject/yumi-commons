/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.function;

import java.util.function.Function;

/**
 * Represents a function that produces a byte-valued result.
 * This is the {@code byte}-producing primitive specialization for {@link Function}.
 * <p>
 * This is a {@linkplain java.util.function functional interface}
 * whose functional method is {@link #applyAsByte(Object)}.
 *
 * @param <T> the type of the input to the function
 *
 * @version 1.0.0
 * @see Function
 * @since 1.0.0
 */
@FunctionalInterface
public interface ToByteFunction<T> {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param value the function argument
	 * @return the function result
	 */
	byte applyAsByte(T value);

	/**
	 * {@return a function that always returns {@code 0}}
	 * @param <T> the type of the input objects to the function
	 */
	static <T> ToByteFunction<T> zero() {
		return ignored -> (byte) 0;
	}
}
