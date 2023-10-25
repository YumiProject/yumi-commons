/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecutionTester {
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
