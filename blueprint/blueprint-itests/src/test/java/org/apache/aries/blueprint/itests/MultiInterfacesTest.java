/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.blueprint.itests;

import static org.apache.aries.itest.ExtraOptions.mavenBundle;
import static org.apache.aries.itest.ExtraOptions.paxLogging;
import static org.apache.aries.itest.ExtraOptions.testOptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.ops4j.pax.exam.CoreOptions.equinox;

import org.apache.aries.blueprint.testbundlea.multi.InterfaceA;
import org.apache.aries.blueprint.testbundlea.multi.InterfaceB;
import org.apache.aries.blueprint.testbundlea.multi.InterfaceC;
import org.apache.aries.blueprint.testbundlea.multi.InterfaceD;
import org.apache.aries.itest.AbstractIntegrationTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.service.blueprint.container.BlueprintContainer;

@RunWith(JUnit4TestRunner.class)
public class MultiInterfacesTest extends AbstractIntegrationTest {

    @Test
    public void testMultiInterfaceReferences() throws Exception {
        //bundlea provides the ns handlers, bean processors, interceptors etc for this test.
        Bundle bundlea = context().getBundleByName("org.apache.aries.blueprint.testbundlea");
        assertNotNull(bundlea);
        bundlea.start();
        
        //bundleb makes use of the extensions provided by bundlea
        Bundle bundleb = context().getBundleByName("org.apache.aries.blueprint.testbundleb");
        assertNotNull(bundleb);
        bundleb.start();
        
        //bundleb's container will hold the beans we need to query to check the function
        //provided by bundlea functioned as expected
        BlueprintContainer beanContainer = 
            Helper.getBlueprintContainerForBundle(context(), "org.apache.aries.blueprint.testbundleb");
        assertNotNull(beanContainer);

        Object obj1 = beanContainer.getComponentInstance("OnlyA");
        Object obj2 = beanContainer.getComponentInstance("AandB");
        Object obj3 = beanContainer.getComponentInstance("AandBandC");
        Object obj4 = beanContainer.getComponentInstance("AandBandCandD");
        
        assertEquals("A", ((InterfaceA)obj1).methodA());
        assertEquals("A", ((InterfaceA)obj2).methodA());
        assertEquals("A", ((InterfaceA)obj3).methodA());
        assertEquals("B", ((InterfaceB)obj2).methodB());
        assertEquals("C", ((InterfaceC)obj3).methodC());
        
        assertFalse(obj1 instanceof InterfaceC);
        assertFalse(obj2 instanceof InterfaceC);
        assertFalse(obj1 instanceof InterfaceB);
        
        assertTrue(obj4 instanceof InterfaceD);
        try {
            ((InterfaceD)obj4).methodD();
            fail("This should not work");
        } catch (org.osgi.service.blueprint.container.ServiceUnavailableException t) {
            //expected
        }        
    }
    
    @org.ops4j.pax.exam.junit.Configuration
    public static Option[] configuration() {
        return testOptions(
            paxLogging("DEBUG"),
            Helper.blueprintBundles(),
            mavenBundle("org.apache.aries.blueprint", "org.apache.aries.blueprint.testbundlea").noStart(),
            mavenBundle("org.apache.aries.blueprint", "org.apache.aries.blueprint.testbundleb").noStart(),
            equinox().version("3.5.0")
        );
    } 
}
