package xyz.vsl.wsimitator.imitator.actions;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.StringUtils;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vsl on 13.05.2016.
 */
public class StaticFile extends AbstractAction {
    public final static String TARGET_FILE = "target static file";
    protected String fileNameTemplate;
    private final static Pattern PARAM = Pattern.compile("\\{([^}]+)\\}");
    private Logger logger = Logger.getLogger(getClass());

    public StaticFile(Element element) {
        super(element);
        fileNameTemplate = StringUtils.trimToNull(element.getAttribute("file"));
    }

    @Override
    public ResultType process(WSIContext context) {
        File file = getFile(context);
        if (file == null || !file.isFile()) {
            logger.warn("File "+file+" not found");
            if (getChildren().isEmpty())
                return ResultType.tryNext;
            context.enter();
            if (file != null)
                context.set(TARGET_FILE, file.getAbsolutePath());
            return ResultType.enter;
        }
        try {
            byte[] data = getFileContent(file, context);
            context.getResponse().setIntHeader("Content-Length", data.length);
            ServletOutputStream outputStream = context.getResponse().getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
            logger.trace("file sent");
        } catch (IOException e) {
            logger.error(file.getAbsolutePath(), e);
        }
        return ResultType.stop;
    }

    protected byte[] getFileContent(File file, WSIContext context) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file), 128 * 1024)) {
            byte[] buf = new byte[128 * 1024];
            int r;
            while ((r = is.read(buf)) >= 0)
                baos.write(buf, 0, r);
        }
        return baos.toByteArray();
    }

    protected File getFile(WSIContext context) {
        if (StringUtils.isEmpty(fileNameTemplate))
            return null;
        String fileName = context.fileSystemSafeSubstitute(fileNameTemplate, PARAM);
        return StringUtils.isEmpty(fileName) ? null : new File(context.getBaseDirectory(), fileName);
    }
}
