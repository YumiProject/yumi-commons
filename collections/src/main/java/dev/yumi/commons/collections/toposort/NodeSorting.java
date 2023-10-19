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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Contains a topological sort implementation, with tie breaking using a {@link Comparator}.
 * <p>
 * The final order is always deterministic (i.e. doesn't change with the order of the input elements or the edges),
 * assuming that they are all different according to the comparator. This also holds in the presence of cycles.
 * <p>
 * The steps are as follows:
 * <ol>
 * 	<li>Compute node SCCs (Strongly Connected Components, i.e. cycles).</li>
 * 	<li>Sort nodes within SCCs using the comparator.</li>
 * 	<li>Sort SCCs with respect to each other by respecting constraints, and using the comparator in case of a tie.</li>
 * </ol>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class NodeSorting {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeSorting.class);

	@VisibleForTesting
	public static boolean ENABLE_CYCLE_WARNING = true;

	private NodeSorting() {
		throw new UnsupportedOperationException("NodeSorting only contains static-definitions.");
	}

	/**
	 * Sorts a list of nodes of a graph.
	 * <p>
	 * Uses the node comparable identifier as the tie-breaker to order elements within a cycle.
	 *
	 * @param sortedNodes the list of nodes to sort which will be modified in-place
	 * @param elementDescription a description of the elements, used for logging in the presence of cycles
	 * @return {@code true} if all the constraints were satisfied, {@code false} if there was at least one cycle
	 * @see #sort(List, String, Comparator)
	 */
	public static <I extends Comparable<? super I>, N extends SortableNode<I, N>> boolean sort(
			@NotNull List<N> sortedNodes,
			@NotNull String elementDescription
	) {
		return sort(sortedNodes, elementDescription, Comparator.comparing(SortableNode::getId));
	}

	/**
	 * Sorts a list of nodes of a graph.
	 *
	 * @param sortedNodes the list of nodes to sort which will be modified in-place
	 * @param elementDescription a description of the elements, used for logging in the presence of cycles
	 * @param comparator the comparator to break ties and to order elements within a cycle
	 * @return {@code true} if all the constraints were satisfied, {@code false} if there was at least one cycle
	 * @see #sort(List, String)
	 */
	public static <I, N extends SortableNode<I, N>> boolean sort(
			@NotNull List<N> sortedNodes,
			@NotNull String elementDescription,
			@NotNull Comparator<N> comparator
	) {
		// FIRST KOSARAJU SCC VISIT
		var toposort = new ArrayList<N>(sortedNodes.size());

		for (N node : sortedNodes) {
			forwardVisit(node, toposort);
		}

		clearStatus(toposort);
		Collections.reverse(toposort);

		// SECOND KOSARAJU SCC VISIT
		var nodeToScc = new IdentityHashMap<N, NodeScc<I, N>>();

		for (N node : toposort) {
			if (!node.visited) {
				List<N> sccNodes = new ArrayList<>();
				// Collect nodes in SCC.
				backwardVisit(node, sccNodes);
				// Sort nodes.
				sccNodes.sort(comparator);
				// Mark nodes as belonging to this SCC.
				var scc = new NodeScc<>(sccNodes);

				for (N nodeInScc : sccNodes) {
					nodeToScc.put(nodeInScc, scc);
				}
			}
		}

		clearStatus(toposort);

		// Build SCC graph
		for (var scc : nodeToScc.values()) {
			for (N node : scc.nodes) {
				for (N nextNode : node.nextNodes) {
					NodeScc<I, N> nextScc = nodeToScc.get(nextNode);

					if (nextScc != scc) {
						scc.nextSccs.add(nextScc);
						nextScc.inDegree++;
					}
				}
			}
		}

		// Order SCCs according to the priorities. When there is a choice, use the SCC with the lowest id.
		// The priority queue contains all SCCs that currently have 0 in-degree.
		var pq = new PriorityQueue<NodeScc<I, N>>(Comparator.comparing(scc -> scc.nodes.get(0), comparator));
		sortedNodes.clear();

		for (NodeScc<I, N> scc : nodeToScc.values()) {
			if (scc.inDegree == 0) {
				pq.add(scc);
				// Prevent adding the same SCC multiple times, as nodeToScc may contain the same value multiple times.
				scc.inDegree = -1;
			}
		}

		boolean noCycle = true;

		while (!pq.isEmpty()) {
			NodeScc<I, N> scc = pq.poll();
			sortedNodes.addAll(scc.nodes);

			if (scc.nodes.size() > 1) {
				noCycle = false;

				if (ENABLE_CYCLE_WARNING) {
					// Print cycle warning.
					var builder = new StringBuilder();
					builder.append("Found cycle while sorting ").append(elementDescription).append(":\n");

					for (N node : scc.nodes) {
						builder.append("\t").append(node.getId()).append("\n");
					}

					LOGGER.warn("{}", builder);
				}
			}

			for (NodeScc<I, N> nextScc : scc.nextSccs) {
				nextScc.inDegree--;

				if (nextScc.inDegree == 0) {
					pq.add(nextScc);
				}
			}
		}

		return noCycle;
	}

	@ApiStatus.Internal
	private static <I, N extends SortableNode<I, N>> void forwardVisit(N node, List<N> toposort) {
		if (!node.visited) {
			// Not yet visited.
			node.visited = true;

			for (N data : node.nextNodes) {
				forwardVisit(data, toposort);
			}

			toposort.add(node);
		}
	}

	@ApiStatus.Internal
	private static <I, N extends SortableNode<I, N>> void clearStatus(List<N> nodes) {
		for (N node : nodes) {
			node.visited = false;
		}
	}

	@ApiStatus.Internal
	private static <I, N extends SortableNode<I, N>> void backwardVisit(N node, List<N> sccNodes) {
		if (!node.visited) {
			node.visited = true;
			sccNodes.add(node);

			for (N data : node.previousNodes) {
				backwardVisit(data, sccNodes);
			}
		}
	}

	@ApiStatus.Internal
	private static class NodeScc<I, N extends SortableNode<I, N>> {
		final List<N> nodes;
		final List<NodeScc<I, N>> nextSccs = new ArrayList<>();
		int inDegree = 0;

		private NodeScc(List<N> nodes) {
			this.nodes = nodes;
		}
	}
}
