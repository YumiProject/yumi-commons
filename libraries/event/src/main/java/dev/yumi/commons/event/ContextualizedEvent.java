/*
 * Copyright 2024 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event;

import module org.jetbrains.annotations;

import dev.yumi.commons.collections.toposort.SortableNode;

import java.util.function.Predicate;

/**
 * Represents a contextualized event of a {@linkplain FilteredEvent filtered event}.
 *
 * @param <I> the phase identifier type
 * @param <T> the type of the listeners, and the type of the invoker used to execute an event
 * @param <C> the type of the context used to filter out which listeners should be invoked by this contextualized event
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ContextualizedEvent<I extends Comparable<? super I>, T, C> extends Event<I, T> {
	/**
	 * The context relevant to this event and listeners.
	 */
	private final C context;

	@SuppressWarnings({"unchecked", "RedundantSuppression"})
	public ContextualizedEvent(FilteredEvent<I, T, C> parent, C context) {
		super(parent.type(), parent.defaultPhaseId(), parent.invokerFactory);
		this.context = context;

		for (var phase : parent.phases.values()) {
			var copy = this.phases.computeIfAbsent(
					phase.getId(),
					ignored -> ((FilteredEvent<I, T, C>.FilteredPhaseData) phase).copyFor(context)
			);

			phase.getPreviousNodes().forEach(node -> {
				var previousCopy = this.phases.computeIfAbsent(
						node.getId(),
						ignored -> ((FilteredEvent<I, T, C>.FilteredPhaseData) node).copyFor(context)
				);
				SortableNode.link(previousCopy, copy);
			});

			phase.getNextNodes().forEach(node -> {
				var nextCopy = this.phases.computeIfAbsent(
						node.getId(),
						ignored -> ((FilteredEvent<I, T, C>.FilteredPhaseData) node).copyFor(context)
				);
				SortableNode.link(copy, nextCopy);
			});
		}

		this.sortedPhases.addAll(this.phases.values());

		this.sortPhases();
		this.rebuildInvoker(this.sortedPhases.stream().mapToInt(value -> value.listeners.length).sum());
	}

	/**
	 * {@return the context of this contextualized event}
	 */
	@Contract(pure = true)
	public @NotNull C context() {
		return this.context;
	}

	void registerFromParent(@NotNull I phaseIdentifier, @NotNull T listener, @NotNull Predicate<C> selector) {
		if (selector.test(context)) {
			this.register(phaseIdentifier, listener);
		}
	}
}
