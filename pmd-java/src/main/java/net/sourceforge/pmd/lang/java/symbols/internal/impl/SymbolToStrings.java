/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.impl;

import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JConstructorSymbol;
import net.sourceforge.pmd.lang.java.symbols.JElementSymbol;
import net.sourceforge.pmd.lang.java.symbols.JFieldSymbol;
import net.sourceforge.pmd.lang.java.symbols.JFormalParamSymbol;
import net.sourceforge.pmd.lang.java.symbols.JLocalVariableSymbol;
import net.sourceforge.pmd.lang.java.symbols.JMethodSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeDeclSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeParameterSymbol;
import net.sourceforge.pmd.lang.java.symbols.SymbolVisitor;

public class SymbolToStrings {

    public static final SymbolToStrings SHARED = new SymbolToStrings("");
    public static final SymbolToStrings FAKE = new SymbolToStrings("fake");
    public static final SymbolToStrings REFLECT = new SymbolToStrings("reflect");
    public static final SymbolToStrings AST = new SymbolToStrings("ast");

    private final ToStringVisitor visitor;

    public SymbolToStrings(String impl) {
        this.visitor = new ToStringVisitor(impl);
    }

    public String toString(JElementSymbol symbol) {
        return symbol.acceptVisitor(visitor, new StringBuilder()).toString();
    }

    private static class ToStringVisitor implements SymbolVisitor<StringBuilder, StringBuilder> {

        private final String impl;

        private ToStringVisitor(String impl) {
            this.impl = impl;
        }

        private StringBuilder withImpl(StringBuilder builder, String kind, Object first, Object... rest) {
            if (!impl.isEmpty()) {
                builder.append(impl).append(':');
            }
            builder.append(kind).append('(').append(first);
            for (Object s : rest) {
                builder.append(", ").append(s);
            }
            return builder.append(')');
        }

        @Override
        public StringBuilder visitSymbol(JElementSymbol sym, StringBuilder builder) {
            throw new IllegalStateException("Unknown symbol " + sym.getClass());
        }

        @Override
        public StringBuilder visitClass(JClassSymbol sym, StringBuilder param) {
            String kind;
            if (sym.isUnresolved()) {
                kind = "unresolved";
            } else if (sym.isEnum()) {
                kind = "enum";
            } else if (sym.isAnnotation()) {
                kind = "annot";
            } else {
                kind = "class";
            }

            return withImpl(param, kind, sym.getBinaryName());
        }

        @Override
        public StringBuilder visitArray(JClassSymbol sym, JTypeDeclSymbol component, StringBuilder param) {
            param.append("array(");
            return component.acceptVisitor(this, param).append(")");
        }

        @Override
        public StringBuilder visitTypeParam(JTypeParameterSymbol sym, StringBuilder param) {
            return withImpl(param, "tparam", sym.getSimpleName(), sym.getDeclaringSymbol());
        }

        @Override
        public StringBuilder visitCtor(JConstructorSymbol sym, StringBuilder param) {
            return withImpl(param, "ctor", sym.getEnclosingClass());
        }

        @Override
        public StringBuilder visitMethod(JMethodSymbol sym, StringBuilder param) {
            return withImpl(param, "method", sym.getSimpleName(), sym.getEnclosingClass());
        }

        @Override
        public StringBuilder visitField(JFieldSymbol sym, StringBuilder param) {
            return withImpl(param, "field", sym.getSimpleName(), sym.getEnclosingClass());
        }

        @Override
        public StringBuilder visitLocal(JLocalVariableSymbol sym, StringBuilder param) {
            return withImpl(param, "local", sym.getSimpleName());
        }

        @Override
        public StringBuilder visitFormal(JFormalParamSymbol sym, StringBuilder param) {
            return withImpl(param, "formal", sym.getSimpleName());
        }
    }

}