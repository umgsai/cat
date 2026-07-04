package com.dianping.cat.consumer.top.model.transform;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Machine;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;

public interface IParser<T> {
   TopReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   void parseForError(IMaker<T> maker, ILinker linker, Error parent, T node);

   void parseForMachine(IMaker<T> maker, ILinker linker, Machine parent, T node);

   void parseForSegment(IMaker<T> maker, ILinker linker, Segment parent, T node);
}
