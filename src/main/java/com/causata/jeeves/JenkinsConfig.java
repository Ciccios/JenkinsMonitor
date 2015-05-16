package com.causata.jeeves;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2015 NICE Systems
 */
public class JenkinsConfig {

    private String jenkinsBaseUrl;
    private JSONObject jsonConfig;

    public JenkinsConfig(String jenkinsBaseUrl) throws IOException, JSONException {
        Preconditions.checkNotNull(jenkinsBaseUrl);
        this.jenkinsBaseUrl = jenkinsBaseUrl;
        fetchCurrentJobsConfig();
    }

    public void fetchCurrentJobsConfig() throws IOException, JSONException {
        String jobsUrl = "/api/json?pretty=true&depth=10&tree=jobs[name,displayName,url,scm[branches[name]]]";
        HttpGet httpGet = new HttpGet(jenkinsBaseUrl + jobsUrl);
        HttpResponse response = new DefaultHttpClient().execute(httpGet);
        if(response.getStatusLine().getStatusCode() != 200) {
            throw new IllegalStateException("Configuration Fetch Failed");
        }
        String json = EntityUtils.toString(response.getEntity());
        jsonConfig = new JSONObject(json);
    }

    public Map<String, JobProperties> getJobsMap() throws JSONException {
        Map<String, JobProperties> jobNamesToProperties = Maps.newHashMap();
        JSONArray jobs = jsonConfig.getJSONArray("jobs");
        for (int i = 0; i < jobs.length(); i++) {
            JSONObject job = jobs.getJSONObject(i);
            String name = job.getString("name");
            String displayName = job.getString("displayName");
            String url = job.getString("url");

            String scmName = getScmNameIfExists(job);
            jobNamesToProperties.put(name, new JobProperties(displayName, url, scmName));
        }
        return jobNamesToProperties;
    }

    private String getScmNameIfExists(JSONObject job) throws JSONException {
        JSONObject scm = job.getJSONObject("scm");
        if(scm.has("branches")) {
            return scm.getJSONArray("branches").getJSONObject(0).getString("name");
        }
        return null;
    }

    public Set<String> getAllBranchesNames() throws JSONException {
        Set<String> branchNames = Sets.newHashSet();
        Map<String, JobProperties> jobsMap = getJobsMap();
        for (JobProperties jobProperties : jobsMap.values()) {
            String branchName = jobProperties.getBranchName();
            if (branchName != null) {
                String[] split = branchName.split("/");
                branchNames.add(split[1]);
            }
        }
        return branchNames;
    }

    public static void main(String[] args) throws IOException, JSONException {
        JenkinsConfig jenkinsConfig = new JenkinsConfig("http://tlvcausatabuild01.nice.com:8080/");
        jenkinsConfig.fetchCurrentJobsConfig();
        Map<String, JobProperties> jobsMap = jenkinsConfig.getJobsMap();
        System.out.println(jobsMap);
        System.out.println("ALl Branches :" + jenkinsConfig.getAllBranchesNames());

    }

    public static class JobProperties {
        private final String displayName;
        private final String url;
        private final String branchName;

        public JobProperties(String displayName, String url, String branchName) {
            this.displayName = displayName;
            this.url = url;
            this.branchName = branchName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getUrl() {
            return url;
        }

        public String getBranchName() {
            return branchName;
        }

        @Override
        public String toString() {
            return "JobProperties{" +
                    "displayName='" + displayName + '\'' +
                    ", url='" + url + '\'' +
                    ", branchName='" + branchName + '\'' +
                    '}';
        }
    }

}
