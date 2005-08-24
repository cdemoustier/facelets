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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.el.ELException;

import com.sun.facelets.FaceletException;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.el.ELText;
import com.sun.facelets.tag.CompositeFaceletHandler;
import com.sun.facelets.tag.Tag;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagException;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: TextUnit.java,v 1.8 2005/08/24 04:38:54 jhook Exp $
 */
final class TextUnit extends CompilationUnit {

    private final StringBuffer buffer;

    private final Stack tags;

    private final List children;

    private boolean startTagOpen;
    
    private final String alias;

    public TextUnit(String alias) {
        this.alias = alias;
        this.buffer = new StringBuffer(256);
        this.tags = new Stack();
        this.children = new ArrayList();
        this.startTagOpen = false;
    }

    public FaceletHandler createFaceletHandler() {
        this.flushBufferToConfig(false);
        
        if (this.children.size() == 0) {
            return LEAF;
        }
        
        FaceletHandler[] h = new FaceletHandler[this.children.size()];
        Object obj;
        for (int i = 0; i < h.length; i++) {
            obj = this.children.get(i);
            if (obj instanceof FaceletHandler) {
                h[i] = (FaceletHandler) obj;
            } else {
                h[i] = ((CompilationUnit) obj).createFaceletHandler();
            }
        }
        if (h.length == 1) {
            return h[0];
        }
        return new CompositeFaceletHandler(h);
    }

    public void write(String text) {
        this.finishStartTag();
        this.buffer.append(text);
    }

    public void startTag(Tag tag) {

        // finish any previously written tags
        this.finishStartTag();

        // push this tag onto the stack
        this.tags.push(tag);

        // write it out
        this.buffer.append('<');
        this.buffer.append(tag.getQName());
        TagAttribute[] attrs = tag.getAttributes().getAll();
        if (attrs.length > 0) {
            for (int i = 0; i < attrs.length; i++) {
                this.buffer.append(' ').append(attrs[i].getQName()).append(
                        "=\"").append(attrs[i].getValue()).append("\"");
            }
        }

        // notify that we have an open tag
        this.startTagOpen = true;
    }

    private void finishStartTag() {
        if (this.tags.size() > 0 && this.startTagOpen) {
            this.buffer.append(">");
            this.startTagOpen = false;
        }
    }

    public void endTag() {
        Tag tag = (Tag) this.tags.pop();
        if (this.startTagOpen) {
            this.buffer.append("/>");
            this.startTagOpen = false;
        } else {
            this.buffer.append("</").append(tag.getQName()).append('>');
        }
    }

    public void addChild(CompilationUnit unit) {
        // if we are adding some other kind of unit
        // then we need to capture our buffer into a UITextHandler
        this.finishStartTag();
        this.flushBufferToConfig(true);
        this.children.add(unit);
    }

    protected void flushBufferToConfig(boolean child) {
        if (this.buffer.length() > 0) {
            String s = this.buffer.toString();
            if (s.trim().length() > 0) {
                if (child) {
                    s = trimRight(s);
                }
                if (s.length() > 0) {
                    try {
                        ELText txt = ELText.parse(s);
                        if (txt != null) {
                            if (txt.isLiteral()) {
                                this.children.add(new UILiteralTextHandler(txt.toString()));
                            } else {
                                this.children.add(new UITextHandler(this.alias, txt));
                            }
                        }
                    } catch (ELException e) {
                        if (this.tags.size() > 0) {
                            throw new TagException((Tag) this.tags.peek(), e
                                    .getMessage());
                        } else {
                            throw new ELException(this.alias + ": "+e.getMessage(), e.getCause());
                        }
                    }
                }
            }
            this.buffer.setLength(0);
        }
    }
    
    public boolean isClosed() {
        return this.tags.empty();
    }

    private final static String trimRight(String s) {
        int i = s.length() - 1;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }
        if (i == s.length() - 1) {
            return s;
        } else if (i + 1 < s.length() && Character.isSpaceChar(s.charAt(i + 1))) {
            return s.substring(0, i + 2);
        } else {
            return s.substring(0, i + 1);
        }
    }

    public String toString() {
        return "TextUnit["+this.children.size()+"]";
    }
}
