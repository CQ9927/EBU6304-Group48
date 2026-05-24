package com.ebu6304.group48.repository;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates repository instances backed by a temporary directory,
 * avoiding dependency on a real ServletContext in unit tests.
 */
class RepositoryTestBase {

    static Path tempDataDir;

    static ServletContext mockContext(Path dataDir) {
        return new ServletContext() {
            @Override public String getInitParameter(String name) {
                if ("dataDirectory".equals(name)) return dataDir.toString();
                return null;
            }
            @Override public String getContextPath() { return "/ta-recruitment"; }
            @Override public String getRealPath(String path) { return null; }
            @Override public ServletContext getContext(String s) { return null; }
            @Override public int getMajorVersion() { return 4; }
            @Override public int getMinorVersion() { return 0; }
            @Override public int getEffectiveMajorVersion() { return 4; }
            @Override public int getEffectiveMinorVersion() { return 0; }
            @Override public String getMimeType(String s) { return null; }
            @Override public java.util.Set<String> getResourcePaths(String s) { return null; }
            @Override public java.net.URL getResource(String s) { return null; }
            @Override public java.io.InputStream getResourceAsStream(String s) { return null; }
            @Override public javax.servlet.RequestDispatcher getRequestDispatcher(String s) { return null; }
            @Override public javax.servlet.RequestDispatcher getNamedDispatcher(String s) { return null; }
            @Override public void log(String s) { }
            @Override public void log(String s, Throwable throwable) { }
            @Override public void log(Exception e, String msg) { }
            @Override public String getServletContextName() { return "test"; }
            @Override public String getServerInfo() { return "test/4.0.1"; }
            @Override public javax.servlet.Servlet getServlet(String s) { return null; }
            @Override public java.util.Enumeration<javax.servlet.Servlet> getServlets() { return null; }
            @Override public java.util.Enumeration<String> getServletNames() { return null; }
            @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String s, String s1) { return null; }
            @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String s, javax.servlet.Filter filter) { return null; }
            @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String s, Class<? extends javax.servlet.Filter> aClass) { return null; }
            @Override public <T extends javax.servlet.Filter> T createFilter(Class<T> aClass) { return null; }
            @Override public javax.servlet.FilterRegistration getFilterRegistration(String s) { return null; }
            @Override public java.util.Map<String, ? extends javax.servlet.FilterRegistration> getFilterRegistrations() { return null; }
            @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String s, String s1) { return null; }
            @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String s, javax.servlet.Servlet servlet) { return null; }
            @Override public javax.servlet.ServletRegistration.Dynamic addJspFile(String jspName, String jspFile) { return null; }
            @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String s, Class<? extends javax.servlet.Servlet> aClass) { return null; }
            @Override public <T extends javax.servlet.Servlet> T createServlet(Class<T> aClass) { return null; }
            @Override public javax.servlet.ServletRegistration getServletRegistration(String s) { return null; }
            @Override public java.util.Map<String, ? extends javax.servlet.ServletRegistration> getServletRegistrations() { return null; }
            @Override public java.util.Set<javax.servlet.SessionTrackingMode> getDefaultSessionTrackingModes() { return null; }
            @Override public java.util.Set<javax.servlet.SessionTrackingMode> getEffectiveSessionTrackingModes() { return null; }
            @Override public void setSessionTrackingModes(java.util.Set<javax.servlet.SessionTrackingMode> set) { }
            @Override public void addListener(String s) { }
            @Override public <T extends java.util.EventListener> void addListener(T t) { }
            @Override public void addListener(Class<? extends java.util.EventListener> aClass) { }
            @Override public <T extends java.util.EventListener> T createListener(Class<T> aClass) { return null; }
            @Override public javax.servlet.descriptor.JspConfigDescriptor getJspConfigDescriptor() { return null; }
            @Override public ClassLoader getClassLoader() { return null; }
            @Override public void declareRoles(String... strings) { }
            @Override public String getVirtualServerName() { return null; }
            @Override public int getSessionTimeout() { return 30; }
            @Override public void setSessionTimeout(int i) { }
            @Override public boolean setInitParameter(String name, String value) { return false; }
            @Override public java.util.Enumeration<String> getInitParameterNames() { return java.util.Collections.emptyEnumeration(); }
            @Override public String getRequestCharacterEncoding() { return "UTF-8"; }
            @Override public void setRequestCharacterEncoding(String s) { }
            @Override public String getResponseCharacterEncoding() { return "UTF-8"; }
            @Override public void setResponseCharacterEncoding(String s) { }
            @Override public javax.servlet.SessionCookieConfig getSessionCookieConfig() { return null; }
            @Override public void setAttribute(String s, Object o) { }
            @Override public Object getAttribute(String s) { return null; }
            @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
            @Override public void removeAttribute(String s) { }
        };
    }

    static void setupTempDir() throws IOException {
        tempDataDir = Files.createTempDirectory("ebu6304-test-");
    }

    static void cleanupTempDir() throws IOException {
        if (tempDataDir != null && Files.exists(tempDataDir)) {
            Files.walk(tempDataDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> { try { Files.delete(p); } catch (IOException e) { } });
        }
    }
}
