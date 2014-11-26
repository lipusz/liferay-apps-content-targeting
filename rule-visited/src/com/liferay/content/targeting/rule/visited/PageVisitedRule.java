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

package com.liferay.content.targeting.rule.visited;

import com.liferay.content.targeting.InvalidRuleException;
import com.liferay.content.targeting.analytics.service.AnalyticsEventLocalService;
import com.liferay.content.targeting.analytics.util.AnalyticsUtil;
import com.liferay.content.targeting.anonymous.users.model.AnonymousUser;
import com.liferay.content.targeting.api.model.BaseRule;
import com.liferay.content.targeting.api.model.Rule;
import com.liferay.content.targeting.model.RuleInstance;
import com.liferay.content.targeting.rule.categories.BehaviorRuleCategory;
import com.liferay.content.targeting.util.ContentTargetingContextUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.model.Layout;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;

import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 */
@Component(immediate = true, service = Rule.class)
public class PageVisitedRule extends BaseRule {

	@Activate
	@Override
	public void activate() {
		super.activate();
	}

	@Deactivate
	@Override
	public void deActivate() {
		super.deActivate();
	}

	@Override
	public boolean evaluate(
			HttpServletRequest request, RuleInstance ruleInstance,
			AnonymousUser anonymousUser)
		throws Exception {

		long plid = GetterUtil.getLong(ruleInstance.getTypeSettings());

		int count = _analyticsEventLocalService.getAnalyticsEventsCount(
			anonymousUser.getAnonymousUserId(), Layout.class.getName(), plid,
			"view");

		if (count > 0) {
			return true;
		}

		return false;
	}

	@Override
	public void exportData(
			PortletDataContext portletDataContext, Element ruleInstanceElement,
			RuleInstance ruleInstance)
		throws Exception {

		long plid = GetterUtil.getLong(ruleInstance.getTypeSettings());

		Layout layout = LayoutLocalServiceUtil.fetchLayout(plid);

		if (layout != null ) {
			ruleInstance.setTypeSettings(layout.getUuid());
		}
	}

	@Override
	public String getIcon() {
		return "icon-file";
	}

	@Override
	public String getRuleCategoryKey() {
		return BehaviorRuleCategory.KEY;
	}

	@Override
	public String getSummary(RuleInstance ruleInstance, Locale locale) {
		long plid = GetterUtil.getLong(ruleInstance.getTypeSettings());

		Layout layout = null;

		try {
			layout = LayoutLocalServiceUtil.fetchLayout(plid);
		}
		catch (SystemException e) {
		}

		if (layout != null) {
			return layout.getTitle(locale);
		}

		return StringPool.BLANK;
	}

	@Override
	public void importData(
			PortletDataContext portletDataContext, RuleInstance ruleInstance)
		throws Exception {

		String layoutUuid = ruleInstance.getTypeSettings();

		Layout layout = LayoutLocalServiceUtil.fetchLayoutByUuidAndCompanyId(
			layoutUuid, portletDataContext.getCompanyId());

		if (layout != null ) {
			ruleInstance.setTypeSettings(String.valueOf(layout.getPlid()));
		}
	}

	@Override
	public String processRule(
			PortletRequest request, PortletResponse response, String id,
			Map<String, String> values)
		throws InvalidRuleException {

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		String friendlyURL = values.get("friendlyURL");

		try {
			Layout layout = LayoutLocalServiceUtil.fetchLayoutByFriendlyURL(
				themeDisplay.getScopeGroupId(), false, friendlyURL);

			if (layout == null) {
				layout = LayoutLocalServiceUtil.fetchLayoutByFriendlyURL(
					themeDisplay.getScopeGroupId(), true, friendlyURL);
			}

			if (layout != null) {
				return String.valueOf(layout.getPlid());
			}
			else {
				throw new InvalidRuleException(
					"a-page-with-this-friendly-url-could-not-be-found");
			}
		}
		catch (SystemException e) {
		}

		return StringPool.BLANK;
	}

	@Reference
	public void setAnalyticsEventLocalService(
		AnalyticsEventLocalService analyticsEventLocalService) {

		_analyticsEventLocalService = analyticsEventLocalService;
	}

	protected String getFormTemplatePath() {
		return _FORM_TEMPLATE_PATH_PAGE;
	}

	@Override
	protected void populateContext(
		RuleInstance ruleInstance, Map<String, Object> context,
		Map<String, String> values) {

		ThemeDisplay themeDisplay = (ThemeDisplay)context.get("themeDisplay");

		String friendlyURLBase = StringPool.BLANK;

		try {
			friendlyURLBase = PortalUtil.getGroupFriendlyURL(
				themeDisplay.getScopeGroup(), false, themeDisplay);
		}
		catch (Exception e) {
			_log.error(e);
		}

		context.put("friendlyURLBase", friendlyURLBase);

		String friendlyURL = StringPool.BLANK;

		if (!values.isEmpty()) {
			friendlyURL = values.get("friendlyURL");
		}
		else if (ruleInstance != null) {
			long plid = GetterUtil.getLong(ruleInstance.getTypeSettings());

			try {
				Layout layout = LayoutLocalServiceUtil.fetchLayout(plid);

				if (layout != null) {
					friendlyURL = layout.getFriendlyURL();
				}
			}
			catch (SystemException e) {
			}
		}

		context.put("friendlyURL", friendlyURL);

		long groupId = (Long)context.get("scopeGroupId");

		boolean trackingPageEnabled = AnalyticsUtil.isAnalyticsPageEnabled(
			groupId);

		context.put("trackingPageEnabled", trackingPageEnabled);

		if (!trackingPageEnabled) {
			ContentTargetingContextUtil.populateContextAnalyticsSettingsURLs(
				context);
		}
	}

	private static final String _FORM_TEMPLATE_PATH_PAGE =
		"templates/ct_fields_page.ftl";

	private static Log _log = LogFactoryUtil.getLog(PageVisitedRule.class);

	private AnalyticsEventLocalService _analyticsEventLocalService;

}