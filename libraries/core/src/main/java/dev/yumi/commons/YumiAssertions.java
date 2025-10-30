/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A set of various assertion utilities.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class YumiAssertions {
	private YumiAssertions() {
		throw new UnsupportedOperationException("YumiAssertions only contains static definitions.");
	}

	/**
	 * Ensures that the given array does not contain duplicates, or throws an exception otherwise.
	 *
	 * @param items the array of items to check
	 * @param exceptionFactory the exception factory in the case of a duplicate
	 * @param <T> the type of items of the array
	 * @param <E> the exception factory when the check fails
	 */
	public static <T, E extends Throwable> void ensureNoDuplicates(
			@Nullable T[] items,
			Function<@Nullable T, E> exceptionFactory
	) throws E {
		for (int i = 0; i < items.length; ++i) {
			for (int j = i + 1; j < items.length; ++j) {
				if (Objects.equals(items[i], items[j])) {
					throw exceptionFactory.apply(items[i]);
				}
			}
		}
	}
}
