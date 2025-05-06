/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker.dynamic;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.RETURN;

/**
 * Represents a factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners
 * for which the invoker implementation is dynamically generated in the case the invoker doesn't return anything.
 *
 * @param <T> the type of the invoker executed by the event
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ApiStatus.Internal
public final class SequenceInvokerFactory<T> extends DynamicInvokerFactory<T> {
	public SequenceInvokerFactory(@NotNull Class<? super T> type) {
		super(type);
	}

	public SequenceInvokerFactory(@NotNull Class<? super T> type, @NotNull Method method) {
		super(type, method);
	}

	@Override
	protected void checkMethod(@NotNull Method method) {
		if (method.getReturnType() != void.class) {
			throw new IllegalArgumentException("Expected listener function to return void.");
		}
	}

	@Override
	protected void writeImplementationMethod(MethodVisitor mv, WriterContext context) {
		this.writeMethodStart(mv, context);
		this.writeIncrement(mv, context);

		mv.visitLabel(context.getForEndLabel());
		mv.visitInsn(RETURN);
	}
}
