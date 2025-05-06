/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker.dynamic;

import dev.yumi.commons.event.invoker.InvokerFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

/**
 * Represents a factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners
 * for which the invoker implementation is dynamically generated.
 *
 * @param <T> the type of the invoker executed by the event
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
// TODO Java 25: replace ASM with the Class File API.
@ApiStatus.Internal
sealed abstract class DynamicInvokerFactory<T> extends InvokerFactory<T>
		permits FilterInvokerFactory, SequenceInvokerFactory, TriStateFilterInvokerFactory {
	private static final String SELF_BINARY_NAME = DynamicInvokerFactory.class.getName().replace('.', '/');
	private static final Module MODULE = DynamicInvokerFactory.class.getModule();
	protected static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
	protected final MethodType listenerMethodType;
	protected final MethodType lambdaMethodType;
	protected final Function<T[], T> factory;

	protected DynamicInvokerFactory(@NotNull Class<? super T> type) {
		this(type, getFunctionalMethod(type));
	}

	protected DynamicInvokerFactory(@NotNull Class<? super T> type, @NotNull Method listenerMethod) {
		super(type);
		this.checkMethod(listenerMethod);
		this.ensureModuleConstraints();

		try {
			this.listenerMethodType = MethodType.methodType(listenerMethod.getReturnType(), listenerMethod.getParameterTypes());
			this.lambdaMethodType = this.listenerMethodType.insertParameterTypes(0, type.arrayType());

			var implementationInnerClassRawName = this.type.getSimpleName() + "Impl";

			var implName = "$" + this.getClass().getSimpleName();
			var implementationFullInnerClassRawName = DynamicInvokerFactory.class.getSimpleName() + implName;
			var implementationClassRawName = SELF_BINARY_NAME + implName;

			this.factory = this.buildClass(
					listenerMethod.getName(),
					this.type.getName().replace('.', '/'),
					new ImplementationName(
							implementationInnerClassRawName,
							implementationFullInnerClassRawName,
							implementationClassRawName
					)
			);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			throw new DynamicInvokerCreationException("Failed to generate the dynamic invoker factory", e);
		}
	}

	/**
	 * Checks whether the given invoker method is valid for this invoker factory.
	 *
	 * @param method the invoker method
	 */
	@Contract(pure = true)
	protected abstract void checkMethod(@NotNull Method method);

	private void ensureModuleConstraints() {
		// Since we are defining a class that implements type at runtime,
		// we need to ensure we can actually read the class we implement.
		MODULE.addReads(this.type.getModule());
	}

	@SuppressWarnings("unchecked")
	private Function<T[], T> buildClass(
			String listenerMethodName,
			String typeRawName,
			ImplementationName name
	) throws IllegalAccessException, NoSuchMethodException {
		String factoryDescriptor = MethodType.methodType(this.type, this.type.arrayType()).toMethodDescriptorString();

		var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
		cw.visit(Opcodes.V17, ACC_PUBLIC | ACC_FINAL | ACC_SUPER,
				name.classRawName(),
				"Ljava/util/function/Function<[L" + typeRawName + ";" + typeRawName + ";>;",
				"java/lang/Object",
				new String[]{
						"java/util/function/Function",
				}
		);
		cw.visitSource(name.fullInnerClassRawName(), null);

		{ // Write implementation class constructor
			var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		{ // Write Function apply bridge
			var mv = cw.visitMethod(
					ACC_PUBLIC | ACC_SYNTHETIC | ACC_BRIDGE,
					"apply", "(Ljava/lang/Object;)Ljava/lang/Object;",
					null, null
			);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, "[L" + typeRawName + ";");
			mv.visitMethodInsn(
					INVOKEVIRTUAL,
					name.classRawName(), "apply", "([L%s;)L%s;".formatted(typeRawName, typeRawName),
					false
			);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}

		{ // Write lambda
			var mv = cw.visitMethod(
					ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC,
					"lambda$apply", this.lambdaMethodType.toMethodDescriptorString(),
					null, null
			);

			var context = new WriterContext(
					listenerMethodName,
					typeRawName,
					name.innerClassName(),
					name.fullInnerClassRawName(),
					name.classRawName(),
					this.getParamTable()
			);

			this.writeImplementationMethod(mv, context);

			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		{ // Write Function apply bridge
			var mv = cw.visitMethod(
					ACC_PUBLIC,
					"apply", factoryDescriptor,
					null, null
			);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInvokeDynamicInsn(
					listenerMethodName, factoryDescriptor,
					new Handle(
							H_INVOKESTATIC,
							SELF_BINARY_NAME, "metafactory",
							"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
							false
					),
					Descriptors.asmType(this.listenerMethodType),
					new Handle(
							H_INVOKESTATIC,
							name.classRawName(), "lambda$apply",
							this.lambdaMethodType.toMethodDescriptorString(),
							false
					),
					Descriptors.asmType(this.listenerMethodType)
			);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
		}

		byte[] bytes = cw.toByteArray();
		var lookup = LOOKUP.defineHiddenClass(bytes, true);
		var constructor = lookup.findConstructor(lookup.lookupClass(), MethodType.methodType(void.class));

		try {
			return (Function<T[], T>) constructor.invoke();
		} catch (Throwable e) {
			throw new DynamicInvokerCreationException("Failed to instantiate the generated invoker factory constructor.", e);
		}
	}

	// This is used as the replacement implementation of LambdaMetaFactory so that the lambda generated
	// in the invoker hidden class is actually callable.
	// This can be package-private because the hidden class is nestmate of this class.
	// Thanks to https://stackoverflow.com/a/71249339
	@SuppressWarnings("unused")
	static CallSite metafactory(
			MethodHandles.Lookup caller, String interfaceMethodName, MethodType factoryType,
			MethodType interfaceMethodType, MethodHandle implementation, MethodType dynamicMethodType
	) throws Throwable {
		MethodHandle invoker = MethodHandles.exactInvoker(implementation.type());
		if (factoryType.parameterCount() == 0) {
			// Non-capturing lambda.
			factoryType = factoryType.appendParameterTypes(MethodHandle.class);
			CallSite cs = LambdaMetafactory.metafactory(
					caller, interfaceMethodName, factoryType, interfaceMethodType, invoker, dynamicMethodType
			);
			Object instance = cs.dynamicInvoker()
					.asType(MethodType.methodType(Object.class, MethodHandle.class))
					.invokeExact(implementation);
			return new ConstantCallSite(MethodHandles.constant(factoryType.returnType(), instance));
		} else {
			// Capturing.
			// Should not happen in our case.
			MethodType lambdaMt = factoryType.insertParameterTypes(0, MethodHandle.class);
			CallSite cs = LambdaMetafactory.metafactory(
					caller, interfaceMethodName, lambdaMt, interfaceMethodType, invoker, dynamicMethodType
			);
			return new ConstantCallSite(cs.dynamicInvoker().bindTo(implementation));
		}
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
		// Get array length into local variable.
		mv.visitVarInsn(ALOAD, context.listenersVar());
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
		mv.visitVarInsn(ALOAD, context.listenersVar());
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
		return this.factory.apply(listeners);
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
		private final String listenerMethodName;
		private final String typeRawName;
		private final String implementationInnerClassRawName;
		private final String implementationFullInnerClassRawName;
		private final String implementationClassRawName;
		private final ParamTable paramTable;
		private final int listenersLengthVar;
		private final int iVar;
		private final Label forStartLabel = new Label();
		private final Label forEndLabel = new Label();

		WriterContext(
				String listenerMethodName,
				String typeRawName,
				String implementationInnerClassRawName,
				String implementationFullInnerClassRawName,
				String implementationClassRawName,
				ParamTable paramTable
		) {
			this.listenerMethodName = listenerMethodName;
			this.typeRawName = typeRawName;
			this.implementationInnerClassRawName = implementationInnerClassRawName;
			this.implementationFullInnerClassRawName = implementationFullInnerClassRawName;
			this.implementationClassRawName = implementationClassRawName;
			this.paramTable = paramTable;
			this.listenersLengthVar = paramTable.localStart + 1;
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
			return 0;
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

		public void writeMethodInvoke(MethodVisitor mv) {
			mv.visitMethodInsn(INVOKEINTERFACE,
					this.typeRawName,
					this.listenerMethodName, DynamicInvokerFactory.this.listenerMethodType.toMethodDescriptorString(),
					true
			);
		}
	}

	protected record ParamTable(int localStart, List<LocalEntry> params) {}

	protected record LocalEntry(int index, Class<?> type) {}

	private record ImplementationName(String innerClassName, String outerClassRawName, String outerClassFullRawName) {
		public String fullInnerClassRawName() {
			return this.outerClassRawName + "$" + this.innerClassName;
		}

		public String classRawName() {
			return this.outerClassFullRawName + "$" + this.innerClassName;
		}
	}

	static class DynamicInvokerCreationException extends RuntimeException {
		public DynamicInvokerCreationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
