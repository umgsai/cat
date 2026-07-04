package com.dianping.cat.home.service;

public interface IEntity<T> {
   void accept(IVisitor visitor);

   void mergeAttributes(T other);

}
