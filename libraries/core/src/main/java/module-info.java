/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * <h2>The Yumi Commons Core Library</h2>
 * <p>
 * This library provides core utilities as a foundational stone of the Yumi ecosystem.
 * <p>
 * Those utilities may be useful data types (like {@link dev.yumi.commons.Either}), utility methods, and functional utilities.
 */
module dev.yumi.commons.core {
	requires static transitive org.jetbrains.annotations;

	exports dev.yumi.commons;
	exports dev.yumi.commons.function;
}
