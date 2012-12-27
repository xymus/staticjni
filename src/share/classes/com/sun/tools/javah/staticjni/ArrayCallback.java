package com.sun.tools.javah.staticjni;

import javax.lang.model.type.ArrayType;

public class ArrayCallback {
	public ArrayType arrayType;

    public ArrayCallback( ArrayType t ) {
    	this.arrayType = t;
    }
    public String toString() {
        return "<ArrayAccess " + arrayType.toString() + ">";
    }
    
    @Override
    public int hashCode() {
    	return arrayType.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	return this.getClass() == obj.getClass() &&
    			((ArrayCallback)obj).arrayType.equals(arrayType);
    }
}
