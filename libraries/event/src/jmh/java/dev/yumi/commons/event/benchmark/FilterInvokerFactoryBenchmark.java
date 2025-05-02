/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.commons.event.benchmark;

import dev.yumi.commons.event.invoker.FilterInvokerFactory;
import dev.yumi.commons.event.invoker.InvokerFactory;
import dev.yumi.commons.event.test.FilterTestCallback;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

public class FilterInvokerFactoryBenchmark {
	private static final FilterTestCallback[] DUMMY_CALLBACKS = new FilterTestCallback[]{
			text -> false,
			text -> false,
			text -> false,
			text -> text.equals("Hello world!"),
			text -> text.equals("Yumi Commons"),
			text -> false,
	};

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public static class Creation {
		@Benchmark
		public void baseLineFilterInvokerFactoryCreation(Blackhole blackhole) {
			var factory = new InvokerFactory<>(FilterTestCallback.class) {
				@Override
				public FilterTestCallback apply(FilterTestCallback[] callbacks) {
					return (dummy) -> {
						for (var callback : callbacks) {
							if (callback.filter(dummy)) {
								return true;
							}
						}

						return false;
					};
				}
			};
			blackhole.consume(factory.apply(DUMMY_CALLBACKS));
		}

		@Benchmark
		public void filterInvokerFactoryCreation(Blackhole blackhole) {
			var factory = new FilterInvokerFactory<>(FilterTestCallback.class, false);
			blackhole.consume(factory.apply(DUMMY_CALLBACKS));
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@State(Scope.Benchmark)
	public static class Invocation {
		private static final InvokerFactory<FilterTestCallback> BASE_LINE_FACTORY = new InvokerFactory<>(FilterTestCallback.class) {
			@Override
			public FilterTestCallback apply(FilterTestCallback[] callbacks) {
				return (dummy) -> {
					for (var callback : callbacks) {
						if (callback.filter(dummy)) {
							return true;
						}
					}

					return false;
				};
			}
		};

		private static final InvokerFactory<FilterTestCallback> FILTER_FACTORY = new FilterInvokerFactory<>(FilterTestCallback.class, false);

		@Param({"", "Hello", "Hello world!", "Yumi Commons", "_"})
		String dummyValue;

		public FilterTestCallback baseLineInvoker;
		public FilterTestCallback filterInvoker;

		@Setup(Level.Trial)
		public void setup() {
			this.baseLineInvoker = BASE_LINE_FACTORY.apply(DUMMY_CALLBACKS);
			this.filterInvoker = FILTER_FACTORY.apply(DUMMY_CALLBACKS);
		}

		@Benchmark
		public void baseLineInvoke(Blackhole blackhole) {
			blackhole.consume(this.baseLineInvoker.filter(this.dummyValue));
		}

		@Benchmark
		public void filterInvoke(Blackhole blackhole) {
			blackhole.consume(this.filterInvoker.filter(this.dummyValue));
		}
	}
}
