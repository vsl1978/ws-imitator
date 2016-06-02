package xyz.vsl.wsimitator.imitator.actions;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vsl on 13.05.2016.
 */
public class TemplateFile extends StaticFile {
    private String inputEncoding = "UTF-8";
    private String outputEncoding = "UTF-8";
    private String prefix = "#{{";
    private String suffix = "}}";
    private Pattern pattern;

    public TemplateFile(Element element) {
        super(element);
        if (element.hasAttribute("charset")) {
            inputEncoding =  outputEncoding = element.getAttribute("charset");
        }
        else {
            if (element.hasAttribute("input-charset"))
                inputEncoding = element.getAttribute("input-charset");
            if (element.hasAttribute("output-charset"))
                outputEncoding = element.getAttribute("output-charset");
        }

        String prefix = StringUtils.trimToNull(element.getAttribute("prefix"));
        if (prefix != null)
            this.prefix = prefix;
        String suffix = StringUtils.trimToNull(element.getAttribute("suffix"));
        if (suffix != null)
            this.suffix = suffix;
        pattern = Pattern.compile(Pattern.quote(this.prefix)+"(.+?)"+Pattern.quote(this.suffix), Pattern.MULTILINE);
    }

    @Override
    protected byte[] getFileContent(File file, WSIContext context) throws IOException {
        byte[] data = super.getFileContent(file, context);
        String text = new String(data, inputEncoding);
        text = context.substitute(text, pattern);
        return text != null ? text.getBytes(outputEncoding) : new byte[0];
    }
}
