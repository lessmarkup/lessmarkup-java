package com.lessmarkup.engine.testutilities

import com.lessmarkup.engine.data.ChangeTrackerImpl
import com.lessmarkup.interfaces.data.{ChangeListener, DomainModelProvider}

class TestChangeTrackerImpl(domainModelProvider: DomainModelProvider) extends ChangeTrackerImpl(domainModelProvider) {
  override def subscribe(listener: ChangeListener) {
  }

  override def unsubscribe(listener: ChangeListener) {
  }
}
