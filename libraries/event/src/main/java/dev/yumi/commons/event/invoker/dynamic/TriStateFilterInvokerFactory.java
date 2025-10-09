/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker.dynamic;

import module org.jetbrains.annotations;

import dev.yumi.commons.TriState;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;

/// Represents a factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners
/// for which the invoker implementation is dynamically generated for which the invoker returns a {@link TriState}.
///
/// The first listener to return a value other than {@link TriState#DEFAULT} will make the
/// invoker implementation return early with that value and skip the next listeners,
/// or it returns {@link TriState#DEFAULT} otherwise.
///
/// @param <T> the type of the invoker executed by the event
/// @author LambdAurora
/// @version 2.0.0
/// @since 1.0.0
@ApiStatus.Internal
public final class TriStateFilterInvokerFactory<T> extends DynamicInvokerFactory<T> {
	private static final ClassDesc CD_TRISTATE = ClassDesc.of(TriState.class.getName());

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

	private void writeGetTriStateDefault(CodeBuilder mb) {
		mb.getstatic(CD_TRISTATE, TriState.DEFAULT.name(), CD_TRISTATE);
	}

	@Override
	protected void writeImplementationMethod(WriterContext context) {
		int resultVar = context.data().iVar() + 1;
		var earlyReturnLabel = context.codeBuilder().newLabel();

		context.writeMethod(block -> {
			block.codeBuilder().astore(resultVar);

			// Compare result with TriState.DEFAULT to determine early return.
			block.codeBuilder().aload(resultVar);
			this.writeGetTriStateDefault(block.codeBuilder());
			block.codeBuilder().if_acmpne(earlyReturnLabel); // if (result != TriState.DEFAULT) goto earlyReturn;
		});
		this.writeGetTriStateDefault(context.codeBuilder());
		context.codeBuilder().areturn();

		// earlyReturn:
		context.codeBuilder()
				.labelBinding(earlyReturnLabel)
				.aload(resultVar)
				.areturn(); // return result;
	}
}
