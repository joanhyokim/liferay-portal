<%--
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
--%>

<%@ include file="/html/portlet/document_library/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

ResultRow row = (ResultRow)request.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);

FileVersion fileVersion = (FileVersion)row.getObject();

FileEntry fileEntry = fileVersion.getFileEntry();
%>

<liferay-ui:icon-menu direction='<%= "down" %>' icon="<%= StringPool.BLANK %>" message="<%= StringPool.BLANK %>">
	<liferay-ui:icon
		iconCssClass="icon-download"
		message="download"
		method="get"
		url="<%= DLUtil.getPreviewURL(fileEntry, fileVersion, themeDisplay, StringPool.BLANK) %>"
	/>

	<portlet:renderURL var="viewFileVersionURL">
		<portlet:param name="mvcRenderCommandName" value="/document_library/view_file_entry" />
		<portlet:param name="redirect" value="<%= redirect %>" />
		<portlet:param name="fileEntryId" value="<%= String.valueOf(fileEntry.getFileEntryId()) %>" />
		<portlet:param name="version" value="<%= fileVersion.getVersion() %>" />
	</portlet:renderURL>

	<liferay-ui:icon
		iconCssClass="icon-search"
		message="view[action]"
		url="<%= viewFileVersionURL %>"
	/>

	<portlet:renderURL var="viewFileEntryURL">
		<portlet:param name="mvcRenderCommandName" value="/document_library/view_file_entry" />
		<portlet:param name="redirect" value="<%= redirect %>" />
		<portlet:param name="fileEntryId" value="<%= String.valueOf(fileEntry.getFileEntryId()) %>" />
	</portlet:renderURL>

	<c:if test="<%= (fileVersion.getStatus() != WorkflowConstants.STATUS_IN_TRASH) && DLFileEntryPermission.contains(permissionChecker, fileEntry, ActionKeys.UPDATE) && (fileVersion.getStatus() == WorkflowConstants.STATUS_APPROVED) && !fileEntry.getLatestFileVersion().getVersion().equals(fileVersion.getVersion()) %>">
		<portlet:actionURL name="/document_library/edit_file_entry" var="revertURL">
			<portlet:param name="<%= Constants.CMD %>" value="<%= Constants.REVERT %>" />
			<portlet:param name="redirect" value="<%= viewFileEntryURL %>" />
			<portlet:param name="fileEntryId" value="<%= String.valueOf(fileEntry.getFileEntryId()) %>" />
			<portlet:param name="version" value="<%= String.valueOf(fileVersion.getVersion()) %>" />
		</portlet:actionURL>

		<liferay-ui:icon
			iconCssClass="icon-undo"
			message="revert"
			url="<%= revertURL %>"
		/>
	</c:if>

	<c:if test="<%= (fileVersion.getStatus() != WorkflowConstants.STATUS_IN_TRASH) && DLFileEntryPermission.contains(permissionChecker, fileEntry, ActionKeys.DELETE) && (fileVersion.getStatus() == WorkflowConstants.STATUS_APPROVED) && (fileEntry.getModel() instanceof DLFileEntry) && (((DLFileEntry)fileEntry.getModel()).getFileVersionsCount(WorkflowConstants.STATUS_APPROVED) > 1) %>">
		<portlet:actionURL name="/document_library/edit_file_entry" var="deleteURL">
			<portlet:param name="<%= Constants.CMD %>" value="<%= Constants.DELETE %>" />
			<portlet:param name="redirect" value="<%= viewFileEntryURL %>" />
			<portlet:param name="fileEntryId" value="<%= String.valueOf(fileEntry.getFileEntryId()) %>" />
			<portlet:param name="version" value="<%= String.valueOf(fileVersion.getVersion()) %>" />
		</portlet:actionURL>

		<liferay-ui:icon-delete
			message="delete-version"
			url="<%= deleteURL %>"
		/>
	</c:if>
</liferay-ui:icon-menu>