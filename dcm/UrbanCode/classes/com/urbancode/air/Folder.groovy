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

import java.util.Map;


class Folder {
    def id
    def name
    def parentId
    def Map<String, Folder> children = new HashMap<String, Folder>()
    
    public Folder(id, name, parentId) {
        this.id = id
        this.name = name
        this.parentId = parentId
    }

    public boolean equals(Object o) {
        if (o instanceof Folder && o != null) {
            return id.equals(o.id)
        }
        else {
            return false
        }
    }

    public String toString() {
        return "[id:$id, name:$name, parent:$parentId, children:${children.keySet()}]"
    }
}

