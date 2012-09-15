/*
 * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.javah;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes.Name;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import com.sun.javadoc.Type;
import com.sun.tools.javah.TypeSignature.SignatureException;
import com.sun.tools.javah.Util.Exit;



/**
 * Header file generator for JNI.
 *
 * Supports only the creation of the body of the frontier.
 */
public abstract class StaticJNIGen extends JNI {
    StaticJNIGen(Util util, StaticJNIClassHelper helper) {
        super(util);
        this.helper = helper;
    }
    
    StaticJNIClassHelper helper;

    protected String staticjniType(TypeMirror t) throws Util.Exit {
    //	return staticjniType( t, null );
   // }
    //protected String staticjniType(TypeMirror t, Set<TypeMirror> usedTypes ) throws Util.Exit {
        TypeElement throwable = elems.getTypeElement("java.lang.Throwable");
        TypeElement jClass = elems.getTypeElement("java.lang.Class");
        TypeElement jString = elems.getTypeElement("java.lang.String");
        Element tclassDoc = types.asElement(t);

        switch (t.getKind()) {
            case ARRAY: {
                TypeMirror ct = ((ArrayType) t).getComponentType();
                switch (ct.getKind()) {
                    case BOOLEAN:  return "jbooleanArray";
                    case BYTE:     return "jbyteArray";
                    case CHAR:     return "jcharArray";
                    case SHORT:    return "jshortArray";
                    case INT:      return "jintArray";
                    case LONG:     return "jlongArray";
                    case FLOAT:    return "jfloatArray";
                    case DOUBLE:   return "jdoubleArray";
                    case ARRAY:
                    case DECLARED: return "jobjectArray";
                    default: throw new Error(ct.toString());
                }
            }

            case VOID:     return "void";
            case BOOLEAN:  return "jboolean";
            case BYTE:     return "jbyte";
            case CHAR:     return "jchar";
            case SHORT:    return "jshort";
            case INT:      return "jint";
            case LONG:     return "jlong";
            case FLOAT:    return "jfloat";
            case DOUBLE:   return "jdouble";

            case DECLARED: {
                if (tclassDoc.equals(jString))
                    return "jstring";
                else if (types.isAssignable(t, throwable.asType()))
                    return "jthrowable";
                else if (types.isAssignable(t, jClass.asType()))
                    return "jclass";
                else
                {
               // 	if ( usedTypes != null )
                //		usedTypes.add( t );
                    return types.asElement(t).getSimpleName().toString();
                }
            }
        }

        util.bug("jni.unknown.type");
        return null; /* dead code. */
    }
    
    protected String getCName( ExecutableElement md, TypeElement clazz, boolean longName ) throws SignatureException {
    	return mangler.mangleMethod(md, clazz, Mangle.Type.METHOD_JNI_SHORT ).substring(5);
                /*(longName) ?
                Mangle.Type.METHOD_JNI_LONG :
                Mangle.Type.METHOD_JNI_SHORT);*/
    }
    protected String getCImplName( ExecutableElement md, TypeElement clazz, boolean longName ) throws SignatureException { 
    		return getCName( md, clazz, longName ) + "__impl";
	}

    protected String castToStaticjni(TypeMirror arg, String name) {
    	if ( advancedStaticType( arg ) )
    		return "(" + types.asElement(arg).getSimpleName().toString() + ")" + name;
    	else
    		return name;
	}

    protected String castFromStaticjni(TypeMirror arg, String name) {
    	if ( advancedStaticType( arg ) )
    		return "(" + jniType(arg) +")" + name;
    	else
    		return name;
	}
    
    protected boolean	advancedStaticType( TypeMirror t ) {
		return t.getKind() == TypeKind.DECLARED;
	}
    
    @Override
    public void write(OutputStream o, TypeElement clazz) throws Exit {
    	helper.setCurrentClass( clazz );
    }
}
