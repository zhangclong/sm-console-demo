package com.uh.tools.utils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.Map;


public class VelocityTemplateOperator {

    private VelocityEngine velocityEngine;

    public VelocityTemplateOperator() {
        velocityEngine = new VelocityEngine();
        velocityEngine.init();
    }

    public String evaluatedTemplate(String template, Map<String, Object> variable) {
        variable.put("D", "$"); //使用 ${D} 去替换 $ 符号

        try(StringWriter stringWriter = new StringWriter()) {
            VelocityContext context = new VelocityContext();
            for (String key : variable.keySet()) {
                context.put(key, variable.get(key));
            }
            velocityEngine.evaluate(context, stringWriter, "evaluateTemplate", template);

            return stringWriter.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
