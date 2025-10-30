/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.invoker.dynamic;

import module java.base;
import module org.jetbrains.annotations;

import dev.yumi.commons.event.invoker.InvokerFactory;

import static java.lang.classfile.ClassFile.*;
import static java.lang.constant.ConstantDescs.*;

/// Represents a factory of an invoker implementation of an {@link dev.yumi.commons.event.Event} given an array of listeners
/// for which the invoker implementation is dynamically generated.
///
/// @param <T> the type of the invoker executed by the event
/// @author LambdAurora
/// @version 2.0.0
/// @since 1.0.0
@ApiStatus.Internal
sealed abstract class DynamicInvokerFactory<T> extends InvokerFactory<T>
		permits FilterInvokerFactory, SequenceInvokerFactory, TriStateFilterInvokerFactory {
	private static final String SELF_BINARY_NAME = DynamicInvokerFactory.class.getName().replace('.', '/');
	private static final ClassDesc CD_SELF = ClassDesc.of(DynamicInvokerFactory.class.getName());
	private static final ClassDesc CD_FUNCTION = ClassDesc.of(Function.class.getName());
	private static final MethodTypeDesc METAFACTORY_METHOD_DESC = MethodTypeDesc.of(
			CD_CallSite, CD_MethodHandles_Lookup, CD_String, CD_MethodType, CD_MethodType, CD_MethodHandle, CD_MethodType
	);
	private static final Module MODULE = DynamicInvokerFactory.class.getModule();
	protected static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
	protected final MethodType listenerMethodType;
	protected final MethodTypeDesc listenerMethodTypeDesc;
	protected final MethodTypeDesc lambdaMethodType;
	protected final Function<T[], T> factory;

	protected DynamicInvokerFactory(Class<? super T> type) {
		this(type, getFunctionalMethod(type));
	}

	protected DynamicInvokerFactory(Class<? super T> type, Method listenerMethod) {
		super(type);
		this.checkMethod(listenerMethod);
		this.ensureModuleConstraints(listenerMethod);

		try {
			this.listenerMethodType = MethodType.methodType(listenerMethod.getReturnType(), listenerMethod.getParameterTypes());
			this.listenerMethodTypeDesc = MethodTypeDesc.ofDescriptor(this.listenerMethodType.descriptorString());
			this.lambdaMethodType = MethodTypeDesc.ofDescriptor(this.listenerMethodType.insertParameterTypes(0, type.arrayType()).descriptorString());

			var implementationInnerClassRawName = this.type.getSimpleName() + "Impl";

			var implName = "$" + this.getClass().getSimpleName();
			var implementationClassRawName = SELF_BINARY_NAME + implName;

			this.factory = this.buildClass(
					listenerMethod.getName(),
					ClassDesc.of(this.type.getName()),
					new ImplementationName(
							implementationInnerClassRawName,
							implementationClassRawName
					)
			);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			throw new DynamicInvokerCreationException("Failed to generate the dynamic invoker factory", e);
		}
	}

	/// Checks whether the given invoker method is valid for this invoker factory.
	///
	/// @param method the invoker method
	@Contract(pure = true)
	protected abstract void checkMethod(Method method);

	private void ensureModuleConstraints(Method method) {
		// Since we are defining a class that implements type at runtime,
		// we need to ensure we can actually read the class we implement...
		MODULE.addReads(this.type.getModule());
		// as well as the classes we manipulate in the listener method.
		ensureModuleConstraintForType(method.getReturnType());
		for (var paramType : method.getParameterTypes()) {
			ensureModuleConstraintForType(paramType);
		}
	}

	private static void ensureModuleConstraintForType(Class<?> type) {
		if (type.isPrimitive()) return;
		if (type.isArray()) {
			ensureModuleConstraintForType(type.componentType());
			return;
		}
		MODULE.addReads(type.getModule());
	}

	@SuppressWarnings("unchecked")
	private Function<T[], T> buildClass(
			String listenerMethodName,
			ClassDesc typeDesc,
			ImplementationName name
	) throws IllegalAccessException, NoSuchMethodException {
		var classDesc = ClassDesc.ofInternalName(name.classRawName());
		var typeArrayDesc = typeDesc.arrayType();
		var factoryDescriptor = MethodTypeDesc.of(typeDesc, typeArrayDesc);

		byte[] bytes = ClassFile.of().build(
				classDesc,
				cb -> cb
						.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL, AccessFlag.SUPER)
						.withInterfaceSymbols(CD_FUNCTION)
						// Write implementation class constructor
						.withMethodBody(INIT_NAME, MTD_void, ACC_PUBLIC,
								mb -> mb.aload(0).invokespecial(CD_Object, INIT_NAME, MTD_void).return_())
						// Write Function apply bridge
						.withMethodBody("apply", MethodTypeDesc.of(CD_Object, CD_Object), ACC_PUBLIC | ACC_SYNTHETIC | ACC_BRIDGE,
								mb -> mb
										.aload(0)
										.aload(1)
										.checkcast(typeArrayDesc)
										.invokevirtual(classDesc, "apply", factoryDescriptor)
										.areturn()
						)
						.withMethodBody("apply", factoryDescriptor, ACC_PUBLIC,
								mb -> mb
										.aload(1)
										.invokedynamic(DynamicCallSiteDesc.of(
												MethodHandleDesc.ofMethod(
														DirectMethodHandleDesc.Kind.STATIC,
														CD_SELF, "metafactory", METAFACTORY_METHOD_DESC
												),
												listenerMethodName, factoryDescriptor,
												this.listenerMethodTypeDesc,
												MethodHandleDesc.ofMethod(
														DirectMethodHandleDesc.Kind.STATIC,
														classDesc, "lambda$apply", this.lambdaMethodType
												),
												this.listenerMethodTypeDesc
										))
										.areturn()
						)
						.withMethodBody("lambda$apply", this.lambdaMethodType, ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC,
								mb -> {
									var context = new WriterContext(
											mb,
											new WriterData(
													typeDesc,
													listenerMethodName,
													this.listenerMethodTypeDesc,
													this.getParamTable()
											)
									);

									this.writeImplementationMethod(context);
								})
		);

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

	protected abstract void writeImplementationMethod(WriterContext context);

	@Override
	public T apply(T[] listeners) {
		if (listeners.length == 1) {
			return listeners[0];
		} else {
			return this.factory.apply(listeners);
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

	protected static class WriterData {
		private final ClassDesc typeDesc;
		private final String listenerMethodName;
		private final MethodTypeDesc listenerMethodTypeDesc;
		private final ParamTable paramTable;
		private final int listenersLengthVar;
		private final int iVar;

		WriterData(ClassDesc typeDesc, String listenerMethodName, MethodTypeDesc listenerMethodTypeDesc, ParamTable paramTable) {
			this.typeDesc = typeDesc;
			this.listenerMethodName = listenerMethodName;
			this.listenerMethodTypeDesc = listenerMethodTypeDesc;
			this.paramTable = paramTable;
			this.listenersLengthVar = paramTable.localStart + 1;
			this.iVar = this.listenersLengthVar + 1;
		}

		public ClassDesc typeDesc() {
			return this.typeDesc;
		}

		public String listenerMethodName() {
			return this.listenerMethodName;
		}

		public MethodTypeDesc listenerMethodTypeDesc() {
			return this.listenerMethodTypeDesc;
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
	}

	protected record WriterContext(CodeBuilder codeBuilder, WriterData data) {
		public void writeMethodInvoke() {
			this.codeBuilder.invokeinterface(this.data.typeDesc(), this.data.listenerMethodName(), this.data.listenerMethodTypeDesc());
		}

		protected void writeMethod(Consumer<WriterContext> blockConsumer) {
			this.preparePreLoop();
			this.writeLoop(block -> {
				// Prepare to invoke the listener.
				for (var param : this.data.paramTable().params()) {
					OpcodeUtils.doLoadFromType(block.codeBuilder, param.type(), param.index());
				}
				block.writeMethodInvoke();
				blockConsumer.accept(block);
			});
		}

		public void preparePreLoop() {
			// Get array length into local variable.
			this.codeBuilder
					.aload(this.data.listenersVar())
					.arraylength()
					.istore(this.data.listenersLengthVar())
					// int i = 0;
					.iconst_0()
					.istore(this.data.iVar());
		}

		protected void writeLoop(Consumer<WriterContext> blockConsumer) {
			// Start of the loop.
			this.codeBuilder.block(block -> {
				var blockContext = new WriterContext(block, this.data);

				block
						.iload(this.data.iVar())
						.iload(this.data.listenersLengthVar())
						.if_icmpge(block.endLabel()) // if (i >= listeners.length) goto end;
						// Load listeners[i]
						.aload(this.data.listenersVar())
						.iload(this.data.iVar())
						.aaload();
				// Delegate to user code.
				blockConsumer.accept(blockContext);

				blockContext.writeIncrement();
			});
		}

		public void writeIncrement() {
			this.codeBuilder
					.iinc(this.data.iVar(), 1)
					.goto_(this.codeBuilder.startLabel());
		}
	}

	protected record ParamTable(int localStart, List<LocalEntry> params) {}

	protected record LocalEntry(int index, Class<?> type) {}

	private record ImplementationName(String innerClassName, String outerClassFullRawName) {
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
