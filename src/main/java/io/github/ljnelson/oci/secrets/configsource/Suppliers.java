/*
 * Copyright © 2022–2023 Laird Nelson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.github.ljnelson.oci.secrets.configsource;

import java.util.function.Supplier;

public final class Suppliers {

    private Suppliers() {
        super();
    }

    public static <R> Supplier<R> memoizedSupplier(Supplier<R> s) {
        if (s.getClass().getEnclosingClass() == Suppliers.class) {
            assert s.getClass().isAnonymousClass();
            return s;
        }
        return new Supplier<>() {

            private Supplier<R> d = this::compute;

            private boolean initialized;

            @Override
            public R get() {
                return this.d.get();
            }

            private synchronized R compute() {
                if (!this.initialized) {
                    R r = s.get();
                    this.d = () -> r;
                    this.initialized = true;
                }
                return this.d.get();
            }
        };
    }

}