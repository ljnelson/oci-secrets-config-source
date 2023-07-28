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

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import java.util.function.Function;
import java.util.function.Predicate;

import java.util.regex.Pattern;

public final class Guards {

    private Guards() {
        super();
    }

    public static Set<?> acceptList() {
        return acceptList(ConfigAccessor.ofMicroProfileConfig());
    }

    @SuppressWarnings("unchecked")
    public static Set<?> acceptList(final ConfigAccessor c) {
        return Set.copyOf(c.get(Guards.class.getName() + ".acceptList", List.class).orElse(List.of()));
    }

    public static Pattern acceptPattern() {
        return acceptPattern(ConfigAccessor.ofMicroProfileConfig());
    }

    public static Pattern acceptPattern(final ConfigAccessor c) {
        return acceptPattern(c.get(Guards.class.getName() + ".acceptPattern", String.class).orElse("^.*$"));
    }

    public static Pattern acceptPattern(final CharSequence pattern) {
        return Pattern.compile(pattern.toString());
    }

    public static Set<?> denyList() {
        return denyList(ConfigAccessor.ofMicroProfileConfig());
    }

    @SuppressWarnings("unchecked")
    public static Set<?> denyList(final ConfigAccessor c) {
        return Set.copyOf(c.get(Guards.class.getName() + ".denyList", List.class).orElse(List.of()));
    }

    public static <T, R> Function<T, R> guard(Function<T, R> f, Predicate<? super T> p) {
        Objects.requireNonNull(f, "f");
        Objects.requireNonNull(p, "p");
        return t -> p.test(t) ? f.apply(t) : null;
    }

    public static <T, R> Function<T, R> guardWithAcceptList(Function<T, R> f) {
        return guardWithAcceptList(f, acceptList());
    }

    public static <T, R> Function<T, R> guardWithAcceptList(Function<T, R> f, ConfigAccessor c) {
        return guardWithAcceptList(f, acceptList(c));
    }

    public static <T, R> Function<T, R> guardWithAcceptList(Function<T, R> f, Collection<?> acceptList) {
        return guard(f, acceptList::contains);
    }

    public static <T extends CharSequence, R> Function<T, R> guardWithAcceptPattern(Function<T, R> f) {
        return guardWithAcceptPattern(f, acceptPattern());
    }

    public static <T extends CharSequence, R> Function<T, R> guardWithAcceptPattern(Function<T, R> f, ConfigAccessor c) {
        return guardWithAcceptPattern(f, acceptPattern(c));
    }

    public static <T extends CharSequence, R> Function<T, R> guardWithAcceptPattern(Function<T, R> f, CharSequence pattern) {
        return guardWithAcceptPattern(f, acceptPattern(pattern));
    }

    public static <T extends CharSequence, R> Function<T, R> guardWithAcceptPattern(Function<T, R> f, Pattern p) {
        return guard(f, cs -> p.matcher(cs).matches());
    }

    public static <T, R> Function<T, R> guardWithDenyList(Function<T, R> f) {
        return guardWithDenyList(f, denyList());
    }

    public static <T, R> Function<T, R> guardWithDenyList(Function<T, R> f, ConfigAccessor c) {
        return guardWithDenyList(f, denyList(c));
    }

    public static <T, R> Function<T, R> guardWithDenyList(Function<T, R> f, Collection<?> denyList) {
        return guard(f, Predicate.not(denyList::contains));
    }

}
