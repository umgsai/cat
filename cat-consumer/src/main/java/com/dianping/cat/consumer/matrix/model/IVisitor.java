package com.dianping.cat.consumer.matrix.model;

import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;

public interface IVisitor {

   void visitMatrix(Matrix matrix);

   void visitMatrixReport(MatrixReport matrixReport);

   void visitRatio(Ratio ratio);
}
