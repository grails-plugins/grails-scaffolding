import grails.util.Metadata

log4j = {
	error 'org.codehaus.groovy.grails',
	      'org.springframework',
	      'org.hibernate',
	      'net.sf.ehcache.hibernate'
}

// for integration tests
Metadata.current[Metadata.APPLICATION_NAME] = 'grails-scaffolding'