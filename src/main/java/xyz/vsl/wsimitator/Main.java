package xyz.vsl.wsimitator;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import xyz.vsl.wsimitator.imitator.*;
import xyz.vsl.wsimitator.imitator.checks.Method;
import xyz.vsl.wsimitator.util.Numbers;
import xyz.vsl.wsimitator.util.StringUtils;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

public class Main {
    private static final String LOG4J_PROPERTIES = "log4j.properties";
    private int keepAliveTimeout = 200;

    private Options supportedOptions;
    private int port = 8642;
    private String hostname;
    private File baseDirectory = new File(".");
    private List<Config> configs;
    private WSICommand action;
    private boolean verbose;
    private String generatorURI;
    private String generatorPOSTFile;
    private String generatorHeadersFile;
    private String generatorDataFile;
    private String generatorPause;

    private static Tomcat tomcat;
    private static Thread stopTomcat;

    public static void main(String[] args) throws Exception {
        Main app = new Main();

        app.parseCommandLine(args);
        Runnable task = null;
        switch (app.action) {
            case start: task = app.new TaskStart(); break;
            case stop: task = app.new TaskStop(); break;
            case help: task = app.new TaskHelp(); break;
            case generate: task = app.new TaskGenerate(); break;
        }

        task.run();
    }

    private class TaskGenerate implements Runnable {
        @Override
        public void run() {
            System.out.println("WS-Imitator v1.0 (Generator)");
            initLog4j();
            new RequestGenerator(port)
                    .setRequestURI(generatorURI)
                    .loadRequestHeaders(generatorHeadersFile)
                    .loadRequestBody(generatorPOSTFile)
                    .loadDataSet(generatorDataFile)
                    .setPause(generatorPause)
                    .run();
        }
    }

    private class TaskHelp implements Runnable {
        @Override
        public void run() {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("java -jar ws-imitator.jar [options]\nor java -jar ws-imitator.jar [-p port] -s", supportedOptions);
        }
    }

    private class TaskStop implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{127,0,0,1}), port);
                String request = "STOP / HTTP/1.0\r\n\r\n";
                OutputStream os = socket.getOutputStream();
                os.write(request.getBytes());
                os.flush();

                if (verbose) {
                    InputStream is = socket.getInputStream();
                    int ch;
                    while ((ch = is.read()) != -1)
                        System.out.print((char) ch);
                }

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class TaskStart implements Runnable {

        @Override
        public void run() {
            System.out.println("WS-Imitator v1.0 (Server)");
            initLog4j();
            parseConfigs();
            startWatchers();
            try {
                startTomcat();
            } catch (LifecycleException | ServletException e) {
                e.printStackTrace();
            }
        }

        private void startWatchers() {
            for (Config config : configs)
                new ConfigWatcher(config).start();
        }

        private void parseConfigs() {
            createStopConfig();
            for (Config config : configs)
                config.setBaseDirectory(baseDirectory).parse();
        }

        private void createStopConfig() {
            new Config(new ProcessingNode(new Method("STOP")).addChild(new ProcessingNode(new StopHandler())));
            stopTomcat = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    Logger logger = Logger.getLogger(Main.class);
                    if (verbose) logger.info("Shutdown WS-Imitator...");
                    ConfigWatcher.stopWatching();
                    try {
                        tomcat.stop();
                    } catch (LifecycleException e) {
                        logger.error("Tomcat.stop()", e);
                    } finally {
                        try {
                            tomcat.destroy();
                        } catch (LifecycleException e) {
                            logger.error("Tomcat.destroy()", e);
                        }
                    }
                    if (verbose) logger.info("Done");
                }
            };
            stopTomcat.setDaemon(true);
        }

        private class StopHandler implements RequestProcessor {
            @Override
            public ResultType process(WSIContext context) {
                stopTomcat.start();
                return ResultType.stop;
            }
        }

        private void startTomcat() throws LifecycleException, ServletException {
            tomcat = new Tomcat();
            tomcat.setPort(port);
            if (hostname != null)
                tomcat.setHostname(hostname);

            tomcat.getConnector().setAttribute("keepAliveTimeout", keepAliveTimeout);

            StandardServer server = (StandardServer)tomcat.getServer();
            AprLifecycleListener listener = new AprLifecycleListener();
            server.addLifecycleListener(listener);

            Context rootCtx = tomcat.addWebapp("", baseDirectory.getAbsolutePath());
            FilterDef filter = new FilterDef();
            filter.setFilterName("WSImitatorFilter");
            filter.setFilterClass(WSImitatorFilter.class.getName());
            rootCtx.addFilterDef(filter);
            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(filter.getFilterName());
            filterMap.addURLPattern("/*");
            rootCtx.addFilterMap(filterMap);

            tomcat.start();
            tomcat.getServer().await();
        }
    }



    private void parseCommandLine(String[] args) throws ParseException {
        supportedOptions = new Options();
        supportedOptions.addOption("p", "port", true, "port number to listen http requests (default: 8642)");
        supportedOptions.addOption("h", "host", true, "Tomcat's host name (default: localhost)");
        supportedOptions.addOption("s", "stop", false, "stop runned ws-imitator");
        supportedOptions.addOption("v", "verbose", false, "verbose mode");
        //supportedOptions.addOption("X:persistence", false, "enable auto learning (store responses from remote servers)");
        //supportedOptions.addOption("X:dumps", false, "enable request dumps");
        //supportedOptions.addOption("X:sleep", false, "enable response time imitation");
        supportedOptions.addOption("X", true, ":persistence, :dumps, :sleep - enable request processing features");
        supportedOptions.addOption("b", "basedir", true, "base directory for returned files");
        supportedOptions.addOption("k", "keep-alive-timeout", true, "Tomcat's HTTP connector property \"keepAliveTimeout\" (default: 500, original Tomcat's default: 60000)");
        supportedOptions.addOption("c", "config", true, "config file (more than one -c <file> allowed)");
        supportedOptions.addOption("gu", "gen-uri", true, "generate set of requests to (ws-imitator instance) http://127.0.0.1:8642/<URI>");
        supportedOptions.addOption("gp", "gen-post", true, "file to POST (request body w/o headers) to --gen-uri");
        supportedOptions.addOption(null, "gen-pause", true, "pause between requests");
        supportedOptions.addOption("gh", "gen-headers", true, "file with headers (in \"name:value\" format) for requests to be sent to --gen-uri");
        supportedOptions.addOption("gd", "gen-data", true, "csv-file with data to replace \"#{{CSV_COLUMN_NAME}}\" entities in --gen-post and --gen-uri");
        supportedOptions.addOption("?", "help", false, "pring this message");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(supportedOptions, args);

        action = cmd.hasOption('?') ? WSICommand.help : cmd.hasOption('s') ? WSICommand.stop : cmd.hasOption("gen-uri") ? WSICommand.generate : WSICommand.start;

        verbose = cmd.hasOption('v');
        configs = new ArrayList<>();

        if (cmd.hasOption('p'))
            this.port = Numbers.val(cmd.getOptionValue('p'), port, 1024, 65000);
        if (cmd.hasOption('h'))
            this.hostname = StringUtils.trimToNull(cmd.getOptionValue('h'));

        if (cmd.hasOption('b')) {
            this.baseDirectory = new File(cmd.getOptionValue('b'));
            if (!this.baseDirectory.exists()) {
                if (!this.baseDirectory.mkdirs())
                    throw new IllegalArgumentException("Failed to create base directory " + this.baseDirectory);
            } else if (!this.baseDirectory.isDirectory()) {
                throw new IllegalArgumentException(this.baseDirectory + " is not a directory");
            }
        }

        if (action == WSICommand.start) {
            if (!cmd.hasOption('c'))
                throw new IllegalStateException("Parameter -c is required");
            for (String path : cmd.getOptionValues('c')) {
                Config config = new Config();
                File file = new File(path);
                if (!file.exists())
                    throw new IllegalArgumentException("File " + file + " not found");
                if (!file.isFile())
                    throw new IllegalArgumentException(file + " is not a file");
                config.setConfigFile(file);
                configs.add(config);
            }

            Set<String> x = cmd.hasOption('X') ? new HashSet<>(Arrays.asList(cmd.getOptionValues('X'))) : Collections.emptySet();
            if (x.contains("persistence") || x.contains(":persistence")) {
                System.setProperty("xyz.vsl.WSImitator.autoLearning", "true");
            }
            if (x.contains("dumpHeaders") || x.contains(":dumpHeaders")) {
                System.setProperty("xyz.vsl.WSImitator.dumpHeaders", "true");
            }
            if (x.contains("dumpAttributes") || x.contains(":dumpAttributes")) {
                System.setProperty("xyz.vsl.WSImitator.dumpAttributes", "true");
            }
            if (x.contains("dumpBody") || x.contains(":dumpBody")) {
                System.setProperty("xyz.vsl.WSImitator.dumpBody", "true");
            }
            if (x.contains("dumps") || x.contains(":dumps")) {
                System.setProperty("xyz.vsl.WSImitator.dumpHeaders", "true");
                System.setProperty("xyz.vsl.WSImitator.dumpAttributes", "true");
                System.setProperty("xyz.vsl.WSImitator.dumpBody", "true");
            }
            if (x.contains("sleep") || x.contains(":sleep")) {
                System.setProperty("xyz.vsl.WSImitator.sleep", "true");
            }
        }
        if (action == WSICommand.generate) {
            generatorURI = cmd.getOptionValue("gen-uri");
            generatorPOSTFile = cmd.getOptionValue("gen-post");
            generatorHeadersFile = cmd.getOptionValue("gen-headers");
            generatorDataFile = cmd.getOptionValue("gen-data");
            generatorPause = cmd.getOptionValue("gen-pause");
        }

    }

    private void initLog4j() {
        Set<String> locations = new LinkedHashSet<>();
        File properties;
        search:
        {
            // current directory
            properties = new File(LOG4J_PROPERTIES);
            if (properties.isFile())
                break search;
            locations.add(properties.getAbsolutePath());

            // next to config file
            for (Config config : configs) {
                properties = new File(config.getConfigFile().getAbsoluteFile().getParent(), LOG4J_PROPERTIES);
                if (properties.isFile())
                    break search;
                locations.add(properties.getAbsolutePath());
            }

            // jar directory
            try {
                URL codeSource = getClass().getProtectionDomain().getCodeSource().getLocation();
                File jar = new File(codeSource.toURI());
                if (jar.isFile()) {
                    properties = new File(jar.getParent(), LOG4J_PROPERTIES);
                    if (properties.isFile())
                        break search;
                    locations.add(properties.getAbsolutePath());
                }
            } catch (SecurityException | NullPointerException | URISyntaxException e) {
                // do nothing
            }

            initLog4jUsingDefaultProperties();
            Logger logger = Logger.getLogger(getClass());
            for (String location : locations)
                logger.debug("File not found: "+location);
            logger.info("Using classpath:" + LOG4J_PROPERTIES);
            return;
        }
        PropertyConfigurator.configure(properties.getAbsolutePath());
        Logger.getLogger(getClass()).info("Using " + properties);
    }

    private void initLog4jUsingDefaultProperties() {
        try {
            try (InputStream is = getClass().getResourceAsStream("/"+LOG4J_PROPERTIES)) {
                if (is != null)
                    PropertyConfigurator.configure(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
