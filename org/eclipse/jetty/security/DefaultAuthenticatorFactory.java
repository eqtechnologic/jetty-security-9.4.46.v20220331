//
//  ========================================================================
//  Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.security;

import jakarta.servlet.ServletContext;

import org.eclipse.jetty.security.Authenticator.AuthConfiguration;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.ClientCertAuthenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.security.authentication.SpnegoAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.security.Constraint;

/**
 * The Default Authenticator Factory.
 * Uses the {@link AuthConfiguration#getAuthMethod()} to select an {@link Authenticator} from: <ul>
 * <li>{@link org.eclipse.jetty.security.authentication.BasicAuthenticator}</li>
 * <li>{@link org.eclipse.jetty.security.authentication.DigestAuthenticator}</li>
 * <li>{@link org.eclipse.jetty.security.authentication.FormAuthenticator}</li>
 * <li>{@link org.eclipse.jetty.security.authentication.ClientCertAuthenticator}</li>
 * </ul>
 * All authenticators derived from {@link org.eclipse.jetty.security.authentication.LoginAuthenticator} are
 * wrapped with a {@link org.eclipse.jetty.security.authentication.DeferredAuthentication}
 * instance, which is used if authentication is not mandatory.
 *
 * The Authentications from the {@link org.eclipse.jetty.security.authentication.FormAuthenticator} are always wrapped in a
 * {@link org.eclipse.jetty.security.authentication.SessionAuthentication}
 * <p>
 * If a {@link LoginService} has not been set on this factory, then
 * the service is selected by searching the {@link Server#getBeans(Class)} results for
 * a service that matches the realm name, else the first LoginService found is used.
 */
public class DefaultAuthenticatorFactory implements Authenticator.Factory
{
    LoginService _loginService;

    @Override
    public Authenticator getAuthenticator(Server server, ServletContext context, AuthConfiguration configuration, IdentityService identityService, LoginService loginService)
    {
        String auth = configuration.getAuthMethod();
        Authenticator authenticator = null;

        if (Constraint.__BASIC_AUTH.equalsIgnoreCase(auth))
            authenticator = new BasicAuthenticator();
        else if (Constraint.__DIGEST_AUTH.equalsIgnoreCase(auth))
            authenticator = new DigestAuthenticator();
        else if (Constraint.__FORM_AUTH.equalsIgnoreCase(auth))
            authenticator = new FormAuthenticator();
        else if (Constraint.__SPNEGO_AUTH.equalsIgnoreCase(auth))
            authenticator = new SpnegoAuthenticator();
        else if (Constraint.__NEGOTIATE_AUTH.equalsIgnoreCase(auth)) // see Bug #377076
            authenticator = new SpnegoAuthenticator(Constraint.__NEGOTIATE_AUTH);
        if (Constraint.__CERT_AUTH.equalsIgnoreCase(auth) || Constraint.__CERT_AUTH2.equalsIgnoreCase(auth))
            authenticator = new ClientCertAuthenticator();

        return authenticator;
    }

    /**
     * @return the loginService
     */
    public LoginService getLoginService()
    {
        return _loginService;
    }

    /**
     * @param loginService the loginService to set
     */
    public void setLoginService(LoginService loginService)
    {
        _loginService = loginService;
    }
}
