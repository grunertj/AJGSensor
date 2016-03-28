# Moving average over the column of a data file 
# awk -f moving_maximum.awk linear-2016-03-23-17_40_52.txt > ma200.dat
BEGIN {
    P = 3;
   count = 5; 
   OFFSET = 0
}
 
{   
    if (OFFSET == 0 ) {
    OFFSET = $4 * 1.0
  }
    time = ($4-OFFSET)/1000

    x = sqrt(sqrt($1*$1+$2*$2+$3*$3));	
    i = NR % P; 
    # print i;
    
    #MA += (x - Z[i]) / P; 
    Z[i] = x;
    if (Z[1] > Z[0] && Z[1] > Z[2]) {
     # print time,Z[1]
      Z[1] = 0
    } 
    ## print "Average:",MA,"Value:",$1,"Count:",NR,"Bufferindex:",i;
    #print time,avg(sqrt(sqrt($1*$1+$2*$2+$3*$3)))

    new_max = max(sqrt(sqrt($1*$1+$2*$2+$3*$3)))

    if ( new_max > -10000) {
      if (abs(previous_time-previous_max_time) > 0) {
        print previous_time,new_max,(60.0/abs(previous_time-previous_max_time))
        previous_max_time = previous_time
      }
    }
    previous_time = time
}

func abs(data) {
  if (data > 0) {
    return data
  } else {
    return -1 * data
  }
}

func avg (data) {
  total = 0.0;
  i = 0;
  for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
  }
  
  buffer[0] = data;

  for (i = 0 ; i < count ; i++) {
    total = total + buffer[i];
  }

  return (total/count);
}

func max (data) {
  total = 0.0;
  i = 0;
  for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
  }
  
  buffer[0] = data;

  if (buffer[0] < buffer[1] && buffer[2] < buffer[1]) {
    return buffer[1]
  } else {
    return -10000
  }
}


