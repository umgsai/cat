package com.dianping.cat.consumer.business.model.transform;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.dianping.cat.consumer.business.model.IVisitor;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

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

   public static byte[] build(BusinessReport businessReport) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(8192);

      build(businessReport, out);
      return out.toByteArray();
   }

   public static void build(BusinessReport businessReport, OutputStream out) {
      businessReport.accept(new DefaultNativeBuilder(out));
   }

   @Override
   public void visitBusinessItem(BusinessItem businessItem) {
      if (businessItem.getId() != null) {
         writeTag(1, 1);
         writeString(businessItem.getId());
      }

      if (businessItem.getType() != null) {
         writeTag(2, 1);
         writeString(businessItem.getType());
      }

      if (!businessItem.getSegments().isEmpty()) {
         writeTag(33, 2);
         writeInt(businessItem.getSegments().size());

         for (Segment segment : businessItem.getSegments().values()) {
            segment.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitBusinessReport(BusinessReport businessReport) {
      writeTag(63, 0);

      if (businessReport.getDomain() != null) {
         writeTag(1, 1);
         writeString(businessReport.getDomain());
      }

      if (businessReport.getStartTime() != null) {
         writeTag(2, 1);
         writeDate(businessReport.getStartTime());
      }

      if (businessReport.getEndTime() != null) {
         writeTag(3, 1);
         writeDate(businessReport.getEndTime());
      }

      if (!businessReport.getBusinessItems().isEmpty()) {
         writeTag(33, 2);
         writeInt(businessReport.getBusinessItems().size());

         for (BusinessItem businessItem : businessReport.getBusinessItems().values()) {
            businessItem.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitSegment(Segment segment) {
      if (segment.getId() != null) {
         writeTag(1, 1);
         writeInt(segment.getId());
      }

      writeTag(2, 0);
      writeInt(segment.getCount());

      writeTag(3, 0);
      writeDouble(segment.getSum());

      writeTag(4, 0);
      writeDouble(segment.getAvg());

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
