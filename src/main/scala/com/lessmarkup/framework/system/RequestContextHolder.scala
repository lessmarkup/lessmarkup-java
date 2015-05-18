import com.lessmarkup.interfaces.system.RequestContext

package com.lessmarkup.framework.system {
  object RequestContextHolder {

    private val contextHolder = new ThreadLocal[RequestContext]

    def onRequestStarted(context: RequestContext): Unit = {
      contextHolder.set(context)
    }

    def onRequestFinished(): Unit = {
      contextHolder.remove()
    }

    def getContext: RequestContext = {
      contextHolder.get()
    }
  }
}
