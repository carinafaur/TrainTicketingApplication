package service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {

    private final boolean enabled;
    private final String host;
    private final int port;
    private final boolean starttls;
    private final boolean auth;
    private final String username;
    private final String password;
    private final String fromAddress;

    private EmailConfig(boolean enabled, String host, int port, boolean starttls, boolean auth,
                        String username, String password, String fromAddress) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.starttls = starttls;
        this.auth = auth;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
    }

    public boolean isEnabled() { return enabled; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public boolean isStarttls() { return starttls; }
    public boolean isAuth() { return auth; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFromAddress() { return fromAddress; }

    public static EmailConfig loadFromClasspath(String resourceName) {
        Properties props = new Properties();
        try (InputStream in = EmailConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("EmailConfig: " + resourceName + " not found on classpath.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("EmailConfig: failed to read " + resourceName, e);
        }

        boolean enabled  = readBool(props, "email.enabled",      false);
        String host      = props.getProperty("email.smtp.host",  "").trim();
        int port         = readInt (props, "email.smtp.port",    587);
        boolean starttls = readBool(props, "email.smtp.starttls", true);
        boolean auth     = readBool(props, "email.smtp.auth",    true);
        String user      = props.getProperty("email.username",   "").trim();
        String pass      = props.getProperty("email.password",   "").trim();
        String from      = props.getProperty("email.from",       "noreply@localhost").trim();

        if (enabled && (host.isEmpty() || user.isEmpty() || pass.isEmpty())) {
            throw new IllegalStateException(
                    "EmailConfig: enabled=true but host/username/password are empty in " + resourceName + ".");
        }

        return new EmailConfig(enabled, host, port, starttls, auth, user, pass, from);
    }

    private static boolean readBool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int readInt(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}
