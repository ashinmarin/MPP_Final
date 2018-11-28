#!/usr/bin/env python3
__author__  = "Dong Wang"

import os
import sys
import json 
import subprocess

if __name__ == '__main__':
    lists = ['Coarse', 'Fine', 'Lazy', 'LockFree', 'Optimistic', 'EliminationBackoff', 'EliminationCombining']
    # lists = ['EliminationCombining']
    data_1_4 = {}
    for contains in range(20,100,20):
        contains_data = {} 
        for list_name in lists:
            list_data = []
            print('-----------------------------------------')
            print('List {} -- Contasin {}%'.format(list_name, contains))
            print('-----------------------------------------')
            for thread in range(4,42,2):
                output = subprocess.getoutput('java Measurement {} {} {}'.format(list_name, thread, contains))
                chunks = output.split(' ')
                list_data.append(float(chunks[-1]))
                print(output)
            contains_data[list_name] = list_data
            # print(json.dumps(list_data, indent=4))
        data_1_4[int(contains)] = contains_data
        # print(json.dumps(data_1_4, indent=4))

    data_1_4_path = os.path.join(os.getcwd(), 'res', 'data_1_4.txt')
    with open(data_1_4_path, 'w') as outfile:
        outfile.write(json.dumps(data_1_4, indent=4))

     
    data_5 = {}
    thread = 20
    for list_name in lists:
        contains_data = []
        for contains in range(20,100,20):
            print('-----------------------------------------')
            print('List {} -- Contasin {}%'.format(list_name, contains))
            print('-----------------------------------------')
            output = subprocess.getoutput('java Measurement {} {} {}'.format(list_name, thread, contains))
            chunks = output.split(' ')
            contains_data.append(float(chunks[-1]))
            print(output)
        data_5[list_name] = contains_data
    print(json.dumps(data_5, indent=4))
    data_5_path = os.path.join(os.getcwd(), 'res', 'data_5.txt')
    with open(data_5_path, 'w') as outfile:
        outfile.write(json.dumps(data_5, indent=4))
