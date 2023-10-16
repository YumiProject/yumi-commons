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

package dev.yumi.commons.collections.test.toposort;

import dev.yumi.commons.collections.toposort.NodeSorting;
import dev.yumi.commons.collections.toposort.SortableNode;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class NodeSortingTest {
	@Test
	public void test() {
		var veryEarly = new TestNode("very_early");
		var early = new TestNode("early");
		var def = new TestNode("default");
		var late = new TestNode("late");
		var veryLate = new TestNode("very_late");

		TestNode.link(veryEarly, early);
		TestNode.link(early, def);
		TestNode.link(def, late);
		TestNode.link(late, veryLate);

		testAllPermutations(new ArrayList<>(), List.of(veryEarly, early, def, late, veryLate), testNodes -> {
			var testList = new ArrayList<>(testNodes);

			assertTrue(NodeSorting.sort(testList, "test nodes"));
			assertSame(veryEarly, testList.get(0));
			assertSame(early, testList.get(1));
			assertSame(def, testList.get(2));
			assertSame(late, testList.get(3));
			assertSame(veryLate, testList.get(4));
		});
	}


	/**
	 * Ensure that nodes get sorted deterministically regardless of the order in which constraints are registered.
	 *
	 * <p>The graph is displayed here as ASCII art, and also in the file graph.png.
	 * <pre>
	 *             +-------------------+
	 *             v                   |
	 * +---+     +---+     +---+     +---+
	 * | a | --> | z | --> | b | --> | y |
	 * +---+     +---+     +---+     +---+
	 *             ^
	 *             |
	 *             |
	 * +---+     +---+
	 * | d | --> | e |
	 * +---+     +---+
	 * +---+
	 * | f |
	 * +---+
	 * </pre>
	 * Notice the cycle z -> b -> y -> z. The elements of the cycle are ordered [b, y, z],
	 * and the cycle itself is ordered with its lowest id "b".
	 * We get for the final order: [a, d, e, cycle [b, y, z], f].
	 */
	@Test
	public void testDeterministicOrdering() {
		var a = new TestNode("a");
		var b = new TestNode("b");
		var d = new TestNode("d");
		var e = new TestNode("e");
		var f = new TestNode("f");
		var y = new TestNode("y");
		var z = new TestNode("z");

		TestNode.link(a, z);
		TestNode.link(d, e);
		TestNode.link(e, z);
		TestNode.link(z, b);
		TestNode.link(b, y);
		TestNode.link(y, z);

		var list = new ArrayList<>(List.of(
				a, b, d, e, f, y, z
		));

		testAllPermutations(new ArrayList<>(), list, testNodes -> {
			var testList = new ArrayList<>(testNodes);

			NodeSorting.ENABLE_CYCLE_WARNING = false;
			boolean result = NodeSorting.sort(testList, "test nodes");
			NodeSorting.ENABLE_CYCLE_WARNING = true;

			assertFalse(result, "No cycles were detected while there is a cycle.");
			assertSame(a, testList.get(0));
			assertSame(d, testList.get(1));
			assertSame(e, testList.get(2));
			assertSame(b, testList.get(3));
			assertSame(y, testList.get(4));
			assertSame(z, testList.get(5));
			assertSame(f, testList.get(6));
		});
	}

	/**
	 * Test deterministic node sorting with two cycles.
	 * <pre>
	 * e --> a <--> b <-- d <--> c
	 * </pre>
	 */
	@Test
	public void testTwoCycles() {
		var a = new TestNode("a");
		var b = new TestNode("b");
		var c = new TestNode("c");
		var d = new TestNode("d");
		var e = new TestNode("e");

		TestNode.link(e, a);
		TestNode.link(a, b);
		TestNode.link(b, a);
		TestNode.link(d, b);
		TestNode.link(d, c);
		TestNode.link(c, d);

		var list = new ArrayList<>(List.of(
				a, b, e, d, c
		));

		testAllPermutations(new ArrayList<>(), list, testNodes -> {
			var testList = new ArrayList<>(testNodes);

			NodeSorting.ENABLE_CYCLE_WARNING = false;
			boolean result = NodeSorting.sort(testList, "test nodes");
			NodeSorting.ENABLE_CYCLE_WARNING = true;

			assertFalse(result, "No cycles were detected while there is two cycles.");
			assertSame(c, testList.get(0));
			assertSame(d, testList.get(1));
			assertSame(e, testList.get(2));
			assertSame(a, testList.get(3));
			assertSame(b, testList.get(4));
		});
	}

	@SuppressWarnings("SuspiciousListRemoveInLoop")
	private static <T> void testAllPermutations(List<T> selected, List<T> toSelect, Consumer<List<T>> action) {
		if (toSelect.isEmpty()) {
			action.accept(selected);
		} else {
			for (int i = 0; i < toSelect.size(); ++i) {
				selected.add(toSelect.get(i));
				List<T> remaining = new ArrayList<>(toSelect);
				remaining.remove(i);
				testAllPermutations(selected, remaining, action);
				selected.remove(selected.size() - 1);
			}
		}
	}

	private static class TestNode extends SortableNode<String, TestNode> {
		private final String id;

		private TestNode(String id) {
			this.id = id;
		}

		@Override
		public @NotNull String getId() {
			return this.id;
		}
	}
}
