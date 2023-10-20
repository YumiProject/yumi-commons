/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker;

final class Descriptors {
	private Descriptors() {
		throw new UnsupportedOperationException("Descriptors only contain static definitions.");
	}

	public static final String VOID = "V";
	public static final String BOOLEAN = "Z";
	public static final String CHAR = "C";
	public static final String BYTE = "B";
	public static final String SHORT = "S";
	public static final String INT = "I";
	public static final String LONG = "J";
	public static final String FLOAT = "F";
	public static final String DOUBLE = "D";

	public static String describe(Class<?> c) {
		if (c == void.class) {
			return VOID;
		} else if (c == boolean.class) {
			return BOOLEAN;
		} else if (c == char.class) {
			return CHAR;
		} else if (c == byte.class) {
			return BYTE;
		} else if (c == short.class) {
			return SHORT;
		} else if (c == int.class) {
			return INT;
		} else if (c == long.class) {
			return LONG;
		} else if (c == float.class) {
			return FLOAT;
		} else if (c == double.class) {
			return DOUBLE;
		} else if (c.isArray()) {
			return '[' + describe(c.componentType());
		} else {
			return 'L' + c.getName().replace('.', '/') + ';';
		}
	}

	public static int getTypeSize(String descriptor) {
		return switch (descriptor) {
			case LONG, DOUBLE -> 2;
			default -> 1;
		};
	}
}
