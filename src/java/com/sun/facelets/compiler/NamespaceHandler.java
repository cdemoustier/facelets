/**
 * Licensed under the Common Development and Distribution License,
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.sun.com/cddl/
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.sun.facelets.compiler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.el.ELException;
import javax.el.FunctionMapper;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.el.CompositeFunctionMapper;
import com.sun.facelets.tag.TagLibrary;

final class NamespaceHandler extends FunctionMapper implements FaceletHandler {

    private final TagLibrary library;
    private final Map ns;
    private FaceletHandler next;
    
    public NamespaceHandler(FaceletHandler next, TagLibrary library, Map ns) {
        this.library = library;
        this.ns = ns;
        this.next = next;
    }

    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        FunctionMapper orig = ctx.getFunctionMapper();
        ctx.setFunctionMapper(new CompositeFunctionMapper(this, orig));
        try {
            next.apply(ctx, parent);
        } finally {
            ctx.setFunctionMapper(orig);
        }
    }

    public Method resolveFunction(String prefix, String localName) {
        String uri = (String) this.ns.get(prefix);
        if (uri != null) {
            return this.library.createFunction(uri, localName);
        }
        return null;
    }

}
