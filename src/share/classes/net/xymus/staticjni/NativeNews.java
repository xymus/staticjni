package net.xymus.staticjni;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface NativeNews {
    public String[] value();

}
