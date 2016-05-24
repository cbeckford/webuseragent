package com.webuseragent.main;

import com.webuseragent.helpers.ConfigHelper;
import com.webuseragent.helpers.FileHelper;
import com.webuseragent.helpers.JsonHelper;
import com.webuseragent.helpers.WebUserReport;
import com.webuseragent.models.WebUser;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.simple.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import static spark.Spark.*;

/**
 * <h1>Web User Agent API!</h1>
 * <p>
 * The <b>Web User Agent API</b> library provides an application that implements
 * the <b>"UAT Simulation"</b> framework which provides standard application
 * classes and interfaces to facilitate the creation of automated acceptance
 * tests sing a modeling process that does not require any coding skills.
 *
 * The library is intended to enable the development of automated testing
 * framework that can make customer/end user testing AGILE. </p>
 *
 * @author Clive Beckford
 * <a href="https://github.com/cbeckford">https://github.com/cbeckford</a>
 * @version 1.0
 * @since 2016-04-24
 *
 */
public class App {

    public static final Map<String, String> SINGLE_OPTIONS = new HashMap();
    public static final List<String> MULTI_OPTIONS = new ArrayList<>();
    private static Properties APP_PROPS = ConfigHelper.getInstance().getProperties();
    private static Integer loggingLevel = 3;  //0=QUIET 1=ERROR, 2=WARNING, 3=INFO, 4=DEBUG
    private static String theJsonFileName = null;
    private static String theJsonString = null;
    private static File theJsonFile;
    private static ArrayList<String> WEB_USERS = new ArrayList<>();
    private static final ArrayList<String> theJsonFileNames = new ArrayList<>();
    private static final LinkedHashMap<Integer, WebUserReport> webUserReports = new LinkedHashMap();
    private static Integer wsPort = 8080;
    /**
     * @return the WEB_USERS
     */
    public static ArrayList<String> getWebUsers() {
        return WEB_USERS;
    }

    /**
     * @param aWebUsers the WEB_USERS to set
     */
    public static void setWebUsers(ArrayList<String> aWebUsers) {
        WEB_USERS = aWebUsers;
    }

    /**
     * This is the main method use when the jar is run from a command line
     *
     * @param args Use to provide a list of arguments, use "--help" to see
     * syntax details
     */
    public static void main(String[] args) {
        System.out.println("Starting Web User Agent API..");
        parseArgs(args);
        say("[INFO]", SINGLE_OPTIONS.get("f"));

        if (MULTI_OPTIONS.contains("help")) {
            say("INFO", "-----------------------------------");
            say("INFO", "Help on command line usage syntax");
            say("INFO", "-----------------------------------");
            say("INFO", "[-l <level>] Sets logging to one of the following: QUIET, ERROR, WARNING, INFO, DEBUG");
            say("INFO", "[-p <port#>] The port on which the web service will listen. Defaults to 8080");
            say("INFO", "[-j <JSON string>] JSON text defining the Web User Agent");
            say("INFO", "[-d <JSON files folder>] Path to a folder with JSON files to define the Web User Agent");
            say("INFO", "[-f <JSON file>] Path to a JSON file to define the Web User Agent");
            say("INFO", "-----------------------------------");
            say("INFO", "[--help] Displays this message");
            say("INFO", "[--google-test] Sets logging to one of the following: DEBUG, INFO, WARNING, ERROR. NOTE: Overrides [-j]");
            say("INFO", "[--server] Runs as a light weight RESTful Web Service");
            say("INFO", "");
            return;
        }

        //Set logging from arguments or configuration (defaults to 3=INFO)
        if (SINGLE_OPTIONS.get("-l") != null && !SINGLE_OPTIONS.get("-l").isEmpty()) {
            switch (SINGLE_OPTIONS.get("-l")) {
                case "QUIET":
                    setLoggingLevel(0);
                    break;
                case "ERROR":
                    setLoggingLevel(1);
                    break;
                case "WARNING":
                    setLoggingLevel(2);
                    break;
                case "INFO":
                    setLoggingLevel(3);
                    break;
                case "DEBUG":
                    setLoggingLevel(4);
                    break;
                default:
                    setLoggingLevel();
                    break;
            }
        } else {
            setLoggingLevel();
        }

        //Use the Google Test JSON file
        if (MULTI_OPTIONS.contains("google-test")) {
            theJsonFileName = "/WebUserAgentGoogleStory.json";
        } else {
            //Run the Google Test JSON file
            if (SINGLE_OPTIONS.get("-j") != null && !SINGLE_OPTIONS.get("-j").isEmpty()) {
                say("INFO", "Using the JSON string provided");
                theJsonString = SINGLE_OPTIONS.get("-j");
            }
            if (SINGLE_OPTIONS.get("-p") != null && SINGLE_OPTIONS.get("-j").isEmpty()) {
                say("INFO", "Missing Port number argument, using default (8080)");
                theJsonString = SINGLE_OPTIONS.get("-j");
            } 
            if (SINGLE_OPTIONS.get("-p") != null && !SINGLE_OPTIONS.get("-j").isEmpty()) {
                say("INFO", "Using default (8080)");
                wsPort = Integer.valueOf( SINGLE_OPTIONS.get("-p") );
            } 
            if (SINGLE_OPTIONS.get("-f") != null && !SINGLE_OPTIONS.get("-f").isEmpty()) {
                theJsonFileName = SINGLE_OPTIONS.get("-f");
                try {
                    theJsonFile = new File(theJsonFileName);
                    if (theJsonFile.exists() && theJsonFile.isFile()) {
                        //Add the json file to the list
                        theJsonFileNames.add(theJsonFileName);
                        say("INFO", "Found json file (" + theJsonFile.getPath() + ")");
                    } else {
                        say("ERROR", " File does not exist: " + theJsonFileName + "");
                    }
                } catch (Exception ex) {
                    say("ERROR", " Cannot open " + theJsonFileName + "");
                    return;
                }
            }
            if (SINGLE_OPTIONS.get("-d") != null && !SINGLE_OPTIONS.get("-d").isEmpty()) {
                File srcDir = new File(SINGLE_OPTIONS.get("-d"));

                //Invalid directory!
                if (srcDir.isDirectory() == false) {
                    say("ERROR", " Cannot find the directory (" + SINGLE_OPTIONS.get("-d") + ")!");
                    return;
                }
                File[] jsonFiles = new File(SINGLE_OPTIONS.get("-d")).listFiles((File file) -> file.getName().endsWith(".json"));

                //No json files found in the directory specified
                if (jsonFiles.length < 1) {
                    say(String.valueOf(jsonFiles.length));
                    say("ERROR", " Cannot find any .json files in the directory (" + SINGLE_OPTIONS.get("-d") + ")!");
                    return;
                }
                say("INFO", "Using the JSON files found in the directory provided");
                for (File file : jsonFiles) {
                    if (file.isFile()) {
                        //Add the json file to the list
                        theJsonFileNames.add(file.getPath());
                        say("INFO", "Found json file (" + file.getPath() + ")");
                    }
                }
                if (theJsonFileNames.size() > 0) {
                    say("INFO", "Using the JSON files found in the directory provided");
                }
            }
        }

        //Processing Mode (webservice or commandline)
        if (MULTI_OPTIONS.contains("server")) {
            startWebService();
        } else {
            commandFilesProcessing();
        }
    }

    private static void startWebService() {
        port(wsPort);
        Spark.externalStaticFileLocation(App.getAPP_PROPS().getProperty("public.localPath"));
                
        //Set static files location
        //Spark.staticFileLocation("/public");
        //Spark.externalStaticFileLocation(theJsonFileName);
        
        get("/", (req, res)
                -> "<h1>A Simulated Web User Agent Driver Service</h1>"
                + "<p>An automated acceptance testing service</p>");

        get("/webuseragent/randomuser", new Route() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
               
                return "Random User";
            } 
        });

        /*Add Web User Agent Submission Page*/
        get("/webuseragent/add", new Route() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
                /*  get the default test JSON file  */
                String webUserJson = "";
                if (readJsonFile("src/main/resources/WebUserAgentGoogleStory.json")) {
                    webUserJson = theJsonString;
                }

                /*  first, get and initialize an engine  */
                VelocityEngine ve = new VelocityEngine();
                ve.init();
                /*  next, get the Template  */
                Template t = ve.getTemplate("/src/main/resources/views/AddWebUser.vm");
                /*  create a context and add data */
                VelocityContext context = new VelocityContext();
                context.put("web-user-json", webUserJson);
                /* now render the template into a StringWriter */
                StringWriter writer = new StringWriter();
                t.merge(context, writer);
                /* show the World */
                //System.out.println( writer.toString() );
                return writer.toString();
            }
        });

        post("/webuseragent/run", (req, res) -> {
            //App.getWebUsers().add(req.queryParams("web-user-json"));
            Integer reportNumber = commandStringProcessing(req.queryParams("web-user-json"));
            res.status(200);
            res.redirect("/webuseragent/report/" + reportNumber.toString());
            return "";
        });

        /*Watch Web User Agent Page*/
        get("/webuseragent/reports", new Route() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
                StringBuilder html = new StringBuilder();
                if (App.webUserReports.size()>0) {
                    html.append("<ol>");
                    App.webUserReports.keySet().forEach((thing) -> {
                        html.append("<li>")
                                .append("<a href=/webuseragent/report/")
                                .append(thing)
                                .append(">Open report #")
                                .append(thing)
                                .append("</a>")
                                .append("</li>");
                    });
                    html.append("</ol>");
                }
                html.append("<br><b>Commands:</b> | <a href='/webuseragent/add'>Add Web User Agent</a> | ");
                return html.toString();
            }
        });

        /*Watch Web User Agent Page*/
        get("/webuseragent/report/:reportNumber", new Route() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
                //StringBuilder html = new StringBuilder();                
                /*  first, get and initialize an engine  */
                VelocityEngine ve = new VelocityEngine();
                ve.init();
                /*  next, get the Template  */
                Template t = ve.getTemplate("/src/main/resources/views/WatchWebUser.vm");
                /*  create a context and add data */
                VelocityContext context = new VelocityContext();

                if (Integer.valueOf(req.params(":reportNumber")) <= 0 || webUserReports.size() < Integer.valueOf(req.params(":reportNumber"))) {
                    context.put("web-user-report", "<h1>Web User Report Not Found!</h>");
                }                
                if (webUserReports.size()<=0 || webUserReports.size() >= Integer.valueOf(req.params(":reportNumber"))) {
                    WebUserReport wr = webUserReports.get(Integer.valueOf(req.params(":reportNumber")));
                    context.put("web-user-report", wr.toString("HTML"));          
                }
                /* now render the template into a StringWriter */
                StringWriter writer = new StringWriter();
                t.merge(context, writer);
                /* show the World */
                return writer.toString();
            }
        });

        /*Watch Web User Agent Page*/
        get("/webuseragent/reports/sample", new Route() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
                //StringBuilder html = new StringBuilder();                
                /*  first, get and initialize an engine  */
                VelocityEngine ve = new VelocityEngine();
                ve.init();
                /*  next, get the Template  */
                Template t = ve.getTemplate("/src/main/resources/views/WatchWebUser.vm");
                /*  create a context and add data */
                VelocityContext context = new VelocityContext();

                if (App.getWebUsers().isEmpty()) {
                    WebUserReport wr = new WebUserReport();
                    wr.loadTestReport();
                    context.put("web-user-report", wr.toString("HTML"));
                }
                /* now render the template into a StringWriter */
                StringWriter writer = new StringWriter();
                t.merge(context, writer);
                /* show the World */
                return writer.toString();
            }
        });
    }
    
    private static void commandFilesProcessing() {
        //Read the contents of each targeted json file
        theJsonFileNames.stream().filter((thisJsonFileName) -> (readJsonFile(thisJsonFileName))).forEach(new Consumer<String>() {
            @Override
            public void accept(String thisJsonFileName) {
                say("DEBUG", "JSON CONTENT:" + theJsonString);

                if (theJsonString == null || theJsonString.isEmpty()) {
                    say("INFO]", "Nothing to do! (" + theJsonFileName + ")!");
                    return;
                }

                JSONObject wuser = new JsonHelper().jsonObjectToJson(
                        new JsonHelper().jsonObjectToJson(
                                new JsonHelper().stringToJson(theJsonString),
                                "results"),
                        "webUserAgent");
                WebUser webUser = new WebUser(wuser);
                webUser.closeBrowser();
                say("DONE!");
            }
        });
    }
    
    private static Integer commandStringProcessing(String jsonString) {
        //Read the contents of each targeted json file            
        say("DEBUG", "JSON CONTENT:" + theJsonString);

        if (jsonString == null || jsonString.isEmpty()) {
            say("WARNING", "Undefined Web User Agent!");
            return 0;
        }
        
        JSONObject wuser = new JsonHelper().jsonObjectToJson(
                new JsonHelper().jsonObjectToJson(
                        new JsonHelper().stringToJson(jsonString),
                        "results"),
                "webUserAgent");
        WebUser webUser = new WebUser(wuser);
        webUser.closeBrowser();
        //webUser.closeCurrentBrowserWindow();
        say("DONE!");
        Integer reportNumber = webUserReports.size() + 1;
        webUserReports.put(reportNumber, webUser.wr);
        webUser=null;
        return reportNumber;
    }

    private static boolean readJsonFile(String thisFileName) {
        try {
            theJsonString = new FileHelper().readFile(thisFileName, Charset.forName("UTF-8"));
            return true;
        } catch (Exception ex) {
            say("ERROR", "Unable to read file " + ex.getMessage());
            return false;
        }
    }

    private static void parseArgs(String[] args) {

        for (int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].length() < 2) {
                        throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                    }
                    if (args[i].charAt(1) == '-') {
                        if (args[i].length() < 3) {
                            throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                        }
                        // --opt
                        MULTI_OPTIONS.add(args[i].substring(2, args[i].length()));
                    } else {
                        if (args.length - 1 == i) {
                            throw new IllegalArgumentException("Expected arg after: " + args[i]);
                        }
                        // -opt
                        SINGLE_OPTIONS.put(args[i], args[i + 1]);
                        i++;
                    }
                    break;
                default:
                    // arg
                    MULTI_OPTIONS.add(args[i]);
                    break;
            }
        }
    }

    public static void say(String type, String what) {
        if (getLoggingLevel() == 0) {
            //QUIET mode
            return;
        }
        switch (type) {
            case "ERROR":
                if (getLoggingLevel() < 1) {
                    return;
                }
                break;
            case "WARNING":
                if (getLoggingLevel() < 2) {
                    return;
                }
                break;
            case "INFO":
                if (getLoggingLevel() < 3) {
                    return;
                }
                break;
            case "DEBUG":
                if (getLoggingLevel() < 4) {
                    return;
                }
                break;
            default:
                return;
        }
        
        if (getAPP_PROPS().getProperty("webUserLogging.TYPE").toUpperCase().equals("CONSOLE")) {
            System.out.println(when() + " [" + type + "] |" + what);
        }
    }

    public static void say(String what) {
        if (what != null) {
            say("INFO", what);
        }
    }

    /**
     * @return the APP_PROPS
     */
    public static Properties getAPP_PROPS() {
        return APP_PROPS;
    }

    /**
     * @param aAPP_PROPS the APP_PROPS to set
     */
    public static void setAPP_PROPS(Properties aAPP_PROPS) {
        APP_PROPS = aAPP_PROPS;
    }

    public static String when() {
        return (String) (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()));
    }

    public static String whenFile() {
        return (String) (new SimpleDateFormat("yyyy-MM-dd_HHmmssSSS").format(new Date()));
    }

    /**
     * @return the loggingLevel
     */
    public static Integer getLoggingLevel() {
        return loggingLevel;
    }

    /**
     * @param aLoggingLevel the loggingLevel to set
     */
    public static void setLoggingLevel(Integer aLoggingLevel) {
        loggingLevel = aLoggingLevel;
    }

    public static void setLoggingLevel() {
        if (getAPP_PROPS().get("webUserLogging.LEVEL").toString().equals("0")
                || getAPP_PROPS().get("webUserLogging.LEVEL").toString().equals("1")
                || getAPP_PROPS().get("webUserLogging.LEVEL").toString().equals("2")
                || getAPP_PROPS().get("webUserLogging.LEVEL").toString().equals("3")
                || getAPP_PROPS().get("webUserLogging.LEVEL").toString().equals("4")) {
            setLoggingLevel(Integer.parseInt(getAPP_PROPS().get("webUserLogging.LEVEL").toString()));
        }
    }

    /**
     * @return the wsPort
     */
    public static int getWsPort() {
        return wsPort;
    }

    /**
     * @param aWsPort the wsPort to set
     */
    public static void setWsPort(int aWsPort) {
        wsPort = aWsPort;
    }

}
