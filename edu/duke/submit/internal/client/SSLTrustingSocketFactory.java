/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
 *******************************************************************************/
/*
 * Created on Jun 18, 2003
 *
 */
package edu.duke.submit.internal.client;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author Duke Curious
 */
public class SSLTrustingSocketFactory {
    private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    } };

    private SSLContext sc;

    private SocketFactory factory;

    private static SSLTrustingSocketFactory instance;

    private SSLTrustingSocketFactory() {
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            factory = sc.getSocketFactory();
        } catch (KeyManagementException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public Socket createSocket() {
        try {
            return factory.createSocket();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SSLTrustingSocketFactory getDefault() {
        if (instance == null)
            instance = new SSLTrustingSocketFactory();
        return instance;
    }
}
