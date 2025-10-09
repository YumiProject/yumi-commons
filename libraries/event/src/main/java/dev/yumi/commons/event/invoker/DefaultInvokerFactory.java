/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker;

import module org.jetbrains.annotations;

import dev.yumi.commons.TriState;
import dev.yumi.commons.event.invoker.dynamic.FilterInvokerFactory;
import dev.yumi.commons.event.invoker.dynamic.SequenceInvokerFactory;
import dev.yumi.commons.event.invoker.dynamic.TriStateFilterInvokerFactory;

import java.lang.reflect.Method;

/**
 * Represents the default factory of invoker implementations of an {@link dev.yumi.commons.event.Event} given an array of listeners.
 * <p>
 * This invoker factory is used for {@link dev.yumi.commons.event.EventManager}'s event creation methods as the default
 * invoker factory to use, it dynamically generates the implementations for invokers that:
 * <ul>
 * 	<li>don't return anything;</li>
 * 	<li>return a {@code boolean} as some kind of filter;</li>
 * 	<li>return a {@link TriState}, for which {@link TriState#DEFAULT} is the default return value,
 * 	and whenever a listener returns another value it returns early.</li>
 * </ul>
 *
 * @param <T> the type of the invoker executed by the event
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class DefaultInvokerFactory<T> extends InvokerFactory<T> {
	private final InvokerFactory<T> wrapped;

	public DefaultInvokerFactory(@NotNull Class<? super T> type) {
		super(type);

		Method method = getFunctionalMethod(type);

		if (method.getReturnType() == void.class) {
			this.wrapped = new SequenceInvokerFactory<>(type, method);
		} else if (method.getReturnType() == boolean.class) {
			this.wrapped = new FilterInvokerFactory<>(type, method, false);
		} else if (method.getReturnType() == TriState.class) {
			this.wrapped = new TriStateFilterInvokerFactory<>(type, method);
		} else {
			throw new IllegalArgumentException("Cannot determine default invoker factory for the given type " + type + ".");
		}
	}

	@Override
	public T apply(T[] listeners) {
		return this.wrapped.apply(listeners);
	}
}
