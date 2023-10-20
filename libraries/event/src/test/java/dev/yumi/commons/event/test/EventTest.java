/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.test;

import dev.yumi.commons.TriState;
import dev.yumi.commons.collections.YumiCollections;
import dev.yumi.commons.event.EventManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
	private static final EventManager<String> EVENTS = new EventManager<>("default");

	@Test
	public void test() {
		var tester = new ExecutionTester();
		var event = EVENTS.create(TestCallback.class);

		event.register(text -> tester.assertOrder(0));
		event.register(text -> tester.assertOrder(1));
		event.register(text -> tester.assertOrder(2));

		event.invoker().call("3");
		tester.assertCalled(3);
	}

	@Test
	public void testPhases() {
		var tester = new ExecutionTester();
		record Entry(String phase, List<TestCallback> listeners) {
			Entry(String phase, TestCallback listener) {
				this(phase, List.of(listener));
			}
		}

		YumiCollections.forAllPermutations(
				List.of(
						new Entry("very_early", text -> tester.assertOrder(0)),
						new Entry("early", text -> tester.assertOrder(1)),
						new Entry("default", List.of(text -> tester.assertOrder(2), text -> tester.assertOrder(3))),
						new Entry("late", text -> tester.assertOrder(4)),
						new Entry("very_late", List.of(
								text -> tester.assertOrder(5),
								text -> tester.assertOrder(6),
								text -> tester.assertOrder(7)
						))
				),
				entries -> {
					var event = EVENTS.createWithPhases(TestCallback.class,
							"very_early", "early", "default", "late", "very_late"
					);

					tester.reset();
					for (var entry : entries) {
						for (var listener : entry.listeners) {
							event.register(entry.phase, listener);
						}
					}

					event.invoker().call("Hello World!");
					tester.assertCalled(8);
				});
	}

	@Test
	public void testDefaultInvoker() {
		var tester = new ExecutionTester();
		var event = EVENTS.create(TestCallback.class);

		tester.reset();
		event.invoker().call("Empty");
		tester.assertCalled(0);

		tester.reset();
		event.register(text -> {
			tester.assertOrder(0);
		});

		event.invoker().call("Single listener");
		tester.assertCalled(1);

		tester.reset();
		event.register(line -> {
			tester.assertOrder(1);
		});

		event.invoker().call("Multiple listeners");
		tester.assertCalled(2);
	}

	@Test
	public void testDefaultInvokerForFilter() {
		var tester = new ExecutionTester();
		var event = EVENTS.create(FilterTestCallback.class);

		assertFalse(event.invoker().call("Empty"));

		tester.reset();
		event.register(text -> {
			tester.assertOrder(0);
			return text.isEmpty();
		});

		assertTrue(event.invoker().call(""));
		tester.assertCalled(1);
		tester.reset();
		assertFalse(event.invoker().call("Single listener"));
		tester.assertCalled(1);

		tester.reset();
		event.register(line -> {
			tester.assertOrder(1);
			return line.contains("e");
		});

		assertTrue(event.invoker().call("Hello World!"));
		tester.assertCalled(2);
		tester.reset();
		assertTrue(event.invoker().call(""));
		tester.assertCalled(1);
		tester.reset();
		assertFalse(event.invoker().call("Hi World!"));
		tester.assertCalled(2);
	}

	@Test
	public void testDefaultInvokerForTriStateFilter() {
		var event = EVENTS.create(TriStateCallback.class);

		assertEquals(TriState.DEFAULT, event.invoker().call("Empty"));

		event.register(text -> {
			if (text.isEmpty()) {
				return TriState.FALSE;
			} else {
				return TriState.DEFAULT;
			}
		});

		assertEquals(TriState.DEFAULT, event.invoker().call("Single listener!"));
		assertEquals(TriState.FALSE, event.invoker().call(""));

		event.register(line -> {
			if (line.endsWith("World!") || line.isBlank()) {
				return TriState.TRUE;
			} else {
				return TriState.DEFAULT;
			}
		});

		assertEquals(TriState.TRUE, event.invoker().call("Hello World!"));
		assertEquals(TriState.FALSE, event.invoker().call(""));
		assertEquals(TriState.DEFAULT, event.invoker().call("Yippee"));

		event.register(text -> {
			if (text.isBlank()) {
				return TriState.TRUE;
			} else {
				return TriState.DEFAULT;
			}
		});

		assertEquals(TriState.TRUE, event.invoker().call("Good night World!"));
		assertEquals(TriState.FALSE, event.invoker().call(""));
		assertEquals(TriState.TRUE, event.invoker().call("\t"));
		assertEquals(TriState.DEFAULT, event.invoker().call("Whoop"));
	}

	static class ExecutionTester {
		private int calls;

		public void reset() {
			this.calls = 0;
		}

		public void assertOrder(int order) {
			assertEquals(order, this.calls, "Expected listener n°" + order + " to be called.");
			this.calls++;
		}

		public void assertCalled(int called) {
			assertEquals(called, this.calls, "Expected a specific amount of listener calls.");
		}
	}
}
