package xyz.vsl.wsimitator.imitator;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Created by vsl on 13.05.2016.
 */
public class RequestProcessorsFactory {
    private static Properties implementations = new Properties();
    private Logger logger = Logger.getLogger(getClass());

    public RequestProcessor build(String name, Element element) {
        String className = implementations.getProperty(name);
        if (logger.isTraceEnabled())
            logger.trace("Element <" + name + "> is " + className);
        if (className == null)
            return null;
        try {
            Class<?> klazz = Class.forName(className);
            if (!RequestProcessor.class.isAssignableFrom(klazz))
                throw new IllegalStateException("Class "+className+" is not a "+RequestProcessor.class.getName());
            RequestProcessor rp;
            Constructor<?> constructor;
            Object[] args;
            try {
                constructor = klazz.getConstructor(Element.class);
                args = new Object[] {element};
            } catch (NoSuchMethodException e2) {
                try {
                    constructor = klazz.getConstructor();
                    args = new Object[] {};
                } catch (NoSuchMethodException e3) {
                    throw new IllegalStateException("Can't instantiate "+klazz.getName());
                }
            }
            rp = (RequestProcessor) constructor.newInstance(args);
            return rp;
        } catch (ClassNotFoundException e) {
            logger.error("Element <"+name+">", e);
        } catch (InstantiationException e) {
            logger.error("Element <" + name + ">", e);
        } catch (IllegalAccessException e) {
            logger.error("Element <" + name + ">", e);
        } catch (InvocationTargetException e) {
            logger.error("Element <"+name+">", e);
        }
        return null;
    }

    static {
        InputStream is = RequestProcessorsFactory.class.getResourceAsStream("/wsimitator.properties");
        if (is != null) {
            try {
                implementations.load(is);
                is.close();
            } catch (IOException e) {
                e.getMessage(); // do nothing
            }
        } else {
            System.err.println("wsimitator.properties not found in classpath!");
        }
    }
}
