/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.benchmark;

import dev.yumi.commons.event.invoker.InvokerFactory;
import dev.yumi.commons.event.invoker.SequenceInvokerFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

public class SequenceInvokerFactoryBenchmark {
	private static final JmhTestCallback[] DUMMY_CALLBACKS = new JmhTestCallback[]{
			Blackhole::consume,
			Blackhole::consume,
			Blackhole::consume,
			Blackhole::consume,
			Blackhole::consume
	};

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public static class Instantiation {
		@Benchmark
		public InvokerFactory<JmhTestCallback> baseLineSequenceInvokerFactoryInstantiation() {
			return new InvokerFactory<>(JmhTestCallback.class) {
				@Override
				public JmhTestCallback apply(JmhTestCallback[] callbacks) {
					return (bh, dummy) -> {
						for (var callback : callbacks) {
							callback.call(bh, dummy);
						}
					};
				}
			};
		}

		@Benchmark
		public InvokerFactory<JmhTestCallback> sequenceInvokerFactoryInstantiation() {
			return new SequenceInvokerFactory<>(JmhTestCallback.class);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@State(Scope.Benchmark)
	public static class Creation {
		private InvokerFactory<JmhTestCallback> baseLineInvokerFactory;
		private InvokerFactory<JmhTestCallback> sequenceInvokerFactory;

		@Setup(Level.Trial)
		public void setup() {
			this.baseLineInvokerFactory = new InvokerFactory<>(JmhTestCallback.class) {
				@Override
				public JmhTestCallback apply(JmhTestCallback[] callbacks) {
					return (bh, dummy) -> {
						for (var callback : callbacks) {
							callback.call(bh, dummy);
						}
					};
				}
			};
			this.sequenceInvokerFactory = new SequenceInvokerFactory<>(JmhTestCallback.class);
		}

		@Benchmark
		public JmhTestCallback baseLineSequenceInvokerFactoryCreation() {
			return this.baseLineInvokerFactory.apply(DUMMY_CALLBACKS);
		}

		@Benchmark
		public JmhTestCallback sequenceInvokerFactoryCreation() {
			return this.sequenceInvokerFactory.apply(DUMMY_CALLBACKS);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@State(Scope.Benchmark)
	public static class Invocation {
		private static final InvokerFactory<JmhTestCallback> BASE_LINE_FACTORY = new InvokerFactory<>(JmhTestCallback.class) {
			@Override
			public JmhTestCallback apply(JmhTestCallback[] callbacks) {
				return (bh, dummy) -> {
					for (var callback : callbacks) {
						callback.call(bh, dummy);
					}
				};
			}
		};

		private static final InvokerFactory<JmhTestCallback> SEQUENCE_FACTORY = new SequenceInvokerFactory<>(JmhTestCallback.class);

		private JmhTestCallback baseLineInvoker;
		private JmhTestCallback sequenceInvoker;

		@Setup(Level.Trial)
		public void setup() {
			this.baseLineInvoker = BASE_LINE_FACTORY.apply(DUMMY_CALLBACKS);
			this.sequenceInvoker = SEQUENCE_FACTORY.apply(DUMMY_CALLBACKS);
		}

		@Benchmark
		public void baseLineSequenceInvoke(Blackhole blackhole) {
			this.baseLineInvoker.call(blackhole, "Hello world!");
		}

		@Benchmark
		public void sequenceInvoke(Blackhole blackhole) {
			this.sequenceInvoker.call(blackhole, "Hello world!");
		}
	}
}
