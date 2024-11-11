/*
 * Copyright 2024 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.test;

import dev.yumi.commons.collections.YumiCollections;
import dev.yumi.commons.event.EventManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class FilteredEventTest {
	private static final EventManager<String> EVENTS = new EventManager<>("default", Function.identity());

	@Test
	public void test() {
		var tester = new ExecutionTester();
		var event = EVENTS.<TestCallback, String>createFiltered(TestCallback.class);

		event.register(text -> tester.assertOrder(0));
		event.register(text -> tester.assertOrder(1));
		event.register(text -> tester.assertOrder(2));
		event.register(text -> tester.assertOrder(3), context -> context.equals("test context"));

		// Without filtering.
		event.invoker().call("3");
		tester.assertCalled(3);

		tester.reset();

		var filtered = event.forContext("test context");
		filtered.invoker().call("4");
		tester.assertCalled(4);

		tester.reset();

		filtered = event.forContext("other context");
		filtered.invoker().call("3 again");
		tester.assertCalled(3);
	}

	// @TODO test phases with filtered event
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
}
