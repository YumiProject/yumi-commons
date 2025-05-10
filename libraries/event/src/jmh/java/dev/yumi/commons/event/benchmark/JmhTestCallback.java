/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.benchmark;

import org.openjdk.jmh.infra.Blackhole;

@FunctionalInterface
public interface JmhTestCallback {
	void call(Blackhole blackhole, String dummy);
}
