package org.faktorips.devtools.core.ui.controls;

import org.faktorips.devtools.core.model.ValueSet;

/**
 * Interface which impelements a changeListener  
 * @author Andy R�sch
 */
public interface ValueSetChangeListener {
    /**
     * Gets fired when the given ValueSet has changed
     *
     */
    public void valueSetChanged (ValueSet valueSet);
}
