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

package com.liferay.blogs.web.messaging;

import aQute.bnd.annotation.metatype.Configurable;

import com.liferay.blogs.configuration.BlogsSystemConfiguration;
import com.liferay.blogs.web.constants.BlogsPortletKeys;
import com.liferay.portal.kernel.messaging.BaseSchedulerEntryMessageListener;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.scheduler.SchedulerEntry;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.TriggerType;
import com.liferay.portal.model.Portlet;
import com.liferay.portlet.blogs.service.BlogsEntryLocalServiceUtil;

import java.util.Map;

import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Zsolt Berentey
 */
@Component(
	configurationPid = "com.liferay.blogs.configuration.BlogsSystemConfiguration",
	property = {"javax.portlet.name=" + BlogsPortletKeys.BLOGS},
	service = SchedulerEntry.class
)
public class CheckEntryMessageListener
	extends BaseSchedulerEntryMessageListener {

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		schedulerEntry.setTimeUnit(TimeUnit.MINUTE);
		schedulerEntry.setTriggerType(TriggerType.SIMPLE);

		_blogsSystemConfiguration = Configurable.createConfigurable(
			BlogsSystemConfiguration.class, properties);

		schedulerEntry.setTriggerValue(
			_blogsSystemConfiguration.entryCheckInterval());
	}

	@Override
	protected void doReceive(Message message) throws Exception {
		BlogsEntryLocalServiceUtil.checkEntries();
	}

	@Reference(target = "(javax.portlet.name=" + BlogsPortletKeys.BLOGS + ")")
	protected void setPortlet(Portlet portlet) {
	}

	@Reference(target = "(original.bean=*)", unbind = "-")
	protected void setServletContext(ServletContext servletContext) {
	}

	private volatile BlogsSystemConfiguration _blogsSystemConfiguration;

}