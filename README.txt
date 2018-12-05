Project 11.Elimination-Combining Set
Authors: Dong Wang & Ashin Marin Thomas

#########################
## How to run Java code
#########################
```
    cd <Path to code directory>
    make
    cd src
    java Measurement <Set Name> <# of thread> <add percentage> <rm percentage>
```
- add + contains + rm = 100
- Set Names
    *   Coarse
    *   Fine
    *   Lazy
    *   LockFree
    *   Optimistic
    *   EliminationBackoff
    *   EliminationCombining

#################################
## How to run measurement script
#################################
```
    cd <Path to code directory>/src
    python3 measurement.py
    python3 result_analyzing.py
```
* After finishing above script, you can go to ***cd [code directory]/res/plot_res*** to check measurement plots
* contains-*.png are symmetric plots
* add-* -rm-*.png are assymetric plots
