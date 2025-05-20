/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.collections.test.toposort;

import dev.yumi.commons.collections.YumiCollections;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcatTest {
	@Test
	public void testListConcatTwo() {
		var result = YumiCollections.concat(List.of("a", "b", "c"), List.of("d", "e"));
		assertEquals(5, result.size());
		assertEquals("a", result.get(0));
		assertEquals("b", result.get(1));
		assertEquals("c", result.get(2));
		assertEquals("d", result.get(3));
		assertEquals("e", result.get(4));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testListConcatN() {
		YumiCollections.forAllPermutations(
				List.of(
						List.of("a", "b", "c"),
						List.of("d", "e"),
						List.of("f", "g", "h", "i", "j"),
						List.of("x", "y", "z")
				),
				lists -> {
					var result = YumiCollections.concat(lists.get(0), lists.get(1), lists.subList(2, lists.size()).toArray(List[]::new));
					assertEquals(13, result.size());

					int fullIndex = 0;
					for (int i = 0; i < 3; i++) {
						var list = lists.get(i);

						for (int j = 0; j < list.size(); j++) {
							assertEquals(
									list.get(j), result.get(fullIndex++),
									"Could not validate %d concat index with %d index of list %d".formatted(fullIndex, j, i)
							);
						}
					}
				}
		);
	}

	@Test
	public void testSetConcatTwo() {
		var first = Set.of("a", "b", "c", "z");
		var second = Set.of("d", "e", "c", "z");
		var result = YumiCollections.concat(first, second);
		assertEquals(6, result.size());
		assertTrue(result.containsAll(first));
		assertTrue(result.containsAll(second));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetConcatN() {
		YumiCollections.forAllPermutations(
				List.of(
						Set.of("a", "b", "c", "d"),
						Set.of("d", "e", "x", "y", "z"),
						Set.of("f", "g", "h", "i", "j"),
						Set.of("x", "y", "z", "c")
				),
				sets -> {
					var result = YumiCollections.concat(sets.get(0), sets.get(1), sets.subList(2, sets.size()).toArray(Set[]::new));
					assertEquals(13, result.size());

					for (int i = 0; i < 3; i++) {
						var set = sets.get(i);
						assertTrue(result.containsAll(set));
					}
				}
		);
	}
}
