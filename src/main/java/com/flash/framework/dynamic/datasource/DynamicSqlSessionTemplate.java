package com.flash.framework.dynamic.datasource;

import com.flash.framework.dynamic.datasource.annotation.Ds;
import com.flash.framework.dynamic.datasource.annotation.Master;
import com.google.common.collect.Maps;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.*;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.*;
import static org.springframework.util.Assert.notNull;

/**
 * Thread safe, Spring managed, {@code SqlSession} that works with Spring
 * transaction management to ensure that that the actual SqlSession used is the
 * one associated with the current Spring transaction. In addition, it manages
 * the session life-cycle, including closing, committing or rolling back the
 * session as necessary based on the Spring transaction configuration.
 * <p>
 * The template needs a SqlSessionFactory to create SqlSessions, passed as a
 * constructor argument. It also can be constructed indicating the executor type
 * to be used, if not, the default executor type, defined in the session factory
 * will be used.
 * <p>
 * This template converts MyBatis PersistenceExceptions into unchecked
 * DataAccessExceptions, using, by default, a {@code MyBatisExceptionTranslator}.
 * <p>
 * Because SqlSessionTemplate is thread safe, a single instance can be shared
 * by all DAOs; there should also be a small memory savings by doing this. This
 * pattern can be used in Spring configuration files as follows:
 *
 * <pre class="code">
 * {@code
 * <bean id="sqlSessionTemplate" class="org.mybatis.spring.SqlSessionTemplate">
 *   <constructor-arg ref="sqlSessionFactory" />
 * </bean>
 * }
 * </pre>
 *
 * @author Putthiphong Boonphong
 * @author Hunter Presnall
 * @author Eduardo Macarron
 * @see SqlSessionFactory
 * @see MyBatisExceptionTranslator
 */
public class DynamicSqlSessionTemplate extends SqlSessionTemplate {

    @Autowired
    private DynamicGroupDataSource dynamicGroupDataSource;

    private static Map<Class<?>, Ds> callDaoCache = Maps.newConcurrentMap();

    private static Map<Class<?>, Set<String>> masterMethodCache = Maps.newHashMap();

    private final SqlSessionFactory sqlSessionFactory;

    private final ExecutorType executorType;

    private final SqlSession sqlSessionProxy;

    private final PersistenceExceptionTranslator exceptionTranslator;

    /**
     * Constructs a Spring managed SqlSession with the {@code SqlSessionFactory}
     * provided as an argument.
     *
     * @param sqlSessionFactory a factory of SqlSession
     */
    public DynamicSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
    }

    /**
     * Constructs a Spring managed SqlSession with the {@code SqlSessionFactory}
     * provided as an argument and the given {@code ExecutorType}
     * {@code ExecutorType} cannot be changed once the {@code SqlSessionTemplate}
     * is constructed.
     *
     * @param sqlSessionFactory a factory of SqlSession
     * @param executorType      an executor type on session
     */
    public DynamicSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
        this(sqlSessionFactory, executorType,
                new MyBatisExceptionTranslator(
                        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true));
    }

    /**
     * Constructs a Spring managed {@code SqlSession} with the given
     * {@code SqlSessionFactory} and {@code ExecutorType}.
     * A custom {@code SQLExceptionTranslator} can be provided as an
     * argument so any {@code PersistenceException} thrown by MyBatis
     * can be custom translated to a {@code RuntimeException}
     * The {@code SQLExceptionTranslator} can also be null and thus no
     * exception translation will be done and MyBatis exceptions will be
     * thrown
     *
     * @param sqlSessionFactory   a factory of SqlSession
     * @param executorType        an executor type on session
     * @param exceptionTranslator a translator of exception
     */
    public DynamicSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
                                     PersistenceExceptionTranslator exceptionTranslator) {
        super(sqlSessionFactory, executorType, exceptionTranslator);
        notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
        notNull(executorType, "Property 'executorType' is required");

        this.sqlSessionFactory = sqlSessionFactory;
        this.executorType = executorType;
        this.exceptionTranslator = exceptionTranslator;
        this.sqlSessionProxy = (SqlSession) newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[]{SqlSession.class},
                new DynamicSqlSessionTemplate.SqlSessionInterceptor());
    }

    @Override
    public SqlSessionFactory getSqlSessionFactory() {
        return this.sqlSessionFactory;
    }

    @Override
    public ExecutorType getExecutorType() {
        return this.executorType;
    }

    @Override
    public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
        return this.exceptionTranslator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T selectOne(String statement) {
        return this.sqlSessionProxy.selectOne(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return this.sqlSessionProxy.selectOne(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, mapKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        return this.sqlSessionProxy.selectCursor(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return this.sqlSessionProxy.selectCursor(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectCursor(statement, parameter, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement) {
        return this.sqlSessionProxy.selectList(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.sqlSessionProxy.selectList(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectList(statement, parameter, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(String statement, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(String statement) {
        return this.sqlSessionProxy.insert(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(String statement, Object parameter) {
        return this.sqlSessionProxy.insert(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(String statement) {
        return this.sqlSessionProxy.update(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(String statement, Object parameter) {
        return this.sqlSessionProxy.update(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String statement) {
        return this.sqlSessionProxy.delete(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String statement, Object parameter) {
        return this.sqlSessionProxy.delete(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit(boolean force) {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback(boolean force) {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCache() {
        this.sqlSessionProxy.clearCache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return this.sqlSessionFactory.getConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() {
        return this.sqlSessionProxy.getConnection();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0.2
     */
    @Override
    public List<BatchResult> flushStatements() {
        return this.sqlSessionProxy.flushStatements();
    }

    /**
     * Allow gently dispose bean:
     * <pre>
     * {@code
     *
     * <bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
     *  <constructor-arg index="0" ref="sqlSessionFactory" />
     * </bean>
     * }
     * </pre>
     * <p>
     * The implementation of {@link DisposableBean} forces spring context to use {@link DisposableBean#destroy()} method instead of {@link SqlSessionTemplate#close()} to shutdown gently.
     *
     * @see SqlSessionTemplate#close()
     * @see "org.springframework.beans.factory.support.DisposableBeanAdapter#inferDestroyMethodIfNecessary(Object, RootBeanDefinition)"
     * @see "org.springframework.beans.factory.support.DisposableBeanAdapter#CLOSE_METHOD_NAME"
     */
    @Override
    public void destroy() {
        //This method forces spring disposer to avoid call of SqlSessionTemplate.close() which gives UnsupportedOperationException
    }

    /**
     * Proxy needed to route MyBatis method calls to the proper SqlSession got
     * from Spring's Transaction Manager
     * It also unwraps exceptions thrown by {@code Method#invoke(Object, Object...)} to
     * pass a {@code PersistenceException} to the {@code PersistenceExceptionTranslator}.
     */
    private class SqlSessionInterceptor implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Ds ds = parseCallObject(args[0].toString());
            if (null != ds && !StringUtils.isEmpty(ds.name())) {
                if (methodName.startsWith("select")) {
                    if (useMaster(args[0].toString())) {
                        dynamicGroupDataSource.selectDataSource(ds.name(), DsType.Wirte);
                    } else {
                        dynamicGroupDataSource.selectDataSource(ds.name(), DsType.Read);
                    }
                } else {
                    dynamicGroupDataSource.selectDataSource(ds.name(), DsType.Wirte);
                }
            } else {
                dynamicGroupDataSource.selectDataSource(null, DsType.Wirte);
            }
            SqlSession sqlSession = getSqlSession(
                    DynamicSqlSessionTemplate.this.sqlSessionFactory,
                    DynamicSqlSessionTemplate.this.executorType,
                    DynamicSqlSessionTemplate.this.exceptionTranslator);
            try {
                Object result = method.invoke(sqlSession, args);
                if (!isSqlSessionTransactional(sqlSession, DynamicSqlSessionTemplate.this.sqlSessionFactory)) {
                    // force commit even on non-dirty sessions because some databases require
                    // a commit/rollback before calling close()
                    sqlSession.commit(true);
                }
                return result;
            } catch (Throwable t) {
                Throwable unwrapped = unwrapThrowable(t);
                if (DynamicSqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                    // release the connection to avoid a deadlock if the translator is no loaded. See issue #22
                    closeSqlSession(sqlSession, DynamicSqlSessionTemplate.this.sqlSessionFactory);
                    sqlSession = null;
                    Throwable translated = DynamicSqlSessionTemplate.this.exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                throw unwrapped;
            } finally {
                DynamicDataSourceContextHolder.clear();
                if (sqlSession != null) {
                    closeSqlSession(sqlSession, DynamicSqlSessionTemplate.this.sqlSessionFactory);
                }
            }
        }
    }

    /**
     * 获取调用类
     *
     * @param callMethod
     * @return
     * @throws Throwable
     */
    private Class<?> getCallClass(String callMethod) throws Throwable {
        // 解析调用对象
        String callStr = callMethod.substring(0, callMethod.lastIndexOf("."));
        return Class.forName(callStr);
    }

    /**
     * 获取调用方法
     *
     * @param callMethod
     * @return
     */
    private String getCallMethod(String callMethod) {
        return callMethod.substring(callMethod.lastIndexOf(".") + 1);
    }

    /**
     * 解析@Master注解
     *
     * @param callMethod
     * @return
     */
    private boolean useMaster(String callMethod) throws Throwable {
        Class<?> clazz = getCallClass(callMethod);
        if (!masterMethodCache.containsKey(clazz)) {
            Method[] methods = clazz.getMethods();
            if (null != methods && methods.length > 0) {
                masterMethodCache.put(clazz, Arrays.asList(methods)
                        .stream()
                        .filter(method -> null != method.getAnnotation(Master.class))
                        .map(method -> method.getName())
                        .collect(Collectors.toSet()));
            }
        }
        return CollectionUtils.isEmpty(masterMethodCache.get(clazz)) ? false : masterMethodCache.get(clazz).contains(getCallMethod(callMethod));
    }

    /**
     * 解析调用对象与调用函数
     *
     * @param callMethod
     * @return
     * @throws Throwable
     */
    private Ds parseCallObject(String callMethod) throws Throwable {
        Class<?> clazz = getCallClass(callMethod);
        if (!callDaoCache.containsKey(clazz)) {
            Ds ds = clazz.getAnnotation(Ds.class);
            if (null != ds) {
                callDaoCache.put(clazz, ds);
            }
        }
        return callDaoCache.get(clazz);
    }
}
