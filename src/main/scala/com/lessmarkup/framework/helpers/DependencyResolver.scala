/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.helpers

import java.lang.annotation.Annotation

import com.google.inject.{AbstractModule, Guice, Injector}
import com.lessmarkup.interfaces.annotations.{UseInstanceFactory, Implements}
import com.lessmarkup.interfaces.cache.InstanceFactory
import com.lessmarkup.interfaces.exceptions.CommonException

object DependencyResolver {

  var injector: Injector = Guice.createInjector()

  def apply[T] (typeToResolve: Class[T], params: Any*): T = {

    if (params.nonEmpty) {
      val implements = typeToResolve.getAnnotation(classOf[Implements])

      val actualType = if (implements != null) implements.value() else typeToResolve
      val factoryAnnotation = actualType.getAnnotation(classOf[UseInstanceFactory])

      if (factoryAnnotation != null) {
        val factory: InstanceFactory = injector.getInstance(factoryAnnotation.value)
        return factory.createInstance(params: _*).asInstanceOf
      } else {
        throw new CommonException("Cannot find instance factory")
      }
    }

    injector.getInstance(typeToResolve)
  }

  private def getAnnotation[T, TA <: Annotation](f: Class[T], a: Class[TA]) = {
    Option[TA] (f.getAnnotation(a))
  }

  private class Module(classes: List[Class[_]]) extends AbstractModule {
    private def setOverride[T](abstractType: Class[T], concreteType: Class[_ <: T]): Unit = {
      bind(abstractType).to(concreteType)
    }

    override def configure() = {
      for (
        concreteType <- classes;
        implements = getAnnotation(concreteType, classOf[Implements]);
        useInstanceFactory = getAnnotation(concreteType, classOf[UseInstanceFactory])
        if implements.isDefined && useInstanceFactory.isEmpty;
        abstractType = implements.get.value()
        if abstractType.isAssignableFrom(concreteType)
      ) {
        setOverride(abstractType, concreteType.asSubclass(abstractType))
      }
    }
  }

  def reset(classes: List[Class[_]]): Unit = {
    injector = Guice.createInjector(new Module(classes))
  }

  def add(classes: List[Class[_]]): Unit = {
    injector = injector.createChildInjector(new Module(classes))
  }
}
