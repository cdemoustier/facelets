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

package com.sun.facelets.tag.jsf;

import com.sun.facelets.tag.TagConfig;

/**
 * Used in creating ValidateHandler's and all implementations.
 * 
 * @see com.sun.facelets.tag.jsf.ValidateHandler
 * @author Jacob Hookom
 * @version $Id: ValidatorConfig.java,v 1.2 2005/08/24 04:38:51 jhook Exp $
 */
public interface ValidatorConfig extends TagConfig {

    /**
     * The validator-id associated with a particular validator in your faces-config
     * @return passable to Application.createValidator(String)
     */
    public String getValidatorId();
    
}
