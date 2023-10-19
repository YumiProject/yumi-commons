/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;

/**
 * Represents an operation upon two {@code boolean}-valued operands and producing a
 * {@code boolean}-valued result. This is the primitive type specialization of
 * {@link BinaryOperator} for {@code boolean}.
 * <p>
 * This is a {@linkplain java.util.function functional interface}
 * whose functional method is {@link #apply(boolean, boolean)}.
 *
 * @version 1.0.0
 * @see BinaryOperator
 * @since 1.0.0
 */
@FunctionalInterface
public interface BooleanBinaryOperator {
	/**
	 * Represents the AND boolean operator.
	 */
	BooleanBinaryOperator AND = (a, b) -> a && b;
	/**
	 * Represents the OR boolean operator.
	 */
	BooleanBinaryOperator OR = (a, b) -> a || b;
	/**
	 * Represents the XOR boolean operator.
	 */
	BooleanBinaryOperator XOR = (a, b) -> a ^ b;

	/**
	 * Applies this operator to the given operands.
	 *
	 * @param left the first operand
	 * @param right the second operand
	 * @return the operator result
	 */
	boolean apply(boolean left, boolean right);

	/**
	 * {@return a binary operator that represents the logical negation of this binary operator}
	 */
	@Contract(pure = true)
	default @NotNull BooleanBinaryOperator negate() {
		return (a, b) -> !this.apply(a, b);
	}
}
