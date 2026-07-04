package com.dianping.cat.alarm.sender;

public interface IEntity<T> {
   void accept(IVisitor visitor);

   void mergeAttributes(T other);

}
