/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker.dynamic;

import module org.jetbrains.annotations;

import java.lang.reflect.Method;

/// Represents a factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners
/// for which the invoker implementation is dynamically generated for which the invoker returns a {@code boolean}
/// as a kind of filter system.
///
/// The first listener to return `true` (or `false` if `invert` is set to `true`) will make the
/// invoker implementation return early with that value and skip the next listeners.
///
/// @param <T> the type of the invoker executed by the event
/// @author LambdAurora
/// @version 2.0.0
/// @since 1.0.0
@ApiStatus.Internal
public final class FilterInvokerFactory<T> extends DynamicInvokerFactory<T> {
	private final boolean invert;

	public FilterInvokerFactory(@NotNull Class<? super T> type, boolean invert) {
		super(type);
		this.invert = invert;
	}

	public FilterInvokerFactory(@NotNull Class<? super T> type, @NotNull Method method, boolean invert) {
		super(type, method);
		this.invert = invert;
	}

	@Override
	protected void checkMethod(@NotNull Method method) {
		if (method.getReturnType() != boolean.class) {
			throw new IllegalArgumentException("Expected listener function to return a boolean.");
		}
	}

	@Override
	protected void writeImplementationMethod(WriterContext context) {
		var earlyReturnLabel = context.codeBuilder().newLabel();

		context.writeMethod(block -> {
			if (this.invert) {
				block.codeBuilder().ifeq(earlyReturnLabel);
			} else {
				block.codeBuilder().ifne(earlyReturnLabel);
			}
		});

		if (this.invert) context.codeBuilder().iconst_1();
		else context.codeBuilder().iconst_0();
		context.codeBuilder().ireturn();

		// earlyReturn:
		context.codeBuilder().labelBinding(earlyReturnLabel);
		if (this.invert) context.codeBuilder().iconst_0();
		else context.codeBuilder().iconst_1();
		context.codeBuilder().ireturn();
	}
}
