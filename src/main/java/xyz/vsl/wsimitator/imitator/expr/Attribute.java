package xyz.vsl.wsimitator.imitator.expr;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.regex.Matcher;

/**
 * Created by vsl on 13.05.2016.
 */
@Accessors(chain = true)
@Data
public class Attribute {
    private String name;
    private Object value;
    private Object object;

    private Matcher nameMatcher;
    private Matcher valueMatcher;
}
