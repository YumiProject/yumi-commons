/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker;

import static org.objectweb.asm.Opcodes.*;

final class OpcodeUtils {
	private OpcodeUtils() {
		throw new UnsupportedOperationException("OpcodeUtils only contains static definition.");
	}

	public static int getLoadOpcodeFromType(Class<?> clazz) {
		if (clazz == int.class || clazz == boolean.class || clazz == byte.class || clazz == char.class || clazz == short.class) {
			return ILOAD;
		} else if (clazz == long.class) {
			return LLOAD;
		} else if (clazz == float.class) {
			return FLOAD;
		} else if (clazz == double.class) {
			return DLOAD;
		} else {
			return ALOAD;
		}
	}

	public static int getLoadOpcodeFromType(String type) {
		return switch (type) {
			case Descriptors.BOOLEAN, Descriptors.CHAR, Descriptors.BYTE, Descriptors.SHORT, Descriptors.INT -> ILOAD;
			case Descriptors.LONG -> LLOAD;
			case Descriptors.FLOAT -> FLOAD;
			case Descriptors.DOUBLE -> DLOAD;
			default -> ALOAD;
		};
	}
}
