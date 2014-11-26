/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.content.targeting.api.model;

import com.liferay.content.targeting.InvalidRuleException;
import com.liferay.content.targeting.anonymous.users.model.AnonymousUser;
import com.liferay.content.targeting.model.RuleInstance;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.xml.Element;

import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides the Rule interface, allowing custom rule creation and evaluation
 * for the Content Targeting Portlet.
 *
 * @author Eudaldo Alonso
 */
public interface Rule {

	/**
	 * Does processing when the rule is installed.
	 */
	public void activate();

	/**
	 * Does processing when the rule is uninstalled.
	 */
	public void deActivate();

	public void deleteData(RuleInstance ruleInstance)
		throws PortalException, SystemException;

	/**
	 * Returns <code>true</code> if the user complies with the rule instance.
	 *
	 * @param  ruleInstance the rule instance with stored configuration
	 * @param  anonymousUser the user who evaluates the rule
	 * @return <code>true</code> if the user complies with the rule instance;
	 *         <code>false</code> otherwise
	 */
	public boolean evaluate(
			HttpServletRequest request, RuleInstance ruleInstance,
			AnonymousUser anonymousUser)
		throws Exception;

	public void exportData(
			PortletDataContext portletDataContext, Element ruleInstanceElement,
			RuleInstance ruleInstance)
		throws Exception;

	/**
	 * Returns the rule description.
	 *
	 * @param  locale the language's locale
	 * @return the rule locale
	 */
	public String getDescription(Locale locale);

	/**
	 * Returns the HTML code containing the form fields required to edit the
	 * rule instance's configuration, based on the context.
	 *
	 * @param  ruleInstance the rule instance with stored configuration
	 * @param  context the map defining the form's evaluation context
	 * @param  values the values configured by users for the current rule
	 *         instance based on the form controls from the HTML. This will be
	 *         used when there is an error and the form is reloaded.
	 * @return the HTML code containing the form fields required to edit the
	 *         rule instance's configuration, based on the context
	 */
	public String getFormHTML(
		RuleInstance ruleInstance, Map<String, Object> context,
		Map<String, String> values);

	/**
	 * Returns the Font Awesome CSS class for the rule's icon.
	 *
	 * @return the Font Awesome CSS class for the rule's icon
	 * @see    <a href="http://fortawesome.github.io/Font-Awesome/3.2.1/">Font
	 *         Awesome documentation</a>
	 */
	public String getIcon();

	/**
	 * Returns the rule's name.
	 *
	 * @param  locale the language's locale
	 * @return the rule's name
	 */
	public String getName(Locale locale);

	/**
	 * Returns the key that identifies the category of the rule.
	 *
	 * @return the key that identifies the category
	 */
	public String getRuleCategoryKey();

	/**
	 * Returns the key that identifies the rule. The rule instances of this rule
	 * are identified by their rule key.
	 *
	 * @return the key that identifies the rule
	 */
	public String getRuleKey();

	/**
	 * Returns the rule short description.
	 *
	 * @param  locale the language's locale
	 * @return the rule locale
	 */
	public String getShortDescription(Locale locale);

	/**
	 * Returns the rule instance's localized summary.
	 *
	 * @param  ruleInstance the rule instance with stored configuration
	 * @param  locale the language's locale
	 * @return the rule instance's localized summary
	 */
	public String getSummary(RuleInstance ruleInstance, Locale locale);

	public void importData(
			PortletDataContext portletDataContext, RuleInstance ruleInstance)
		throws Exception;

	/**
	 * Returns <code>true</code> if the rule can be used more than once with
	 * different values for a user segment.
	 *
	 * @return <code>true</code> if the rule can be used more than once;
	 *         <code>false</code> otherwise
	 */
	public boolean isInstantiable();

	/**
	 * Returns the result of evaluating the rule's form fields in the context of
	 * the request and response.
	 *
	 * @param  request the request from which to get the request parameters
	 * @param  response the response to receive the render parameters
	 * @param  id the identifier that differentiates between rule instances of
	 *         the same type of an instantiable rule
	 * @param  values the values configured by users for the current rule
	 *         instance based on the form controls from the HTML
	 * @return the result of evaluating the rule's form fields in the context of
	 *         the request and response
	 */
	public String processRule(
			PortletRequest request, PortletResponse response, String id,
			Map<String, String> values)
		throws InvalidRuleException;

}