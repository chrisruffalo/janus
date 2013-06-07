package com.janus.support;

import java.util.UUID;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

public final class DeploymentFactory {

	/**
	 * Private constructor for utility class
	 */
	private DeploymentFactory() {
		
	}
	
	public static WebArchive createDeployment() {
		// resolver
		MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class);
		
		// create archive
		WebArchive archive = ShrinkWrap.create(WebArchive.class, "janus-server-test-" + UUID.randomUUID() + ".war");
		
		// whole project
		archive.addPackages(true, "com.janus");
		
		// import supporting libraries
		resolver.configureFrom("pom.xml");
		resolver.useCentralRepo(false);
		resolver.goOffline();
		
		archive.addAsLibraries(resolver.artifact("org.tmatesoft.sqljet:sqljet:1.1.7").resolveAsFiles());

		archive.addAsLibraries(resolver.artifact("org.hibernate:hibernate-search:4.2.0.Final")
									   .exclusion("org.hibernate:hibernate-core")
									   .exclusion("org.slf4j:slf4j-api")
									   .exclusion("org.hibernate.common:hibernate-commons-annotations")
									   .exclusion("org.jboss.logging:jboss-logging")
									   .resolveAsFiles());		
		
		archive.addAsLibraries(resolver.artifact("org.hibernate:hibernate-search-infinispan:4.2.0.Final")
									   .exclusion("org.hibernate:hibernate-search-engine")
									   .exclusion("org.infinispan:infinispan-core")
									   .exclusion("org.apache.lucene:lucene-core")
									   .resolveAsFiles());
		
		// persistence
		archive.addAsResource("META-INF/persistence.xml");
		archive.addAsResource("janus-hibernate-search-infinispan.xml");
		
		// janus configuration
		archive.addAsResource("default-janus.xml");
		archive.addAsResource("janus.properties");
		
		// web resources
		archive.addAsWebInfResource("web.xml");
		archive.addAsWebInfResource("beans.xml");
		archive.addAsWebInfResource("jboss-deployment-structure.xml");
		
		return archive;		
	}
	
}
