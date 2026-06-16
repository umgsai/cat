package com.dianping.cat.consumer.business.model.transform;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.dianping.cat.consumer.business.model.IVisitor;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public class DefaultNativeParser implements IVisitor {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DataInputStream m_in;

   public DefaultNativeParser(InputStream in) {
      m_in = new DataInputStream(in);
   }

   public static BusinessReport parse(byte[] data) {
      return parse(new ByteArrayInputStream(data));
   }

   public static BusinessReport parse(InputStream in) {
      DefaultNativeParser parser = new DefaultNativeParser(in);
      BusinessReport businessReport = new BusinessReport();

      try {
         businessReport.accept(parser);
      } catch (RuntimeException e) {
         if (e.getCause() !=null && e.getCause() instanceof java.io.EOFException) {
            // ignore it
         } else {
            throw e;
         }
      }
      
      parser.m_linker.finish();
      return businessReport;
   }

   @Override
   public void visitBusinessItem(BusinessItem businessItem) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitBusinessItemChildren(businessItem, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitBusinessItemChildren(BusinessItem businessItem, int _field, int _type) {
      switch (_field) {
         case 1:
            businessItem.setId(readString());
            break;
         case 2:
            businessItem.setType(readString());
            break;
         case 33:
            if (_type == 1) { 
              Segment segment = new Segment();

              visitSegment(segment);
              m_linker.onSegment(businessItem, segment);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Segment segment = new Segment();

                 visitSegment(segment);
                 m_linker.onSegment(businessItem, segment);
               }
            }
            break;
      }
   }

   @Override
   public void visitBusinessReport(BusinessReport businessReport) {
      byte tag;

      if ((tag = readTag()) != -4) {
         throw new RuntimeException(String.format("Malformed payload, expected: %s, but was: %s!", -4, tag));
      }

      while ((tag = readTag()) != -1) {
         visitBusinessReportChildren(businessReport, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitBusinessReportChildren(BusinessReport businessReport, int _field, int _type) {
      switch (_field) {
         case 1:
            businessReport.setDomain(readString());
            break;
         case 2:
            businessReport.setStartTime(readDate());
            break;
         case 3:
            businessReport.setEndTime(readDate());
            break;
         case 33:
            if (_type == 1) { 
              BusinessItem businessItem = new BusinessItem();

              visitBusinessItem(businessItem);
              m_linker.onBusinessItem(businessReport, businessItem);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 BusinessItem businessItem = new BusinessItem();

                 visitBusinessItem(businessItem);
                 m_linker.onBusinessItem(businessReport, businessItem);
               }
            }
            break;
      }
   }

   @Override
   public void visitSegment(Segment segment) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitSegmentChildren(segment, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitSegmentChildren(Segment segment, int _field, int _type) {
      switch (_field) {
         case 1:
            segment.setId(readInt());
            break;
         case 2:
            segment.setCount(readInt());
            break;
         case 3:
            segment.setSum(readDouble());
            break;
         case 4:
            segment.setAvg(readDouble());
            break;
      }
   }

   private java.util.Date readDate() {
      try {
         return new java.util.Date(readVarint(64));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private double readDouble() {
      try {
         return Double.longBitsToDouble(readVarint(64));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private int readInt() {
      try {
         return (int) readVarint(32);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private String readString() {
      try {
         return m_in.readUTF();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private byte readTag() {
      try {
         return m_in.readByte();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected long readVarint(final int length) throws IOException {
      int shift = 0;
      long result = 0;

      while (shift < length) {
         final byte b = m_in.readByte();
         result |= (long) (b & 0x7F) << shift;
         if ((b & 0x80) == 0) {
            return result;
         }
         shift += 7;
      }

      throw new RuntimeException("Malformed variable int " + length + "!");
   }
}
