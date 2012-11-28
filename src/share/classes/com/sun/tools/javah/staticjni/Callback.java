package com.sun.tools.javah.staticjni;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class Callback {
    public TypeElement recvType;
    public ExecutableElement meth;
    
    public Callback( TypeElement rt, ExecutableElement m ) {
        recvType = rt;
        meth = m;
    }
    
    public String toString() {
        return "<Callback " + recvType.getSimpleName() + ", " + meth.getSimpleName() + ">";
    }
}
