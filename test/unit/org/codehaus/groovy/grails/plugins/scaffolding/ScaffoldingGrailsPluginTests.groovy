package org.codehaus.groovy.grails.plugins.scaffolding

import grails.util.Metadata

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.spring.GrailsRuntimeConfigurator
import org.codehaus.groovy.grails.commons.spring.WebRuntimeSpringConfiguration
import org.codehaus.groovy.grails.plugins.DefaultGrailsPlugin
import org.codehaus.groovy.grails.plugins.MockGrailsPluginManager
import org.codehaus.groovy.grails.plugins.PluginMetaManager
import org.codehaus.groovy.grails.support.MockApplicationContext
import org.springframework.core.io.Resource
import org.springframework.mock.web.MockServletContext

class ScaffoldingGrailsPluginTests extends GroovyTestCase {

	protected GroovyClassLoader gcl = new GroovyClassLoader()
	protected DefaultGrailsApplication ga
	protected MockApplicationContext ctx = new MockApplicationContext()

	protected void setUp() {
		super.setUp()
		ExpandoMetaClass.enableGlobally()

		ctx.registerMockBean(GrailsRuntimeConfigurator.CLASS_LOADER_BEAN, gcl)

		parseClasses()

		ga = new DefaultGrailsApplication(gcl.loadedClasses, gcl)
		ga.metadata[Metadata.APPLICATION_NAME] = getClass().name

		ga.setApplicationContext(ctx)
		ga.initialise()
		ctx.registerMockBean(GrailsApplication.APPLICATION_ID, ga)
	}

	protected void tearDown() {
		super.tearDown()
		ExpandoMetaClass.disableGlobally()
	}

	protected void parseClasses() {
		gcl.parseClass('''
			dataSource {
				pooled = true
				driverClassName = 'org.h2.Driver'
				username = 'sa'
				password = ''
				dbCreate = 'create-drop'
			}
''', 'Config')

		gcl.parseClass('''
class Test {
	Long id
	Long version
}
class TestController {
	def scaffold = Test
}
class TestTagLib {
	def myTag = { attrs ->
		out << 'Test'
	}
}
''')
	}

	void testScaffoldingPlugin() {

		def mockManager = new MockGrailsPluginManager()
		ctx.registerMockBean('pluginManager', mockManager)
		ctx.registerMockBean(PluginMetaManager.BEAN_ID, new org.codehaus.groovy.grails.plugins.DefaultPluginMetaManager([] as Resource[]))

		def dependantPluginClasses = []
		dependantPluginClasses << gcl.loadClass('org.codehaus.groovy.grails.plugins.CoreGrailsPlugin')
		dependantPluginClasses << gcl.loadClass('org.codehaus.groovy.grails.plugins.datasource.DataSourceGrailsPlugin')
		dependantPluginClasses << gcl.loadClass('org.codehaus.groovy.grails.plugins.web.mapping.UrlMappingsGrailsPlugin')
		dependantPluginClasses << gcl.loadClass('org.codehaus.groovy.grails.plugins.web.ControllersGrailsPlugin')
		dependantPluginClasses << gcl.loadClass('org.codehaus.groovy.grails.plugins.web.GroovyPagesGrailsPlugin')
		dependantPluginClasses << gcl.loadClass('org.codehaus.groovy.grails.plugins.i18n.I18nGrailsPlugin')
		dependantPluginClasses << gcl.loadClass('org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin')
		dependantPluginClasses << MockHibernateGrailsPlugin

		def dependentPlugins = dependantPluginClasses.collect { new DefaultGrailsPlugin(it, ga)}
		def springConfig = new WebRuntimeSpringConfiguration(ctx)
		springConfig.servletContext = new MockServletContext()

		dependentPlugins.each {
			mockManager.registerMockPlugin(it)
			it.manager = mockManager
		}
		dependentPlugins*.doWithRuntimeConfiguration(springConfig)

		def pluginClass = gcl.loadClass('ScaffoldingGrailsPlugin')
		def plugin = new DefaultGrailsPlugin(pluginClass, ga)
		plugin.manager = mockManager

		plugin.doWithRuntimeConfiguration(springConfig)

		def appCtx = springConfig.applicationContext
		ga.mainContext = appCtx
		dependentPlugins*.doWithDynamicMethods(appCtx)
		assert appCtx.containsBean('dataSource')
		assert appCtx.containsBean('TestValidator')

		// Check that the plugin does not blow up if a TagLib is modified, as opposed to a controller
		def taglibClass = gcl.loadedClasses.find { it.name.endsWith('TagLib') }
		assert taglibClass
		plugin.notifyOfEvent DefaultGrailsPlugin.EVENT_ON_CHANGE, taglibClass
	}
}
