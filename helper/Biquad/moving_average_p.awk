# Moving average over the column of a data file 
# awk -f moving_average.awk linear-2016-03-23-17_40_52.txt > ma200.dat
BEGIN {
    P = 50; 
}
 
{   

    time = $1
    x = $3;	
    i = NR % P; 
    MA += (x - Z[i]) / P; 
    Z[i] = x; 
    # print "Average:",MA,"Value:",$1,"Count:",NR,"Bufferindex:",i;
    print time,MA
}


