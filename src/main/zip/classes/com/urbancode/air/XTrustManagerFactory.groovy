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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public class XTrustManagerFactory extends TrustManagerFactorySpi {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //------------------------------------------------------------------------------------------
    public XTrustManagerFactory() {
    }

    //------------------------------------------------------------------------------------------
    @Override
    protected void engineInit(KeyStore keystore) {
    }

    //------------------------------------------------------------------------------------------
    @Override
    protected void engineInit(ManagerFactoryParameters managerParams)
    throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException(
                "XTrustManagerFactory cannot be initialized using ManagerFactoryParameters");
    }

    //------------------------------------------------------------------------------------------
    @Override
    protected TrustManager[] engineGetTrustManagers() {
        List<TrustManager> managerList = new ArrayList<TrustManager>();
        managerList.add(new OpenX509TrustManager());

        return managerList.toArray(new TrustManager[managerList.size()]);
    }
}
