/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

/**
 * Represents a factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners
 * for which the invoker implementation is dynamically generated for which the invoker returns a {@code boolean}
 * as a kind of filter system.
 * <p>
 * The first listener to return {@code true} (or {@code false} if {@code invert} is set to {@code true}) will make the
 * invoker implementation return early with that value and skip the next listeners.
 *
 * @param <T> the type of the invoker executed by the event
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
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
	protected void writeImplementationMethod(MethodVisitor mv, WriterContext context) {
		var earlyReturnLabel = new Label();

		this.writeMethodStart(mv, context);
		mv.visitJumpInsn(this.invert ? IFEQ : IFNE, earlyReturnLabel);
		this.writeIncrement(mv, context);

		mv.visitLabel(earlyReturnLabel);
		mv.visitInsn(this.invert ? ICONST_0 : ICONST_1);
		mv.visitInsn(IRETURN);

		mv.visitLabel(context.getForEndLabel());
		mv.visitInsn(this.invert ? ICONST_1 : ICONST_0);
		mv.visitInsn(IRETURN);
	}
}
