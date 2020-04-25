package goodgid.odot.interceptor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.Lists;

import goodgid.odot.util.ObjectEncryptionUtil;

/**
 * Query Log Interceptor
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class })
})
@Component
public class SqlQueryLogInterceptor implements Interceptor {

    public static final Logger LOGGER = LoggerFactory.getLogger("SqlQueryLogInterceptor");

    public static final int DEFAULT_LINE_SIZE = 1024 * 4; // 4KB

    public static final int DEFAULT_SLOW_QUERY_TIMEOUT = 1000; // 1000 ms

    private static final Pattern SQL_PATTERN = Pattern.compile("\\?");

    private static final Pattern MAPPER_FILE_PATTERN = Pattern.compile("(?!.*?\\/(.*?\\.xml).*?)");

    private static final String CRLF = "\n";

    @Autowired
    ObjectEncryptionUtil objectEncryptionUtil;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (!isMappedStatmentExecution(invocation)) {
            return invocation.proceed();
        }

        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameterObject = args[1];

        String mapperName = getMapperName(mappedStatement);

        try {
            Object encryptedObject = objectEncryptionUtil.encryptObject(parameterObject);

            String loggingMessage = mergeQueryStatementWithParameter(mappedStatement, encryptedObject);

            long startTime = System.currentTimeMillis();

            Object result = invocation.proceed();

            printQueryLog(loggingMessage, startTime);

            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    // Query Log needs to be written carefully so that GC does not occur a lot.
    private String mergeQueryStatementWithParameter(MappedStatement mappedStatement, Object parameterObject) {

        BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);

        String sql = removeBreakingWhitespace(boundSql.getSql());

        List<String> parameterList = getParameterList(boundSql.getParameterMappings(), mappedStatement,
                                                      parameterObject, boundSql);

        StringBuffer sb = new StringBuffer(DEFAULT_LINE_SIZE);
        sb.append(CRLF);
        sb.append("## SQL Logging " + CRLF);
        sb.append("[Id] " + mappedStatement.getId() + CRLF);
        sb.append("[Statement] ");

        Matcher matcher = SQL_PATTERN.matcher(sql);

        int parameterIndex = 0;
        while (matcher.find()) {
            matcher.appendReplacement(sb, parameterList.get(parameterIndex));
            parameterIndex++;
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private boolean isMappedStatmentExecution(Invocation invocation) {

        Object[] args = invocation.getArgs();

        return !ObjectUtils.isEmpty(args) && args.length >= 2 && args[0] instanceof MappedStatement;
    }

    private void printQueryLog(String queryStatement, long startTime) {

        long elapsedMillis = System.currentTimeMillis() - startTime;

        boolean isSlowQuery = elapsedMillis > DEFAULT_SLOW_QUERY_TIMEOUT;

        if (isSlowQuery) {
            LOGGER.warn("{}, {}ms Elapsed. Slow Query Detected ! ", queryStatement, elapsedMillis);
        } else {
            LOGGER.info("{}, {}ms Elapsed. ", queryStatement, elapsedMillis);
        }
    }

    private String getMapperName(MappedStatement mappedStatement) {
        String resource = mappedStatement.getResource();

        if (null != resource) {
            Matcher matcher = MAPPER_FILE_PATTERN.matcher(resource);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
        return null;
    }

    protected String removeBreakingWhitespace(String original) {
        StringTokenizer whitespaceStripper = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        while (whitespaceStripper.hasMoreTokens()) {
            builder.append(whitespaceStripper.nextToken());
            builder.append(" ");
        }
        return builder.toString();
    }

    private List<String> getParameterList(List<ParameterMapping> parameterMappings,
                                          MappedStatement mappedStatement, Object parameterObject,
                                          BoundSql boundSql) {
        List<String> result = Lists.newArrayList();

        if (parameterMappings == null) {
            return result;
        }

        MetaObject metaObject = null;

        if (parameterObject != null) {
            metaObject = mappedStatement.getConfiguration().newMetaObject(parameterObject);
        }

        for (ParameterMapping parameterMapping : parameterMappings) {
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                String propertyName = parameterMapping.getProperty();
                PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                if (parameterObject == null) {
                    value = null;
                } else if (mappedStatement.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(
                        parameterObject.getClass())) {
                    value = parameterObject;
                } else if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql
                        .hasAdditionalParameter(prop.getName())) {
                    value = boundSql.getAdditionalParameter(prop.getName());
                    if (value != null) {
                        value = mappedStatement.getConfiguration().newMetaObject(value).getValue(
                                propertyName.substring(prop.getName().length()));
                    }
                } else {
                    value = metaObject == null ? null : metaObject.getValue(propertyName);
                }
                result.add(getBoundParameterString(value, parameterMapping.getProperty()));
            }
        }

        return result;
    }

    private String getBoundParameterString(Object value, String property) {
        StringBuilder stringBuilder = new StringBuilder(100);

        if (null != property && !property.startsWith("__frch_item_")) {
            stringBuilder.append("/*").append(property).append("*/");
        }

        if (null == value) {
            stringBuilder.append("null");
        } else {
            if (value instanceof Date) {
                stringBuilder.append("TIMESTAMP'").append(
                        new Timestamp(((Date) value).getTime()).toString()).append("'");
            } else if (value instanceof Number || value instanceof Boolean) {
                stringBuilder.append(value);
            } else {
                stringBuilder.append("'")
                             .append(value.toString().replace("$", "\\$"))
                             .append("'");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
