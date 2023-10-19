/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *   Copyright 2016, 2017, 2018, 2019 FabricMC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.yumi.commons.collections.toposort;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a graph node that can be sorted using topological sorting.
 * <p>
 * A node may have previous and subsequent nodes.
 *
 * @param <I> the type of the node identifier
 * @param <N> the type of the node
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class SortableNode<I, N extends SortableNode<I, N>> {
	final List<N> nextNodes = new ArrayList<>();
	final List<N> previousNodes = new ArrayList<>();
	boolean visited = false;

	/**
	 * {@return the identifier of this node}
	 */
	@Contract(pure = true)
	public abstract @NotNull I getId();

	/**
	 * Links two given nodes together.
	 *
	 * @param first the node that should be sorted first
	 * @param second the node that should be sorted second
	 * @param <N> the type of the sortable node
	 */
	public static <I, N extends SortableNode<I, N>> void link(@NotNull N first, @NotNull N second) {
		if (first == second) {
			throw new IllegalArgumentException("Cannot link a node to itself!");
		}

		first.nextNodes.add(second);
		second.previousNodes.add(first);
	}
}
