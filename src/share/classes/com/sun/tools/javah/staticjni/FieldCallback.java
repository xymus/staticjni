package com.sun.tools.javah.staticjni;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class FieldCallback {
    public TypeElement recvType;
    public VariableElement field;
    
    public FieldCallback( TypeElement rt, VariableElement m ) {
        recvType = rt;
        field = m;
    }
    
    public String toString() {
        return "<FieldCallback " + recvType.getSimpleName() + ", " + field.getSimpleName() + ">";
    }
    
    @Override
    public int hashCode() {
    	return recvType.hashCode()*1024 + field.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	return this.getClass() == obj.getClass() && ((FieldCallback)obj).recvType.equals(recvType) &&
    			((FieldCallback)obj).field.equals(field);
    }
}
