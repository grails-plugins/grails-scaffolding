grails.project.work.dir = 'target'

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		mavenCentral()
		grailsCentral()
	}

	plugins {
		build ':release:3.0.0', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}
