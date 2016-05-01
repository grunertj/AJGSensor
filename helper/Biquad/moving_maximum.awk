# Moving average over the column of a data file 
# awk -f moving_maximum.awk linear-2016-03-23-17_40_52.txt > ma200.dat
# http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter

# /*
#  * time smoothing constant for low-pass filter
#  * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
#  * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
#  */
# static final float ALPHA = 0.15f;
#  
# /**
#  * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
#  * @see http://developer.android.com/reference/android/hardware/SensorEvent.html#values
#  */
# protected float[] lowPass( float[] input, float[] output ) {
#     if ( output == null ) return input;
#      
#     for ( int i=0; i<input.length; i++ ) {
#         output[i] = output[i] + ALPHA * (input[i] - output[i]);
#     }
#     return output;
# }

BEGIN {
  count = 11
  center = 5
}
{   
  max($1,$2)
  min($1,$2)
#  op = lowPass($4,op)
#  print $1,op
  #zero = iszero(op, zero,$1)
  #j = jens($1,j,$1)
}

func jens (input,output,time) {
  diff = input - output
  print time,diff
  output = input
  return output
}

func lowPass (input, output) {
  output = output + 0.001 * (input - output)
  return output
}

func iszero (input, output,time) {
  if ( input < 0 && output > 0 || input > 0 && output < 0 ) {
    print time,0
  }
  output = input
  return output
}

func max (t, d) {
  for (i = count - 1 ; i > 0 ; i--) {
      time[i] = time[i-1];
      buffer[i] = buffer[i-1];
  }
  
  time[0] = t;
  buffer[0] = d;

  for ( i = 0 ; i < center ; i++ ) {
    if (buffer[i] > buffer[center]) {
      return -1000
    }
  }

  for ( i = center+1 ; i < count ; i++ ) {
    if (buffer[i] > buffer[center]) {
      return -1000
    }
  }
  if ( time[center] != "") {
    print "max",time[center],buffer[center]
  }
}

func min (t, d) {
  for (i = count - 1 ; i > 0 ; i--) {
      time[i] = time[i-1];
      buffer[i] = buffer[i-1];
  }
  
  time[0] = t;
  buffer[0] = d;

  for ( i = 0 ; i < center ; i++ ) {
    if (buffer[i] < buffer[center]) {
      return -1000
    }
  }

  for ( i = center+1 ; i < count ; i++ ) {
    if (buffer[i] < buffer[center]) {
      return -1000
    }
  }

  if ( time[center] != "") {
    print "min",time[center],buffer[center]
  }
}



