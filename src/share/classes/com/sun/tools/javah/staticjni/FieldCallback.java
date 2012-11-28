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
}
