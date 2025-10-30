/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.test;

import dev.yumi.commons.collections.YumiCollections;
import dev.yumi.commons.event.EventManager;
import dev.yumi.commons.event.ListenerPhase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventListenAllTest {
	@Test
	public void testListenAllRejectNoEvents() {
		var events = new EventManager<>("default", Function.identity());

		assertThrows(IllegalArgumentException.class, () -> events.listenAll(new Listener(new ExecutionTester())));
	}

	@Test
	public void testListenAllRejectMissingEvents() {
		var events = new EventManager<>("default", Function.identity());
		var testEvent = events.create(TestCallback.class);
		var testFilterEvent = events.create(FilterTestCallback.class);

		assertThrows(IllegalArgumentException.class, () -> events.listenAll(new Object(), testEvent, testFilterEvent));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testListenAllRejectGenerics() {
		var events = new EventManager<>("default", Function.identity());
		var testGenericEvent = events.create(Generic.class, listeners -> arg -> {
			for (var listener : listeners) {
				listener.invoke(arg);
			}
		});
		var generic = new Generic<String>() {
			@Override
			public void invoke(String arg) {
			}
		};

		assertThrows(IllegalArgumentException.class, () -> events.listenAll(generic, testGenericEvent));
	}

	@Test
	public void testListenAll() {
		var events = new EventManager<>("default", Function.identity());
		var testEvent = events.create(TestCallback.class);
		var testFilterEvent = events.create(FilterTestCallback.class);
		var tester = new ExecutionTester();

		events.listenAll(new Listener(tester), testEvent, testFilterEvent);

		testEvent.invoker().call("Test");
		tester.assertCalled(1);

		testFilterEvent.invoker().filter("Yippee");
		tester.assertCalled(2);
	}

	@Test
	public void testListenAllPhases() {
		var events = new EventManager<>("default", Function.identity());
		var tester = new ExecutionTester();

		YumiCollections.forAllPermutations(
				List.of(
						List.of(new PhaseListenerEarly(tester)),
						List.of(new PhaseListenerDefault(tester), new PhaseListenerExplicitDefault(tester)),
						List.of(new PhaseListenerLate(tester))
				),
				listeners -> {
					var testEvent = events.createWithPhases(TestCallback.class, "early", "default", "late");
					var filterEvent = events.createWithPhases(FilterTestCallback.class, "early", "default", "late");

					for (var phaseListeners : listeners) {
						for (var listener : phaseListeners) {
							events.listenAll(listener, testEvent, filterEvent);
						}
					}

					tester.reset();
					testEvent.invoker().call("Test");
					tester.assertCalled(4);
					tester.reset();
					assertTrue(filterEvent.invoker().filter("Yippee"));
					tester.assertCalled(3);
				});
	}

	interface Generic<T> {
		void invoke(T arg);
	}

	@ListenerPhase(callbackTarget = TestCallback.class, value = "early")
	@ListenerPhase(callbackTarget = FilterTestCallback.class, value = "late")
	record PhaseListenerEarly(ExecutionTester tester) implements TestCallback, FilterTestCallback {
		@Override
		public void call(String text) {
			this.tester.assertOrder(0);
		}

		@Override
		public boolean filter(String text) {
			throw new IllegalStateException("The filter callback should have already returned a value.");
		}
	}

	record PhaseListenerDefault(ExecutionTester tester) implements TestCallback, FilterTestCallback {
		@Override
		public void call(String text) {
			this.tester.assertOrder(1);
		}

		@Override
		public boolean filter(String text) {
			this.tester.assertOrder(1);
			return false;
		}
	}

	@ListenerPhase(callbackTarget = TestCallback.class, value = "default")
	@ListenerPhase(callbackTarget = FilterTestCallback.class, value = "default")
	record PhaseListenerExplicitDefault(ExecutionTester tester) implements TestCallback, FilterTestCallback {
		@Override
		public void call(String text) {
			this.tester.assertOrder(2);
		}

		@Override
		public boolean filter(String text) {
			this.tester.assertOrder(2);
			return true;
		}
	}

	@ListenerPhase(callbackTarget = TestCallback.class, value = "late")
	@ListenerPhase(callbackTarget = FilterTestCallback.class, value = "early")
	record PhaseListenerLate(ExecutionTester tester) implements TestCallback, FilterTestCallback {
		@Override
		public void call(String text) {
			this.tester.assertOrder(3);
		}

		@Override
		public boolean filter(String text) {
			this.tester.assertOrder(0);
			return false;
		}
	}

	record Listener(ExecutionTester tester) implements TestCallback, FilterTestCallback {

		@Override
		public void call(String text) {
			this.tester.assertOrder(0);
		}

		@Override
		public boolean filter(String text) {
			this.tester.assertOrder(1);
			return false;
		}
	}
}
