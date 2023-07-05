package com.main.stepper.server.servlets.history.flow;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.main.stepper.engine.definition.api.IEngine;
import com.main.stepper.engine.executor.api.IFlowRunResult;
import com.main.stepper.engine.executor.implementation.FlowRunResult;
import com.main.stepper.server.constants.ServletAttributes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name="FlowRunHistoryServlet", urlPatterns = "/history/flow")
public class FlowRunHistoryServlet extends HttpServlet {
    private List<IFlowRunResult> getClient(List<UUID> uuids, String cookie) {
        // check if empty
        if (uuids.isEmpty()) {
            return new ArrayList<>();
        }
        else {
            IEngine engine = (IEngine) getServletContext().getAttribute(ServletAttributes.ENGINE);
            List<IFlowRunResult> flowRunsFromList = engine.getFlowRunsFromList(uuids);
            return flowRunsFromList.stream().filter(f -> f.user().equals(cookie)).collect(Collectors.toList());
        }
    }

    private List<IFlowRunResult> getAdmin(List<UUID> uuids) {
        // check if empty
        if (uuids.isEmpty()) {
            return new ArrayList<>();
        }
        else {
            IEngine engine = (IEngine) getServletContext().getAttribute(ServletAttributes.ENGINE);
            return engine.getFlowRunsFromList(uuids);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // get requested uuids
        Gson gson = new Gson();
        List<UUID> uuids = gson.fromJson(req.getReader(), new TypeToken<List<UUID>>() {}.getType());
        // get user cookie
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> c.getName().equals("name")).findFirst();
        // initialize return list
        List<IFlowRunResult> results;
        if (cookie.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            results = getClient(uuids, cookie.get().getValue());
        }
        else {
            // check if admin
            String isAdmin = req.getHeader("isAdmin");
            if (isAdmin != null && isAdmin.equals("true")) {
                resp.setStatus(HttpServletResponse.SC_OK);
                results = getAdmin(uuids);
            }
            else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                results = new ArrayList<>();
            }
        }
        gson.toJson(results, new TypeToken<List<FlowRunResult>>() {}.getType(), resp.getWriter());
    }
}