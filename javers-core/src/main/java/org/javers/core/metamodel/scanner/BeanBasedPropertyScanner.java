package org.javers.core.metamodel.scanner;

import org.javers.common.reflection.JaversMethod;
import org.javers.common.reflection.ReflectionUtil;
import org.javers.core.metamodel.annotation.PropertyName;
import org.javers.core.metamodel.property.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pawel szymczyk
 */
class BeanBasedPropertyScanner extends PropertyScanner {

    private final AnnotationNamesProvider annotationNamesProvider;

    BeanBasedPropertyScanner(AnnotationNamesProvider annotationNamesProvider) {
        this.annotationNamesProvider = annotationNamesProvider;
    }

    @Override
    public PropertyScan scan(Class<?> managedClass, boolean ignoreDeclaredProperties) {
        List<JaversMethod> getters = ReflectionUtil.findAllPersistentGetters(managedClass);
        List<Property> beanProperties = new ArrayList<>();

        for (JaversMethod getter : getters) {
            boolean isIgnoredInType = ignoreDeclaredProperties && getter.getDeclaringClass().equals(managedClass);
            boolean hasTransientAnn = getter.hasAnyAnnotation(annotationNamesProvider.getTransientAliases());
            boolean hasShallowReferenceAnn = getter.hasAnyAnnotation(annotationNamesProvider.getShallowReferenceAliases());
            if(getter.hasAnyAnnotation(annotationNamesProvider.getPropertyNameAliases())){
                String customPropertyName = getter.getRawMember().getAnnotation(PropertyName.class).value();
                beanProperties.add(new Property(getter, hasTransientAnn || isIgnoredInType, hasShallowReferenceAnn, customPropertyName));
            } else {
                beanProperties.add(new Property(getter, hasTransientAnn || isIgnoredInType, hasShallowReferenceAnn, getter.propertyName()));
            }

        }
        return new PropertyScan(beanProperties);
    }
}
