package com.dianping.cat.home.server.transform;

import java.util.Stack;

import com.dianping.cat.home.server.IEntity;
import com.dianping.cat.home.server.IVisitor;
import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public class DefaultMerger implements IVisitor {

   private Stack<Object> m_objs = new Stack<Object>();

   private ServerMetricConfig m_serverMetricConfig;

   public DefaultMerger() {
   }

   public DefaultMerger(ServerMetricConfig serverMetricConfig) {
      m_serverMetricConfig = serverMetricConfig;
      m_objs.push(serverMetricConfig);
   }

   public ServerMetricConfig getServerMetricConfig() {
      return m_serverMetricConfig;
   }

   protected Stack<Object> getObjects() {
      return m_objs;
   }

   public <T> void merge(IEntity<T> to, IEntity<T> from) {
      m_objs.push(to);
      from.accept(this);
      m_objs.pop();
   }

   protected void mergeGroup(Group to, Group from) {
      to.mergeAttributes(from);
   }

   protected void mergeItem(Item to, Item from) {
      to.mergeAttributes(from);
   }

   protected void mergeSegment(Segment to, Segment from) {
      to.mergeAttributes(from);
   }

   protected void mergeServerMetricConfig(ServerMetricConfig to, ServerMetricConfig from) {
      to.mergeAttributes(from);
   }

   @Override
   public void visitGroup(Group from) {
      Group to = (Group) m_objs.peek();

      mergeGroup(to, from);
      visitGroupChildren(to, from);
   }

   protected void visitGroupChildren(Group to, Group from) {
      for (Item source : from.getItems().values()) {
         Item target = to.findItem(source.getId());

         if (target == null) {
            target = new Item(source.getId());
            to.addItem(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitItem(Item from) {
      Item to = (Item) m_objs.peek();

      mergeItem(to, from);
      visitItemChildren(to, from);
   }

   protected void visitItemChildren(Item to, Item from) {
      for (Segment source : from.getSegments().values()) {
         Segment target = to.findSegment(source.getId());

         if (target == null) {
            target = new Segment(source.getId());
            to.addSegment(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitSegment(Segment from) {
      Segment to = (Segment) m_objs.peek();

      mergeSegment(to, from);
      visitSegmentChildren(to, from);
   }

   protected void visitSegmentChildren(Segment to, Segment from) {
   }

   @Override
   public void visitServerMetricConfig(ServerMetricConfig from) {
      ServerMetricConfig to = (ServerMetricConfig) m_objs.peek();

      mergeServerMetricConfig(to, from);
      visitServerMetricConfigChildren(to, from);
   }

   protected void visitServerMetricConfigChildren(ServerMetricConfig to, ServerMetricConfig from) {
      for (Group source : from.getGroups().values()) {
         Group target = to.findGroup(source.getId());

         if (target == null) {
            target = new Group(source.getId());
            to.addGroup(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }
}
