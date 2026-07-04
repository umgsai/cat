package com.dianping.cat.home.business;

public interface IEntity<T> {
   void accept(IVisitor visitor);

   void mergeAttributes(T other);

}
