package com.lessmarkup.interfaces.cache

trait CacheHandlerFactory {
  def createHandler(id: Long): CacheHandler
}
