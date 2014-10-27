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
 package com.urbancode.air;

public class AirPluginTool {

    //**************************************************************************
    // CLASS
    //**************************************************************************

    //**************************************************************************
    // INSTANCE
    //**************************************************************************
    
    final public def isWindows = (System.getProperty('os.name') =~ /(?i)windows/).find()

    def out = System.out;
    def err = System.err;

    private def inPropsFile;
    private def outPropsFile;

    def outProps;

    public AirPluginTool(def inFile, def outFile){
        inPropsFile = inFile;
        outPropsFile = outFile;
        outProps = new Properties();
    }

    public Properties getStepProperties() {
        def props = new Properties();
        final def inputPropsFile = this.inPropsFile;
        final def inputPropsStream = null;
        try {
            inputPropsStream = new FileInputStream(inputPropsFile);
            props.load(inputPropsStream);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            inputPropsStream.close();
        }
        return props;
    }

    public void setOutputProperty(String name, String value) {
        this.outProps.setProperty(name, value);
    }

    public void setOutputProperties() {
        final OutputStream outputPropsStream = null;
        try {
            outputPropsStream = new FileOutputStream(this.outPropsFile);
            outProps.store(outputPropsStream, "");
        }
        finally {
            if (outputPropsStream != null) {
                outputPropsStream.close();
            }   
        }
    }

    public String getAuthToken() {
        String authToken = System.getenv("AUTH_TOKEN");
        return "{\"token\" : \"" + authToken + "\"}";
    }

    public String getAuthTokenUsername() {
        return "PasswordIsAuthToken";
    }

    public void storeOutputProperties() {
        setOutputProperties();
    }
}
