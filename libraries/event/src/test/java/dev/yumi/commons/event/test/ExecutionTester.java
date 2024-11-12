/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutionTester {
	private int callOrder;
	private boolean strictOrder = true;
	private int calls;

	public ExecutionTester useStrictOrder(boolean strictOrder) {
		this.strictOrder = strictOrder;
		return this;
	}

	public ExecutionTester reset() {
		this.callOrder = 0;
		this.strictOrder = true;
		this.calls = 0;

		return this;
	}

	public void assertOrder(int order) {
		if (this.strictOrder) {
			assertEquals(order, this.callOrder, "Expected listener n°" + order + " to be called.");
			this.callOrder++;
		} else {
			assertTrue(this.callOrder <= order, "Expected any listener before n°" + order + " to be called.");
			this.callOrder = order;
		}

		this.calls++;
	}

	public void skip() {
		this.callOrder++;
	}

	public void assertCalled(int called) {
		assertEquals(called, this.calls, "Expected a specific amount of listener calls.");
	}
}
