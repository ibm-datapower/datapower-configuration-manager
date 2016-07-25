/**
 * Copyright 2014 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
 package com.urbancode.air

import java.security.Provider;
import java.security.Security;

public class XTrustProvider extends Provider {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private long serialVersionUID = 1L;

    //----------------------------------------------------------------------------------------------
    static public void install() {
        if (Security.getProvider(XTrustProvider.class.getSimpleName()) == null) {
            Security.insertProviderAt(new XTrustProvider(), 2);
            Security.setProperty("ssl.TrustManagerFactory.algorithm", "XTrust509");
        }
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public XTrustProvider() {
        super(XTrustProvider.class.getSimpleName(), 1D,
                "Basic XTrustProvider ignoring invalid certificates");

        put("TrustManagerFactory.XTrust509", XTrustManagerFactory.class.getName());
    }
}
