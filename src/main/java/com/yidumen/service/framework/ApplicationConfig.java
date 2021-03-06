package com.yidumen.service.framework;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author 蔡迪旻
 */
@javax.ws.rs.ApplicationPath("/")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.yidumen.service.framework.RangeHeader.class);
        resources.add(com.yidumen.service.framework.RangeHeaderConverter.class);
        resources.add(com.yidumen.service.VideoService.class);
    }
    
}
