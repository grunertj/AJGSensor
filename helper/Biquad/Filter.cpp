#include <math.h>
#include "Biquad.h"
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

int main(int argc, char* argv[]) {
  Biquad *lpFilterHP = new Biquad();	// create a Biquad, lpFilterHP;
  Biquad *lpFilterLP = new Biquad();	// create a Biquad, lpFilterHP;
  Processing *processing = new Processing();


  // lpFilterHP->setBiquad(bq_type_lowpass,243.80/44100,0.707,0);
  lpFilterLP->setBiquad(bq_type_lowpass,0.5/100.0,0.707,0);
  lpFilterHP->setBiquad(bq_type_highpass,0.1/100.0,0.707,0);

  ifstream input(argv[1]);

  string line;

  float t,x,y,z,f,zero;

  // while (getline(input, line)) {
  //  cout << line << endl;
  // }

  while (input >> t >>x >> y >> z) {
    f = lpFilterHP->process(lpFilterLP->process(z));
    zero = processing->IsZero(f,zero,t);
    cout << t << " " << z << " " << f << " " << processing->Period(t) << endl;
  }

  input.close();

  //float in[10] = {1,2,3,4,5,6,7,8,9,10};
 // float out;
 // int bufSize = 10;

  //lpFilterHP->setBiquad(bq_type_lowpass, Fc / sampleRate, 0.707, 0);

// filter a buffer of input samples, in-place
  //for (int idx = 0; idx < bufSize; idx++) {
  //  cout << "In: " << in[idx];
  //  out = lpFilterHP->process(in[idx]);
  //  cout << " Out: " << out << endl;
 // }
  return 0;
}

