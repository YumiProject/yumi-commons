/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker;

import dev.yumi.commons.TriState;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

/**
 * Represents a factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners
 * for which the invoker implementation is dynamically generated for which the invoker returns a {@link TriState}.
 * <p>
 * The first listener to return a value other than {@link TriState#DEFAULT} will make the
 * invoker implementation return early with that value and skip the next listeners,
 * or it returns {@link TriState#DEFAULT} otherwise.
 *
 * @param <T> the type of the invoker executed by the event
 * @version 1.0.0
 * @since 1.0.0
 */
public class TriStateFilterInvokerFactory<T> extends DynamicInvokerFactory<T> {
	public TriStateFilterInvokerFactory(@NotNull Class<? super T> type) {
		super(type);
	}

	public TriStateFilterInvokerFactory(@NotNull Class<? super T> type, @NotNull Method method) {
		super(type, method);
	}

	@Override
	protected void checkMethod(@NotNull Method method) {
		if (method.getReturnType() != TriState.class) {
			throw new IllegalArgumentException("Expected listener function to return a TriState.");
		}
	}

	private void writeGetTriStateDefault(MethodVisitor mv) {
		mv.visitFieldInsn(GETSTATIC,
				TriState.class.getName().replace('.', '/'), TriState.DEFAULT.name(),
				Descriptors.describe(TriState.class)
		);
	}

	@Override
	protected void writeImplementationMethod(MethodVisitor mv, WriterContext context) {
		int resultVar = context.iVar() + 1;
		var earlyReturnLabel = new Label();

		this.writeMethodStart(mv, context);
		mv.visitVarInsn(ASTORE, resultVar);

		// Compare result with TriState.DEFAULT to determine early return.
		mv.visitVarInsn(ALOAD, resultVar);
		this.writeGetTriStateDefault(mv);
		mv.visitJumpInsn(IF_ACMPNE, earlyReturnLabel); // if (result != TriState.DEFAULT) goto earlyReturn;
		this.writeIncrement(mv, context);

		// earlyReturn:
		mv.visitLabel(earlyReturnLabel);
		mv.visitVarInsn(ALOAD, resultVar);
		mv.visitInsn(ARETURN); // return result;

		mv.visitLabel(context.getForEndLabel());
		mv.visitFieldInsn(GETSTATIC,
				TriState.class.getName().replace('.', '/'), TriState.DEFAULT.name(),
				Descriptors.describe(TriState.class)
		);
		mv.visitInsn(ARETURN);
	}
}
