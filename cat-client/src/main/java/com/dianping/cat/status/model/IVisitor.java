package com.dianping.cat.status.model;

import com.dianping.cat.status.model.entity.DiskInfo;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.ExtensionDetail;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.OsInfo;
import com.dianping.cat.status.model.entity.RuntimeInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;

public interface IVisitor {

   void visitDisk(DiskInfo disk);

   void visitDiskVolume(DiskVolumeInfo diskVolume);

   void visitExtension(Extension extension);

   void visitExtensionDetail(ExtensionDetail extensionDetail);

   void visitGc(GcInfo gc);

   void visitMemory(MemoryInfo memory);

   void visitMessage(MessageInfo message);

   void visitOs(OsInfo os);

   void visitRuntime(RuntimeInfo runtime);

   void visitStatus(StatusInfo status);

   void visitThread(ThreadsInfo thread);
}
