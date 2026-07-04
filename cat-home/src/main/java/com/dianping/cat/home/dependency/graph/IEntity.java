package com.dianping.cat.home.dependency.graph;

public interface IEntity<T> {
   void accept(IVisitor visitor);

   void mergeAttributes(T other);

}
