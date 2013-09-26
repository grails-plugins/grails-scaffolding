grails.project.work.dir = 'target'

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		mavenCentral()
		grailsCentral()
	}

    dependencies {
        test "org.grails:grails-datastore-gorm-hibernate:2.0.1.RELEASE"
    }

	plugins {
		build ':release:3.0.1', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}
