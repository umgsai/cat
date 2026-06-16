package com.dianping.cat.configuration.server.transform;

import java.util.Stack;

import com.dianping.cat.configuration.server.IEntity;
import com.dianping.cat.configuration.server.IVisitor;
import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HarfsConfig;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;

public class DefaultMerger implements IVisitor {

   private Stack<Object> m_objs = new Stack<Object>();

   private ServerConfig m_serverConfig;

   public DefaultMerger() {
   }

   public DefaultMerger(ServerConfig serverConfig) {
      m_serverConfig = serverConfig;
      m_objs.push(serverConfig);
   }

   public ServerConfig getServerConfig() {
      return m_serverConfig;
   }

   protected Stack<Object> getObjects() {
      return m_objs;
   }

   public <T> void merge(IEntity<T> to, IEntity<T> from) {
      m_objs.push(to);
      from.accept(this);
      m_objs.pop();
   }

   protected void mergeConsumer(ConsumerConfig to, ConsumerConfig from) {
      to.mergeAttributes(from);
   }

   protected void mergeDomain(Domain to, Domain from) {
      to.mergeAttributes(from);
   }

   protected void mergeHarfs(HarfsConfig to, HarfsConfig from) {
      to.mergeAttributes(from);
   }

   protected void mergeHdfs(HdfsConfig to, HdfsConfig from) {
      to.mergeAttributes(from);
   }

   protected void mergeLongConfig(LongConfig to, LongConfig from) {
      to.mergeAttributes(from);
   }

   protected void mergeProperty(Property to, Property from) {
      to.mergeAttributes(from);
   }

   protected void mergeServer(Server to, Server from) {
      to.mergeAttributes(from);
   }

   protected void mergeServerConfig(ServerConfig to, ServerConfig from) {
      to.mergeAttributes(from);
   }

   protected void mergeStorage(StorageConfig to, StorageConfig from) {
      to.mergeAttributes(from);
   }

   @Override
   public void visitConsumer(ConsumerConfig from) {
      ConsumerConfig to = (ConsumerConfig) m_objs.peek();

      mergeConsumer(to, from);
      visitConsumerChildren(to, from);
   }

   protected void visitConsumerChildren(ConsumerConfig to, ConsumerConfig from) {
      if (from.getLongConfig() != null) {
         LongConfig target = to.getLongConfig();

         if (target == null) {
            target = new LongConfig();
            to.setLongConfig(target);
         }

         m_objs.push(target);
         from.getLongConfig().accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitDomain(Domain from) {
      Domain to = (Domain) m_objs.peek();

      mergeDomain(to, from);
      visitDomainChildren(to, from);
   }

   protected void visitDomainChildren(Domain to, Domain from) {
   }

   @Override
   public void visitHarfs(HarfsConfig from) {
      HarfsConfig to = (HarfsConfig) m_objs.peek();

      mergeHarfs(to, from);
      visitHarfsChildren(to, from);
   }

   protected void visitHarfsChildren(HarfsConfig to, HarfsConfig from) {
   }

   @Override
   public void visitHdfs(HdfsConfig from) {
      HdfsConfig to = (HdfsConfig) m_objs.peek();

      mergeHdfs(to, from);
      visitHdfsChildren(to, from);
   }

   protected void visitHdfsChildren(HdfsConfig to, HdfsConfig from) {
   }

   @Override
   public void visitLongConfig(LongConfig from) {
      LongConfig to = (LongConfig) m_objs.peek();

      mergeLongConfig(to, from);
      visitLongConfigChildren(to, from);
   }

   protected void visitLongConfigChildren(LongConfig to, LongConfig from) {
      for (Domain source : from.getDomains().values()) {
         Domain target = to.findDomain(source.getName());

         if (target == null) {
            target = new Domain(source.getName());
            to.addDomain(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitProperty(Property from) {
      Property to = (Property) m_objs.peek();

      mergeProperty(to, from);
      visitPropertyChildren(to, from);
   }

   protected void visitPropertyChildren(Property to, Property from) {
   }

   @Override
   public void visitServer(Server from) {
      Server to = (Server) m_objs.peek();

      mergeServer(to, from);
      visitServerChildren(to, from);
   }

   protected void visitServerChildren(Server to, Server from) {
      for (Property source : from.getProperties().values()) {
         Property target = to.findProperty(source.getName());

         if (target == null) {
            target = new Property(source.getName());
            to.addProperty(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      if (from.getStorage() != null) {
         StorageConfig target = to.getStorage();

         if (target == null) {
            target = new StorageConfig();
            to.setStorage(target);
         }

         m_objs.push(target);
         from.getStorage().accept(this);
         m_objs.pop();
      }

      if (from.getConsumer() != null) {
         ConsumerConfig target = to.getConsumer();

         if (target == null) {
            target = new ConsumerConfig();
            to.setConsumer(target);
         }

         m_objs.push(target);
         from.getConsumer().accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitServerConfig(ServerConfig from) {
      ServerConfig to = (ServerConfig) m_objs.peek();

      mergeServerConfig(to, from);
      visitServerConfigChildren(to, from);
   }

   protected void visitServerConfigChildren(ServerConfig to, ServerConfig from) {
      for (Server source : from.getServers().values()) {
         Server target = to.findServer(source.getId());

         if (target == null) {
            target = new Server(source.getId());
            to.addServer(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitStorage(StorageConfig from) {
      StorageConfig to = (StorageConfig) m_objs.peek();

      mergeStorage(to, from);
      visitStorageChildren(to, from);
   }

   protected void visitStorageChildren(StorageConfig to, StorageConfig from) {
      for (HdfsConfig source : from.getHdfses().values()) {
         HdfsConfig target = to.findHdfs(source.getId());

         if (target == null) {
            target = new HdfsConfig(source.getId());
            to.addHdfs(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (HarfsConfig source : from.getHarfses().values()) {
         HarfsConfig target = to.findHarfs(source.getId());

         if (target == null) {
            target = new HarfsConfig(source.getId());
            to.addHarfs(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (Property source : from.getProperties().values()) {
         Property target = to.findProperty(source.getName());

         if (target == null) {
            target = new Property(source.getName());
            to.addProperty(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }
}
