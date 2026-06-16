package com.dianping.cat.home.service.client.transform;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.dianping.cat.home.service.client.IVisitor;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public class DefaultNativeBuilder implements IVisitor {

   private IVisitor m_visitor;

   private DataOutputStream m_out;

   public DefaultNativeBuilder(OutputStream out) {
      this(out, null);
   }

   public DefaultNativeBuilder(OutputStream out, IVisitor visitor) {
      m_out = new DataOutputStream(out);
      m_visitor = (visitor == null ? this : visitor);
   }

   public static byte[] build(ClientReport clientReport) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(8192);

      build(clientReport, out);
      return out.toByteArray();
   }

   public static void build(ClientReport clientReport, OutputStream out) {
      clientReport.accept(new DefaultNativeBuilder(out));
   }

   @Override
   public void visitClientReport(ClientReport clientReport) {
      writeTag(63, 0);

      if (clientReport.getDomain() != null) {
         writeTag(1, 1);
         writeString(clientReport.getDomain());
      }

      if (clientReport.getStartTime() != null) {
         writeTag(2, 1);
         writeDate(clientReport.getStartTime());
      }

      if (clientReport.getEndTime() != null) {
         writeTag(3, 1);
         writeDate(clientReport.getEndTime());
      }

      if (!clientReport.getDomains().isEmpty()) {
         writeTag(33, 2);
         writeInt(clientReport.getDomains().size());

         for (Domain domain : clientReport.getDomains().values()) {
            domain.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitDomain(Domain domain) {
      if (domain.getId() != null) {
         writeTag(1, 1);
         writeString(domain.getId());
      }

      writeTag(2, 0);
      writeLong(domain.getTotalCount());

      writeTag(3, 0);
      writeLong(domain.getFailureCount());

      writeTag(4, 0);
      writeDouble(domain.getFailurePercent());

      writeTag(5, 0);
      writeDouble(domain.getSum());

      writeTag(6, 0);
      writeDouble(domain.getAvg());

      if (!domain.getMethods().isEmpty()) {
         writeTag(33, 2);
         writeInt(domain.getMethods().size());

         for (Method method : domain.getMethods().values()) {
            method.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitMethod(Method method) {
      if (method.getId() != null) {
         writeTag(1, 1);
         writeString(method.getId());
      }

      if (method.getService() != null) {
         writeTag(2, 1);
         writeString(method.getService());
      }

      writeTag(3, 0);
      writeLong(method.getTotalCount());

      writeTag(4, 0);
      writeLong(method.getFailureCount());

      writeTag(5, 0);
      writeDouble(method.getFailurePercent());

      writeTag(6, 0);
      writeDouble(method.getSum());

      writeTag(7, 0);
      writeDouble(method.getAvg());

      writeTag(8, 0);
      writeDouble(method.getQps());

      writeTag(9, 0);
      writeDouble(method.getTimeout());

      writeTag(63, 3);
   }

   private void writeDate(java.util.Date value) {
      try {
         writeVarint(value.getTime());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeDouble(double value) {
      try {
         writeVarint(Double.doubleToLongBits(value));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeInt(int value) {
      try {
         writeVarint(value & 0xFFFFFFFFL);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeLong(long value) {
      try {
         writeVarint(value);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeString(String value) {
      try {
         m_out.writeUTF(value);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeTag(int field, int type) {
      try {
         m_out.writeByte((field << 2) + type);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected void writeVarint(long value) throws IOException {
      while (true) {
         if ((value & ~0x7FL) == 0) {
            m_out.writeByte((byte) value);
            return;
         } else {
            m_out.writeByte(((byte) value & 0x7F) | 0x80);
            value >>>= 7;
         }
      }
   }
}
