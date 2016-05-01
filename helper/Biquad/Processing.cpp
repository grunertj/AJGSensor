#include "Processing.h"
#include <iostream>
#include <fstream>
#include <vector>
#include <pcrecpp.h>
#include <string>
#include <sstream>
#include <ctime>
#include <algorithm>

using namespace std;

Processing::Processing() {
  t0 = 0.0;
}

float Processing::Period(float input) {
  float ouput;
  ouput = input - t0;
  t0 = input;
  return ouput;
}

float Processing::IsZero(float input, float output, float time) {
  static float prev_time = 0;
  if ( input < 0 && output > 0 || input > 0 && output < 0 ) {
    cout << time << " 0 " << time - prev_time << endl;
    prev_time = time;
  }
  output = input;
  return output;
}

