/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.filesystem;

import java.util.ArrayList;
import java.util.List;

public class XmlMinifyFile {
    final List<XmlMinifyResource> resources = new ArrayList<>();
    
    public List<XmlMinifyResource> getResources() { return resources; }
}
