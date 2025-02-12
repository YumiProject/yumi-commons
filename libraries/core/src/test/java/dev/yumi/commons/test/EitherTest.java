/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.test;

import dev.yumi.commons.Either;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class EitherTest {
	@Test
	public void testIs() {
		var left = Either.left(1L);
		var right = Either.right("Hello");

		assertTrue(left.isLeft(), "Either.left didn't return true to isLeft.");
		assertFalse(left.isRight(), "Either.left didn't return false to isRight.");
		assertTrue(right.isRight(), "Either.right didn't return true to isRight.");
		assertFalse(right.isLeft(), "Either.right didn't return false to isLeft.");
	}

	@Test
	public void testGet() {
		var left = Either.left(1L);
		var right = Either.right("Hello");

		assertEquals(1L, left.getLeft());
		assertEquals("Hello", right.getRight());
		assertThrows(NoSuchElementException.class, left::getRight);
		assertThrows(NoSuchElementException.class, right::getLeft);
	}

	@Test
	public void testIf() {
		var left = Either.left(1L);
		var right = Either.right("Hello");
		var calls = new int[1];

		left.ifLeft(value -> {
			assertEquals(1L, value);
			calls[0]++;
		});
		assertEquals(1, calls[0], "Expected ifLeft on Either.Left to execute action.");

		left.ifRight(value -> {
			throw new AssertionError("Expected ifRight on Either.Left to not execute anything.");
		});

		right.ifLeft(value -> {
			throw new AssertionError("Expected ifLeft on Either.Right to not execute anything.");
		});

		right.ifRight(value -> {
			assertEquals("Hello", value);
			calls[0]++;
		});
		assertEquals(2, calls[0], "Expected ifRight on Either.Right to execute action.");
	}

	@Test
	public void testIfOrElse() {
		var left = Either.left(1L);
		var right = Either.right("Hello");
		var calls = new int[1];

		left.ifLeftOrElse(value -> {
			assertEquals(1L, value);
			calls[0]++;
		}, () -> {
			throw new AssertionError("Expected ifLeftOrElse on Either.Left to not execute right action.");
		});
		assertEquals(1, calls[0], "Expected ifLeftOrElse on Either.Left to execute left action.");

		left.ifRightOrElse(value -> {
			throw new AssertionError("Expected ifRightOrElse on Either.Left to not execute right action.");
		}, () -> calls[0]++);
		assertEquals(2, calls[0], "Expected ifRightOrElse on Either.Left to execute left action.");

		right.ifLeftOrElse(value -> {
			throw new AssertionError("Expected ifLeftOrElse on Either.Right to not execute left action.");
		}, () -> calls[0]++);
		assertEquals(3, calls[0], "Expected ifLeftOrElse on Either.Right to execute right action.");

		right.ifRightOrElse(value -> {
			assertEquals("Hello", value);
			calls[0]++;
		}, () -> {
			throw new AssertionError("Expected ifRightOrElse on Either.Right to not execute left action.");
		});
		assertEquals(4, calls[0], "Expected ifRightOrElse on Either.Right to execute right action.");
	}

	@Test
	public void testApply() {
		var left = Either.left(1L);
		var right = Either.right("Hello");
		var calls = new int[1];

		left.apply(leftValue -> {
			assertEquals(1L, leftValue);
			calls[0]++;
		}, rightValue -> {
			throw new AssertionError("Expected apply on Either.Left to not execute right action.");
		});
		assertEquals(1, calls[0], "Expected apply on Either.Left to execute left action.");

		right.apply(leftValue -> {
			throw new AssertionError("Expected apply on Either.Right to not execute left action.");
		}, rightValue -> {
			assertEquals("Hello", rightValue);
			calls[0]++;
		});
		assertEquals(2, calls[0], "Expected apply on Either.Right to execute right action.");
	}

	@Test
	public void testMap() {
		var left = Either.left(1L);
		var right = Either.right("Hello");

		assertEquals(Either.left(2), left.mapLeft(value -> value.intValue() + 1));
		assertSame(left, left.mapRight(o -> {
			throw new AssertionError("mapRight on Either.Left should not call mapper function.");
		}), "mapRight on Either.Left should return itself.");

		assertSame(right, right.mapLeft(o -> {
			throw new AssertionError("mapLeft on Either.Right should not call mapper function.");
		}), "mapLeft on Either.Right should return itself.");
		assertEquals(Either.right("Hello World!"), right.mapRight(value -> value + " World!"));

		assertEquals(Either.left(42), left.map(value -> value.intValue() + 41, o -> {
			throw new AssertionError("map on Either.Left should not call right mapper function.");
		}));
		assertEquals(Either.right("Hello Earth!"), right.map(o -> {
			throw new AssertionError("map on Either.Right should not call left mapper function.");
		}, value -> value + " Earth!"));
	}

	@Test
	public void testFlatMap() {
		var left = Either.left(1L);
		var right = Either.right("Hello");

		assertEquals(Either.left(2), left.flatMapLeft(value -> Either.left(value.intValue() + 1)));
		assertSame(left, left.flatMapRight(o -> {
			throw new AssertionError("mapRight on Either.Left should not call mapper function.");
		}), "mapRight on Either.Left should return itself.");

		assertSame(right, right.flatMapLeft(o -> {
			throw new AssertionError("mapLeft on Either.Right should not call mapper function.");
		}), "mapLeft on Either.Right should return itself.");
		assertEquals(Either.right("Hello World!"), right.flatMapRight(value -> Either.right(value + " World!")));

		assertEquals(Either.right("Hello 1!"), left.flatMapLeft(value -> Either.right("Hello " + value + "!")));
		assertEquals(Either.left(5), right.flatMapRight(value -> Either.left(value.length())));
	}

	@Test
	public void testFold() {
		var left = Either.left(1L);
		var right = Either.right("Hello");

		assertEquals(42, left.fold(value -> value.intValue() + 41, this.<Object, Integer>withReturn(o -> {
			throw new AssertionError("fold on Either.Left should not call right mapper function.");
		})));
		assertEquals("Hello World!", right.fold(o -> {
			throw new AssertionError("fold on Either.Right should not call left mapper function.");
		}, value -> value + " World!"));
	}

	@Test
	public void testSwap() {
		var left = Either.left(1L);
		var right = Either.right("Hello");

		assertEquals(Either.right(1L), left.swap());
		assertEquals(Either.left("Hello"), right.swap());
	}

	@SuppressWarnings("unchecked")
	private <T, O> Function<T, O> withReturn(Function<T, ?> function) {
		return (Function<T, O>) function;
	}
}
