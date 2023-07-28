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
import java.util.Objects;
import java.util.Set;

import java.util.function.Function;
import java.util.function.Predicate;

import java.util.regex.Pattern;

/**
 * A utility class containing useful operations for guarding {@link Function}s.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #guard(Function, Predicate)
 */
public final class Guards {

    private Guards() {
        super();
    }

    /**
     * Using {@linkplain ConfigAccessor#ofMicroProfileConfig() MicroProfileConfig}, retrieves the value of the {@code
     * io.github.ljnelson.oci.secrets.configsource.Guards.acceptList} configuration property as a {@link Set}.
     *
     * @return a non-{@code null} {@link Set}
     */
    public static Set<?> acceptList() {
        return acceptList(ConfigAccessor.ofMicroProfileConfig());
    }

    /**
     * Using the supplied {@link ConfigAccessor}, {@linkplain ConfigAccessor#get(String, Class) retrieves} the value of
     * the {@code io.github.ljnelson.oci.secrets.configsource.Guards.acceptList} configuration property as a {@link
     * Set}.
     *
     * @return a non-{@code null} {@link Set}
     */
    @SuppressWarnings("unchecked")
    public static Set<?> acceptList(final ConfigAccessor c) {
        return Set.copyOf(c.get(Guards.class.getName() + ".acceptList", List.class).orElse(List.of()));
    }

    /**
     * Using {@linkplain ConfigAccessor#ofMicroProfileConfig() MicroProfileConfig}, retrieves the value of the {@code
     * io.github.ljnelson.oci.secrets.configsource.Guards.acceptPattern} configuration property as a {@link Pattern}.
     *
     * <p><strong>Note:</strong> Conversion is handled by this class, not by a registered MicroProfile Config
     * converter.</p>
     *
     * @return a non-{@code null} {@link Pattern}
     */
    public static Pattern acceptPattern() {
        return acceptPattern(ConfigAccessor.ofMicroProfileConfig());
    }

    /**
     * Using the supplied {@link ConfigAccessor}, {@linkplain ConfigAccessor#get(String, Class) retrieves} the value of
     * the {@code io.github.ljnelson.oci.secrets.configsource.Guards.acceptPattern} configuration property as a {@link
     * Pattern}.
     *
     * @return a non-{@code null} {@link Pattern}
     */
    public static Pattern acceptPattern(final ConfigAccessor c) {
        return acceptPattern(c.get(Guards.class.getName() + ".acceptPattern", String.class).orElse("^.*$"));
    }

    /**
     * {@linkplain Pattern#compile(String) Compiles} the supplied regular expression into a {@link Pattern} and
     * returns it.
     *
     * @return a non-{@code null} {@link Pattern}
     */
    public static Pattern acceptPattern(final CharSequence pattern) {
        return Pattern.compile(pattern.toString());
    }

    /**
     * Using {@linkplain ConfigAccessor#ofMicroProfileConfig() MicroProfileConfig}, retrieves the value of the {@code
     * io.github.ljnelson.oci.secrets.configsource.Guards.denyList} configuration property as a {@link Set}.
     *
     * @return a non-{@code null} {@link Set}
     */
    public static Set<?> denyList() {
        return denyList(ConfigAccessor.ofMicroProfileConfig());
    }

    /**
     * Using the supplied {@link ConfigAccessor}, {@linkplain ConfigAccessor#get(String, Class) retrieves} the value of
     * the {@code io.github.ljnelson.oci.secrets.configsource.Guards.denyList} configuration property as a {@link
     * Set}.
     *
     * @return a non-{@code null} {@link Set}
     */
    @SuppressWarnings("unchecked")
    public static Set<?> denyList(final ConfigAccessor c) {
        return Set.copyOf(c.get(Guards.class.getName() + ".denyList", List.class).orElse(List.of()));
    }

    /**
     * Returns a {@link Function} that guards the supplied {@link Function} with the supplied {@link Predicate},
     * returning {@code null} if the {@link Predicate}'s {@link Predicate#test(Object)} method returns {@code false},
     * and returning the result of invoking the supplied {@link Function} otherwise.
     *
     * @param <T> the type of the supplied {@link Function}'s sole parameter
     *
     * @param <R> the return type of the supplied {@link Function}
     *
     * @param f the {@link Function} to guard; must not be {@code null}
     *
     * @param p the {@link Predicate} used to guard the supplied {@link Function}; must not be {@code null}
     *
     * @return {@code null} if the supplied {@link Predicate}'s {@link Predicate#test(Object)} method returns {@code
     * false}, and returning the result of invoking the supplied {@link Function} otherwise
     *
     * @exception NullPointerException if any argument is {@code null}
     */
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
