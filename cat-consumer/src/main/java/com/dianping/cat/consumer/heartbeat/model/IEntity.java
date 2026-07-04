package com.dianping.cat.consumer.heartbeat.model;

public interface IEntity<T> {
   void accept(IVisitor visitor);

   void mergeAttributes(T other);

}
