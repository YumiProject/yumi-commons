/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker.dynamic;

import module org.jetbrains.annotations;

import java.lang.classfile.CodeBuilder;

@ApiStatus.Internal
final class OpcodeUtils {
	private OpcodeUtils() {
		throw new UnsupportedOperationException("OpcodeUtils only contains static definition.");
	}

	public static void doLoadFromType(CodeBuilder mb, Class<?> clazz, int index) {
		if (clazz == int.class || clazz == boolean.class || clazz == byte.class || clazz == char.class || clazz == short.class) {
			mb.iload(index);
		} else if (clazz == long.class) {
			mb.lload(index);
		} else if (clazz == float.class) {
			mb.fload(index);
		} else if (clazz == double.class) {
			mb.dload(index);
		} else {
			mb.aload(index);
		}
	}
}
