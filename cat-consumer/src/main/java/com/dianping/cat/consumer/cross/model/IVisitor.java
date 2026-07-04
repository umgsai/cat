package com.dianping.cat.consumer.cross.model;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;

public interface IVisitor {

   void visitCrossReport(CrossReport crossReport);

   void visitLocal(Local local);

   void visitName(Name name);

   void visitRemote(Remote remote);

   void visitType(Type type);
}
