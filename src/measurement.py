#!/usr/bin/env python3
__author__ = "Dong Wang"

import os
import sys
import json
import subprocess
import statistics

if __name__ == '__main__':
    # make
    subprocess.check_call('cd ..; make; cd src/', shell=True)
    print('------------------------------------------')

    ##################################################################
    # global data
    ##################################################################
    list_names = ['Coarse',
                  'Fine',
                  'Lazy',
                  'LockFree',
                  'Optimistic',
                  'EliminationBackoff',
                  'EliminationCombining'
                  ]
    iter_num = 1
    thread_nums = [num for num in range(4, 40, 4)]

    symmetric_res_dir = os.path.join(os.getcwd(), 'res', 'symmetric')
    asymmetric_res_dir = os.path.join(os.getcwd(), 'res', 'asymmetric')

    if not os.path.exists(symmetric_res_dir):
        os.makedirs(symmetric_res_dir)
    if not os.path.exists(asymmetric_res_dir):
        os.makedirs(asymmetric_res_dir)

    ##################################################################
    # symmetric Add/Remove Operation
    ##################################################################
    contains_nums = [num for num in range(20, 100, 20)]
    symmetric_nums = [(100 - num)//2 for num in contains_nums]

    # print(symmetric_res_dir)

    for index, symmetric_num in enumerate(symmetric_nums):
        filename = os.path.join(symmetric_res_dir, 'contains-{}.txt'.format(contains_nums[index]))
        case_data = {}
        for list_name in list_names:
            list_data = []
            for thread_num in thread_nums:
                thread_data = []
                for i in range(iter_num):
                    print('Symmetric: {}, ListName: {} ---- Thread Num: {}, Iter Num: {}'.format(symmetric_num,
                                                                                                 list_name,
                                                                                                 thread_num,
                                                                                                 i
                                                                                                 ))
                    cmd = 'java Measurement {} {} {} {}'.format(list_name, thread_num, symmetric_num, symmetric_num)
                    # print(cmd)
                    output = subprocess.getoutput(cmd)
                    segments = output.split(' ')
                    thread_data.append(float(segments[-1]))
                list_data.append(statistics.mean(thread_data))
            print('------------------------------------------')
            case_data[list_name] = list_data
        with open(filename, 'w') as outfile:
            outfile.write(json.dumps(case_data, indent=4))

    ##################################################################
    # asymmetric Add/Remove Operation
    ##################################################################
    add_nums = [num for num in range(20, 100, 20)]
    rm_nums = [100 - num for num in add_nums]
    for index, add_num in enumerate(add_nums):
        rm_num = rm_nums[index]
        filename = os.path.join(asymmetric_res_dir, 'add-{}-rm-{}.txt'.format(add_num, rm_num))
        case_data = {}
        for list_name in list_names:
            list_data = []
            for thread_num in thread_nums:
                thread_data = []
                for i in range(iter_num):
                    print('Add: {}, Reomve: {}, ListName: {} --- Thread Num: {}, Iter Num: {}'.format(add_num,
                                                                                                      rm_num,
                                                                                                      list_name,
                                                                                                      thread_num,
                                                                                                      i
                                                                                                      ))
                    cmd = 'java Measurement {} {} {} {}'.format(list_name, thread_num, add_num, rm_num)
                    # print(cmd)
                    output = subprocess.getoutput(cmd)
                    segments = output.split(' ')
                    thread_data.append(float(segments[-1]))
                list_data.append(statistics.mean(thread_data))
            print('------------------------------------------')
            case_data[list_name] = list_data
            # print(case_data)
        with open(filename, 'w') as outfile:
            outfile.write(json.dumps(case_data, indent=4))

    ##################################################################
    # thread 20 contains 0
    ##################################################################


    # make clean
    subprocess.check_call('cd ..; make clean; cd src/', shell=True)
