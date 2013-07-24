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
    
    @Override
    public int hashCode() {
    	return recvType.hashCode()*1024 + meth.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	return this.getClass() == obj.getClass() && ((Callback)obj).recvType.equals(recvType) &&
    			((Callback)obj).meth.equals(meth);
    }
}
