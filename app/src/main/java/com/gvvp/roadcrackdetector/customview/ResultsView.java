package com.gvvp.roadcrackdetector.customview;

import java.util.List;
import com.gvvp.roadcrackdetector.tflite.Classifier.Recognition;

public interface ResultsView {
  public void setResults(final List<Recognition> results);
}
