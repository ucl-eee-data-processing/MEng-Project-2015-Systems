/*
 * Copyright 2016 tokwii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.oryx.lazarus.serving;
import org.bitpipeline.lib.owm.OwnClient;

/**
 *
 * @author tokwii
 */
public class LazarusServingUtility {
    public static final String OWN_API_KEY = "795c1ff0b7c8af640f1f88310e296cd8";
    public static final String GOOGLE_API_KEY = "AIzaSyCfIz0XAMftyZ98phzC9dgEXZcKsyC7XLo";
    private OwmClient own;
    
    public LazarusSevingUtility(String Address){
        own = new OwnClient();
        own.setAPPID(API_KEY);
    }
    
}
