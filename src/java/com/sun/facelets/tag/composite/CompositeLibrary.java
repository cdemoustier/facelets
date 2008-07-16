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

package com.sun.facelets.tag.composite;

import com.sun.facelets.tag.AbstractTagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id: CompositeLibrary.java,v 1.1.2.4 2008/07/16 17:52:46 edburns Exp $
 */
public final class CompositeLibrary extends AbstractTagLibrary {

    public final static String Namespace = "http://java.sun.com/jsf/composite";

    public final static CompositeLibrary Instance = new CompositeLibrary();

    public CompositeLibrary() {
        super(Namespace);

        this.addTagHandler("interface", InterfaceHandler.class);
        
        this.addTagHandler("attribute", AttributeHandler.class);
        this.addTagHandler("deferred-value", DeferredValueHandler.class);
        this.addTagHandler("type", TypeHandler.class);
        this.addTagHandler("deferred-method", DeferredMethodHandler.class);
        this.addTagHandler("method-signature", MethodSignatureHandler.class);
        
        this.addTagHandler("editableValueHolder", EditableValueHolderAttachedObjectTargetHandler.class);
        this.addTagHandler("actionSource", ActionSource2AttachedObjectTargetHandler.class);
        this.addTagHandler("valueHolder", ValueHolderAttachedObjectTargetHandler.class);
        
        this.addTagHandler("facet", FacetHandler.class);
        
        this.addTagHandler("implementation", ImplementationHandler.class);
        this.addTagHandler("children", ChildrenHandler.class);
    }
}
