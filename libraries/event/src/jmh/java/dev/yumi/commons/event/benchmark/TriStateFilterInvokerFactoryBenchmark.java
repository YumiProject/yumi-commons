/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.benchmark;

import dev.yumi.commons.TriState;
import dev.yumi.commons.event.invoker.InvokerFactory;
import dev.yumi.commons.event.invoker.TriStateFilterInvokerFactory;
import dev.yumi.commons.event.test.TriStateCallback;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

public class TriStateFilterInvokerFactoryBenchmark {
	private static final TriStateCallback[] DUMMY_CALLBACKS = new TriStateCallback[]{
			text -> TriState.DEFAULT,
			text -> TriState.DEFAULT,
			text -> TriState.DEFAULT,
			text -> text.equals("Hello world!") ? TriState.TRUE : TriState.DEFAULT,
			text -> text.equals("Yumi Commons") ? TriState.FALSE : TriState.DEFAULT,
			text -> TriState.FALSE,
	};

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public static class Creation {
		@Benchmark
		public void baseLineFilterInvokerFactoryCreation(Blackhole blackhole) {
			var factory = new InvokerFactory<>(TriStateCallback.class) {
				@Override
				public TriStateCallback apply(TriStateCallback[] callbacks) {
					return (dummy) -> {
						for (var callback : callbacks) {
							var result = callback.call(dummy);

							if (result != TriState.DEFAULT) {
								return result;
							}
						}

						return TriState.DEFAULT;
					};
				}
			};
			blackhole.consume(factory.apply(DUMMY_CALLBACKS));
		}

		@Benchmark
		public void filterInvokerFactoryCreation(Blackhole blackhole) {
			var factory = new TriStateFilterInvokerFactory<>(TriStateCallback.class);
			blackhole.consume(factory.apply(DUMMY_CALLBACKS));
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@State(Scope.Benchmark)
	public static class Invocation {
		private static final InvokerFactory<TriStateCallback> BASE_LINE_FACTORY = new InvokerFactory<>(TriStateCallback.class) {
			@Override
			public TriStateCallback apply(TriStateCallback[] callbacks) {
				return (dummy) -> {
					for (var callback : callbacks) {
						var result = callback.call(dummy);

						if (result != TriState.DEFAULT) {
							return result;
						}
					}

					return TriState.DEFAULT;
				};
			}
		};

		private static final InvokerFactory<TriStateCallback> FILTER_FACTORY = new TriStateFilterInvokerFactory<>(TriStateCallback.class);

		@Param({"", "Hello", "Hello world!", "Yumi Commons", "_"})
		String dummyValue;

		public TriStateCallback baseLineInvoker;
		public TriStateCallback filterInvoker;

		@Setup(Level.Trial)
		public void setup() {
			this.baseLineInvoker = BASE_LINE_FACTORY.apply(DUMMY_CALLBACKS);
			this.filterInvoker = FILTER_FACTORY.apply(DUMMY_CALLBACKS);
		}

		@Benchmark
		public void baseLineInvoke(Blackhole blackhole) {
			blackhole.consume(this.baseLineInvoker.call(this.dummyValue));
		}

		@Benchmark
		public void filterInvoke(Blackhole blackhole) {
			blackhole.consume(this.filterInvoker.call(this.dummyValue));
		}
	}
}
