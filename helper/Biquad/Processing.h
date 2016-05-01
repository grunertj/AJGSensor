#ifndef Processing_h
#define Processing_h

class Processing {
  public:
    Processing();
    float Period(float input);
    float IsZero(float input, float output, float time);
  protected:
    float t0;
};

#endif // Processing_h

