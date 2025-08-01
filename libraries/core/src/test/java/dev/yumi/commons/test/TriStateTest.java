/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.test;

import dev.yumi.commons.TriState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class TriStateTest {
	private static final String PROPERTY_KEY = "yumi.commons.test.tristate.from";
	private static final List<String> TRUE_STRINGS = List.of("true", "TRUE", "True", "on", "ON", "On");
	private static final List<String> FALSE_STRINGS = List.of("false", "FALSE", "False", "off", "OFF", "Off");

	@Test
	public void testFrom() {
		assertSame(TriState.TRUE, TriState.from(true));
		assertSame(TriState.FALSE, TriState.from(false));

		assertSame(TriState.TRUE, TriState.from(Boolean.TRUE));
		assertSame(TriState.FALSE, TriState.from(Boolean.FALSE));
		assertSame(TriState.DEFAULT, TriState.from(null));
	}

	@Test
	public void testFromString() {
		for (var value : TRUE_STRINGS) {
			assertSame(TriState.TRUE, TriState.fromString(value),
					"Expected TriState.TRUE for String value \"" + value + "\"."
			);
		}
		for (var value : FALSE_STRINGS) {
			assertSame(TriState.FALSE, TriState.fromString(value),
					"Expected TriState.FALSE for String value \"" + value + "\"."
			);
		}
		assertSame(TriState.DEFAULT, TriState.fromString(null));
		assertSame(TriState.DEFAULT, TriState.fromString("null"));
		assertSame(TriState.DEFAULT, TriState.fromString("Hello World!"));
	}

	@Test
	public void testFromProperty() {
		System.getProperties().remove(PROPERTY_KEY);
		assertSame(TriState.DEFAULT, TriState.fromProperty(PROPERTY_KEY));
		System.setProperty(PROPERTY_KEY, "");
		assertSame(TriState.DEFAULT, TriState.fromProperty(PROPERTY_KEY));
		System.setProperty(PROPERTY_KEY, "Hello World!");
		assertSame(TriState.DEFAULT, TriState.fromString(PROPERTY_KEY));

		for (var value : TRUE_STRINGS) {
			System.setProperty(PROPERTY_KEY, value);
			assertSame(TriState.TRUE, TriState.fromProperty(PROPERTY_KEY),
					"Expected TriState.TRUE for Property value \"" + value + "\"."
			);
		}
		for (var value : FALSE_STRINGS) {
			System.setProperty(PROPERTY_KEY, value);
			assertSame(TriState.FALSE, TriState.fromProperty(PROPERTY_KEY),
					"Expected TriState.FALSE for Property value \"" + value + "\"."
			);
		}
	}

	@Test
	public void testToBoolean() {
		assertEquals(Boolean.TRUE, TriState.TRUE.toBoolean());
		assertEquals(Boolean.FALSE, TriState.FALSE.toBoolean());
		assertNull(TriState.DEFAULT.toBoolean());
	}

	@Test
	public void testToBooleanOrElse() {
		assertTrue(TriState.TRUE.toBooleanOrElse(true));
		assertTrue(TriState.TRUE.toBooleanOrElse(false));
		assertFalse(TriState.FALSE.toBooleanOrElse(true));
		assertFalse(TriState.FALSE.toBooleanOrElse(false));
		assertTrue(TriState.DEFAULT.toBooleanOrElse(true));
		assertFalse(TriState.DEFAULT.toBooleanOrElse(false));
	}

	@Test
	public void testToBooleanOrElseGet() {
		assertTrue(TriState.TRUE.toBooleanOrElseGet(() -> {
			throw new AssertionError("Fallback supplier should not be called for toBooleanOrElseGet on TriState.TRUE");
		}));
		assertFalse(TriState.FALSE.toBooleanOrElseGet(() -> {
			throw new AssertionError("Fallback supplier should not be called for toBooleanOrElseGet on TriState.FALSE");
		}));
		assertTrue(TriState.DEFAULT.toBooleanOrElseGet(() -> true));
		assertFalse(TriState.DEFAULT.toBooleanOrElseGet(() -> false));
	}

	@Test
	public void testToBooleanOrThrow() {
		assertTrue(TriState.TRUE.toBooleanOrElseThrow());
		assertFalse(TriState.FALSE.toBooleanOrElseThrow());
		assertThrows(NoSuchElementException.class, TriState.DEFAULT::toBooleanOrElseThrow);
	}

	@Test
	public void testToBooleanOrThrowGet() {
		assertTrue(TriState.TRUE.toBooleanOrElseThrow(() -> {
			throw new AssertionError("Exception supplier should not be called for toBooleanOrElseThrow on TriState.TRUE");
		}));
		assertFalse(TriState.FALSE.toBooleanOrElseThrow(() -> {
			throw new AssertionError("Exception supplier should not be called for toBooleanOrElseThrow on TriState.FALSE");
		}));
		assertThrows(IllegalStateException.class, () -> TriState.DEFAULT.toBooleanOrElseThrow(IllegalStateException::new));
	}

	@Test
	public void testOr() {
		// Classic boolean OR truth map.
		assertEquals(TriState.FALSE, TriState.FALSE.or(TriState.FALSE));
		assertEquals(TriState.TRUE, TriState.TRUE.or(TriState.FALSE));
		assertEquals(TriState.TRUE, TriState.FALSE.or(TriState.TRUE));
		assertEquals(TriState.TRUE, TriState.TRUE.or(TriState.TRUE));

		// Now, the messy part.
		assertEquals(TriState.DEFAULT, TriState.DEFAULT.or(TriState.DEFAULT));
		assertEquals(TriState.DEFAULT, TriState.FALSE.or(TriState.DEFAULT));
		assertEquals(TriState.TRUE, TriState.TRUE.or(TriState.DEFAULT));
		assertEquals(TriState.DEFAULT, TriState.DEFAULT.or(TriState.FALSE));
		assertEquals(TriState.TRUE, TriState.DEFAULT.or(TriState.TRUE));
	}

	@Test
	public void testLazyOr() {
		// Classic boolean OR truth map.
		assertEquals(TriState.FALSE, TriState.FALSE.or(rightSide(TriState.FALSE, true)));
		assertEquals(TriState.TRUE, TriState.TRUE.or(rightSide(TriState.FALSE, false)));
		assertEquals(TriState.TRUE, TriState.FALSE.or(rightSide(TriState.TRUE, true)));
		assertEquals(TriState.TRUE, TriState.TRUE.or(rightSide(TriState.TRUE, false)));

		// Now, the messy part.
		assertEquals(TriState.DEFAULT, TriState.DEFAULT.or(rightSide(TriState.DEFAULT, true)));
		assertEquals(TriState.DEFAULT, TriState.FALSE.or(rightSide(TriState.DEFAULT, true)));
		assertEquals(TriState.TRUE, TriState.TRUE.or(rightSide(TriState.DEFAULT, false)));
		assertEquals(TriState.DEFAULT, TriState.DEFAULT.or(rightSide(TriState.FALSE, true)));
		assertEquals(TriState.TRUE, TriState.DEFAULT.or(rightSide(TriState.TRUE, true)));
	}

	@Test
	public void testAnd() {
		// Classic boolean AND truth map.
		assertEquals(TriState.FALSE, TriState.FALSE.and(TriState.FALSE));
		assertEquals(TriState.FALSE, TriState.TRUE.and(TriState.FALSE));
		assertEquals(TriState.FALSE, TriState.FALSE.and(TriState.TRUE));
		assertEquals(TriState.TRUE, TriState.TRUE.and(TriState.TRUE));

		// Now, the messy part.
		assertEquals(TriState.DEFAULT, TriState.DEFAULT.and(TriState.DEFAULT));
		assertEquals(TriState.FALSE, TriState.FALSE.and(TriState.DEFAULT));
		assertEquals(TriState.DEFAULT, TriState.TRUE.and(TriState.DEFAULT));
		assertEquals(TriState.FALSE, TriState.DEFAULT.and(TriState.FALSE));
		assertEquals(TriState.DEFAULT, TriState.DEFAULT.and(TriState.TRUE));
	}

	@Test
	public void testLazyAnd() {
		// Classic boolean AND truth map.
		assertEquals(TriState.FALSE, TriState.FALSE.and(rightSide(TriState.FALSE, false)));
		assertEquals(TriState.FALSE, TriState.TRUE.and(rightSide(TriState.FALSE, true)));
		assertEquals(TriState.FALSE, TriState.FALSE.and(rightSide(TriState.TRUE, false)));
		assertEquals(TriState.TRUE, TriState.TRUE.and(rightSide(TriState.TRUE, true)));

		// Now, the messy part.
		assertEquals(TriState.DEFAULT, TriState.DEFAULT.and(rightSide(TriState.DEFAULT, true)));
		assertEquals(TriState.FALSE, TriState.FALSE.and(rightSide(TriState.DEFAULT, false)));
		assertEquals(TriState.DEFAULT, TriState.TRUE.and(rightSide(TriState.DEFAULT, true)));
		assertEquals(TriState.FALSE, TriState.DEFAULT.and(rightSide(TriState.FALSE, true)));
		assertEquals(TriState.DEFAULT, TriState.DEFAULT.and(rightSide(TriState.TRUE, true)));
	}

	private static Supplier<TriState> rightSide(TriState state, boolean allowCall) {
		return () -> {
			if (!allowCall) {
				fail("Right side operand should not be evaluated.");
			}

			return state;
		};
	}
}
