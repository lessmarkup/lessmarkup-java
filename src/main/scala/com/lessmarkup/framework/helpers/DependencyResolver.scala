package com.lessmarkup.framework.helpers

import java.lang.annotation.Annotation

import com.google.inject.{AbstractModule, Guice, Injector}
import com.lessmarkup.interfaces.module.Implements

object DependencyResolver {

  var injector: Injector = Guice.createInjector()

  def resolve[T] (typeToResolve: Class[T]): T = {
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
