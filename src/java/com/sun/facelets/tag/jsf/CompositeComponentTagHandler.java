/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.facelets.tag.jsf;

import com.sun.facelets.tag.composite.*;
import com.sun.facelets.Facelet;
import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.FaceletFactory;
import com.sun.facelets.FaceletViewHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;
import com.sun.facelets.tag.jsf.ConvertHandler;
import com.sun.facelets.tag.jsf.ValidateHandler;
import com.sun.facelets.tag.jsf.core.ActionListenerHandler;
import com.sun.facelets.tag.jsf.core.ValueChangeListenerHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.ActionSource2;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.webapp.pdl.AttachedObjectHandler;
import javax.faces.webapp.pdl.PDLUtils;

/**
 *
 * @author edburns
 */
public class CompositeComponentTagHandler extends ComponentHandler {
    
    private final TagAttribute componentTypeAttr;

    CompositeComponentTagHandler(Resource compositeComponentResource,
            ComponentConfig config) {
        super(config);
        this.compositeComponentResource = compositeComponentResource;
        this.componentTypeAttr = this.getAttribute("componentType");
    }
    
    private void convertAttributesIntoParams(FaceletContext ctx) {
        TagAttributes tagAttributes = this.tag.getAttributes();
        TagAttribute attrs[] = tagAttributes.getAll();
        String name, value;
        ExpressionFactory expressionFactory = null;
        ValueExpression valueExpression = null;
        VariableMapper variableMapper = null;
        for (int i = 0; i < attrs.length; i++) {
            name = attrs[i].getLocalName();
            if (null != name && 0 < name.length() && 
                !name.equals("id") && !name.equals("binding")){
                value = attrs[i].getValue();
                if (null != value && 0 < value.length()) {
                    // lazily initialize this local variable
                    if (null == expressionFactory) {
                        expressionFactory = ctx.getFacesContext().getApplication().
                                getExpressionFactory();
                        variableMapper = ctx.getVariableMapper();
                    }
                    if (value.startsWith("#{")) {
                        valueExpression = expressionFactory.
                                createValueExpression(ctx, value, Object.class);
                    } else {
                        valueExpression = expressionFactory.
                                createValueExpression(value, Object.class);
                    }
                    variableMapper.setVariable(name, valueExpression);
                }
            }
        }
        
    }
    
    private Resource compositeComponentResource;
    
    

    @Override
    protected UIComponent createComponent(FaceletContext ctx) {
        UIComponent result = null;
        FacesContext context = ctx.getFacesContext();
        Resource componentResource = CompositeComponentTagLibrary.getScriptComponentResource(context, compositeComponentResource);

        // PENDING(edburns): IMPORTANT: Figure out why this is getting
        // called twice for loginPanel on a single render.  
        // I think it has something to do with the resource request because
        // I see that the LoginProductName.png image wasn't rendered initially
        
        if (null != componentResource) {
            result = context.getApplication().createComponent(componentResource);
        }

        if (null == result) {
            if (this.componentTypeAttr != null) {
                ValueExpression ve = this.componentTypeAttr.getValueExpression(ctx,
                        String.class);
                String type = (String) ve.getValue(ctx);
                if (null != type && 0 < type.length()) {
                    result = context.getApplication().createComponent(type);
                }
            }
        }
        if (null == result) {
            componentType = CompositeComponentImpl.TYPE;
            result = super.createComponent(ctx);
        }
        
        if (null != result) {
            result.getAttributes().put(Resource.COMPONENT_RESOURCE_KEY, 
                    compositeComponentResource);
        }
        else {
            throw new NullPointerException("Unable to instantiate component for tag " +
                    this.tag.getLocalName() + " with id " + this.id.getValue(ctx));
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
        PDLUtils.retargetAttachedObjects(ctx.getFacesContext(), c, 
                getAttachedObjectHandlers(c, false));

    }

    static UIComponent getCurrentCompositeComponent(ExternalContext extContext) {
        UIComponent result = null;
        result = (UIComponent) extContext.getRequestMap().get("com.sun.facelets.CurrentCompositeComponent");

        return result;
    }

    private static void setCurrentCompositeComponent(ExternalContext extContext,
            UIComponent cur) {
        extContext.getRequestMap().put("com.sun.facelets.CurrentCompositeComponent",
                cur);
    }

    private void applyCompositeComponent(FaceletContext ctx, UIComponent c) {
        Facelet f = null;
        FacesContext facesContext = ctx.getFacesContext();
        FaceletViewHandler faceletViewHandler = (FaceletViewHandler) facesContext.getApplication().getViewHandler();
        FaceletFactory factory = faceletViewHandler.getFaceletFactory();
        try {
            f = factory.getFacelet(compositeComponentResource.getURL());
            convertAttributesIntoParams(ctx);
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

    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component) {
        return getAttachedObjectHandlers(component, true);
    }
    
    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component,
            boolean create) {
        Map<String, Object> attrs = component.getAttributes();
        List<AttachedObjectHandler> result = (List<AttachedObjectHandler>)
                attrs.get("javax.faces.RetargetableHandlers");
        
        if (null == result) {
            if (create) {
                result = new ArrayList<AttachedObjectHandler>();
                attrs.put("javax.faces.RetargetableHandlers", result);
            }
            else {
                result = Collections.EMPTY_LIST;
            }
        }
        return result;
    }
    
    
    
}
