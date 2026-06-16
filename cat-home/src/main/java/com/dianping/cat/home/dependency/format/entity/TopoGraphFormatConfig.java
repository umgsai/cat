package com.dianping.cat.home.dependency.format.entity;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.home.dependency.format.BaseEntity;
import com.dianping.cat.home.dependency.format.IVisitor;

public class TopoGraphFormatConfig extends BaseEntity<TopoGraphFormatConfig> {
   private List<ProductLine> m_productLines = new ArrayList<ProductLine>();

   public TopoGraphFormatConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitTopoGraphFormatConfig(this);
   }

   public TopoGraphFormatConfig addProductLine(ProductLine productLine) {
      m_productLines.add(productLine);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof TopoGraphFormatConfig) {
         TopoGraphFormatConfig _o = (TopoGraphFormatConfig) obj;

         if (!equals(getProductLines(), _o.getProductLines())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public List<ProductLine> getProductLines() {
      return m_productLines;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      for (ProductLine e : m_productLines) {
         hash = hash * 31 + (e == null ? 0 :e.hashCode());
      }


      return hash;
   }

   @Override
   public void mergeAttributes(TopoGraphFormatConfig other) {
   }

}
