package com.dianping.cat.home.service.client.transform;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.dianping.cat.home.service.client.IVisitor;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public class DefaultNativeParser implements IVisitor {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DataInputStream m_in;

   public DefaultNativeParser(InputStream in) {
      m_in = new DataInputStream(in);
   }

   public static ClientReport parse(byte[] data) {
      return parse(new ByteArrayInputStream(data));
   }

   public static ClientReport parse(InputStream in) {
      DefaultNativeParser parser = new DefaultNativeParser(in);
      ClientReport clientReport = new ClientReport();

      try {
         clientReport.accept(parser);
      } catch (RuntimeException e) {
         if (e.getCause() !=null && e.getCause() instanceof java.io.EOFException) {
            // ignore it
         } else {
            throw e;
         }
      }
      
      parser.m_linker.finish();
      return clientReport;
   }

   @Override
   public void visitClientReport(ClientReport clientReport) {
      byte tag;

      if ((tag = readTag()) != -4) {
         throw new RuntimeException(String.format("Malformed payload, expected: %s, but was: %s!", -4, tag));
      }

      while ((tag = readTag()) != -1) {
         visitClientReportChildren(clientReport, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitClientReportChildren(ClientReport clientReport, int _field, int _type) {
      switch (_field) {
         case 1:
            clientReport.setDomain(readString());
            break;
         case 2:
            clientReport.setStartTime(readDate());
            break;
         case 3:
            clientReport.setEndTime(readDate());
            break;
         case 33:
            if (_type == 1) { 
              Domain domain = new Domain();

              visitDomain(domain);
              m_linker.onDomain(clientReport, domain);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Domain domain = new Domain();

                 visitDomain(domain);
                 m_linker.onDomain(clientReport, domain);
               }
            }
            break;
      }
   }

   @Override
   public void visitDomain(Domain domain) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitDomainChildren(domain, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitDomainChildren(Domain domain, int _field, int _type) {
      switch (_field) {
         case 1:
            domain.setId(readString());
            break;
         case 2:
            domain.setTotalCount(readLong());
            break;
         case 3:
            domain.setFailureCount(readLong());
            break;
         case 4:
            domain.setFailurePercent(readDouble());
            break;
         case 5:
            domain.setSum(readDouble());
            break;
         case 6:
            domain.setAvg(readDouble());
            break;
         case 33:
            if (_type == 1) { 
              Method method = new Method();

              visitMethod(method);
              m_linker.onMethod(domain, method);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Method method = new Method();

                 visitMethod(method);
                 m_linker.onMethod(domain, method);
               }
            }
            break;
      }
   }

   @Override
   public void visitMethod(Method method) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitMethodChildren(method, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitMethodChildren(Method method, int _field, int _type) {
      switch (_field) {
         case 1:
            method.setId(readString());
            break;
         case 2:
            method.setService(readString());
            break;
         case 3:
            method.setTotalCount(readLong());
            break;
         case 4:
            method.setFailureCount(readLong());
            break;
         case 5:
            method.setFailurePercent(readDouble());
            break;
         case 6:
            method.setSum(readDouble());
            break;
         case 7:
            method.setAvg(readDouble());
            break;
         case 8:
            method.setQps(readDouble());
            break;
         case 9:
            method.setTimeout(readDouble());
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

   private long readLong() {
      try {
         return readVarint(64);
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
