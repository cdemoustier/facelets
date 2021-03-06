/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.util;

/**
 * @author Jacob Hookom
 * @version $Id: Path.java,v 1.3 2008/07/13 19:01:34 rlubke Exp $
 */
public final class Path {

    public static final String normalize(String path) {
        if (path.length() == 0)
            return path;
        String n = path;
        boolean abs = false;
        while (n.indexOf('\\') >= 0) {
            n = n.replace('\\', '/');
        }
        if (n.charAt(0) != '/') {
            n = '/' + n;
            abs = true;
        }
        int idx = 0;
        while (true) {
            idx = n.indexOf("%20");
            if (idx == -1) {
                break;
            }
            n = n.substring(0, idx) + " " + n.substring(idx + 3);
        }
        while (true) {
            idx = n.indexOf("/./");
            if (idx == -1) {
                break;
            }
            n = n.substring(0, idx) + n.substring(idx + 2);
        }
        if (abs) {
            n = n.substring(1);
        }
        return n;
    }

    public static final String relative(String ctx, String path) {
        if (path.length() == 0) {
            return context(ctx);
        }
        String c = context(normalize(ctx));
        String p = normalize(path);
        p = c + p;

        int idx = 0;
        while (true) {
            idx = p.indexOf("/../");
            if (idx == -1) {
                break;
            }
            int s = p.lastIndexOf('/', idx - 3);
            if (s == -1) {
                break;
            }
            p = p.substring(0, s) + p.substring(idx + 3);
        }
        return p;
    }

    public static final String context(String path) {
        int idx = path.lastIndexOf('/');
        if (idx == -1) {
            return "/";
        }
        return path.substring(0, idx + 1);
    }

}
