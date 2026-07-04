package com.dianping.cat.consumer.matrix.model.transform;

import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;

public interface ILinker {

   boolean onMatrix(MatrixReport parent, Matrix matrix);

   boolean onRatio(Matrix parent, Ratio ratio);
}
