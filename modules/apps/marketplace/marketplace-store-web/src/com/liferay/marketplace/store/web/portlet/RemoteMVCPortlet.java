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

package com.liferay.marketplace.store.web.portlet;

import com.liferay.marketplace.store.web.oauth.util.OAuthManager;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * @author Ryan Park
 * @author Joan Kim
 */
public class RemoteMVCPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		String remoteMVCPath = renderRequest.getParameter("remoteMVCPath");

		if (remoteMVCPath != null) {
			try {
				remoteRender(renderRequest, renderResponse);
			}
			catch (IOException ioe) {
				throw ioe;
			}
			catch (Exception e) {
				throw new PortletException(e);
			}
		}
		else {
			super.render(renderRequest, renderResponse);
		}
	}

	@Override
	public void serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws IOException, PortletException {

		try {
			remoteServeResource(resourceRequest, resourceResponse);
		}
		catch (IOException ioe) {
			throw ioe;
		}
		catch (Exception e) {
			throw new PortletException(e);
		}
	}

	protected OAuthRequest getGetOAuthRequest(
			PortletRequest portletRequest, PortletResponse portletResponse)
		throws Exception {

		OAuthRequest oAuthRequest = new OAuthRequest(
			Verb.GET, getServerPortletURL());

		setRequestParameters(portletRequest, portletResponse, oAuthRequest);

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Token token = _oAuthManager.getAccessToken(themeDisplay.getUser());

		if (token != null) {
			OAuthService oAuthService = _oAuthManager.getOAuthService();

			oAuthService.signRequest(token, oAuthRequest);
		}

		return oAuthRequest;
	}

	protected String getServerNamespace() {
		return StringPool.BLANK;
	}

	protected String getServerPortletURL() {
		return StringPool.BLANK;
	}

	protected void remoteRender(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws Exception {

		OAuthRequest oAuthRequest = getGetOAuthRequest(
			renderRequest, renderResponse);

		Response response = oAuthRequest.send();

		renderResponse.setContentType(ContentTypes.TEXT_HTML);

		PrintWriter printWriter = renderResponse.getWriter();

		printWriter.write(response.getBody());
	}

	protected void remoteServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		OAuthRequest oAuthRequest = getGetOAuthRequest(
			resourceRequest, resourceResponse);

		oAuthRequest.addQuerystringParameter("p_p_lifecycle", "2");
		oAuthRequest.addQuerystringParameter(
			"p_p_resource_id", resourceRequest.getResourceID());

		Response response = oAuthRequest.send();

		PortletResponseUtil.write(resourceResponse, response.getStream());
	}

	protected void setOAuthManager(OAuthManager oAuthManager) {
		_oAuthManager = oAuthManager;
	}

	protected void setRequestParameters(
			PortletRequest portletRequest, PortletResponse portletResponse,
			OAuthRequest oAuthRequest)
		throws Exception {

		String currentURL = PortalUtil.getCurrentURL(portletRequest);

		oAuthRequest.addQuerystringParameter("clientURL", currentURL);
		oAuthRequest.addQuerystringParameter(
			"clientPortletNamespace", portletResponse.getNamespace());

		String serverNamespace = getServerNamespace();

		Map<String, String[]> parameterMap = portletRequest.getParameterMap();

		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			String[] values = entry.getValue();

			if (ArrayUtil.isEmpty(values) || Validator.isNull(values[0])) {
				continue;
			}

			oAuthRequest.addQuerystringParameter(
				serverNamespace.concat(entry.getKey()), values[0]);
		}
	}

	private OAuthManager _oAuthManager;

}