/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.facelets.tag.composite;

import com.sun.facelets.Facelet;
import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.FaceletFactory;
import com.sun.facelets.FaceletViewHandler;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;
import com.sun.facelets.tag.jsf.core.ActionListenerHandler;
import com.sun.facelets.tag.jsf.core.ValueChangeListenerHandler;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.ActionSource2;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author edburns
 */
public class CompositeComponentTagHandler extends ComponentHandler {
    
    CompositeComponentTagHandler(Resource compositeComponentResource,
            ComponentConfig config) {
        super(config);
        this.compositeComponentResource = compositeComponentResource;
    }
    
    private Resource compositeComponentResource;
    
    @Override
    protected UIComponent createComponent(FaceletContext ctx) {
        UIComponent result = null;
        FacesContext context = ctx.getFacesContext();
        Resource componentResource = CompositeComponentTagLibrary.
                getScriptComponentResource(context, compositeComponentResource);

        if (null != componentResource) {
            result = context.getApplication().createComponent(componentResource);
        }
        
        if (null == result) {
            result = super.createComponent(ctx);
            ((CompositeComponentImpl) result).setResource(compositeComponentResource);
        }
        
        return result;
    }

    @Override
    protected void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException, FacesException, ELException {
        // Allow any nested elements that reside inside the markup element
        // for this tag to get applied
        super.applyNextHandler(ctx, c);
        // Apply the facelet for this composite component
        ExternalContext extContext = ctx.getFacesContext().getExternalContext();
        setCurrentCompositeComponent(extContext, c);
        applyCompositeComponent(ctx, c);
        setCurrentCompositeComponent(extContext, null);
        // Allow any PDL declared attached objects to be retargeted
        retargetAttachedObjects(ctx , c);
        
    }
    
    static UIComponent getCurrentCompositeComponent(ExternalContext extContext) {
        UIComponent result = null;  
        result = (UIComponent) 
                extContext.getRequestMap().get("com.sun.facelets.CurrentCompositeComponent");
        
        return result;
    }
    
    private static void setCurrentCompositeComponent(ExternalContext extContext, 
            UIComponent cur) {
        extContext.getRequestMap().put("com.sun.facelets.CurrentCompositeComponent",
                cur);
    }
    
    private void retargetAttachedObjects(FaceletContext ctx, UIComponent c) {
        Iterator<ActionListenerHandler> actionListeners = this.findNextByType(ActionListenerHandler.class);
        ActionListenerHandler cur = null;
        if (actionListeners.hasNext()) {
            cur = actionListeners.next();
            List<UIComponent> targets = AttachedObjectTargetHandler.
                    getAttachedObjectTargets(c);
            List<RetargetableAttachedObjectHandler> handlers = 
                    AttachedObjectTargetHandler.getRetargetableHandlers(c);
            for (UIComponent curTarget : targets) {
                if (curTarget instanceof ActionSource2) {
                    for (RetargetableAttachedObjectHandler curHandler : handlers) {
                        if (curHandler instanceof ActionListenerHandler) {
                            curHandler.applyAttachedObjectToComponent(ctx, 
                                    curTarget);
                        }
                    }
                }
                if (curTarget instanceof EditableValueHolder) {
                    for (RetargetableAttachedObjectHandler curHandler : handlers) {
                        if (curHandler instanceof ValueChangeListenerHandler) {
                            curHandler.applyAttachedObjectToComponent(ctx, 
                                    curTarget);
                        }
                    }
                    
                }
            }
        }
    }
    
    private void applyCompositeComponent(FaceletContext ctx, UIComponent c) {
        Facelet f = null;
        FacesContext facesContext = ctx.getFacesContext();
        FaceletViewHandler faceletViewHandler = (FaceletViewHandler) 
                facesContext.getApplication().getViewHandler();
        FaceletFactory factory = faceletViewHandler.getFaceletFactory();
        try {
            f = factory.getFacelet(compositeComponentResource.getURL());
            f.apply(facesContext, c);
        } catch (IOException ex) {
            Logger.getLogger(CompositeComponentTagHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FaceletException ex) {
            Logger.getLogger(CompositeComponentTagHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FacesException ex) {
            Logger.getLogger(CompositeComponentTagHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ELException ex) {
            Logger.getLogger(CompositeComponentTagHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
                     
    }
    
    
}