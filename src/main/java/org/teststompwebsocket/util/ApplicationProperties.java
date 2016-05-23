package org.teststompwebsocket.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.teststompwebsocket.Application;

/**
 * Application settings component.
 * 
 * @author Sergey Stotskiy
 *
 */
@Component("applicationProperties")
public class ApplicationProperties {

    private static boolean testServer;
    private static boolean localProfile;
    private static boolean devProfile;
    private static String activeProfiles;
    private static String buildVersion;
    private static String buildDate;

    private static int plusexpirationseconds;
    private static boolean killSession;

    private ApplicationProperties() {
    }

    @Value("${privatesettings.testserver}")
    private void setTestServer(boolean testServer) {
        ApplicationProperties.testServer = testServer;
    }

    @Value("${spring.profiles:}")
    private void setActiveProfiles(String activeProfiles) {
        ApplicationProperties.activeProfiles = activeProfiles;
        if (StringUtils.isEmpty(activeProfiles)
            || activeProfiles.equals(Application.PROFILE_LOCALDEBUG)) {
            ApplicationProperties.localProfile = true;
        } else if (activeProfiles != null
            && activeProfiles.contains(Application.PROFILE_DEV)) {
            ApplicationProperties.devProfile = true;
        }
    }

    @Value("${privatesettings.build.version}")
    private void setBuildVersion(String buildVersion) {
        ApplicationProperties.buildVersion = buildVersion;
    }

    @Value("${privatesettings.build.date}")
    private void setBuildDate(String buildDate) {
        ZonedDateTime zdt = ZonedDateTime.parse(buildDate,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DateUtil.DATE_FULL_FORMAT);
        ApplicationProperties.buildDate = dtf
            .format(zdt.plusSeconds(zdt.getOffset().getTotalSeconds()));
    }

    public static boolean getKillSession() {
        return ApplicationProperties.killSession;
    }

    @Value("${privatesettings.expirationtime.killsession}")
    public void setKillSession(int killSession) {
        if (killSession == 0) {
            ApplicationProperties.killSession = false;
        }
        ApplicationProperties.killSession = true;
    }

    @Value("${privatesettings.expirationtime.plusexpirationseconds}")
    private void setPlusSeconds(int plusseconds) {
        ApplicationProperties.plusexpirationseconds = plusseconds;
    }

    public static boolean isTestserver() {
        return testServer;
    }

    public static boolean isLocalProfile() {
        return localProfile;
    }

    public static void setLocalProfile(boolean localProfile) {
        ApplicationProperties.localProfile = localProfile;
    }

    public static boolean isDevProfile() {
        return devProfile;
    }

    public static void setDevProfile(boolean devProfile) {
        ApplicationProperties.devProfile = devProfile;
    }

    public static String getActiveProfiles() {
        return activeProfiles;
    }

    public static String getBuildVersion() {
        return buildVersion;
    }

    public static String getBuildDate() {
        return buildDate;
    }

    public static int getPlusseconds() {
        return plusexpirationseconds;
    }
}
