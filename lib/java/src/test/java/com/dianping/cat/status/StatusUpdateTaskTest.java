package com.dianping.cat.status;

import com.dianping.cat.Cat;
import com.dianping.cat.log.CatLogger;
import com.dianping.cat.status.model.entity.RuntimeInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class StatusUpdateTaskTest {
    @Test
    public void skipNonNumericExtensionValueInsteadOfWritingCustomInfo() throws Exception {
        Cat.initializeByDomainForce("cat");
        StatusExtension extension = new StatusExtension() {
            @Override
            public String getDescription() {
                return "test extension";
            }

            @Override
            public String getId() {
                return "TestExtension";
            }

            @Override
            public Map<String, String> getProperties() {
                return Collections.singletonMap("status", "UP");
            }
        };
        StatusExtensionRegister register = StatusExtensionRegister.getInstance();
        StatusInfo status = new StatusInfo();

        register.register(extension);
        try {
            StatusUpdateTask task = new StatusUpdateTask();
            Method method = StatusUpdateTask.class.getDeclaredMethod("buildExtenstion", StatusInfo.class);

            method.setAccessible(true);
            method.invoke(task, status);
        } finally {
            register.unregister(extension);
        }

        String xml = status.toString();

        Assert.assertFalse(xml.contains("<customInfo"));
        Assert.assertFalse(xml.contains("<customInfos"));
    }

    @Test
    public void catLoggerSupportsWarn() {
        CatLogger.getInstance().warn("test warning, key={}, value={}.", "sample", "1");
    }

    @Test
    public void buildRuntimeContainsStaticInfo() throws Exception {
        StatusUpdateTask task = new StatusUpdateTask();
        StatusInfo status = new StatusInfo();

        task.buildRuntime(status);

        RuntimeInfo runtime = status.getRuntime();

        Assert.assertNotNull(runtime);
        Assert.assertTrue(runtime.getStartTime() > 0);
        Assert.assertTrue(runtime.getUpTime() >= 0);
        Assert.assertNotNull(runtime.getJavaClasspath());
        Assert.assertEquals(System.getProperty("java.version"), runtime.getJavaVersion());
        Assert.assertEquals(System.getProperty("user.dir"), runtime.getUserDir());
        Assert.assertEquals(System.getProperty("user.name"), runtime.getUserName());
    }
}
