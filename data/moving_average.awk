# Moving average over the column of a data file 
# awk -f moving_average.awk linear-2016-03-23-17_40_52.txt > ma200.dat
BEGIN {
    P = 100; 
    OFFSET = 0
}
 
{   
  if (OFFSET == 0 ) {
    OFFSET = $4 * 1.0
  }
    time = ($4-OFFSET)/1000
    x = sqrt(sqrt($1*$1+$2*$2+$3*$3));	
    i = NR % P; 
    MA += (x - Z[i]) / P; 
    Z[i] = x; 
    # print "Average:",MA,"Value:",$1,"Count:",NR,"Bufferindex:",i;
    print time,MA
}


