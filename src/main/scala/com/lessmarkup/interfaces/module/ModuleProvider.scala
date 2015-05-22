package com.lessmarkup.interfaces.module

import java.util.Collection

import com.lessmarkup.interfaces.data.DomainModelProvider
import com.lessmarkup.interfaces.structure.{NodeHandler, Tuple}

trait ModuleProvider {
  def getModules: Collection[ModuleConfiguration]

  def updateModuleDatabase(domainModelProvider: DomainModelProvider)

  def getNodeHandlers: Collection[String]

  def getNodeHandler(id: String): Tuple[Class[_ <: NodeHandler], String]
}