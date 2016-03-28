public class Smooth {
  private float buffer[];
  private int count = 10;

  public Smooth () {
    buffer = new float[count];
    for (int i = 0 ; i < count ; i++) {
      buffer[i] = 0.0f;
    }
    System.out.println("Count: "+count);
  }

  public Smooth (int c) {
    count = c;
    buffer = new float[count];
    for (int i = 0 ; i < count ; i++) {
      buffer[i] = 0.0f;
    }
    System.out.println("Count: "+count);
  }

  public float avg (float data) {
    float total = 0.0f;
    int i = 0;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < count ; i++) {
      total = total + buffer[i];
    }

    return (total/count);
  }

  public float avg (String data_string) {
    float data = 0.0f;
    data = Float.parseFloat(data_string);
    float total = 0.0f;
    int i = 0;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < count ; i++) {
      total = total + buffer[i];
    }

    return (total/count);
  }

}

