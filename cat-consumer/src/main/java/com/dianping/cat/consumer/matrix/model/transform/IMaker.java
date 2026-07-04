package com.dianping.cat.consumer.matrix.model.transform;

import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;

public interface IMaker<T> {

   String buildDomain(T node);

   Matrix buildMatrix(T node);

   MatrixReport buildMatrixReport(T node);

   Ratio buildRatio(T node);
}
