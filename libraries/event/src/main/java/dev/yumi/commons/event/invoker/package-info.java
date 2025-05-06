/*
 * Copyright 2023 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * <h2>The Event Invoker APIs</h2>
 * <p>
 * To call its listeners, an {@link dev.yumi.commons.event.Event Event} object stores an invoker implementation of
 * its listener interface. This functional approach to the event invocation problem avoids the need of slow reflection
 * to call the listeners or the need to create a new event implementation for each kind of event.
 * To be able to generate such invoker implementation the event is given an invoker factory which takes the listeners as
 * input and returns the invoker implementation.
 * <p>
 * Usually invoker factories are simple {@link java.util.function.Function functions}, but this event API additionally offers
 * the {@link dev.yumi.commons.event.invoker.InvokerFactory InvokerFactory} interface which still has the factory function but
 * is also given the type of listener interface in its constructor.
 * <p>
 * As such, the event invoker APIs provide a {@link dev.yumi.commons.event.invoker.DefaultInvokerFactory default invoker factory}
 * which is able to generate at runtime invoker implementations for simple common cases that may be found in most invoker implementations.
 * Please refer to its documentation for more details about which cases are handled.
 *
 * @see dev.yumi.commons.event
 * @see dev.yumi.commons.event.invoker.InvokerFactory
 * @see dev.yumi.commons.event.invoker.DefaultInvokerFactory
 */

package dev.yumi.commons.event.invoker;
