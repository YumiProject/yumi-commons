/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.test;

import dev.yumi.commons.event.Event;
import dev.yumi.commons.event.EventManager;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertSame;

public class EventCreationTest {
	@Test
	public void test() {
		var events = new EventManager<>("default", Function.identity());
		var tester = new ExecutionTester();
		var lastEvent = new Event[1];

		events.getCreationEvent().register((manager, event) -> {
			tester.assertOrder(0);
			lastEvent[0] = event;
		});

		var event = events.create(TestCallback.class);
		tester.assertCalled(1);
		assertSame(event, lastEvent[0]);

		tester.reset();
		var event2 = events.create(TestCallback.class);
		tester.assertCalled(1);
		assertSame(event2, lastEvent[0]);

		tester.reset();
		var filterEvent = events.create(FilterTestCallback.class);
		tester.assertCalled(1);
		assertSame(filterEvent, lastEvent[0]);
	}
}
