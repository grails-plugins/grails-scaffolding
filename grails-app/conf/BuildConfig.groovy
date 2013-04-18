grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

	inherits 'global', {
		// TODO remove
		excludes 'grails-crud', 'grails-plugin-scaffolding'
	}
	log 'warn'

	repositories {
		grailsCentral()
	}

	plugins {
		build ':release:3.0.0', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}
