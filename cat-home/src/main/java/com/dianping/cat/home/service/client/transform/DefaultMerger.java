package com.dianping.cat.home.service.client.transform;

import java.util.Stack;

import com.dianping.cat.home.service.client.IEntity;
import com.dianping.cat.home.service.client.IVisitor;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public class DefaultMerger implements IVisitor {

   private Stack<Object> m_objs = new Stack<Object>();

   private ClientReport m_clientReport;

   public DefaultMerger() {
   }

   public DefaultMerger(ClientReport clientReport) {
      m_clientReport = clientReport;
      m_objs.push(clientReport);
   }

   public ClientReport getClientReport() {
      return m_clientReport;
   }

   protected Stack<Object> getObjects() {
      return m_objs;
   }

   public <T> void merge(IEntity<T> to, IEntity<T> from) {
      m_objs.push(to);
      from.accept(this);
      m_objs.pop();
   }

   protected void mergeClientReport(ClientReport to, ClientReport from) {
      to.mergeAttributes(from);
   }

   protected void mergeDomain(Domain to, Domain from) {
      to.mergeAttributes(from);
   }

   protected void mergeMethod(Method to, Method from) {
      to.mergeAttributes(from);
   }

   @Override
   public void visitClientReport(ClientReport from) {
      ClientReport to = (ClientReport) m_objs.peek();

      mergeClientReport(to, from);
      visitClientReportChildren(to, from);
   }

   protected void visitClientReportChildren(ClientReport to, ClientReport from) {
      for (Domain source : from.getDomains().values()) {
         Domain target = to.findDomain(source.getId());

         if (target == null) {
            target = new Domain(source.getId());
            to.addDomain(target);
         }

         m_objs.push(target);
         source.accept(this);
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
      for (Method source : from.getMethods().values()) {
         Method target = to.findMethod(source.getId());

         if (target == null) {
            target = new Method(source.getId());
            to.addMethod(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitMethod(Method from) {
      Method to = (Method) m_objs.peek();

      mergeMethod(to, from);
      visitMethodChildren(to, from);
   }

   protected void visitMethodChildren(Method to, Method from) {
   }
}
