package grails.plugin.scaffolding

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import grails.util.BuildSettingsHolder

import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.scaffolding.DefaultGrailsTemplateGenerator
import org.codehaus.groovy.grails.scaffolding.GrailsTemplateGenerator

@TestMixin(IntegrationTestMixin)
class ScaffoldingTests {

	static transactional = false

	def grailsApplication
	def pluginManager

	private GrailsTemplateGenerator generator = new DefaultGrailsTemplateGenerator(getClass().classLoader)
	private File generateDir = new File(BuildSettingsHolder.settings.projectWorkDir, '_scaffolding_generate_')
	private GrailsDomainClass domainClass

	void setUp() {
		generator.grailsApplication = grailsApplication
		generator.pluginManager = pluginManager

		generateDir.deleteDir()

		domainClass = grailsApplication.getDomainClass(Thing.name)
	}

	void tearDown() {
		generateDir.deleteDir()
	}

	void testGenerateController() {

		generator.generateController domainClass, generateDir.path

		File controller = new File(generateDir, 'grails-app/controllers/grails/plugin/scaffolding/ThingController.groovy')
		assert controller.exists()

		String text = controller.text

		assert text.contains('package grails.plugin.scaffolding')
		assert text.contains('class ThingController {')
		assert text.contains('respond Thing.list(params), model:[thingCount: Thing.count()]')
	}

	void testGenerateViews() {

		generator.generateViews domainClass, generateDir.path

		File dir = new File(generateDir, 'grails-app/views/thing')
		assert dir.exists()

		def names = dir.list().sort()
		assert ['_form.gsp', 'create.gsp', 'edit.gsp', 'index.gsp', 'show.gsp'] == names

		String text = new File(dir, '_form.gsp').text

		assert text.contains('<g:datePicker name="bar"')
		assert text.contains('<g:field type="number" name="foo"')
		assert text.contains('<g:textField name="name"')
	}

	void testGenerateTest() {

		generator.generateTest domainClass, generateDir.path

		File test = new File(generateDir, 'grails/plugin/scaffolding/ThingControllerSpec.groovy')
		assert test.exists()

		String text = test.text

		assert text.contains('package grails.plugin.scaffolding')
		assert text.contains('class ThingControllerSpec extends Specification {')
		assert text.contains('@TestFor(ThingController)')
		assert text.contains('@Mock(Thing)')
	}
}
