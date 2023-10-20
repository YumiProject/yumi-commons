/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

/**
 * Represents a factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners
 * for which the invoker implementation is dynamically generated.
 *
 * @param <T> the type of the invoker executed by the event
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class DynamicInvokerFactory<T> extends InvokerFactory<T> {
	private static final AtomicInteger CLASS_COUNTER = new AtomicInteger(0);
	protected static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
	protected final MethodType listenerMethodType;
	protected final Class<? extends T> implementationClass;

	protected DynamicInvokerFactory(@NotNull Class<? super T> type) {
		this(type, getFunctionalMethod(type));
	}

	protected DynamicInvokerFactory(@NotNull Class<? super T> type, @NotNull Method listenerMethod) {
		super(type);
		this.checkMethod(listenerMethod);

		try {
			this.listenerMethodType = MethodType.methodType(listenerMethod.getReturnType(), listenerMethod.getParameterTypes());
			var implementationInnerClassRawName = this.type.getSimpleName() + CLASS_COUNTER.getAndIncrement() + "Impl";

			var implName = "$" + implementationInnerClassRawName;
			//noinspection ConstantValue
			if (this.getClass() != DynamicInvokerFactory.class) {
				implName = "$" + this.getClass().getSimpleName() + implName;
			}
			var implementationFullInnerClassRawName = DynamicInvokerFactory.class.getSimpleName() + implName;
			var implementationClassRawName = DynamicInvokerFactory.class.getName().replace('.', '/') + implName;

			this.implementationClass = this.buildClass(
					listenerMethod.getName(),
					this.type.getName().replace('.', '/'),
					implementationInnerClassRawName,
					implementationFullInnerClassRawName,
					implementationClassRawName
			);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Contract(pure = true)
	protected abstract void checkMethod(@NotNull Method method);

	@SuppressWarnings("unchecked")
	private Class<? extends T> buildClass(
			String listenerMethodName,
			String typeRawName,
			String implementationInnerClassRawName,
			String implementationFullInnerClassRawName,
			String implementationClassRawName
	) throws IllegalAccessException {
		var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
		cw.visit(Opcodes.V17, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, implementationClassRawName, null, "java/lang/Object", new String[]{
				typeRawName
		});
		cw.visitSource(implementationFullInnerClassRawName, null);

		var context = new WriterContext(cw,
				listenerMethodName,
				typeRawName,
				implementationInnerClassRawName,
				implementationFullInnerClassRawName,
				implementationClassRawName,
				this.getParamTable()
		);

		context.addField("listeners", Descriptors.describe(this.type.arrayType()));

		{ // Write implementation class constructor
			var descriptor = new StringBuilder("(");
			for (var field : context.fields.values()) {
				descriptor.append(field);
			}
			descriptor.append(")V");

			var mv = cw.visitMethod(ACC_PUBLIC, "<init>", descriptor.toString(), null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			int offset = 1;
			for (var field : context.fields.entrySet()) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(OpcodeUtils.getLoadOpcodeFromType(field.getValue()), offset);
				mv.visitFieldInsn(PUTFIELD, implementationClassRawName, field.getKey(), field.getValue());

				offset += Descriptors.getTypeSize(field.getValue());
			}
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		{
			var mv = cw.visitMethod(ACC_PUBLIC, listenerMethodName, this.listenerMethodType.toMethodDescriptorString(), null, null);

			this.writeImplementationMethod(mv, context);

			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		byte[] bytes = cw.toByteArray();
		return (Class<? extends T>) LOOKUP.defineClass(bytes);
	}

	protected abstract void writeImplementationMethod(MethodVisitor mv, WriterContext context);

	protected void writeMethodStart(MethodVisitor mv, WriterContext context) {
		this.preparePreLoop(mv, context);
		this.writeLoopStart(mv, context);
		// Prepare to invoke the listener.
		for (var param : context.paramTable().params()) {
			mv.visitVarInsn(OpcodeUtils.getLoadOpcodeFromType(param.type()), param.index());
		}
		context.writeMethodInvoke(mv);
	}

	protected void preparePreLoop(MethodVisitor mv, WriterContext context) {
		// Load listeners field into local variable.
		mv.visitVarInsn(ALOAD, 0);
		context.writeGetField(mv, "listeners");
		mv.visitVarInsn(ASTORE, context.listenersVar);
		// Get array length into local variable.
		mv.visitVarInsn(ALOAD, context.listenersVar);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitVarInsn(ISTORE, context.listenersLengthVar);
		// int i = 0;
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, context.iVar);
	}

	protected void writeLoopStart(MethodVisitor mv, WriterContext context) {
		// Start of the loop.
		mv.visitLabel(context.forStartLabel);
		mv.visitVarInsn(ILOAD, context.iVar);
		mv.visitVarInsn(ILOAD, context.listenersLengthVar);
		mv.visitJumpInsn(IF_ICMPGE, context.forEndLabel); // if (i >= listeners.length) goto end;
		mv.visitLabel(new Label());
		// Load listeners[i]
		mv.visitVarInsn(ALOAD, context.listenersVar);
		mv.visitVarInsn(ILOAD, context.iVar);
		mv.visitInsn(AALOAD);
	}

	protected void writeIncrement(MethodVisitor mv, WriterContext context) {
		mv.visitLabel(new Label());
		mv.visitIincInsn(context.iVar, 1);
		mv.visitJumpInsn(GOTO, context.forStartLabel);
	}

	@Override
	public T apply(T[] listeners) {
		try {
			return this.implementationClass.getDeclaredConstructor(this.type.arrayType()).newInstance(new Object[]{listeners});
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	protected ParamTable getParamTable() {
		var params = new ArrayList<LocalEntry>();
		int localStart = 1;

		for (int i = 0; i < this.listenerMethodType.parameterCount(); i++) {
			Class<?> paramType = this.listenerMethodType.parameterType(i);

			params.add(new LocalEntry(localStart, paramType));

			if (paramType == long.class || paramType == double.class) {
				localStart += 2;
			} else {
				localStart++;
			}
		}

		return new ParamTable(localStart, params);
	}

	protected class WriterContext {
		private final ClassWriter cw;
		private final String listenerMethodName;
		private final String typeRawName;
		private final String implementationInnerClassRawName;
		private final String implementationFullInnerClassRawName;
		private final String implementationClassRawName;
		private final ParamTable paramTable;
		private final int listenersVar;
		private final int listenersLengthVar;
		private final int iVar;
		private final Label forStartLabel = new Label();
		private final Label forEndLabel = new Label();
		private final Map<String, String> fields = new HashMap<>();

		WriterContext(
				ClassWriter cw,
				String listenerMethodName,
				String typeRawName,
				String implementationInnerClassRawName,
				String implementationFullInnerClassRawName,
				String implementationClassRawName,
				ParamTable paramTable
		) {
			this.cw = cw;
			this.listenerMethodName = listenerMethodName;
			this.typeRawName = typeRawName;
			this.implementationInnerClassRawName = implementationInnerClassRawName;
			this.implementationFullInnerClassRawName = implementationFullInnerClassRawName;
			this.implementationClassRawName = implementationClassRawName;
			this.paramTable = paramTable;
			this.listenersVar = paramTable.localStart;
			this.listenersLengthVar = this.listenersVar + 1;
			this.iVar = this.listenersLengthVar + 1;
		}

		public String listenerMethodName() {
			return this.listenerMethodName;
		}

		public String typeRawName() {
			return this.typeRawName;
		}

		public String implementationInnerClassRawName() {
			return this.implementationInnerClassRawName;
		}

		public String implementationFullInnerClassRawName() {
			return this.implementationFullInnerClassRawName;
		}

		public String implementationClassRawName() {
			return this.implementationClassRawName;
		}

		public ParamTable paramTable() {
			return this.paramTable;
		}

		public int listenersVar() {
			return this.listenersVar;
		}

		public int listenersLengthVar() {
			return this.listenersLengthVar;
		}

		public int iVar() {
			return this.iVar;
		}

		public Label getForStartLabel() {
			return this.forStartLabel;
		}

		public Label getForEndLabel() {
			return this.forEndLabel;
		}

		public void addField(String name, String descriptor) {
			this.fields.put(name, descriptor);
			cw.visitField(ACC_PRIVATE | ACC_FINAL, name, descriptor, null, null);
		}

		public void writeGetField(MethodVisitor mv, String name) {
			mv.visitFieldInsn(GETFIELD, this.implementationClassRawName, name, this.fields.get(name));
		}

		public void writeMethodInvoke(MethodVisitor mv) {
			mv.visitMethodInsn(INVOKEINTERFACE,
					this.typeRawName,
					this.listenerMethodName, DynamicInvokerFactory.this.listenerMethodType.toMethodDescriptorString(),
					true
			);
		}
	}

	public record ParamTable(int localStart, List<LocalEntry> params) {}

	public record LocalEntry(int index, Class<?> type) {}
}
