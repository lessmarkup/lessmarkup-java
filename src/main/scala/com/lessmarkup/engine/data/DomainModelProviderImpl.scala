package com.lessmarkup.engine.data

import java.util.OptionalInt

import com.google.inject.Inject
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.module.ModuleProvider

class DomainModelProviderImpl @Inject() (moduleProvider: ModuleProvider) extends DomainModelProvider {

  def initialize() {
    import scala.collection.JavaConversions._
    for (module <- moduleProvider.getModules) {
      import scala.collection.JavaConversions._
      for (dataObject <- module.getInitializer.getDataObjectTypes) {
        MetadataStorage.registerDataType(dataObject)
      }
    }
  }

  def create: DomainModel = {
    new DomainModelImpl(scala.Option.empty, false)
  }

  def create(connectionString: String): DomainModel = {
    new DomainModelImpl(scala.Option.apply(connectionString), false)
  }

  def createWithTransaction: DomainModel = {
    new DomainModelImpl(scala.Option.empty, true)
  }

  def getCollectionId(collectionType: Class[_]): OptionalInt = {
    val ret: Option[Int] = MetadataStorage.getCollectionId(collectionType)
    if (ret.isDefined) OptionalInt.of(ret.get) else OptionalInt.empty
  }
}
