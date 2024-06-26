/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.container.jdk.client;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.glassfish.tyrus.spi.UpgradeRequest;

/**
 * Adds getHttpMethod method to @UpgradeRequest. Wraps an upgrade request and
 * delegates all method calls except {@link #getHttpMethod()} to it.
 *
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
abstract class JdkUpgradeRequest extends UpgradeRequest {

	private final UpgradeRequest upgradeRequest;

	/**
	 * Create new {@link org.glassfish.tyrus.container.jdk.client.JdkUpgradeRequest}
	 * wrapping
	 *
	 * @param upgradeRequest wrapped upgrade request.
	 */
	JdkUpgradeRequest(UpgradeRequest upgradeRequest) {
		this.upgradeRequest = upgradeRequest;
	}

	/**
	 * Returns a HTTP method that should be used when composing HTTP request.
	 *
	 * @return a HTTP method.
	 */
	public abstract String getHttpMethod();

	@Override
	public String getHeader(String name) {
		return upgradeRequest.getHeader(name);
	}

	@Override
	public boolean isSecure() {
		return upgradeRequest.isSecure();
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return upgradeRequest.getHeaders();
	}

	@Override
	public Principal getUserPrincipal() {
		return upgradeRequest.getUserPrincipal();
	}

	@Override
	public URI getRequestURI() {
		return upgradeRequest.getRequestURI();
	}

	@Override
	public boolean isUserInRole(String role) {
		return upgradeRequest.isUserInRole(role);
	}

	@Override
	public Object getHttpSession() {
		return upgradeRequest.getHttpSession();
	}

	@Override
	public Map<String, List<String>> getParameterMap() {
		return upgradeRequest.getParameterMap();
	}

	@Override
	public String getQueryString() {
		return upgradeRequest.getQueryString();
	}
}
