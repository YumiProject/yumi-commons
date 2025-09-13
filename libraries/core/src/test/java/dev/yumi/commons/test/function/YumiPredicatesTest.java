/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.test.function;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static dev.yumi.commons.function.YumiPredicates.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class YumiPredicatesTest {
	@Test
	public void testAnyOf() {
		assertFalse(anyOf().test(null));
		assertTrue(anyOf(Objects::isNull).test(null));

		assertFalse(anyOf(alwaysFalse(), alwaysFalse()).test(null));
		assertTrue(anyOf(alwaysTrue(), alwaysFalse()).test(null));
		assertTrue(anyOf(alwaysFalse(), alwaysTrue()).test(null));
		assertTrue(anyOf(alwaysTrue(), o -> {
			throw new AssertionError("Should not reach here. Second operand should not evaluate.");
		}).test(null));

		assertFalse(anyOf(alwaysFalse(), alwaysFalse(), alwaysFalse()).test(null));
		assertTrue(anyOf(alwaysFalse(), alwaysTrue(), o -> {
			throw new AssertionError("Should not reach here. Third operand should not evaluate.");
		}).test(null));
	}

	@Test
	public void testAllOf() {
		assertFalse(allOf().test(null));
		assertTrue(allOf(Objects::isNull).test(null));

		assertFalse(allOf(alwaysFalse(), alwaysFalse()).test(null));
		assertFalse(allOf(alwaysTrue(), alwaysFalse()).test(null));
		assertFalse(allOf(alwaysFalse(), alwaysTrue()).test(null));
		assertTrue(allOf(alwaysTrue(), alwaysTrue()).test(null));

		assertFalse(allOf(alwaysFalse(), alwaysFalse(), alwaysFalse()).test(null));
		assertFalse(allOf(alwaysFalse(), alwaysTrue(), alwaysFalse()).test(null));
		assertTrue(allOf(alwaysTrue(), alwaysTrue(), alwaysTrue()).test(null));
	}
}
