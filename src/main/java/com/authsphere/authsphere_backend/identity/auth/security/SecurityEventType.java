
package com.authsphere.authsphere_backend.identity.auth.security;

public enum SecurityEventType {

    // Authentication events
    LOGIN_SUCCESS,
    LOGIN_FAILED,

    // MFA events
    MFA_SUCCESS,
    MFA_FAILED,

    // Account security events
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,

    // Administrative audit events
    USER_SUSPENDED,
    USER_ACTIVATED,
    USER_DELETED
}
