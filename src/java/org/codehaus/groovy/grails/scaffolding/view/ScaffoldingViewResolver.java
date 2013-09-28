/*
 * Copyright 2004-2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.scaffolding.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.scaffolding.GrailsTemplateGenerator;
import org.codehaus.groovy.grails.web.pages.FastStringWriter;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.servlet.view.GrailsViewResolver;
import org.codehaus.groovy.grails.web.util.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;

/**
 * Overrides the default Grails view resolver and resolves scaffolded views at runtime.
 *
 * @author Graeme Rocher
 * @since 1.1
 */
public class ScaffoldingViewResolver extends GrailsViewResolver {

	GrailsTemplateGenerator templateGenerator;
	Map<String, List<String>> scaffoldedActionMap = Collections.emptyMap();
	Map<String, GrailsDomainClass> scaffoldedDomains = Collections.emptyMap();

	static final Map<ViewKey, View> scaffoldedViews = new ConcurrentHashMap<ViewKey, View>();
	protected static final Log log = LogFactory.getLog(ScaffoldingViewResolver.class);

	/**
	 * Clears any cached scaffolded views.
	 */
	public static void clearViewCache() {
		scaffoldedViews.clear();
	}

	@Override
	protected View createFallbackView(String viewName) throws Exception {
		GrailsWebRequest webRequest = WebUtils.retrieveGrailsWebRequest();
        final ViewKey viewKey = new ViewKey(webRequest.getControllerName(), webRequest.getActionName(), viewName);
        View v = scaffoldedViews.get(viewKey);
        if (v == null) {
    		List<String> controllerActions = scaffoldedActionMap.get(webRequest.getControllerName());
    		if (controllerActions != null && controllerActions.contains(webRequest.getActionName())) {
    			GrailsDomainClass domainClass = scaffoldedDomains.get(webRequest.getControllerName());
    			if (domainClass != null) {
    				String viewFileName;
    				final int i = viewName.lastIndexOf('/');
    				if (i > -1) {
    					viewFileName = viewName.substring(i, viewName.length());
    				}
    				else {
    					viewFileName = viewName;
    				}
					String viewCode = null;
					try {
						viewCode = generateViewSource(viewFileName, domainClass);
					}
					catch (Exception e) {
						log.error("Error generating scaffolded view [" + viewName + "]: " + e.getMessage(),e);
					}
					if (StringUtils.hasLength(viewCode)) {
						v = createScaffoldedView(viewName, viewCode);
						scaffoldedViews.put(viewKey, v);
					}
    			}
    		}
        }
        if (v != null) {
            return v;
        }
		return super.createFallbackView(viewName);
	}

	protected View createScaffoldedView(String viewName, String viewCode) throws Exception {
		final ScaffoldedGroovyPageView view = new ScaffoldedGroovyPageView(viewName, viewCode);
		view.setApplicationContext(getApplicationContext());
		view.setServletContext(getServletContext());
		view.setTemplateEngine(templateEngine);
		view.afterPropertiesSet();
		return view;
	}

	protected String generateViewSource(String viewName, GrailsDomainClass domainClass) throws IOException {
		Writer sw = new FastStringWriter();
		templateGenerator.generateView(domainClass, viewName,sw);
		return sw.toString();
	}

	private static class ViewKey {
		private String controller;
		private String action;
		private String view;
        public ViewKey(String controller, String action, String view) {
            super();
            this.controller = controller;
            this.action = action;
            this.view = view;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((action == null) ? 0 : action.hashCode());
            result = prime * result + ((controller == null) ? 0 : controller.hashCode());
            result = prime * result + ((view == null) ? 0 : view.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ViewKey other = (ViewKey)obj;
            if (action == null) {
                if (other.action != null)
                    return false;
            }
            else if (!action.equals(other.action))
                return false;
            if (controller == null) {
                if (other.controller != null)
                    return false;
            }
            else if (!controller.equals(other.controller))
                return false;
            if (view == null) {
                if (other.view != null)
                    return false;
            }
            else if (!view.equals(other.view))
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "ViewKey [controller=" + controller + ", action=" + action + ", view=" + view + "]";
        }
	}

	public void setTemplateGenerator(GrailsTemplateGenerator templateGenerator) {
		this.templateGenerator = templateGenerator;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setScaffoldedActionMap(Map scaffoldedActionMap) {
		this.scaffoldedActionMap = scaffoldedActionMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setScaffoldedDomains(Map scaffoldedDomains) {
		this.scaffoldedDomains = scaffoldedDomains;
	}
}
