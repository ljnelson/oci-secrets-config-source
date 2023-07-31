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
import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Provides packages related to building {@link ConfigSource} implementations that can {@linkplain
 * Secrets#getSecretBundle(GetSecretBundleRequest) get property values from an OCI secret bundle}.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
@SuppressWarnings({ "requires-automatic", "requires-transitive-automatic" })
module io.github.ljnelson.oci.secrets.configsource {

    exports io.github.ljnelson.oci.secrets.configsource;

    exports io.github.ljnelson.oci.secrets.configsource.adp;

    requires transitive microprofile.config.api;

    requires transitive oci.java.sdk.common;

    requires transitive oci.java.sdk.secrets;

}
