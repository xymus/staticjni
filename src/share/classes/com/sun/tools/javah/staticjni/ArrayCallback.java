package com.sun.tools.javah.staticjni;

import javax.lang.model.type.ArrayType;

public class ArrayCallback {
	public ArrayType arrayType;
	public boolean critical;

    public ArrayCallback( ArrayType t, boolean critical ) {
    	this.arrayType = t;
    	this.critical = critical;
    }
    public String toString() {
        return "<ArrayAccess " + arrayType.toString() + " " + critical + ">";
    }
    
    @Override
    public int hashCode() {
    	return arrayType.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	return this.getClass() == obj.getClass() &&
    			((ArrayCallback)obj).arrayType.equals(arrayType) &&
    			((ArrayCallback)obj).critical == critical;
    }
}
