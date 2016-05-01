CXX =  /sw/gnu/gcc/4.1.2/linux-x86_64/bin/g++
CXX = g++
OPTIM = -Os -Wall

.SUFFIXES: .cpp
.cpp.o:
	$(CXX) $(CFLAGS) -c $<

Processing.o: Processing.cpp Processing.h
Biquad.o: Biquad.cpp
Filter.o: Filter.cpp
Filter: Biquad.o Filter.o Processing.o
	$(CXX) $(CFLAGS) Filter.o Biquad.o Processing.o -o Filter
clean:
	rm -f *.o Filter
