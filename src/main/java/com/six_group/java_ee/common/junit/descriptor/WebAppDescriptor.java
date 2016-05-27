package com.six_group.java_ee.common.junit.descriptor;

import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * DSL Grammar to construct / alter Web Application XML Descriptors
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public interface WebAppDescriptor extends Descriptor
{

   WebAppDescriptor version(String version);

   WebAppDescriptor displayName(String displayName);

   ServletDef servlet(String clazz, String... urlPatterns);

   ServletDef servlet(String name, String clazz, String[] urlPatterns);
}