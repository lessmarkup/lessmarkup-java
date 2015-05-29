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

  def resolve[T] (typeToResolve: Class[T], params: Any*): T = {

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

  def loadClasses(classes: List[Class[_]]): Unit = {
    injector = injector.createChildInjector(new AbstractModule {

      private def setOverride[T](abstractType: Class[T], concreteType: Class[_ <: T]): Unit = {
        bind(abstractType).to(concreteType)
      }

      override def configure() = {
        classes
          .map(concreteType => (concreteType, getAnnotation(concreteType, classOf[Implements])))
          .filter(_._2.isDefined)
          .map { case(concreteType, annotation) => (concreteType, annotation.get.value()) }
          .foreach {
          case (concreteType, abstractType) => setOverride(abstractType, concreteType.asSubclass(abstractType))
        }
      }
    })
  }
}
