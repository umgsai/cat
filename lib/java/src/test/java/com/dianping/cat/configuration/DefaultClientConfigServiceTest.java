package com.dianping.cat.configuration;

import com.dianping.cat.configuration.property.entity.PropertyConfig;
import org.junit.Assert;
import org.junit.Test;

public class DefaultClientConfigServiceTest {
    @Test
    public void parseRouterConfig() {
        PropertyConfig config = DefaultClientConfigService.parseConfig(
                "{startTransactionTypes=Cache.;Squirrel., matchTransactionTypes=SQL, block=false, routers=127.0.0.1:2280;, sample=1.0}");

        Assert.assertEquals("127.0.0.1:2280;", config.findProperty("routers").getValue());
        Assert.assertEquals("1.0", config.findProperty("sample").getValue());
        Assert.assertEquals("false", config.findProperty("block").getValue());
        Assert.assertEquals("Cache.;Squirrel.", config.findProperty("startTransactionTypes").getValue());
        Assert.assertEquals("SQL", config.findProperty("matchTransactionTypes").getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectNonRouterConfigContent() {
        DefaultClientConfigService.parseConfig("127.0.0.1:2280;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectInvalidRouterConfigProperty() {
        DefaultClientConfigService.parseConfig("{routers}");
    }
}
