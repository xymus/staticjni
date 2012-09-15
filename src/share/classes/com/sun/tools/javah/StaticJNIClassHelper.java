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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import net.xymus.staticjni.NativeCall;
import net.xymus.staticjni.NativeCalls;

/**
 * Header file generator for JNI.
 *
 * Not a true Gen... Wrapper for 3 other Gens
 */
public class StaticJNIClassHelper {
    StaticJNIClassHelper( StaticJNI gen ) {
    	this.gen = gen;
    }
    
    StaticJNI gen;
    
    TypeElement currentClass = null;
    
    // Explicit calls
    Set<ExecutableElement> callbacks = new HashSet<>();
    Set<VariableElement> fieldCallbacks = new HashSet<>();
    Set<MethodWithReceiver> remoteCallbacks = new HashSet<>(); // TODO
    
    // Referred types
    Set<TypeMirror> referredTypes = new HashSet<>(); 
    
    // Explicit casts TODO
    
    // Super calls TODO
    
    public void setCurrentClass ( TypeElement clazz ) {
    	if ( clazz != currentClass ) {
    		currentClass = clazz;

    	    callbacks.clear();
    	    fieldCallbacks.clear();
    	    remoteCallbacks.clear();
    	    referredTypes.clear();
    	    
	        List<ExecutableElement> classmethods = ElementFilter.methodsIn(clazz.getEnclosedElements());
	        for (ExecutableElement md: classmethods) {
	            if(md.getModifiers().contains(Modifier.NATIVE)){
	                TypeMirror mtr = gen.types.erasure(md.getReturnType());
	                
	                // return type
	                if ( gen.advancedStaticType( mtr ) )
	                	referredTypes.add( mtr );
	                
	                // params type
	                List<? extends VariableElement> paramargs = md.getParameters();
	                for (VariableElement p: paramargs)  {
	                    TypeMirror t = gen.types.erasure(p.asType());
	                    if ( gen.advancedStaticType( t ) )
	                    	referredTypes.add( t );
	                }
	                
	                // self type
	                if (! md.getModifiers().contains(Modifier.STATIC) ) {
	                	TypeMirror t = clazz.asType();
	                	if ( gen.advancedStaticType( t ) )
	                		referredTypes.add( t );
	                }
	                
                	// scan annotations for NativeCalls
                    List<? extends AnnotationMirror> annotations = md.getAnnotationMirrors();
                    for ( AnnotationMirror annotation: annotations ) {
                    	String str = annotation.getAnnotationType().toString();
                    	
                    	// NativeCall annotation
                		if ( str.equals( NativeCall.class.getCanonicalName() ) )
                			for ( ExecutableElement p: annotation.getElementValues().keySet() )
                				if ( p.getSimpleName().toString().equals( "value" ) ) {
	                				Object v = annotation.getElementValues().get( p ).getValue();
	                				if ( String.class.isInstance(v)) {
	                					tryToRegisterNativeCall( clazz, v.toString(), callbacks, fieldCallbacks );
	                				}
	                			}

                    	// NativeCalls annotation
                		if ( str.equals( NativeCalls.class.getCanonicalName() ) )
                			for ( ExecutableElement p: annotation.getElementValues().keySet() )
                				if ( p.getSimpleName().toString().equals( "value" ) ) {
	                				Object v = annotation.getElementValues().get( p ).getValue();
	                				if ( List.class.isInstance(v) )
	                					for ( Object e: (List)v ) {
	                						// elems.getConstantExpression
		                					tryToRegisterNativeCall( clazz, ((AnnotationValue)e).getValue().toString(), callbacks, fieldCallbacks );
	                					}
	                			}
                		
                    }
                    
                    // Scan imports for types
                    for ( ExecutableElement m: callbacks ) {
                    	TypeMirror r = gen.types.erasure(m.getReturnType());
                    	if ( gen.advancedStaticType(r) )
                    		referredTypes.add( r );

    	                paramargs = m.getParameters();
    	                for (VariableElement p: paramargs)  {
    	                    TypeMirror t = gen.types.erasure(p.asType());
    	                    if ( gen.advancedStaticType( t ) )
    	                    	referredTypes.add( t );
    	                }
    	                
    	                // self type
    	                if (! m.getModifiers().contains(Modifier.STATIC) ) {
    	                	TypeMirror t = clazz.asType();
    	                	if ( gen.advancedStaticType( t ) )
    	                		referredTypes.add( t );
    	                }
                    }
                    
                    for ( VariableElement f: fieldCallbacks ) {
                    	TypeMirror t = gen.types.erasure(f.asType());
                    	if ( gen.advancedStaticType(t) )
                    		referredTypes.add( t );
                    }
                    
                    // TODO add non-local meth and fields
	            }
	        }
    	}
    }
    
    void tryToRegisterNativeCall(TypeElement clazz, String name,
    		Set<ExecutableElement> callbacks,
    		Set<VariableElement> callbacks_to_field ) {
    	
    	// non-local call TODO
    	TypeElement target = clazz;
    	
    	String[] words = name.split(" ");
    	if ( words.length > 1 ) {
	    	Element e = gen.elems.getTypeElement( words[0] );
	    	if ( e != null && TypeElement.class.isInstance(e) ) {
	    		target = (TypeElement)e;
	    		name = words[1];
	    	}
    	}
    	
		List<ExecutableElement> methods = ElementFilter.methodsIn( target.getEnclosedElements() );
		for (ExecutableElement m: methods) {
			if ( name.toString().equals( m.getSimpleName().toString() ) ) {
				callbacks.add(m);
				return;
			}
		}
			
		List<VariableElement> fields = ElementFilter.fieldsIn( target.getEnclosedElements() );
		for ( VariableElement f: fields ) {
			if ( f.getSimpleName().toString().equals( name ) ) {
				callbacks_to_field.add( f );
				return;
			}
		}
		
		gen.util.error("err.staticjni.targetnotfound", name );
    }
    
    class MethodWithReceiver {
    	MethodWithReceiver( TypeElement e, ExecutableElement m ) {
    		receiver = e;
    		meth = m;
    	}
    	
    	TypeElement receiver;
    	ExecutableElement meth;
    }
}
