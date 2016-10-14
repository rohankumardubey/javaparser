package me.tomassetti.symbolsolver.model.declarations;

import me.tomassetti.symbolsolver.model.usages.MethodUsage;
import me.tomassetti.symbolsolver.model.resolution.SymbolReference;
import me.tomassetti.symbolsolver.model.resolution.TypeSolver;
import me.tomassetti.symbolsolver.model.usages.typesystem.ReferenceType;
import me.tomassetti.symbolsolver.model.usages.typesystem.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A declaration of a type. It could be a primitive type, an enum, a class, an interface or a type variable.
 * It cannot be an annotation or an array.
 *
 * @author Federico Tomassetti
 */
public interface TypeDeclaration extends Declaration, TypeParametrizable {

    String getQualifiedName();

    SymbolReference<MethodDeclaration> solveMethod(String name, List<Type> parameterTypes);

    boolean isAssignableBy(Type type);

    default boolean canBeAssignedTo(TypeDeclaration other) {
        return other.isAssignableBy(this);
    }

    /**
     * Note that the type of the field should be expressed using the type variables of this particular type.
     * Consider for example:
     *
     * class Foo<E> { E field; }
     *
     * class Bar extends Foo<String> { }
     *
     * When calling getField("field") on Foo I should get a FieldDeclaration with type E, while calling it on
     * Bar I should get a FieldDeclaration with type String.
     */
    FieldDeclaration getField(String name);

    boolean hasField(String name);
    
    List<FieldDeclaration> getAllFields();

    boolean isAssignableBy(TypeDeclaration other);

    SymbolReference<? extends ValueDeclaration> solveSymbol(String name, TypeSolver typeSolver);

    /**
     * Try to solve a symbol just in the declaration, it does not delegate to the container.
     *
     * @param name
     * @param typeSolver
     * @return
     */
    SymbolReference<TypeDeclaration> solveType(String name, TypeSolver typeSolver);

    List<ReferenceType> getAncestors();
    
    default List<ReferenceType> getAllAncestors() {
    	List<ReferenceType> ancestors = new ArrayList<>();
    	for (ReferenceType ancestor : getAncestors()) {
    		ancestors.add(ancestor);
    		ancestors.addAll(ancestor.getAllAncestors());
    	}
    	return ancestors;
    }

    Set<MethodDeclaration> getDeclaredMethods();

    Set<MethodUsage> getAllMethods();

    default boolean isClass() {
        return false;
    }

    default boolean isInterface() {
        return false;
    }

    default boolean isEnum() {
        return false;
    }

    default boolean isTypeVariable() {
        return false;
    }

    @Override
    default boolean isType() {
        return true;
    }

    @Override
    default TypeDeclaration asType() {
        return this;
    }

    default ClassDeclaration asClass() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    default InterfaceDeclaration asInterface() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    boolean hasDirectlyAnnotation(String canonicalName);

    default boolean hasAnnotation(String canonicalName) {
        if (hasDirectlyAnnotation(canonicalName)) {
            return true;
        }
        return getAllAncestors().stream().anyMatch(it -> it.asReferenceTypeUsage().getTypeDeclaration().hasDirectlyAnnotation(canonicalName));
    }
}
