/*
 * Copyright © 2021 Travis Hoffman (travis@firkin.io)
 * Copyright © 2021 Firkin IO (https://firkin.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.firkin.kstreams.normalizer.demo.v1;

import java.util.Iterator;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Generator<T> extends Supplier<T>, Iterable<T> {

  // The "static" part of the interface should implement the following; replace 'T' with
  // the actual type of the subclass.

  /*
   * Subclasses should copy-n-paste the following static fields and methods. Replace 'T' with
   * the actual type the implementation produces.

  // ---- Implementation for Static Interface -----------------------------------------------------

  private static final Generator<T> instance = new T(-1L);

  public static Generator<T> generator() {
    return instance;
  }

  public static Stream<T> stream() {
    return Stream.generate(instance);
  }
   */

  long seed();
  Random random();

  // ---- Implementation for Supplier -------------------------------------------------------------

  @Override
  T get();

  // ---- Implementation for Iterable -------------------------------------------------------------

  /**
   * Returns an iterator over elements of type {@code T}.
   *
   * @return an Iterator.
   */
  @Override
  default Iterator<T> iterator() {
    return Stream.generate(this).iterator();
  }

  /**
   * Performs the given action for each element of the {@code Iterable}
   * until all elements have been processed or the action throws an
   * exception.  Actions are performed in the order of iteration, if that
   * order is specified.  Exceptions thrown by the action are relayed to the
   * caller.
   * <p>
   * The behavior of this method is unspecified if the action performs
   * side-effects that modify the underlying source of elements, unless an
   * overriding class has specified a concurrent modification policy.
   *
   * @param action The action to be performed for each element
   * @throws NullPointerException if the specified action is null
   * @implSpec <p>The default implementation behaves as if:
   * <pre>{@code
   *     for (T t : this)
   *         action.accept(t);
   * }</pre>
   * @since 1.8
   */
  @Override
  default void forEach(Consumer<? super T> action) {
    Iterable.super.forEach(action);
  }

  /**
   * Creates a {@link Spliterator} over the elements described by this
   * {@code Iterable}.
   *
   * @return a {@code Spliterator} over the elements described by this
   * {@code Iterable}.
   * @implSpec The default implementation creates an
   * <em><a href="../util/Spliterator.html#binding">early-binding</a></em>
   * spliterator from the iterable's {@code Iterator}.  The spliterator
   * inherits the <em>fail-fast</em> properties of the iterable's iterator.
   * @implNote The default implementation should usually be overridden.  The
   * spliterator returned by the default implementation has poor splitting
   * capabilities, is unsized, and does not report any spliterator
   * characteristics. Implementing classes can nearly always provide a
   * better implementation.
   * @since 1.8
   */
  @Override
  default Spliterator<T> spliterator() {
    return Iterable.super.spliterator();
  }

}
