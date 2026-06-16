package com.dianping.cat.consumer.problem.model.transform;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.dianping.cat.consumer.problem.model.IVisitor;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;

public class DefaultNativeParser implements IVisitor {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DataInputStream m_in;

   public DefaultNativeParser(InputStream in) {
      m_in = new DataInputStream(in);
   }

   public static ProblemReport parse(byte[] data) {
      return parse(new ByteArrayInputStream(data));
   }

   public static ProblemReport parse(InputStream in) {
      DefaultNativeParser parser = new DefaultNativeParser(in);
      ProblemReport problemReport = new ProblemReport();

      try {
         problemReport.accept(parser);
      } catch (RuntimeException e) {
         if (e.getCause() !=null && e.getCause() instanceof java.io.EOFException) {
            // ignore it
         } else {
            throw e;
         }
      }
      
      parser.m_linker.finish();
      return problemReport;
   }

   @Override
   public void visitDuration(Duration duration) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitDurationChildren(duration, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitDurationChildren(Duration duration, int _field, int _type) {
      switch (_field) {
         case 1:
            duration.setValue(readInt());
            break;
         case 2:
            duration.setCount(readInt());
            break;
         case 3:
            if (_type == 1) {
                  duration.addMessage(readString());
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                  duration.addMessage(readString());
               }
            }
            break;
      }
   }

   @Override
   public void visitEntity(Entity entity) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitEntityChildren(entity, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitEntityChildren(Entity entity, int _field, int _type) {
      switch (_field) {
         case 1:
            entity.setId(readString());
            break;
         case 2:
            entity.setType(readString());
            break;
         case 3:
            entity.setStatus(readString());
            break;
         case 33:
            if (_type == 1) { 
              JavaThread thread = new JavaThread();

              visitThread(thread);
              m_linker.onThread(entity, thread);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 JavaThread thread = new JavaThread();

                 visitThread(thread);
                 m_linker.onThread(entity, thread);
               }
            }
            break;
         case 34:
            if (_type == 1) { 
              Duration duration = new Duration();

              visitDuration(duration);
              m_linker.onDuration(entity, duration);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Duration duration = new Duration();

                 visitDuration(duration);
                 m_linker.onDuration(entity, duration);
               }
            }
            break;
         case 35:
            GraphTrend graphTrend = new GraphTrend();

            visitGraphTrend(graphTrend);
            m_linker.onGraphTrend(entity, graphTrend);
            break;
      }
   }

   @Override
   public void visitEntry(Entry entry) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitEntryChildren(entry, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitEntryChildren(Entry entry, int _field, int _type) {
      switch (_field) {
         case 1:
            entry.setType(readString());
            break;
         case 2:
            entry.setStatus(readString());
            break;
         case 33:
            if (_type == 1) { 
              Duration duration = new Duration();

              visitDuration(duration);
              m_linker.onDuration(entry, duration);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Duration duration = new Duration();

                 visitDuration(duration);
                 m_linker.onDuration(entry, duration);
               }
            }
            break;
         case 34:
            if (_type == 1) { 
              JavaThread thread = new JavaThread();

              visitThread(thread);
              m_linker.onThread(entry, thread);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 JavaThread thread = new JavaThread();

                 visitThread(thread);
                 m_linker.onThread(entry, thread);
               }
            }
            break;
      }
   }

   @Override
   public void visitGraphTrend(GraphTrend graphTrend) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitGraphTrendChildren(graphTrend, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitGraphTrendChildren(GraphTrend graphTrend, int _field, int _type) {
      switch (_field) {
         case 1:
            graphTrend.setDuration(readInt());
            break;
         case 2:
            graphTrend.setFails(readString());
            break;
      }
   }

   @Override
   public void visitMachine(Machine machine) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitMachineChildren(machine, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitMachineChildren(Machine machine, int _field, int _type) {
      switch (_field) {
         case 1:
            machine.setIp(readString());
            break;
         case 33:
            if (_type == 1) { 
              Entry entry = new Entry();

              visitEntry(entry);
              m_linker.onEntry(machine, entry);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Entry entry = new Entry();

                 visitEntry(entry);
                 m_linker.onEntry(machine, entry);
               }
            }
            break;
         case 34:
            if (_type == 1) { 
              Entity entity = new Entity();

              visitEntity(entity);
              m_linker.onEntity(machine, entity);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Entity entity = new Entity();

                 visitEntity(entity);
                 m_linker.onEntity(machine, entity);
               }
            }
            break;
      }
   }

   @Override
   public void visitProblemReport(ProblemReport problemReport) {
      byte tag;

      if ((tag = readTag()) != -4) {
         throw new RuntimeException(String.format("Malformed payload, expected: %s, but was: %s!", -4, tag));
      }

      while ((tag = readTag()) != -1) {
         visitProblemReportChildren(problemReport, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitProblemReportChildren(ProblemReport problemReport, int _field, int _type) {
      switch (_field) {
         case 1:
            problemReport.setDomain(readString());
            break;
         case 2:
            problemReport.setStartTime(readDate());
            break;
         case 3:
            problemReport.setEndTime(readDate());
            break;
         case 4:
            if (_type == 1) {
                  problemReport.addDomain(readString());
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                  problemReport.addDomain(readString());
               }
            }
            break;
         case 5:
            if (_type == 1) {
                  problemReport.addIp(readString());
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                  problemReport.addIp(readString());
               }
            }
            break;
         case 33:
            if (_type == 1) { 
              Machine machine = new Machine();

              visitMachine(machine);
              m_linker.onMachine(problemReport, machine);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Machine machine = new Machine();

                 visitMachine(machine);
                 m_linker.onMachine(problemReport, machine);
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
            if (_type == 1) {
                  segment.addMessage(readString());
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                  segment.addMessage(readString());
               }
            }
            break;
      }
   }

   @Override
   public void visitThread(JavaThread thread) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitThreadChildren(thread, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitThreadChildren(JavaThread thread, int _field, int _type) {
      switch (_field) {
         case 1:
            thread.setGroupName(readString());
            break;
         case 2:
            thread.setName(readString());
            break;
         case 3:
            thread.setId(readString());
            break;
         case 33:
            if (_type == 1) { 
              Segment segment = new Segment();

              visitSegment(segment);
              m_linker.onSegment(thread, segment);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Segment segment = new Segment();

                 visitSegment(segment);
                 m_linker.onSegment(thread, segment);
               }
            }
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
