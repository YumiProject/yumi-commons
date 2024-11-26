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
import dev.yumi.commons.event.FilteredEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FilteredEventTest {
	private static final EventManager<String> EVENTS = new EventManager<>("default", Function.identity());

	@Test
	public void test() {
		var tester = new ExecutionTester();
		var event = EVENTS.createFiltered(TestCallback.class, String.class);

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

	@Test
	public void testPhases() {
		var tester = new ExecutionTester();

		record Listener(int order, Predicate<String> selector) {
			void register(String phase, ExecutionTester tester, FilteredEvent<String, TestCallback, String> event) {
				if (this.selector == null) {
					event.register(phase, text -> tester.assertOrder(this.order));
				} else {
					event.register(phase, text -> tester.assertOrder(this.order), this.selector);
					event.register(phase, text -> tester.skip(), this.selector.negate());
				}
			}
		}

		record Entry(String phase, List<Listener> listeners) {
			Entry(String phase, Listener listener) {
				this(phase, List.of(listener));
			}
		}

		YumiCollections.forAllPermutations(
				List.of(
						new Entry("very_early", new Listener(0, null)),
						new Entry("early", new Listener(1, null)),
						new Entry("default", List.of(
								new Listener(2, context -> context.equals("contextualized")),
								new Listener(3, null)
						)),
						new Entry("late", new Listener(4, context -> context.equals("contextualized"))),
						new Entry("very_late", List.of(
								new Listener(5, null),
								new Listener(6, context -> context.equals("some other context")),
								new Listener(7, null)
						))
				),
				entries -> {
					var event = EVENTS.createFilteredWithPhases(TestCallback.class, String.class,
							"very_early", "early", "default", "late", "very_late"
					);

					tester.reset();
					for (var entry : entries) {
						for (var listener : entry.listeners) {
							listener.register(entry.phase, tester, event);
						}
					}

					tester.useStrictOrder(false);
					event.invoker().call("Hello world!");
					tester.assertCalled(5);

					tester.reset();

					event.forContext("contextualized").invoker().call("Hello world!");
					tester.assertCalled(7);

					tester.reset();

					event.forContext("some other context").invoker().call("Hello world!");
					tester.assertCalled(6);
				});
	}
}
