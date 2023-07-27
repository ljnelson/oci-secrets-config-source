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

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;

import static io.github.ljnelson.oci.secrets.configsource.Guards.guardWithAcceptPattern;
import static io.github.ljnelson.oci.secrets.configsource.SecretBundleContentDetailsFunctions.secretBundleContentDetailsByName;
import static io.github.ljnelson.oci.secrets.configsource.SecretsSuppliers.secrets;

public final class SecretBundleByNameConfigSource extends AbstractSecretBundleConfigSource {

  public SecretBundleByNameConfigSource() {
    this(ConfigAccessor.ofMicroProfileConfig(), secrets()); // memoized
  }

  private SecretBundleByNameConfigSource(ConfigAccessor c, Supplier<? extends Secrets> ss) {
    super(guardWithAcceptPattern(secretBundleContentDetailsByName(c,
                                                                  GetSecretBundleByNameRequest::builder,
                                                                  ss),
                                 c),
          ss);
  }

}
