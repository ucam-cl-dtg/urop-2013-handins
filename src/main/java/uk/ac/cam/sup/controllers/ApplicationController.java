package uk.ac.cam.sup.controllers;

import uk.ac.cam.sup.helpers.UserHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class ApplicationController {
    @Context
    private HttpServletRequest request;

    public String getApiKey() {
        return request.getSession().getServletContext().getInitParameter("apiKey");
    }

    public String getDashboardUrl() {
        return request.getSession().getServletContext().getInitParameter("dashboardUrl");
    }

    public String getQuestionsUrl() {
        return request.getSession().getServletContext().getInitParameter("questionsUrl");
    }

    public String getCurrentUser() {
        return UserHelper.getCurrentUser(request);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isAdmin() {
        return getCurrentUser() == null;
    }
}
