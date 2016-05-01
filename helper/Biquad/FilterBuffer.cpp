#include <math.h>
#include "Biquad.h"
#include <iostream>
#include <vector>
#include <pcrecpp.h>
#include <string>
#include <sstream>
#include <ctime>
#include <algorithm>

using namespace std;

int main(int argc, char* argv[]) {
  Biquad *lpFilter = new Biquad();	// create a Biquad, lpFilter;


  lpFilter->setBiquad(bq_type_lowpass,200,1.0,0);

  float in[10] = {1,2,3,4,5,6,7,8,9,10};
  float out;
  int bufSize = 10;

  //lpFilter->setBiquad(bq_type_lowpass, Fc / sampleRate, 0.707, 0);

// filter a buffer of input samples, in-place
  for (int idx = 0; idx < bufSize; idx++) {
    cout << "In: " << in[idx];
    out = lpFilter->process(in[idx]);
    cout << " Out: " << out << endl;
  }
  return 0;
}
