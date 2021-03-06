/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hellojavaer.ddal.ddr.shard.simple;

import org.hellojavaer.ddal.ddr.expression.el.function.ELFunctionManager;
import org.hellojavaer.ddal.ddr.expression.format.FormatExpression;
import org.hellojavaer.ddal.ddr.expression.format.FormatExpressionContext;
import org.hellojavaer.ddal.ddr.expression.format.simple.SimpleFormatExpressionContext;
import org.hellojavaer.ddal.ddr.expression.format.simple.SimpleFormatExpressionParser;
import org.hellojavaer.ddal.ddr.shard.ShardRouteRule;
import org.hellojavaer.ddal.ddr.shard.ShardRouteRuleContext;
import org.hellojavaer.ddal.ddr.shard.exception.ExpressionValueNotFoundException;
import org.hellojavaer.ddal.ddr.utils.DDRToStringBuilder;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author <a href="mailto:hellojavaer@gmail.com">Kaiming Zou</a>,created on 15/11/2016.
 */
public class SimpleShardRouteRule implements ShardRouteRule, Serializable {

    private String           scRoute;
    private String           scFormat;
    private String           tbRoute;
    private String           tbFormat;

    private Expression       scRouteExpression;
    private Expression       tbRouteExpression;

    private FormatExpression scFormatExpression;
    private FormatExpression tbFormatExpression;

    public String getScRoute() {
        return scRoute;
    }

    private String filter(String string) {
        if (string != null) {
            string = string.trim();
            if (string.length() == 0) {
                string = null;
            }
        }
        return string;
    }

    public void setScRoute(String scRoute) {
        scRoute = filter(scRoute);
        this.scRoute = scRoute;
        if (scRoute != null) {
            ExpressionParser parser = new SpelExpressionParser();
            this.scRouteExpression = parser.parseExpression(scRoute, PARSER_CONTEXT);
        }
    }

    public String getScFormat() {
        return scFormat;
    }

    public void setScFormat(String scFormat) {
        scFormat = filter(scFormat);
        this.scFormat = scFormat;
        if (scFormat != null) {
            SimpleFormatExpressionParser parser = new SimpleFormatExpressionParser();
            scFormatExpression = parser.parse(scFormat);
        }
    }

    public String getTbRoute() {
        return tbRoute;
    }

    public void setTbRoute(String tbRoute) {
        tbRoute = filter(tbRoute);
        this.tbRoute = tbRoute;
        if (tbRoute != null) {
            ExpressionParser parser = new SpelExpressionParser();

            this.tbRouteExpression = parser.parseExpression(tbRoute, PARSER_CONTEXT);
        }
    }

    public String getTbFormat() {
        return tbFormat;
    }

    public void setTbFormat(String tbFormat) {
        tbFormat = filter(tbFormat);
        this.tbFormat = tbFormat;
        if (tbFormat != null) {
            SimpleFormatExpressionParser parser = new SimpleFormatExpressionParser();
            tbFormatExpression = parser.parse(tbFormat);
        }
    }

    @Override
    public String parseScName(ShardRouteRuleContext context) {
        Object tbRoute = parseScRoute(context);
        return parseScFormat(context, tbRoute);
    }

    @Override
    public String parseTbName(ShardRouteRuleContext context) {
        Object tbRoute = parseTbRoute(context);
        return parseTbFormat(context, tbRoute);
    }

    protected Object parseScRoute(ShardRouteRuleContext context) {
        if (scRouteExpression == null) {
            return context.getSdValue();
        } else {
            EvaluationContext elContext = buildEvaluationContext(scRoute);
            elContext.setVariable("scName", context.getScName());
            elContext.setVariable("tbName", context.getTbName());
            elContext.setVariable("sdValue", context.getSdValue());
            String $0 = scRouteExpression.getValue(elContext, String.class);
            return $0;
        }
    }

    protected String parseScFormat(ShardRouteRuleContext context, Object scRoute) {
        if (scFormatExpression == null) {
            return context.getScName();
        } else {
            FormatExpressionContext efContext = new SimpleFormatExpressionContext();
            efContext.setVariable("scName", context.getScName());
            efContext.setVariable("tbName", context.getTbName());
            efContext.setVariable("sdValue", context.getSdValue());
            efContext.setVariable("scRoute", scRoute);
            return scFormatExpression.getValue(efContext);
        }
    }

    protected Object parseTbRoute(ShardRouteRuleContext context) {
        if (tbRouteExpression == null) {
            return context.getSdValue();
        } else {
            EvaluationContext elContext = buildEvaluationContext(tbRoute);
            elContext.setVariable("scName", context.getScName());
            elContext.setVariable("tbName", context.getTbName());
            elContext.setVariable("sdValue", context.getSdValue());
            String $0 = tbRouteExpression.getValue(elContext, String.class);
            return $0;
        }
    }

    protected String parseTbFormat(ShardRouteRuleContext context, Object tbRoute) {
        if (tbFormatExpression == null) {
            return context.getTbName();
        } else {
            FormatExpressionContext feContext = new SimpleFormatExpressionContext();
            feContext.setVariable("scName", context.getScName());
            feContext.setVariable("tbName", context.getTbName());
            feContext.setVariable("sdValue", context.getSdValue());
            feContext.setVariable("tbRoute", tbRoute);
            return tbFormatExpression.getValue(feContext);
        }
    }

    @Override
    public String toString() {
        return new DDRToStringBuilder()//
        .append("scRoute", scRoute)//
        .append("scFormat", scFormat)//
        .append("tbRoute", tbRoute)//
        .append("tbFormat", tbFormat)//
        .toString();
    }

    private static final Set<String> RESERVED_WORDS = new HashSet<String>();

    static {
        RESERVED_WORDS.add("db");
        RESERVED_WORDS.add("dbName");
        RESERVED_WORDS.add("dbValue");
        RESERVED_WORDS.add("dbRoute");
        RESERVED_WORDS.add("dbFormat");

        RESERVED_WORDS.add("sc");
        RESERVED_WORDS.add("scName");
        RESERVED_WORDS.add("scValue");
        RESERVED_WORDS.add("scRoute");
        RESERVED_WORDS.add("scFormat");

        RESERVED_WORDS.add("tb");
        RESERVED_WORDS.add("tbName");
        RESERVED_WORDS.add("tbValue");
        RESERVED_WORDS.add("tbRoute");
        RESERVED_WORDS.add("tbFormat");

        RESERVED_WORDS.add("sd");
        RESERVED_WORDS.add("sdName");
        RESERVED_WORDS.add("sdKey");
        RESERVED_WORDS.add("sdValue");
        RESERVED_WORDS.add("sdRoute");
        RESERVED_WORDS.add("sdFormat");

        RESERVED_WORDS.add("col");
        RESERVED_WORDS.add("colName");
        RESERVED_WORDS.add("colValue");
        RESERVED_WORDS.add("colRoute");
        RESERVED_WORDS.add("colFormat");
    }

    private static boolean isReservedWords(String str) {
        if (str == null) {
            return false;
        } else {
            return RESERVED_WORDS.contains(str);
        }
    }

    /**
     * load order
     * 1.reserved words
     * 2.function
     * 3.user-define var
     */
    private static EvaluationContext buildEvaluationContext(final String expression) {
        StandardEvaluationContext context = new StandardEvaluationContext() {

            @Override
            public Object lookupVariable(String name) {
                Object val = null;
                if (isReservedWords(name)) {
                    val = super.lookupVariable(name);
                    if (val == null) {
                        throw new ExpressionValueNotFoundException("Value of '" + name
                                                                   + "' is not found when parsing expression '"
                                                                   + expression + "'");
                    }
                } else {
                    val = ELFunctionManager.getRegisteredFunction(name);
                    // TODO: user-define var
                }
                return val;
            }
        };
        return context;
    }

    private static ParserContext PARSER_CONTEXT = new ParserContext() {

                                                    public boolean isTemplate() {
                                                        return true;
                                                    }

                                                    public String getExpressionPrefix() {
                                                        return "{";
                                                    }

                                                    public String getExpressionSuffix() {
                                                        return "}";
                                                    }
                                                };
}
