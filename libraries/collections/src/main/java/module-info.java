/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

module dev.yumi.commons.collections {
	requires transitive dev.yumi.commons.core;
	requires org.slf4j;

	exports dev.yumi.commons.collections;
	exports dev.yumi.commons.collections.toposort;
}
