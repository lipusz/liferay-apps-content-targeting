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

package com.liferay.content.targeting.lar;

import com.liferay.content.targeting.model.RuleInstance;
import com.liferay.content.targeting.service.RuleInstanceLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.lar.BaseStagedModelDataHandler;
import com.liferay.portal.kernel.lar.ExportImportPathUtil;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.service.ServiceContext;

/**
 * @author Eduardo Garcia
 */
public class RuleInstanceStagedModelDataHandler
	extends BaseStagedModelDataHandler<RuleInstance> {

	public static final String[] CLASS_NAMES = {RuleInstance.class.getName()};

	@Override
	public void deleteStagedModel(
			String uuid, long groupId, String className, String extraData)
		throws PortalException, SystemException {

		RuleInstance ruleInstance =
			RuleInstanceLocalServiceUtil.fetchRuleInstanceByUuidAndGroupId(
				uuid, groupId);

		if (ruleInstance != null) {
			RuleInstanceLocalServiceUtil.deleteRuleInstance(ruleInstance);
		}
	}

	@Override
	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	@Override
	protected void doExportStagedModel(
			PortletDataContext portletDataContext, RuleInstance ruleInstance)
		throws Exception {

		Element ruleInstanceElement = portletDataContext.getExportDataElement(
			ruleInstance);

		portletDataContext.addClassedModel(
			ruleInstanceElement,
			ExportImportPathUtil.getModelPath(ruleInstance), ruleInstance);
	}

	@Override
	protected void doImportStagedModel(
			PortletDataContext portletDataContext, RuleInstance ruleInstance)
		throws Exception {

		long userId = portletDataContext.getUserId(ruleInstance.getUserUuid());

		ServiceContext serviceContext = portletDataContext.createServiceContext(
			ruleInstance);

		RuleInstance existingRuleInstance =
			RuleInstanceLocalServiceUtil.fetchRuleInstanceByUuidAndGroupId(
				ruleInstance.getUuid(), portletDataContext.getScopeGroupId());

		RuleInstance importedRuleInstance = null;

		if (existingRuleInstance == null) {
			serviceContext.setUuid(ruleInstance.getUuid());

			importedRuleInstance = RuleInstanceLocalServiceUtil.addRuleInstance(
				userId, ruleInstance.getRuleKey(),
				ruleInstance.getUserSegmentId(), ruleInstance.getTypeSettings(),
				serviceContext);
		}
		else {
			importedRuleInstance =
				RuleInstanceLocalServiceUtil.updateRuleInstance(
					existingRuleInstance.getRuleInstanceId(),
					ruleInstance.getTypeSettings(), serviceContext);
		}

		portletDataContext.importClassedModel(
			ruleInstance, importedRuleInstance);
	}

}