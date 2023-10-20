/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners.
 *
 * @param <T> the type of the invoker executed by the event
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class InvokerFactory<T> implements Function<T[], T> {
	protected final Class<? super T> type;

	protected InvokerFactory(@NotNull Class<? super T> type) {
		Objects.requireNonNull(
				type, "The type of the invoker factory cannot be null."
		);
		this.type = type;
	}

	/**
	 * {@return the type of the invoker executed by the event}
	 */
	@Contract(pure = true)
	public @NotNull Class<? super T> type() {
		return this.type;
	}

	/**
	 * Gets the functional method of the invoker from the given invoker type
	 *
	 * @param type the type of the invoker
	 * @param <T> the type of the invoker
	 * @return the functional method if found
	 * @throws IllegalArgumentException if no valid functional method could be found
	 */
	protected static <T> @NotNull Method getFunctionalMethod(@NotNull Class<? super T> type) {
		Method listenerMethod = null;

		for (var method : type.getMethods()) {
			int modifiers = method.getModifiers();

			if (Modifier.isPublic(modifiers) &&
					Modifier.isAbstract(modifiers)) {
				if (listenerMethod != null) {
					throw new IllegalArgumentException("The given listener type isn't a functional interface.");
				}

				listenerMethod = method;
			}
		}

		if (listenerMethod == null) {
			throw new IllegalArgumentException("The given listener type isn't a function interface.");
		}

		return listenerMethod;
	}
}
