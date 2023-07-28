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

import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;

@FunctionalInterface
public interface ConfigAccessor {

    public <T> Optional<T> get(String name, Class<T> c);

    public static ConfigAccessor ofMicroProfileConfig() {
        return ConfigProvider.getConfig()::getOptionalValue;
    }

    public static ConfigAccessor ofMap(Map<?, ?> m) {
        return new ConfigAccessor() {
            @Override
            public final <T> Optional<T> get(String k, Class<T> c) {
                try {
                    return Optional.ofNullable(c.cast(m.get(k)));
                } catch (ClassCastException e) {
                    return null;
                }
            }
        };
    }

}
