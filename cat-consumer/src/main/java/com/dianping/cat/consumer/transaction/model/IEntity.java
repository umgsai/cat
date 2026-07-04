package com.dianping.cat.consumer.transaction.model;

public interface IEntity<T> {
   void accept(IVisitor visitor);

   void mergeAttributes(T other);

}
