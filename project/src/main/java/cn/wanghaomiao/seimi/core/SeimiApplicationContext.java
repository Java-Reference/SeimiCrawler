package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.annotation.Interceptor;
import cn.wanghaomiao.seimi.annotation.Queue;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 自定义扩展实现ApplicationContext
 */
public class SeimiApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

    private final AnnotatedBeanDefinitionReader reader;
    private final SeimiScanner scanner;

    protected static Set<Class<? extends BaseSeimiCrawler>> crawlers = new HashSet<>();
    protected static Set<Class<? extends SeimiQueue>> hasUsedQuene = new HashSet<>();
    protected static List<SeimiInterceptor> interceptors = new LinkedList<>();
    protected static Map<String,CrawlerModel> crawlerModelContext = new HashMap<>();
    protected static ExecutorService workersPool;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public SeimiApplicationContext() {
        this.reader = new AnnotatedBeanDefinitionReader(this);
        this.scanner = new SeimiScanner(this);
        this.scanner.addIncludeFilter(new AnnotationTypeFilter(Crawler.class,false));
        this.scanner.addIncludeFilter(new AnnotationTypeFilter(Queue.class,false));
        this.scanner.addIncludeFilter(new AnnotationTypeFilter(Interceptor.class,false));
    }

    public SeimiApplicationContext(Class<?>... annotatedClasses) {
        this();
        register(annotatedClasses);
        refresh();
    }

    public SeimiApplicationContext(String... basePackages) {
        this();
        scan(basePackages);
        refresh();
    }

    @Override
    public void setEnvironment(ConfigurableEnvironment environment) {
        super.setEnvironment(environment);
        this.reader.setEnvironment(environment);
        this.scanner.setEnvironment(environment);
    }

    public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
        this.reader.setBeanNameGenerator(beanNameGenerator);
        this.scanner.setBeanNameGenerator(beanNameGenerator);
        getBeanFactory().registerSingleton(
                AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
    }

    public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
        this.reader.setScopeMetadataResolver(scopeMetadataResolver);
        this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
    }


    public void register(Class<?>... annotatedClasses) {
        Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
        this.reader.register(annotatedClasses);
    }

    public void scan(String... basePackages) {
        Assert.notEmpty(basePackages, "At least one base package must be specified");
        this.scanner.scan(basePackages);
    }


    @Override
    protected void prepareRefresh() {
        this.scanner.clearCache();
        super.prepareRefresh();
    }

}

