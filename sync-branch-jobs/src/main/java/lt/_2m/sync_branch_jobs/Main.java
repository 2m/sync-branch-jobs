package lt._2m.sync_branch_jobs;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final static String SERVER = "marsrutai-lt-android.ci.cloudbees.com";
    private final static String JENKINS_HOST = String.format("https://%s/", SERVER);

    private static String username;
    private static String password;

    public static void main(String[] args) throws Exception {

        getJenkinsUsernamePassword(args);

        List<String> openBranches = getOpenBranches();
        System.out.println("Currently open branches: " + openBranches);

        List<String> branchJobs = getBranchJobs();
        System.out.println("Currently created jobs: " + branchJobs);

        List<String> jobsToCreate = new ArrayList<>();
        for (String branch: openBranches) {
            if (!branchJobs.contains(branch)) {
                jobsToCreate.add(branch);
            }
        }
        System.out.println("Need to create jobs: " + jobsToCreate);

        List<String> jobsToDelete = new ArrayList<>();
        for (String job: branchJobs) {
            if (!openBranches.contains(job)) {
                jobsToDelete.add(job);
            }
        }
        System.out.println("Need to delete jobs: " + jobsToDelete);

        for (String job: jobsToCreate) {
            createJob(job);
        }

        for (String job: jobsToDelete) {
            deleteJob(job);
        }
    }

    private static List<String> getOpenBranches() throws IOException {
        Document doc = Jsoup.connect("https://code.google.com/p/marsrutai-lt-mobile/source/list").get();
        Elements branchOptions = doc.select("#branch_select option");

        List<String> openBranches = new ArrayList<>();
        for (Element e: branchOptions) {
            if (!e.text().toLowerCase().contains("closed")) {
                openBranches.add(e.val());
            }
        }

        return openBranches;
    }

    private static List<String> getBranchJobs() throws IOException {
        Document doc = Jsoup.connect("https://marsrutai-lt-android.ci.cloudbees.com/view/builds/api/xml/").get();
        Elements jobList = doc.select("listview job");

        List<String> branchJobs = new ArrayList<>();
        for (Element e: jobList) {
            branchJobs.add(e.getElementsByTag("name").text());
        }

        return branchJobs;
    }

    private static void createJob(String job) throws Exception {
        System.out.println("Creating job: " + job);

        StringBuilder newJobConfig = new StringBuilder(getDefaultJobConfiguration());
        int insertIndex = newJobConfig.indexOf("</scm>");
        newJobConfig.insert(insertIndex, "<branch>" + job + "</branch>");

        executePostRequest("view/builds/createItem?name=" + job, newJobConfig.toString());
    }

    private static void deleteJob(String job) throws Exception {
        System.out.println("Deleting job: " + job);

        executePostRequest("job/" + job + "/doDelete", "");
    }

    private static String getDefaultJobConfiguration() throws Exception {
        return executeGetRequest("job/default/config.xml");
    }

    private static String executePostRequest(String path, String contents) throws Exception {

        HttpClient client = getHttpClient();

        PostMethod post = new PostMethod(String.format("%s/%s", JENKINS_HOST, path));
        post.setDoAuthentication(true);

        RequestEntity entity = new StringRequestEntity(contents, "text/xml; charset=UTF-8", "UTF-8");
        post.setRequestEntity(entity);

        try {
            int result = client.executeMethod(post);
            return post.getResponseBodyAsString();
        } finally {
            post.releaseConnection();
        }
    }

    private static String executeGetRequest(String path) throws Exception {

        HttpClient client = getHttpClient();

        GetMethod get = new GetMethod(String.format("%s/%s", JENKINS_HOST, path));
        get.setDoAuthentication(true);

        try {
            int result = client.executeMethod(get);
            return get.getResponseBodyAsString();
        } finally {
            get.releaseConnection();
        }
    }

    private static HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        HttpState state = new HttpState();
        state.setCredentials(
                new AuthScope(SERVER, 443, "realm"),
                new UsernamePasswordCredentials(username, password)
        );
        client.setState(state);

        HttpClientParams params = client.getParams();
        params.setAuthenticationPreemptive(true);
        client.setParams(params);

        return client;
    }

    private static void getJenkinsUsernamePassword(String[] args) throws Exception {
        // lets just assume there is one argument and it is a path to credentials file
        FileInputStream fis = new FileInputStream(args[0]);
        DataInputStream dis = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(dis));

        username = br.readLine();
        password = br.readLine();

        dis.close();
    }
}