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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import com.sun.tools.javah.staticjni.ArrayCallback;
import com.sun.tools.javah.staticjni.Callback;
import com.sun.tools.javah.staticjni.ExceptionCallback;
import com.sun.tools.javah.staticjni.FieldCallback;

import net.xymus.staticjni.NativeCall;
import net.xymus.staticjni.NativeCalls;
import net.xymus.staticjni.NativeNew;
import net.xymus.staticjni.NativeNews;
import net.xymus.staticjni.NativeSuperCall;

/**
 * Header file generator for JNI.
 *
 * Not a true Gen, actually a wrapper for 3 other Gens
 */
public class StaticJNIClassHelper {
    StaticJNIClassHelper( StaticJNI gen ) {
    	this.gen = gen;
    }
    
    StaticJNI gen;
    
    TypeElement currentClass = null;
    
    // Explicit calls
    Set<Callback> callbacks = new HashSet<Callback>();
    Set<FieldCallback> fieldCallbacks = new HashSet<FieldCallback>();
    Set<Callback> superCallbacks = new HashSet<Callback>();
    Set<Callback> constCallbacks = new HashSet<Callback>();
    Set<ExceptionCallback> exceptionCallbacks = new HashSet<ExceptionCallback>();
    Set<ArrayCallback> arrayCallbacks = new HashSet<ArrayCallback>();
    
    boolean usesString;
    
    // Referred types
    Set<TypeMirror> referredTypes = new HashSet<TypeMirror>(); 
    
    @SuppressWarnings("unchecked")
    public void setCurrentClass ( TypeElement clazz ) {
    	if ( clazz != currentClass ) {
    		currentClass = clazz;

            callbacks.clear();
            fieldCallbacks.clear();
            superCallbacks.clear();
            constCallbacks.clear();
            exceptionCallbacks.clear();
            arrayCallbacks.clear();
            referredTypes.clear();
            
            usesString = false;
            
            List<ExecutableElement> classmethods = ElementFilter.methodsIn(clazz.getEnclosedElements());
            for (ExecutableElement md: classmethods) {
                if(md.getModifiers().contains(Modifier.NATIVE)){
                    TypeMirror mtr = gen.types.erasure(md.getReturnType());
                    
                    // return type
                    referredTypes.add( mtr );
                    
                    // params type
                    List<? extends VariableElement> paramargs = md.getParameters();
                    for (VariableElement p: paramargs)  {
                        TypeMirror t = gen.types.erasure(p.asType());
                        referredTypes.add( t );
                    }
                    
                    // self type
                    if (! md.getModifiers().contains(Modifier.STATIC) ) {
                        TypeMirror t = clazz.asType();
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
                                        tryToRegisterNativeCall( clazz, md, v.toString(), callbacks, fieldCallbacks );
                                    }
                                }

                        // NativeCalls annotation
                        if ( str.equals( NativeCalls.class.getCanonicalName() ) )
                            for ( ExecutableElement p: annotation.getElementValues().keySet() )
                                if ( p.getSimpleName().toString().equals( "value" ) ) {
                                    Object v = annotation.getElementValues().get( p ).getValue();
                                    if ( List.class.isInstance(v) )
                                        for ( Object e: (List<Object>)v ) {
                                            // elems.getConstantExpression
                                            tryToRegisterNativeCall( clazz, md, ((AnnotationValue)e).getValue().toString(), callbacks, fieldCallbacks );
                                        }
                                }

                        // NativeNew annotation
                        if ( str.equals( NativeNew.class.getCanonicalName() ) )
                            for ( ExecutableElement p: annotation.getElementValues().keySet() )
                                if ( p.getSimpleName().toString().equals( "value" ) ) {
                                    Object v = annotation.getElementValues().get( p ).getValue();
                                    if ( String.class.isInstance(v)) {
                                        tryToRegisterNativeNew( clazz, md, v.toString(), constCallbacks );
                                    }
                                }

                        // NativeNews annotation
                        if ( str.equals( NativeNews.class.getCanonicalName() ) )
                            for ( ExecutableElement p: annotation.getElementValues().keySet() )
                                if ( p.getSimpleName().toString().equals( "value" ) ) {
                                    Object v = annotation.getElementValues().get( p ).getValue();
                                    if ( List.class.isInstance(v) )
                                        for ( Object e: (List<Object>)v ) {
                                            tryToRegisterNativeNew( clazz, md, ((AnnotationValue)e).getValue().toString(), constCallbacks );
                                        }
                                }
                        
                        // super
                        if ( str.equals( NativeSuperCall.class.getCanonicalName() ) ) {
                            superCallbacks.add( new Callback(clazz, md) );
                        }
                    }
                    
                    // check possible throw from the throws keyword
                    for ( TypeMirror t : md.getThrownTypes() ) {
                    	exceptionCallbacks.add( new ExceptionCallback(t) );
                    }
                }
            }
                    
            // RuntimeExcpetion is always available
            TypeElement eRuntimeEx = gen.elems.getTypeElement("java.lang.RuntimeException");
            TypeMirror tRuntimeEx = eRuntimeEx.asType();
            exceptionCallbacks.add( new ExceptionCallback(tRuntimeEx) );
            
            // Scan imports for types
            for ( Callback cb: callbacks ) {
                ExecutableElement m = cb.meth;
                
                TypeMirror r = m.getReturnType();
                referredTypes.add( r );

                List<? extends VariableElement> paramargs = m.getParameters();
                for (VariableElement p: paramargs)  {
                    TypeMirror t = p.asType();
                    referredTypes.add( t );
                }
                
                // self type
                if (! m.getModifiers().contains(Modifier.STATIC) ) {
                    TypeMirror t = cb.recvType.asType();
                    referredTypes.add( t );
                }
            }

            for ( Callback cb: constCallbacks ) {
                ExecutableElement m = cb.meth;
                
                TypeMirror r = gen.types.erasure(m.getReturnType());
                referredTypes.add( r );

                List<? extends VariableElement> paramargs = m.getParameters();
                for (VariableElement p: paramargs)  {
                    TypeMirror t = gen.types.erasure(p.asType());
                    referredTypes.add( t );
                }
                
                // self type
                TypeMirror t = cb.recvType.asType();
                referredTypes.add( t );
            }
            
            for ( FieldCallback f: fieldCallbacks ) {
                TypeMirror t = f.recvType.asType();
                referredTypes.add( t );
                
                t = f.field.asType();
                referredTypes.add( t );
            }
            
            // scan types for special needs
            TypeElement jString = gen.elems.getTypeElement("java.lang.String");
            for ( TypeMirror c: referredTypes ) {
            	TypeKind kind = c.getKind();
            	if (kind == TypeKind.ARRAY) {
                    arrayCallbacks.add(new ArrayCallback((ArrayType)c));
                }
            	else if ( kind == TypeKind.DECLARED ) {
            		if ( gen.types.asElement(c).equals(jString) ) {
            		// String is in use
            		usesString = true;
            		break;
            		}
            	}
            }
        }
    }
    
    void tryToRegisterNativeCall(TypeElement clazz, ExecutableElement from_meth, 
            String name,
            Set<Callback> callbacks,
            Set<FieldCallback> callbacks_to_field ) {
        TypeElement from_clazz = clazz;
        String from = from_clazz.toString() + "." + from_meth.toString();
        
        // modifies args for non local calls
        String[] words = name.split(" ");
        if ( words.length > 1 ) {
            Element e = gen.elems.getTypeElement( words[0] );
            if ( e != null && TypeElement.class.isInstance(e) ) {
                clazz = (TypeElement)e;
                name = words[1];
            }
            else {
                gen.util.error("err.staticjni.classnotfound", words[0], from );
            }
        }
        
        //Check for the signature
        String sig = null;
        if( name.contains("(") && name.contains(")") ) {
		String[] t = name.split("\\(");
		sig = "("+ t[1];
		name = t[0];
        }

        // try to find in methods
        List<ExecutableElement> methods = ElementFilter.methodsIn( clazz.getEnclosedElements() );
        for (ExecutableElement m: methods) {
            if ( name.toString().equals( m.getSimpleName().toString() ) ) {
		if ( sig == null ) {
			callbacks.add( new Callback(clazz, m));
			return;
		} else if( sig.equals( gen.signature(m) ) ) {
			callbacks.add( new Callback(clazz, m));
			return;
		}
            }
        }

        // try to find in fields
        List<VariableElement> fields = ElementFilter.fieldsIn( clazz.getEnclosedElements() );
        for ( VariableElement f: fields ) {
            if ( f.getSimpleName().toString().equals( name ) ) {
                callbacks_to_field.add( new FieldCallback(clazz, f) );
                return;
            }
        }
        
        gen.util.error("err.staticjni.methnotfound", name, from );
    }
    
    void tryToRegisterNativeNew(TypeElement clazz, ExecutableElement from_meth, 
            String name, Set<Callback> callbacks ) {
        TypeElement from_clazz = clazz;
        String from = from_clazz.toString() + "." + from_meth.toString();
        
        // find class
        String[] words = name.split(" ");
        //if ( words.length > 1 ) {
            Element e = gen.elems.getTypeElement( words[0] );
            if ( e != null && TypeElement.class.isInstance(e) ) {
                clazz = (TypeElement)e;
            }
            else {
                gen.util.error("err.staticjni.classnotfound", words[0], from );
            }
        //}
        
        //Check for the signature
       String sig = null;
        if( name.contains("(") && name.contains(")") ) {
		String[] t = name.split("\\(");
		sig = "("+ t[1];
		name = t[0];
        }

        // return the first constructor if no signature was specified, or the
        // constructor wich correspond to the signature
        List<ExecutableElement> consts = ElementFilter.constructorsIn( clazz.getEnclosedElements() );
        for (ExecutableElement c: consts) {
		if( sig == null) {
			callbacks.add( new Callback(clazz, c));
			return;
		} else if ( sig.equals( gen.signature(c) ) ) {
			callbacks.add( new Callback(clazz, c));
			return;
		}
        }
        
        gen.util.error("err.staticjni.methnotfound", name, from );
    }
    
    TypeMirror javaNameToType( String name ) {
    	TypeMirror t;
    	
        int p = name.lastIndexOf("[]");
        if ( p != -1 ) {
        	String sub_name = name.substring(0,p);
        	t = javaNameToType( sub_name );
        	return gen.types.getArrayType(t);
        }	
        
        if(name.equals("void")) return gen.types.getPrimitiveType( TypeKind.VOID );
        if(name.equals("boolean")) return gen.types.getPrimitiveType( TypeKind.INT );
        if(name.equals("byte")) return gen.types.getPrimitiveType( TypeKind.BYTE );
        if(name.equals("char")) return gen.types.getPrimitiveType( TypeKind.CHAR );
        if(name.equals("short")) return gen.types.getPrimitiveType( TypeKind.SHORT );
        if(name.equals("int")) return gen.types.getPrimitiveType( TypeKind.INT );
        if(name.equals("long")) return gen.types.getPrimitiveType( TypeKind.LONG );
        if(name.equals("float")) return gen.types.getPrimitiveType( TypeKind.FLOAT );
        if(name.equals("double")) return gen.types.getPrimitiveType( TypeKind.DOUBLE );
        
        return gen.elems.getTypeElement( name ).asType();
    }
}
