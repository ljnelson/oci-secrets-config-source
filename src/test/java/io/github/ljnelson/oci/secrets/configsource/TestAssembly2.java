/*
 * Copyright © 2023 Laird Nelson.
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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger;
import static java.lang.System.Logger.Level.INFO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestAssembly2 {

    private static Logger LOGGER = System.getLogger(TestAssembly2.class.getName());
    
    private TestAssembly2() {
        super();
    }

    @Test
    final void testLambdaIdentity() {
        Runnable r = this::testLambdaIdentity;
        Runnable r1 = this::testLambdaIdentity;
        assertNotSame(r, r1);
        assertNotEquals(r, r1);
    }
  
    @Test
    final void testAssembly() {
        LOGGER.log(INFO, "Starting testAssembly");
        ConfigSource cs = new SecretBundleByNameConfigSource();

        // Make sure non-existent stupid properties are handled.
        assertNull(cs.getValue("bogus"));

        // Make sure properties found elsewhere are handled.
        assertNull(cs.getValue("user.home"));

        // Do the rest of this test only if the following assumptions hold.
        String secretName = System.getProperty("javax.sql.DataSource.test.password.secretName");
        assumeTrue(secretName != null && !secretName.isBlank());
        LOGGER.log(INFO, "secretName: {0}", secretName);
        String vaultId = System.getProperty("javax.sql.DataSource.test.password.vaultId");
        assumeTrue(vaultId != null && !vaultId.isBlank());
        String expectedValue = System.getProperty("javax.sql.DataSource.test.password.expectedValue");
        assumeTrue(expectedValue != null && !expectedValue.isBlank());
        assumeTrue(Files.exists(Paths.get(System.getProperty("user.home"), ".oci", "config")));

        // Make sure our ConfigSource and none other is used to go get
        // the value for this out of the vault.
        assertEquals(expectedValue, cs.getValue("javax.sql.DataSource.test.password"));
        LOGGER.log(INFO, "Ending testAssembly");
    }

    @Test
    final void testDefaultBehavior() {
        LOGGER.log(INFO, "Starting testDefaultBehavior");
        // Do the rest of this test only if the following assumptions hold.
        String secretName = System.getProperty("javax.sql.DataSource.test.password.secretName");
        assumeTrue(secretName != null && !secretName.isBlank());
        String vaultId = System.getProperty("javax.sql.DataSource.test.password.vaultId");
        assumeTrue(vaultId != null && !vaultId.isBlank());
        String expectedValue = System.getProperty("javax.sql.DataSource.test.password.expectedValue");
        assumeTrue(expectedValue != null && !expectedValue.isBlank());
        assumeTrue(Files.exists(Paths.get(System.getProperty("user.home"), ".oci", "config")));

        // Set up the system as though our ConfigSource were
        // discovered in the usual ServiceLoader fashion.  No typical
        // user would ever do this.
        ConfigProviderResolver cpr = ConfigProviderResolver.instance();
        ConfigSource cs = new SecretBundleByNameConfigSource();
        Config c = cpr.getBuilder()
            .addDefaultSources()
            .addDiscoveredConverters()
            .addDiscoveredSources()
            .withSources(cs)
            .build();
        cpr.registerConfig(c, Thread.currentThread().getContextClassLoader());

        // Make sure that our ConfigSource is the one that gets chosen
        // for properties it has declared it handles.
        assertEquals(expectedValue, c.getValue("javax.sql.DataSource.test.password", String.class));

        // Make sure other legitimate property requests are handled.
        assertEquals(System.getProperty("user.home"), c.getValue("user.home", String.class));
        LOGGER.log(INFO, "Ending testDefaultBehavior");
    }
  
}
