package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;

public class ListPageElement extends AbstractCompositePageElement {
    private boolean ordered = true;

    public ListPageElement() {
        super();
    }

    public ListPageElement(List<? extends PageElement> listElements) {
        super();
        getSubElements().addAll(listElements);
    }

    @Override
    public void build() {
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutListPageElement(this);
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    @Override
    public void visitSubElements(ILayouter layouter) {
        for (PageElement subElement : getSubElements()) {
            layouter.layoutWrapperPageElement(new WrapperPageElement(LayouterWrapperType.LISTITEM, subElement));
        }
    }
}
