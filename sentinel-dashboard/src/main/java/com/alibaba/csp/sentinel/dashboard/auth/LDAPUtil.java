package com.alibaba.csp.sentinel.dashboard.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.directory.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * AD Util
 */
public class LDAPUtil {
    private static final Logger logger = LoggerFactory.getLogger(LDAPUtil.class);

    /**
     * 校验AD密码
     *
     * @param adurl ldap://ip:3268/dc=xxx,dc=local
     * @param adusr user@xxxx.local
     * @param adpwd xxx
     * @return
     */
    public static boolean verifyPassword(String adurl, String adusr, String adpwd) {
        boolean verifyResult = false;
        DirContext dirContext = null;
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, adurl);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, adusr);
            env.put(Context.SECURITY_CREDENTIALS, adpwd);
            env.put("com.sun.jndi.ldap.connect.timeout", "1000");
            dirContext = new InitialDirContext(env);
            verifyResult = true;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (dirContext != null) {
                try {
                    dirContext.close();
                } catch (Exception e) {
                }
            }
        }
        return verifyResult;
    }
}
